package com.bookmycut.controller;

import com.bookmycut.dto.AppointmentCreateDTO;
import com.bookmycut.dto.AppointmentDTO;
import com.bookmycut.dto.AuthResponse;
import com.bookmycut.dto.PublicAppointmentCreateDTO;
import com.bookmycut.dto.PublicAppointmentResponseDTO;
import com.bookmycut.entities.User;
import com.bookmycut.repositories.UserRepository;
import com.bookmycut.service.AppointmentService;
import com.bookmycut.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * REST controller for managing appointment-related operations.
 * Provides endpoints to create, query, update and delete appointments.
 */
@RestController
@RequestMapping(value = "/citas", produces = "application/json; charset=UTF-8")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Appointments", description = "Endpoints para gestión de citas. Los clientes pueden crear y ver sus citas, " +
        "los estilistas pueden ver sus citas asignadas, y los administradores pueden gestionar todas las citas.")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuthService authService;
    
    @Operation(
            summary = "Obtener citas",
            description = """
                    Obtiene las citas según el rol del usuario autenticado:
                    - **ADMINISTRADOR**: Puede ver todas las citas o filtrar por clienteId, estilistaId o fecha
                    - **CLIENTE**: Solo ve sus propias citas
                    - **ESTILISTA**: Solo ve las citas asignadas a él
                    
                    Parámetros opcionales (solo para administradores):
                    - `clienteId`: Filtrar por cliente específico
                    - `estilistaId`: Filtrar por estilista específico
                    - `fecha`: Filtrar por fecha específica (formato: YYYY-MM-DD)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de citas obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = AppointmentDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos para acceder a este recurso",
                    content = @Content
            )
    })
    @GetMapping
    public ResponseEntity<?> getAllCitas(
            @Parameter(description = "ID del cliente (solo para administradores)", example = "1")
            @RequestParam(required = false) Long clienteId,
            @Parameter(description = "ID del estilista (solo para administradores)", example = "2")
            @RequestParam(required = false) Long estilistaId,
            @Parameter(description = "Fecha específica (formato: YYYY-MM-DD, solo para administradores)", example = "2024-12-15")
            @RequestParam(required = false) LocalDate fecha,
            @Parameter(description = "Nombre del cliente (búsqueda parcial, solo para administradores)", example = "Juan")
            @RequestParam(required = false) String nombreCliente,
            @Parameter(description = "Nombre del estilista (búsqueda parcial, solo para administradores)", example = "María")
            @RequestParam(required = false) String nombreEstilista,
            @Parameter(description = "Nombre del servicio (búsqueda parcial, solo para administradores)", example = "Corte")
            @RequestParam(required = false) String nombreServicio,
            @Parameter(description = "Estado de la cita", example = "CONFIRMADA")
            @RequestParam(required = false) String estado,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Usar paginación", example = "false")
            @RequestParam(defaultValue = "false") boolean paginated,
            Authentication authentication) {
        
        logger.info("Solicitando citas - ClienteId: {}, EstilistaId: {}, Fecha: {}, NombreCliente: {}, NombreEstilista: {}, NombreServicio: {}, Estado: {}", 
                clienteId, estilistaId, fecha, nombreCliente, nombreEstilista, nombreServicio, estado);
        
        try {
            User user = null;
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                user = userRepository.findByUsernameOrEmail(username).orElse(null);
            }
            
            // Convertir estado String a enum si está presente
            com.bookmycut.entities.Appointment.AppointmentStatus statusEnum = null;
            if (estado != null && !estado.isEmpty()) {
                try {
                    statusEnum = com.bookmycut.entities.Appointment.AppointmentStatus.valueOf(estado.toUpperCase());
                } catch (IllegalArgumentException e) {
                    logger.warn("Estado inválido: {}", estado);
                }
            }
            
            // Si no hay autenticación, solo permitir filtrar por estilista y fecha (para ver disponibilidad)
            if (user == null) {
                logger.info("Solicitud sin autenticación - filtrando por estilistaId y fecha");
                List<AppointmentDTO> appointments;
                if (estilistaId != null && fecha != null) {
                    appointments = appointmentService.getAppointmentsByStylistAndDate(estilistaId, fecha);
                } else if (estilistaId != null) {
                    appointments = appointmentService.getAppointmentsByStylist(estilistaId);
                } else if (fecha != null) {
                    appointments = appointmentService.getAppointmentsByDate(fecha);
                } else {
                    // Sin autenticación y sin filtros, devolver lista vacía
                    appointments = java.util.Collections.emptyList();
                }
                return ResponseEntity.ok(appointments);
            }
            
            // Verificar si se usan filtros avanzados (solo para administradores)
            boolean useAdvancedFilters = user.getRole() == User.Role.ADMINISTRADOR && 
                    (nombreCliente != null || nombreEstilista != null || nombreServicio != null || estado != null);
            
            if (paginated) {
                // Usar paginación
                org.springframework.data.domain.Page<AppointmentDTO> appointments;
                
                if (useAdvancedFilters) {
                    // Usar búsqueda avanzada
                    appointments = appointmentService.searchAppointmentsWithFilters(
                            nombreCliente, nombreEstilista, nombreServicio, fecha, statusEnum, page, size);
                } else if (user.getRole() == User.Role.ADMINISTRADOR) {
                    if (clienteId != null) {
                        appointments = appointmentService.getAppointmentsByClient(clienteId, page, size);
                    } else if (estilistaId != null) {
                        appointments = appointmentService.getAppointmentsByStylist(estilistaId, page, size);
                    } else {
                        appointments = appointmentService.getAllAppointments(page, size);
                    }
                } else if (user.getRole() == User.Role.CLIENTE) {
                    appointments = appointmentService.getAppointmentsByClient(user.getUserId(), page, size);
                } else if (user.getRole() == User.Role.ESTILISTA) {
                    appointments = appointmentService.getAppointmentsByStylist(user.getUserId(), page, size);
                } else {
                    logger.warn("Usuario con rol no reconocido: {}", user.getRole());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                
                return ResponseEntity.ok(appointments);
            } else {
                // Usar lista sin paginar (compatibilidad hacia atrás)
                List<AppointmentDTO> appointments;
                
                if (useAdvancedFilters) {
                    // Usar búsqueda avanzada
                    appointments = appointmentService.searchAppointmentsWithFilters(
                            nombreCliente, nombreEstilista, nombreServicio, fecha, statusEnum);
                } else if (user.getRole() == User.Role.ADMINISTRADOR) {
                    if (clienteId != null) {
                        appointments = appointmentService.getAppointmentsByClient(clienteId);
                    } else if (estilistaId != null) {
                        appointments = appointmentService.getAppointmentsByStylist(estilistaId);
                    } else if (fecha != null) {
                        appointments = appointmentService.getAppointmentsByDate(fecha);
                    } else {
                        appointments = appointmentService.getAllAppointments();
                    }
                } else if (user.getRole() == User.Role.CLIENTE) {
                    appointments = appointmentService.getAppointmentsByClient(user.getUserId());
                } else if (user.getRole() == User.Role.ESTILISTA) {
                    appointments = appointmentService.getAppointmentsByStylist(user.getUserId());
                } else {
                    logger.warn("Usuario con rol no reconocido: {}", user.getRole());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                
                return ResponseEntity.ok(appointments);
            }
        } catch (Exception e) {
            logger.error("Error al obtener citas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(
            summary = "Obtener cita por ID",
            description = "Obtiene una cita específica por su ID. Solo pueden acceder: " +
                    "- El cliente dueño de la cita\n" +
                    "- El estilista asignado a la cita\n" +
                    "- Los administradores"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cita encontrada",
                    content = @Content(schema = @Schema(implementation = AppointmentDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cita no encontrada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos para acceder a esta cita",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDTO> getCitaById(
            @Parameter(description = "ID de la cita", required = true, example = "1")
            @PathVariable Long id, 
            Authentication authentication) {
        logger.info("Solicitando cita con ID: {}", id);
        try {
            AppointmentDTO appointment = appointmentService.getAppointmentById(id);
            
            // Verificar permisos
            String username = authentication.getName();
            User user = userRepository.findByUsernameOrEmail(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            if (user.getRole() != User.Role.ADMINISTRADOR &&
                !appointment.getClientId().equals(user.getUserId()) &&
                !appointment.getStylistId().equals(user.getUserId())) {
                logger.warn("Usuario {} intentó acceder a cita {} sin permisos", username, id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            return ResponseEntity.ok(appointment);
        } catch (com.bookmycut.exception.ResourceNotFoundException e) {
            logger.warn("Cita no encontrada con ID: {}", id);
            throw e; // El GlobalExceptionHandler lo manejará
        } catch (Exception e) {
            logger.error("Error al obtener cita con ID {}: {}", id, e.getMessage(), e);
            throw e; // El GlobalExceptionHandler lo manejará
        }
    }
    
    @Operation(
            summary = "Crear cita (público - sin autenticación)",
            description = """
                    Crea una nueva cita sin necesidad de autenticación. 
                    Si el cliente no existe, se crea automáticamente un usuario CLIENTE.
                    Solo se requiere proporcionar nombre, email y teléfono del cliente.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Cita creada exitosamente",
                    content = @Content(schema = @Schema(implementation = AppointmentDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Estilista o servicios no encontrados",
                    content = @Content
            )
    })
    @PostMapping("/public")
    public ResponseEntity<PublicAppointmentResponseDTO> createPublicCita(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de la cita pública a crear",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PublicAppointmentCreateDTO.class))
            )
            @Valid @RequestBody PublicAppointmentCreateDTO publicDTO) {
        logger.info("Creando nueva cita pública para cliente: {} ({})", publicDTO.getClientName(), publicDTO.getClientEmail());
        try {
            // Buscar o crear usuario CLIENTE automáticamente
            AuthService.ClientCreationResult clientResult = authService.findOrCreateClientUserWithResult(
                    publicDTO.getClientName(),
                    publicDTO.getClientEmail(),
                    publicDTO.getClientPhone(),
                    publicDTO.getClientPassword()
            );
            
            User clientUser = clientResult.getUser();
            
            // Convertir PublicAppointmentCreateDTO a AppointmentCreateDTO
            AppointmentCreateDTO createDTO = new AppointmentCreateDTO();
            createDTO.setClientId(clientUser.getUserId());
            createDTO.setStylistId(publicDTO.getStylistId());
            createDTO.setDate(publicDTO.getDate());
            createDTO.setStartTime(publicDTO.getStartTime());
            createDTO.setEndTime(publicDTO.getEndTime());
            createDTO.setClientPhone(publicDTO.getClientPhone());
            createDTO.setTotalPrice(publicDTO.getTotalPrice());
            createDTO.setServiceIds(publicDTO.getServiceIds());
            
            AppointmentDTO createdAppointment = appointmentService.createAppointment(createDTO);
            logger.info("Cita pública creada exitosamente con ID: {}", createdAppointment.getAppointmentId());
            
            // Si se creó un nuevo usuario con contraseña, generar token JWT
            PublicAppointmentResponseDTO response = new PublicAppointmentResponseDTO();
            response.setAppointment(createdAppointment);
            
            if (clientResult.wasNewUser() && publicDTO.getClientPassword() != null && !publicDTO.getClientPassword().trim().isEmpty()) {
                // Generar token para el nuevo usuario
                String tokenSubject = clientUser.getUsername() != null ? clientUser.getUsername() : clientUser.getEmail();
                String token = authService.getJwtUtil().generateToken(tokenSubject, clientUser.getUserId(), 
                        List.of("ROLE_" + clientUser.getRole().name()));
                
                response.setToken(token);
                response.setAuth(new AuthResponse(
                        token,
                        "Bearer",
                        clientUser.getUserId(),
                        clientUser.getName(),
                        clientUser.getEmail() != null ? clientUser.getEmail() : "",
                        clientUser.getRole()
                ));
                logger.info("Token JWT generado para nuevo usuario CLIENTE con ID: {}", clientUser.getUserId());
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (com.bookmycut.exception.BadRequestException | com.bookmycut.exception.ResourceNotFoundException e) {
            logger.warn("Error al crear cita pública: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error al crear cita pública: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Operation(
            summary = "Crear cita (autenticado)",
            description = """
                    Crea una nueva cita. Solo pueden crear:
                    - Los administradores (pueden crear para cualquier cliente)
                    - Los clientes autenticados (solo para sí mismos)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Cita creada exitosamente",
                    content = @Content(schema = @Schema(implementation = AppointmentDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos para crear citas",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente, estilista o servicios no encontrados",
                    content = @Content
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<AppointmentDTO> createCita(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de la cita a crear",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AppointmentCreateDTO.class))
            )
            @Valid @RequestBody AppointmentCreateDTO createDTO, 
            Authentication authentication) {
        logger.info("Creando nueva cita (autenticado)");
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsernameOrEmail(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            // Si es cliente, usar su propio ID
            if (user.getRole() == User.Role.CLIENTE) {
                createDTO.setClientId(user.getUserId());
            }
            
            AppointmentDTO createdAppointment = appointmentService.createAppointment(createDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAppointment);
        } catch (com.bookmycut.exception.BadRequestException | com.bookmycut.exception.ResourceNotFoundException e) {
            logger.warn("Error al crear cita: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error al crear cita: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Operation(
            summary = "Actualizar cita",
            description = """
                    Actualiza una cita existente. Solo pueden actualizar:
                    - El cliente dueño de la cita
                    - Los administradores
                    
                    **Campos actualizables** (enviar solo los que se desean cambiar):
                    - `estado`: Estado de la cita (CONFIRMADA, CANCELADA, FINALIZADA)
                    - `fecha`: Nueva fecha (YYYY-MM-DD)
                    - `horaInicio`: Nueva hora de inicio (HH:mm)
                    - `horaFin`: Nueva hora de fin (HH:mm)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cita actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = AppointmentDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cita no encontrada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos para actualizar esta cita",
                    content = @Content
            )
    })
    @PatchMapping("/{id}")
    public ResponseEntity<AppointmentDTO> updateCita(
            @Parameter(description = "ID de la cita a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Campos a actualizar (formato JSON con los campos deseados)",
                    required = true,
                    content = @Content(mediaType = "application/json", examples = {
                            @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Actualizar estado",
                                    value = "{\"estado\": \"CANCELADA\"}"
                            ),
                            @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Actualizar fecha y hora",
                                    value = "{\"fecha\": \"2024-12-20\", \"horaInicio\": \"10:00\", \"horaFin\": \"11:00\"}"
                            )
                    })
            )
            @RequestBody Map<String, Object> updates, 
            Authentication authentication) {
        logger.info("Actualizando cita con ID: {}", id);
        try {
            // Verificar permisos primero
            AppointmentDTO appointment = appointmentService.getAppointmentById(id);
            String username = authentication.getName();
            User user = userRepository.findByUsernameOrEmail(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            if (user.getRole() != User.Role.ADMINISTRADOR &&
                !appointment.getClientId().equals(user.getUserId())) {
                logger.warn("Usuario {} intentó actualizar cita {} sin permisos", username, id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            AppointmentDTO updatedAppointment = appointmentService.updateAppointment(id, updates);
            return ResponseEntity.ok(updatedAppointment);
        } catch (com.bookmycut.exception.ResourceNotFoundException e) {
            logger.warn("Cita no encontrada con ID: {}", id);
            throw e; // El GlobalExceptionHandler lo manejará
        } catch (Exception e) {
            logger.error("Error al actualizar cita con ID {}: {}", id, e.getMessage(), e);
            throw e; // El GlobalExceptionHandler lo manejará
        }
    }
    
    @Operation(
            summary = "Eliminar cita",
            description = "Elimina una cita del sistema. Solo pueden eliminar: " +
                    "- El cliente dueño de la cita\n" +
                    "- Los administradores"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Cita eliminada exitosamente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cita no encontrada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos para eliminar esta cita",
                    content = @Content
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCita(
            @Parameter(description = "ID de la cita a eliminar", required = true, example = "1")
            @PathVariable Long id, 
            Authentication authentication) {
        logger.info("Eliminando cita con ID: {}", id);
        try {
            // Verificar permisos primero
            AppointmentDTO appointment = appointmentService.getAppointmentById(id);
            String username = authentication.getName();
            User user = userRepository.findByUsernameOrEmail(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            if (user.getRole() != User.Role.ADMINISTRADOR &&
                !appointment.getClientId().equals(user.getUserId())) {
                logger.warn("Usuario {} intentó eliminar cita {} sin permisos", username, id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            appointmentService.deleteAppointment(id);
            return ResponseEntity.noContent().build();
        } catch (com.bookmycut.exception.ResourceNotFoundException e) {
            logger.warn("Cita no encontrada con ID: {}", id);
            throw e; // El GlobalExceptionHandler lo manejará
        } catch (Exception e) {
            logger.error("Error al eliminar cita con ID {}: {}", id, e.getMessage(), e);
            throw e; // El GlobalExceptionHandler lo manejará
        }
    }
}


