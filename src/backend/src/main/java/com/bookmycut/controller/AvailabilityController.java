package com.bookmycut.controller;

import com.bookmycut.dto.AvailabilityCreateDTO;
import com.bookmycut.dto.AvailabilityDTO;
import com.bookmycut.entities.User;
import com.bookmycut.repositories.UserRepository;
import com.bookmycut.service.AvailabilityService;
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

import java.util.List;

@RestController
@RequestMapping(value = "/disponibilidades", produces = "application/json; charset=UTF-8")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Availability", description = "Endpoints para gestión de disponibilidad de estilistas")
@SecurityRequirement(name = "bearerAuth")
public class AvailabilityController {

    private static final Logger logger = LoggerFactory.getLogger(AvailabilityController.class);

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Obtener disponibilidades", description = "Obtiene las disponibilidades. Puede filtrar por estilistaId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de disponibilidades obtenida exitosamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @GetMapping
    public ResponseEntity<?> getAvailabilities(
            @Parameter(description = "ID del estilista (opcional)", example = "2")
            @RequestParam(required = false) Long estilistaId,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Usar paginación", example = "false")
            @RequestParam(defaultValue = "false") boolean paginated) {
        logger.info("Requesting availabilities - estilistaId: {}, page: {}, size: {}, paginated: {}", 
                estilistaId, page, size, paginated);
        try {
            if (paginated) {
                org.springframework.data.domain.Page<AvailabilityDTO> availabilities = 
                        availabilityService.getAllAvailabilities(estilistaId, page, size);
                return ResponseEntity.ok(availabilities);
            } else {
                List<AvailabilityDTO> availabilities = availabilityService.getAllAvailabilities(estilistaId);
                return ResponseEntity.ok(availabilities);
            }
        } catch (Exception e) {
            logger.error("Error getting availabilities: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Obtener disponibilidad por ID")
    @GetMapping("/{id}")
    public ResponseEntity<AvailabilityDTO> getAvailabilityById(@PathVariable Long id) {
        logger.info("Requesting availability with ID: {}", id);
        try {
            AvailabilityDTO availability = availabilityService.getAvailabilityById(id);
            return ResponseEntity.ok(availability);
        } catch (Exception e) {
            logger.error("Error getting availability: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Crear disponibilidad", description = "Crea una nueva disponibilidad para un estilista")
    @PreAuthorize("hasRole('ESTILISTA') or hasRole('ADMINISTRADOR')")
    @PostMapping
    public ResponseEntity<AvailabilityDTO> createAvailability(
            @Valid @RequestBody AvailabilityCreateDTO createDTO,
            Authentication authentication) {
        logger.info("Creating availability");
        try {
            // Si es estilista, usar su propio ID
            String username = authentication.getName();
            User user = userRepository.findByUsernameOrEmail(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            if (user.getRole() == User.Role.ESTILISTA) {
                createDTO.setStylistId(user.getUserId());
            }
            
            AvailabilityDTO created = availabilityService.createAvailability(createDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating availability: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Actualizar disponibilidad")
    @PreAuthorize("hasRole('ESTILISTA') or hasRole('ADMINISTRADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<AvailabilityDTO> updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityCreateDTO createDTO) {
        logger.info("Updating availability with ID: {}", id);
        try {
            AvailabilityDTO updated = availabilityService.updateAvailability(id, createDTO);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating availability: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Eliminar disponibilidad")
    @PreAuthorize("hasRole('ESTILISTA') or hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable Long id) {
        logger.info("Deleting availability with ID: {}", id);
        try {
            availabilityService.deleteAvailability(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting availability: {}", e.getMessage(), e);
            throw e;
        }
    }
}

