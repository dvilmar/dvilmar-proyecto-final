package com.bookmycut.service;

import com.bookmycut.dto.AuthResponse;
import com.bookmycut.dto.LoginRequest;
import com.bookmycut.dto.RegisterRequest;
import com.bookmycut.exception.BadRequestException;
import com.bookmycut.exception.ConflictException;
import com.bookmycut.exception.UnauthorizedException;
import com.bookmycut.entities.User;
import com.bookmycut.repositories.UserRepository;
import com.bookmycut.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio responsable de la autenticación y registro de usuarios.
 * Gestiona el proceso de login, registro y generación de tokens JWT.
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public JwtUtil getJwtUtil() {
        return jwtUtil;
    }
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param request DTO con los datos del usuario a registrar.
     * @return AuthResponse con el token JWT y datos del usuario.
     * @throws RuntimeException Si el email ya está registrado o el rol no es válido.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        logger.info("Iniciando registro de usuario con rol: {}", request.getRole());
        
        // Los usuarios registrados siempre obtienen el rol CLIENTE
        // Validar email (obligatorio para CLIENTE)
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("El email es obligatorio");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Intento de registro con email ya existente: {}", request.getEmail());
            throw new ConflictException("El email ya está registrado");
        }
        
        // Validar username si se proporciona (opcional para CLIENTE pero permite login)
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            if (userRepository.existsByUsername(request.getUsername())) {
                logger.warn("Intento de registro con username ya existente: {}", request.getUsername());
                throw new ConflictException("El username ya está registrado");
            }
        }
        
        try {
            User user = new User();
            user.setName(request.getName());
            user.setPassword(request.getPassword()); // Guardar contraseña en texto plano
            // Los usuarios registrados siempre obtienen el rol CLIENTE
            user.setRole(User.Role.CLIENTE);
            user.setActive(true);
            
            // Para CLIENTE: email obligatorio, username opcional (si se proporciona permite login)
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            
            // Si se proporciona username, usarlo para permitir login
            if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
                user.setUsername(request.getUsername());
                logger.debug("Creando usuario CLIENTE con username: {} para permitir login", request.getUsername());
            } else {
                // Si no hay username, generar uno basado en email para permitir login
                String usernameFromEmail = request.getEmail().trim().toLowerCase();
                user.setUsername(usernameFromEmail);
                logger.debug("Creando usuario CLIENTE con username generado desde email: {}", usernameFromEmail);
            }
            
            user = userRepository.save(user);
            userRepository.flush(); // Asegurar persistencia antes de insertar en tabla clientes
            logger.info("Usuario registrado exitosamente con ID: {} y rol: {}", user.getUserId(), user.getRole());
            
            // Insertar registro en la tabla clientes (requerido por la foreign key)
            try {
                userRepository.insertClienteRecord(user.getUserId(), request.getPhone());
                logger.info("Registro de cliente insertado en tabla clientes para usuario ID: {}", user.getUserId());
            } catch (Exception e) {
                logger.error("Error al insertar registro en tabla clientes para usuario ID {}: {}", user.getUserId(), e.getMessage(), e);
                throw new RuntimeException("Error al crear registro de cliente: " + e.getMessage(), e);
            }
            
            // Usar username o email para el token
            String tokenSubject = user.getUsername() != null ? user.getUsername() : 
                                 (user.getEmail() != null ? user.getEmail() : String.valueOf(user.getUserId()));
            String token = jwtUtil.generateToken(tokenSubject, user.getUserId(), 
                    List.of("ROLE_" + user.getRole().name()));
            logger.debug("Token JWT generado para usuario ID: {}", user.getUserId());
            
            return new AuthResponse(
                token,
                "Bearer",
                user.getUserId(),
                user.getName(),
                user.getEmail() != null ? user.getEmail() : "",
                user.getRole()
            );
        } catch (Exception e) {
            logger.error("Error al registrar usuario: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Autentica un usuario y genera un token JWT.
     *
     * @param request DTO con las credenciales del usuario.
     * @return AuthResponse con el token JWT y datos del usuario.
     * @throws BadCredentialsException Si las credenciales son incorrectas.
     * @throws RuntimeException Si el usuario no existe o está inactivo.
     */
    public AuthResponse login(LoginRequest request) {
        logger.info("Intento de login para username/email: {}", request.getUsername());
        
        try {
            // Buscar usuario por username o email (el campo puede contener cualquiera de los dos)
            Optional<User> userOpt = userRepository.findByUsernameOrEmail(request.getUsername());
            if (userOpt.isEmpty()) {
                logger.warn("Usuario no encontrado con username o email: {}", request.getUsername());
                throw new BadCredentialsException("Credenciales inválidas");
            }
            
            User user = userOpt.get();
            if (!user.getActive()) {
                logger.warn("Intento de login de usuario inactivo: {}", request.getUsername());
                throw new UnauthorizedException("Usuario inactivo");
            }
            
            // Determinar el identificador real para autenticación (username si existe, sino email)
            String authIdentifier = user.getUsername();
            if (authIdentifier == null || authIdentifier.trim().isEmpty()) {
                authIdentifier = user.getEmail();
            }
            
            logger.debug("Usuario encontrado: ID={}, Username={}, Email={}, Active={}, AuthIdentifier={}", 
                    user.getUserId(), user.getUsername(), user.getEmail(), user.getActive(), authIdentifier);
            
            // Intentar autenticar con Spring Security usando el identificador correcto
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authIdentifier, request.getPassword())
            );
            
            logger.debug("Autenticación exitosa para: {}", authIdentifier);
            
            // Usar username para el token si existe, sino email
            String tokenSubject = user.getUsername() != null ? user.getUsername() : user.getEmail();
            String token = jwtUtil.generateToken(tokenSubject, user.getUserId(), 
                    List.of("ROLE_" + user.getRole().name()));
            logger.info("Login exitoso para usuario ID: {} con rol: {}", user.getUserId(), user.getRole());
            
            return new AuthResponse(
                token,
                "Bearer",
                user.getUserId(),
                user.getName(),
                user.getEmail() != null ? user.getEmail() : "",
                user.getRole()
            );
        } catch (BadCredentialsException e) {
            logger.warn("Credenciales incorrectas para: {} - {}", request.getUsername(), e.getMessage());
            throw new UnauthorizedException("Credenciales inválidas");
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error durante el login para {}: {}", request.getUsername(), e.getMessage(), e);
            throw new UnauthorizedException("Error al autenticar");
        }
    }
    
    /**
     * Encuentra o crea un usuario CLIENTE basado en email.
     * Si el usuario existe, lo actualiza si es necesario (nombre, teléfono).
     * Si no existe, crea un nuevo usuario CLIENTE.
     * Si se proporciona contraseña, se crea con username (basado en email) para permitir login.
     * 
     * @param name Nombre del cliente
     * @param email Email del cliente
     * @param phone Teléfono del cliente
     * @param password Contraseña del cliente (opcional, si se proporciona se crea cuenta con acceso)
     * @return Usuario CLIENTE (existente o recién creado)
     */
    @Transactional
    public User findOrCreateClientUser(String name, String email, String phone, String password) {
        logger.info("Buscando o creando usuario CLIENTE con email: {}", email);
        
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            
            // Si ya existe y es CLIENTE, actualizar datos si es necesario
            if (user.getRole() == User.Role.CLIENTE) {
                logger.debug("Usuario CLIENTE existente encontrado con ID: {}", user.getUserId());
                boolean updated = false;
                
                if (name != null && !name.equals(user.getName())) {
                    user.setName(name);
                    updated = true;
                }
                if (phone != null && !phone.equals(user.getPhone())) {
                    user.setPhone(phone);
                    updated = true;
                    // Actualizar también en la tabla clientes
                    try {
                        userRepository.insertClienteRecord(user.getUserId(), phone);
                    } catch (Exception e) {
                        logger.warn("No se pudo actualizar teléfono en tabla clientes: {}", e.getMessage());
                    }
                }
                
                if (updated) {
                    user = userRepository.save(user);
                    logger.debug("Datos del cliente actualizados");
                }
                
                return user;
            } else {
                // Si existe pero no es CLIENTE, lanzar error
                logger.warn("Email {} ya existe pero no es CLIENTE, es: {}", email, user.getRole());
                throw new ConflictException("El email ya está registrado con otro tipo de cuenta");
            }
        }
        
        // Crear nuevo usuario CLIENTE
        logger.info("Creando nuevo usuario CLIENTE con email: {}", email);
        try {
            User newUser = new User();
            newUser.setName(name);
            newUser.setEmail(email);
            
            // Si se proporciona contraseña, crear usuario con username para permitir login
            if (password != null && !password.trim().isEmpty()) {
                // Usar email como username (sin espacios y en minúsculas)
                String username = email.trim().toLowerCase();
                newUser.setUsername(username);
                newUser.setPassword(password); // Guardar contraseña en texto plano
                logger.info("Usuario CLIENTE se creará con username '{}' para permitir login", username);
            } else {
                // Generar contraseña aleatoria (el cliente no hará login)
                String randomPassword = java.util.UUID.randomUUID().toString();
                newUser.setPassword(randomPassword); // Guardar contraseña en texto plano
                logger.info("Usuario CLIENTE se creará sin username (no tendrá acceso de login)");
            }
            
            newUser.setRole(User.Role.CLIENTE);
            newUser.setActive(true);
            newUser.setPhone(phone);
            
            newUser = userRepository.save(newUser);
            // Hacer flush para asegurar que el usuario esté persistido antes de insertar en clientes
            userRepository.flush();
            logger.info("Usuario CLIENTE creado exitosamente con ID: {}", newUser.getUserId());
            
            // Insertar registro en la tabla clientes (requerido por la foreign key)
            try {
                userRepository.insertClienteRecord(newUser.getUserId(), phone);
                logger.info("Registro de cliente insertado en tabla clientes para usuario ID: {}", newUser.getUserId());
            } catch (Exception e) {
                logger.error("Error al insertar registro en tabla clientes para usuario ID {}: {}", newUser.getUserId(), e.getMessage(), e);
                // Si falla la inserción en clientes, lanzar error para que se revierta la transacción
                throw new RuntimeException("Error al crear registro de cliente: " + e.getMessage(), e);
            }
            
            return newUser;
        } catch (Exception e) {
            logger.error("Error al crear usuario CLIENTE con email {}: {}", email, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Encuentra o crea un usuario CLIENTE y retorna información sobre si fue creado nuevo.
     * 
     * @param name Nombre del cliente
     * @param email Email del cliente
     * @param phone Teléfono del cliente
     * @param password Contraseña del cliente (opcional, si se proporciona se crea cuenta con acceso)
     * @return Resultado con el usuario y si fue creado nuevo
     */
    @Transactional
    public ClientCreationResult findOrCreateClientUserWithResult(String name, String email, String phone, String password) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        boolean wasNewUser = false;
        
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            if (user.getRole() == User.Role.CLIENTE) {
                // Usuario existente, actualizar datos
                boolean updated = false;
                if (name != null && !name.equals(user.getName())) {
                    user.setName(name);
                    updated = true;
                }
                if (phone != null && !phone.equals(user.getPhone())) {
                    user.setPhone(phone);
                    updated = true;
                    try {
                        userRepository.insertClienteRecord(user.getUserId(), phone);
                    } catch (Exception e) {
                        logger.warn("No se pudo actualizar teléfono en tabla clientes: {}", e.getMessage());
                    }
                }
                if (updated) {
                    user = userRepository.save(user);
                }
            } else {
                throw new ConflictException("El email ya está registrado con otro tipo de cuenta");
            }
        } else {
            // Crear nuevo usuario
            wasNewUser = true;
            user = findOrCreateClientUser(name, email, phone, password);
        }
        
        return new ClientCreationResult(user, wasNewUser);
    }
    
    /**
     * Resultado de la creación/búsqueda de cliente.
     */
    public static class ClientCreationResult {
        private final User user;
        private final boolean wasNewUser;
        
        public ClientCreationResult(User user, boolean wasNewUser) {
            this.user = user;
            this.wasNewUser = wasNewUser;
        }
        
        public User getUser() {
            return user;
        }
        
        public boolean wasNewUser() {
            return wasNewUser;
        }
    }
}

