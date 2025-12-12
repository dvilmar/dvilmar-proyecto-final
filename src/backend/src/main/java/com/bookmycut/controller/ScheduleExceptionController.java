package com.bookmycut.controller;

import com.bookmycut.dto.ScheduleExceptionCreateDTO;
import com.bookmycut.dto.ScheduleExceptionDTO;
import com.bookmycut.entities.User;
import com.bookmycut.repositories.UserRepository;
import com.bookmycut.service.ScheduleExceptionService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/excepciones-horario")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Schedule Exceptions", description = "Endpoints para gestión de excepciones de horario")
@SecurityRequirement(name = "bearerAuth")
public class ScheduleExceptionController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleExceptionController.class);

    @Autowired
    private ScheduleExceptionService scheduleExceptionService;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Obtener excepciones de horario", description = "Puede filtrar por estilistaId y/o fecha")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de excepciones obtenida exitosamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @GetMapping
    public ResponseEntity<List<ScheduleExceptionDTO>> getExceptions(
            @Parameter(description = "ID del estilista (opcional)", example = "2")
            @RequestParam(required = false) Long estilistaId,
            @Parameter(description = "Fecha (opcional)", example = "2024-12-25")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        logger.info("Requesting schedule exceptions - estilistaId: {}, fecha: {}", estilistaId, fecha);
        try {
            List<ScheduleExceptionDTO> exceptions = scheduleExceptionService.getAllExceptions(estilistaId, fecha);
            return ResponseEntity.ok(exceptions);
        } catch (Exception e) {
            logger.error("Error getting schedule exceptions: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Obtener excepción por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ScheduleExceptionDTO> getExceptionById(@PathVariable Long id) {
        logger.info("Requesting schedule exception with ID: {}", id);
        try {
            ScheduleExceptionDTO exception = scheduleExceptionService.getExceptionById(id);
            return ResponseEntity.ok(exception);
        } catch (Exception e) {
            logger.error("Error getting schedule exception: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Crear excepción de horario", description = "Solo administradores pueden crear excepciones")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping
    public ResponseEntity<ScheduleExceptionDTO> createException(
            @Valid @RequestBody ScheduleExceptionCreateDTO createDTO,
            Authentication authentication) {
        logger.info("Creating schedule exception");
        try {
            String username = authentication.getName();
            User administrator = userRepository.findByUsernameOrEmail(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            ScheduleExceptionDTO created = scheduleExceptionService.createException(createDTO, administrator);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating schedule exception: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Actualizar excepción de horario")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ScheduleExceptionDTO> updateException(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleExceptionCreateDTO createDTO) {
        logger.info("Updating schedule exception with ID: {}", id);
        try {
            ScheduleExceptionDTO updated = scheduleExceptionService.updateException(id, createDTO);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating schedule exception: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Eliminar excepción de horario")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteException(@PathVariable Long id) {
        logger.info("Deleting schedule exception with ID: {}", id);
        try {
            scheduleExceptionService.deleteException(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting schedule exception: {}", e.getMessage(), e);
            throw e;
        }
    }
}





