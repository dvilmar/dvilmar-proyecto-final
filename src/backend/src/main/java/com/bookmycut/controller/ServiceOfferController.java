package com.bookmycut.controller;

import com.bookmycut.dto.ServiceOfferCreateDTO;
import com.bookmycut.dto.ServiceOfferDTO;
import com.bookmycut.service.ServiceOfferService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing service offer-related operations.
 * Provides endpoints to create, query, update and delete service offers.
 */
@RestController
@RequestMapping(value = "/servicios", produces = "application/json; charset=UTF-8")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Service Offers", description = "Endpoints para gestión de servicios ofrecidos por la peluquería. " +
        "Los servicios pueden ser consultados por todos, pero solo estilistas y administradores pueden crearlos y modificarlos.")
@SecurityRequirement(name = "bearerAuth")
public class ServiceOfferController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceOfferController.class);
    
    @Autowired
    private ServiceOfferService serviceOfferService;
    
    @Operation(
            summary = "Obtener todos los servicios",
            description = "Obtiene la lista completa de servicios disponibles en el sistema. " +
                    "Este endpoint es público y no requiere autenticación."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de servicios obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = ServiceOfferDTO.class))
            )
    })
    @GetMapping
    public ResponseEntity<?> getAllServicios(
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Usar paginación", example = "false")
            @RequestParam(defaultValue = "false") boolean paginated) {
        logger.info("Solicitando todos los servicios - page: {}, size: {}, paginated: {}", page, size, paginated);
        try {
            if (paginated) {
                org.springframework.data.domain.Page<ServiceOfferDTO> services = 
                        serviceOfferService.getAllServiceOffers(page, size);
                return ResponseEntity.ok(services);
            } else {
                List<ServiceOfferDTO> services = serviceOfferService.getAllServiceOffers();
                return ResponseEntity.ok(services);
            }
        } catch (Exception e) {
            logger.error("Error al obtener servicios: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(
            summary = "Obtener servicio por ID",
            description = "Obtiene un servicio específico por su ID. Este endpoint es público y no requiere autenticación."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Servicio encontrado",
                    content = @Content(schema = @Schema(implementation = ServiceOfferDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Servicio no encontrado",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ServiceOfferDTO> getServicioById(
            @Parameter(description = "ID del servicio", required = true, example = "1")
            @PathVariable Long id) {
        logger.info("Solicitando servicio con ID: {}", id);
        try {
            ServiceOfferDTO service = serviceOfferService.getServiceOfferById(id);
            return ResponseEntity.ok(service);
        } catch (com.bookmycut.exception.ResourceNotFoundException e) {
            logger.warn("Servicio no encontrado con ID: {}", id);
            throw e; // El GlobalExceptionHandler lo manejará
        } catch (Exception e) {
            logger.error("Error al obtener servicio con ID {}: {}", id, e.getMessage(), e);
            throw e; // El GlobalExceptionHandler lo manejará
        }
    }
    
    @Operation(
            summary = "Crear nuevo servicio",
            description = """
                    Crea un nuevo servicio en el sistema.
                    
                    **Permisos requeridos**: ESTILISTA o ADMINISTRADOR
                    
                    **Campos requeridos**:
                    - `name`: Nombre del servicio (máximo 100 caracteres)
                    - `duration`: Duración en minutos (debe ser positivo)
                    - `unitPrice`: Precio unitario (debe ser positivo)
                    
                    **Campos opcionales**:
                    - `description`: Descripción del servicio (máximo 500 caracteres)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Servicio creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ServiceOfferDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos para crear servicios",
                    content = @Content
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ESTILISTA')")
    public ResponseEntity<ServiceOfferDTO> createServicio(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del servicio a crear",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ServiceOfferCreateDTO.class))
            )
            @Valid @RequestBody ServiceOfferCreateDTO createDTO) {
        logger.info("Creando nuevo servicio: {}", createDTO.getName());
        try {
            ServiceOfferDTO createdService = serviceOfferService.createServiceOffer(createDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdService);
        } catch (Exception e) {
            logger.error("Error al crear servicio: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @Operation(
            summary = "Actualizar servicio",
            description = """
                    Actualiza un servicio existente.
                    
                    **Permisos requeridos**: ESTILISTA o ADMINISTRADOR
                    
                    **Nota**: Todos los campos se actualizan. Envía todos los campos, incluso los que no cambian.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Servicio actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ServiceOfferDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Servicio no encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos para actualizar servicios",
                    content = @Content
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ESTILISTA')")
    public ResponseEntity<ServiceOfferDTO> updateServicio(
            @Parameter(description = "ID del servicio a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos actualizados del servicio",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ServiceOfferCreateDTO.class))
            )
            @Valid @RequestBody ServiceOfferCreateDTO createDTO) {
        logger.info("Actualizando servicio con ID: {}", id);
        try {
            ServiceOfferDTO updatedService = serviceOfferService.updateServiceOffer(id, createDTO);
            return ResponseEntity.ok(updatedService);
        } catch (com.bookmycut.exception.ResourceNotFoundException e) {
            logger.warn("Servicio no encontrado con ID: {}", id);
            throw e; // El GlobalExceptionHandler lo manejará
        } catch (Exception e) {
            logger.error("Error al actualizar servicio con ID {}: {}", id, e.getMessage(), e);
            throw e; // El GlobalExceptionHandler lo manejará
        }
    }
    
    @Operation(
            summary = "Eliminar servicio",
            description = """
                    Elimina un servicio del sistema.
                    
                    **Permisos requeridos**: Solo ADMINISTRADOR
                    
                    **Advertencia**: Esta operación es permanente y no se puede deshacer.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Servicio eliminado exitosamente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Servicio no encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos para eliminar servicios",
                    content = @Content
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> deleteServicio(
            @Parameter(description = "ID del servicio a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        logger.info("Eliminando servicio con ID: {}", id);
        try {
            serviceOfferService.deleteServiceOffer(id);
            return ResponseEntity.noContent().build();
        } catch (com.bookmycut.exception.ResourceNotFoundException e) {
            logger.warn("Servicio no encontrado con ID: {}", id);
            throw e; // El GlobalExceptionHandler lo manejará
        } catch (Exception e) {
            logger.error("Error al eliminar servicio con ID {}: {}", id, e.getMessage(), e);
            throw e; // El GlobalExceptionHandler lo manejará
        }
    }
}


