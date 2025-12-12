package com.bookmycut.controller;

import com.bookmycut.entities.User;
import com.bookmycut.entities.ServiceOffer;
import com.bookmycut.exception.ResourceNotFoundException;
import com.bookmycut.repositories.UserRepository;
import com.bookmycut.repositories.ServiceOfferRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/estilistas")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Stylist Services", description = "Endpoints para que estilistas gestionen sus servicios")
@SecurityRequirement(name = "bearerAuth")
public class StylistServiceController {

    private static final Logger logger = LoggerFactory.getLogger(StylistServiceController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceOfferRepository serviceOfferRepository;

    @Operation(summary = "Obtener servicios de un estilista")
    @GetMapping("/{stylistId}/servicios")
    public ResponseEntity<List<Map<String, Object>>> getStylistServices(
            @PathVariable Long stylistId) {
        logger.info("Requesting services for stylist ID: {}", stylistId);
        try {
            User stylist = userRepository.findWithServicesByUserId(stylistId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", stylistId));
            
            if (stylist.getRole() != User.Role.ESTILISTA) {
                return ResponseEntity.badRequest().build();
            }
            
            // Cargar servicios (ya cargados con EntityGraph)
            List<ServiceOffer> servicesList = stylist.getServices();
            
            List<Map<String, Object>> services = servicesList.stream()
                    .map(service -> {
                        java.util.Map<String, Object> serviceMap = new java.util.HashMap<>();
                        serviceMap.put("serviceId", service.getServiceId());
                        serviceMap.put("name", service.getName());
                        serviceMap.put("description", service.getDescription() != null ? service.getDescription() : "");
                        serviceMap.put("duration", service.getDuration());
                        serviceMap.put("unitPrice", service.getUnitPrice());
                        return serviceMap;
                    })
                    .toList();
            
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            logger.error("Error getting stylist services: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Obtener mis servicios (estilista autenticado)")
    @GetMapping("/me/servicios")
    @PreAuthorize("hasRole('ESTILISTA')")
    public ResponseEntity<List<Map<String, Object>>> getMyServices(Authentication authentication) {
        logger.info("Requesting services for current stylist");
        try {
            String username = authentication.getName();
            User stylist = userRepository.findByUsernameOrEmail(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            
            // Cargar servicios usando EntityGraph
            User stylistWithServices = userRepository.findWithServicesByUserId(stylist.getUserId())
                    .orElse(stylist);
            List<ServiceOffer> servicesList = stylistWithServices.getServices();
            
            List<Map<String, Object>> services = servicesList.stream()
                    .map(service -> {
                        java.util.Map<String, Object> serviceMap = new java.util.HashMap<>();
                        serviceMap.put("serviceId", service.getServiceId());
                        serviceMap.put("name", service.getName());
                        serviceMap.put("description", service.getDescription() != null ? service.getDescription() : "");
                        serviceMap.put("duration", service.getDuration());
                        serviceMap.put("unitPrice", service.getUnitPrice());
                        return serviceMap;
                    })
                    .toList();
            
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            logger.error("Error getting my services: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Asociar servicios a estilista")
    @PostMapping("/{stylistId}/servicios")
    @PreAuthorize("hasRole('ESTILISTA') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> associateServices(
            @PathVariable Long stylistId,
            @RequestBody Map<String, List<Long>> request,
            Authentication authentication) {
        logger.info("Associating services to stylist ID: {}", stylistId);
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsernameOrEmail(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            
            // Si es estilista, solo puede asociar a sí mismo
            if (currentUser.getRole() == User.Role.ESTILISTA && !currentUser.getUserId().equals(stylistId)) {
                return ResponseEntity.status(403).build();
            }
            
            User stylist = userRepository.findById(stylistId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", stylistId));
            
            if (stylist.getRole() != User.Role.ESTILISTA) {
                return ResponseEntity.badRequest().build();
            }
            
            List<Long> serviceIds = request.get("serviceIds");
            if (serviceIds == null || serviceIds.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            // Obtener servicios desde el repositorio
            List<ServiceOffer> services = serviceOfferRepository.findAllById(serviceIds);
            
            if (services.size() != serviceIds.size()) {
                return ResponseEntity.badRequest().build();
            }
            
            stylist.setServices(services);
            userRepository.save(stylist);
            
            logger.info("Services associated successfully");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error associating services: {}", e.getMessage(), e);
            throw e;
        }
    }
}





