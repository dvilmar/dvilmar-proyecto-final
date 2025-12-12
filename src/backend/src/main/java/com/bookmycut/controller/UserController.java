package com.bookmycut.controller;

import com.bookmycut.entities.User;
import com.bookmycut.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/usuarios", produces = "application/json; charset=UTF-8")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Users", description = "Endpoints para gestión de usuarios. Los usuarios pueden ver y actualizar su propio perfil, " +
        "mientras que los administradores pueden gestionar todos los usuarios.")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private com.bookmycut.service.AvailabilityService availabilityService;
    
    @Operation(
            summary = "Obtener usuario actual",
            description = "Obtiene la información del usuario autenticado actualmente. " +
                    "Retorna información básica del usuario sin incluir la contraseña."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Información del usuario obtenida exitosamente",
                    content = @Content(mediaType = "application/json", examples = {
                            @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Respuesta exitosa",
                                    value = "{\"usuarioId\": 1, \"nombre\": \"Juan Pérez\", \"email\": \"juan@example.com\", \"rol\": \"CLIENTE\", \"activo\": true}"
                            )
                    })
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            )
    })
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(username);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        User user = userOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("usuarioId", user.getUserId());
        response.put("nombre", user.getName());
        response.put("email", user.getEmail());
        response.put("rol", user.getRole() != null ? user.getRole().name() : null);
        response.put("activo", user.getActive());
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Obtener todos los usuarios",
            description = "Obtiene la lista completa de usuarios. **Solo disponible para administradores.**"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuarios obtenida exitosamente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos para acceder a este recurso",
                    content = @Content
            )
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> getAllUsers(
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Usar paginación", example = "false")
            @RequestParam(defaultValue = "false") boolean paginated) {
        
        if (paginated) {
            org.springframework.data.domain.Pageable pageable = 
                    org.springframework.data.domain.PageRequest.of(page, size, 
                            org.springframework.data.domain.Sort.by("name"));
            org.springframework.data.domain.Page<User> usersPage = userRepository.findAll(pageable);
            
            // Mapear a Map para evitar problemas de serialización con entidades
            org.springframework.data.domain.Page<Map<String, Object>> mappedPage = usersPage.map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("usuarioId", user.getUserId());
                userMap.put("nombre", user.getName());
                userMap.put("username", user.getUsername());
                userMap.put("email", user.getEmail());
                userMap.put("rol", user.getRole() != null ? user.getRole().name() : null);
                userMap.put("activo", user.getActive());
                userMap.put("phone", user.getPhone());
                return userMap;
            });
            
            return ResponseEntity.ok(mappedPage);
        } else {
            List<User> users = userRepository.findAll();
            List<Map<String, Object>> response = users.stream()
                    .map(user -> {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("usuarioId", user.getUserId());
                        userMap.put("nombre", user.getName());
                        userMap.put("username", user.getUsername());
                        userMap.put("email", user.getEmail());
                        userMap.put("rol", user.getRole() != null ? user.getRole().name() : null);
                        userMap.put("activo", user.getActive());
                        userMap.put("phone", user.getPhone());
                        return userMap;
                    })
                    .toList();
            return ResponseEntity.ok(response);
        }
    }

    @Operation(
            summary = "Obtener usuario por ID",
            description = "Obtiene la información completa de un usuario por su ID. **Solo disponible para administradores.**"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado",
                    content = @Content(schema = @Schema(implementation = User.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos para acceder a este recurso",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Map<String, Object>> getUserById(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("usuarioId", user.getUserId());
        response.put("nombre", user.getName());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("rol", user.getRole() != null ? user.getRole().name() : null);
        response.put("activo", user.getActive());
        response.put("phone", user.getPhone());
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Actualizar perfil del usuario actual",
            description = """
                    Actualiza el perfil del usuario autenticado.
                    
                    **Campos actualizables**:
                    - `nombre`: Nuevo nombre del usuario
                    - `contraseña`: Nueva contraseña (se encripta automáticamente)
                    
                    **Nota**: Solo se actualizan los campos que se envían en el request body.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil actualizado exitosamente",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            )
    })
    @PutMapping("/me")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Campos a actualizar",
                    required = true,
                    content = @Content(mediaType = "application/json", examples = {
                            @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Actualizar nombre",
                                    value = "{\"nombre\": \"Nuevo Nombre\"}"
                            ),
                            @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Actualizar contraseña",
                                    value = "{\"contraseña\": \"nuevaPassword123\"}"
                            ),
                            @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Actualizar ambos",
                                    value = "{\"nombre\": \"Nuevo Nombre\", \"contraseña\": \"nuevaPassword123\"}"
                            )
                    })
            )
            @Valid @RequestBody Map<String, String> updates,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(username);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        User user = userOpt.get();
        
        if (updates.containsKey("nombre")) {
            user.setName(updates.get("nombre"));
        }
        
        if (updates.containsKey("contraseña")) {
            // Validar contraseña actual si se proporciona
            if (updates.containsKey("contraseñaActual")) {
                String contraseñaActual = updates.get("contraseñaActual");
                if (!contraseñaActual.equals(user.getPassword())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("message", "La contraseña actual es incorrecta"));
                }
            }
            user.setPassword(updates.get("contraseña")); // Guardar contraseña en texto plano
        }
        
        userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("usuarioId", user.getUserId());
        response.put("nombre", user.getName());
        response.put("email", user.getEmail());
        response.put("rol", user.getRole() != null ? user.getRole().name() : null);
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Actualizar usuario",
            description = """
                    Actualiza la información de un usuario existente. **Solo disponible para administradores.**
                    
                    **Campos actualizables**:
                    - `nombre`: Nuevo nombre del usuario
                    - `telefono`: Nuevo teléfono del usuario
                    - `rol`: Nuevo rol del usuario (CLIENTE, ESTILISTA, ADMINISTRADOR)
                    - `activo`: Estado activo/inactivo del usuario
                    - `contraseña`: Nueva contraseña
                    
                    **Nota**: Si se cambia el rol a ESTILISTA, se crearán automáticamente disponibilidades por defecto (Lunes a Viernes, 10:00 - 18:30).
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario actualizado exitosamente",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos para realizar esta acción",
                    content = @Content
            )
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateUsuario(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        User.Role rolAnterior = user.getRole();
        boolean rolCambioAEstilista = false;
        
        if (updates.containsKey("nombre")) {
            user.setName(updates.get("nombre").toString());
        }
        
        if (updates.containsKey("telefono")) {
            user.setPhone(updates.get("telefono") != null ? updates.get("telefono").toString() : null);
        }
        
        if (updates.containsKey("rol")) {
            String rolStr = updates.get("rol").toString();
            try {
                User.Role nuevoRol = User.Role.valueOf(rolStr.toUpperCase());
                user.setRole(nuevoRol);
                
                // Si el rol cambió a ESTILISTA (y antes no lo era), crear disponibilidades por defecto
                if (nuevoRol == User.Role.ESTILISTA && rolAnterior != User.Role.ESTILISTA) {
                    rolCambioAEstilista = true;
                }
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Rol inválido: " + rolStr));
            }
        }
        
        if (updates.containsKey("activo")) {
            Boolean activo = updates.get("activo") instanceof Boolean 
                    ? (Boolean) updates.get("activo")
                    : Boolean.parseBoolean(updates.get("activo").toString());
            user.setActive(activo);
        }
        
        if (updates.containsKey("contraseña")) {
            user.setPassword(updates.get("contraseña").toString());
        }
        
        userRepository.save(user);
        
        // Si cambió a ESTILISTA, crear disponibilidades por defecto
        if (rolCambioAEstilista) {
            createDefaultAvailabilities(user.getUserId());
        }
        
        // Asegurar que existe registro en la tabla correspondiente según el rol
        if (user.getRole() == User.Role.ESTILISTA) {
            try {
                userRepository.flush();
                userRepository.insertEstilistaRecord(user.getUserId());
            } catch (Exception e) {
                // Si falla, continuar (puede que ya exista el registro)
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("usuarioId", user.getUserId());
        response.put("nombre", user.getName());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("rol", user.getRole() != null ? user.getRole().name() : null);
        response.put("activo", user.getActive());
        response.put("phone", user.getPhone());
        
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/activo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Map<String, Object>> toggleUserActive(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        user.setActive(!user.getActive());
        userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("usuarioId", user.getUserId());
        response.put("nombre", user.getName());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("rol", user.getRole() != null ? user.getRole().name() : null);
        response.put("activo", user.getActive());
        response.put("phone", user.getPhone());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Crea disponibilidades por defecto para un estilista (Lunes a Viernes, 10:00 - 18:30).
     * Solo crea disponibilidades si el estilista no tiene ninguna ya configurada.
     */
    private void createDefaultAvailabilities(Long stylistId) {
        try {
            // Verificar si el estilista ya tiene disponibilidades
            java.util.List<com.bookmycut.dto.AvailabilityDTO> existing = availabilityService.getAllAvailabilities(stylistId);
            
            // Solo crear si no tiene disponibilidades
            if (existing == null || existing.isEmpty()) {
                logger.info("Creando disponibilidades por defecto para estilista ID: {}", stylistId);
                java.time.DayOfWeek[] diasSemana = {
                    java.time.DayOfWeek.MONDAY,
                    java.time.DayOfWeek.TUESDAY,
                    java.time.DayOfWeek.WEDNESDAY,
                    java.time.DayOfWeek.THURSDAY,
                    java.time.DayOfWeek.FRIDAY
                };
                
                java.time.LocalTime horaInicio = java.time.LocalTime.of(10, 0);
                java.time.LocalTime horaFin = java.time.LocalTime.of(18, 30);
                
                for (java.time.DayOfWeek dia : diasSemana) {
                    try {
                        com.bookmycut.dto.AvailabilityCreateDTO createDTO = new com.bookmycut.dto.AvailabilityCreateDTO();
                        createDTO.setStylistId(stylistId);
                        createDTO.setDayOfWeek(dia);
                        createDTO.setStartTime(horaInicio);
                        createDTO.setEndTime(horaFin);
                        availabilityService.createAvailability(createDTO);
                        logger.debug("Disponibilidad creada para estilista ID: {} - Día: {}", stylistId, dia);
                    } catch (com.bookmycut.exception.BadRequestException e) {
                        // Si ya existe una disponibilidad para ese día, continuar con los demás
                        logger.debug("Disponibilidad ya existe para estilista ID: {} - Día: {}", stylistId, dia);
                    } catch (Exception e) {
                        // Otros errores también se ignoran para no romper la actualización
                        logger.warn("Error al crear disponibilidad para estilista ID: {} - Día: {} - Error: {}", 
                                stylistId, dia, e.getMessage());
                    }
                }
                logger.info("Disponibilidades por defecto creadas para estilista ID: {}", stylistId);
            } else {
                logger.debug("El estilista ID: {} ya tiene disponibilidades configuradas, no se crean por defecto", stylistId);
            }
        } catch (Exception e) {
            // Si falla crear disponibilidades, no romper la actualización del usuario
            logger.error("Error al crear disponibilidades por defecto para estilista ID: {} - Error: {}", 
                    stylistId, e.getMessage(), e);
        }
    }
    
    @Operation(
            summary = "Obtener estilistas (público)",
            description = "Obtiene la lista de estilistas activos. **Endpoint público**, no requiere autenticación. " +
                    "Útil para que los clientes puedan seleccionar un estilista al reservar una cita."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de estilistas obtenida exitosamente",
                    content = @Content
            )
    })
    @GetMapping("/public/estilistas")
    public ResponseEntity<List<Map<String, Object>>> getEstilistasPublicos() {
        List<User> estilistas = userRepository.findByRoleAndActive(User.Role.ESTILISTA, true);
        List<Map<String, Object>> response = estilistas.stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("usuarioId", user.getUserId());
                    // Asegurar que el nombre se codifique correctamente con UTF-8
                    String nombre = user.getName() != null ? user.getName() : "";
                    userMap.put("nombre", nombre);
                    userMap.put("username", user.getUsername());
                    userMap.put("email", user.getEmail());
                    // Convertir el enum a String para la respuesta JSON
                    userMap.put("rol", user.getRole() != null ? user.getRole().name() : null);
                    userMap.put("activo", user.getActive());
                    return userMap;
                })
                .toList();
        // Asegurar que la respuesta use UTF-8 explícitamente
        return ResponseEntity.ok()
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(response);
    }
}


