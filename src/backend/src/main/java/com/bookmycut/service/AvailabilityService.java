package com.bookmycut.service;

import com.bookmycut.dto.AvailabilityCreateDTO;
import com.bookmycut.dto.AvailabilityDTO;
import com.bookmycut.entities.Availability;
import com.bookmycut.entities.User;
import com.bookmycut.exception.BadRequestException;
import com.bookmycut.exception.ResourceNotFoundException;
import com.bookmycut.repositories.AvailabilityRepository;
import com.bookmycut.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    private static final Logger logger = LoggerFactory.getLogger(AvailabilityService.class);

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private UserRepository userRepository;

    public List<AvailabilityDTO> getAllAvailabilities(Long stylistId) {
        logger.info("Requesting availabilities for stylist ID: {}", stylistId);
        try {
            List<Availability> availabilities;
            if (stylistId != null) {
                User stylist = userRepository.findById(stylistId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", stylistId));
                availabilities = availabilityRepository.findByStylist(stylist);
            } else {
                availabilities = availabilityRepository.findAll();
            }
            return availabilities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting availabilities: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets all availabilities with pagination.
     *
     * @param stylistId Optional stylist ID to filter.
     * @param page Page number (0-indexed).
     * @param size Page size.
     * @return Page of AvailabilityDTO.
     */
    public Page<AvailabilityDTO> getAllAvailabilities(Long stylistId, int page, int size) {
        logger.info("Requesting availabilities - stylistId: {}, page: {}, size: {}", stylistId, page, size);
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("dayOfWeek", "startTime"));
            Page<Availability> availabilities;
            
            if (stylistId != null) {
                User stylist = userRepository.findById(stylistId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", stylistId));
                availabilities = availabilityRepository.findByStylist(stylist, pageable);
            } else {
                availabilities = availabilityRepository.findAll(pageable);
            }
            
            return availabilities.map(this::toDTO);
        } catch (Exception e) {
            logger.error("Error getting paginated availabilities: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AvailabilityDTO getAvailabilityById(Long id) {
        logger.info("Searching for availability with ID: {}", id);
        try {
            Availability availability = availabilityRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Availability not found with ID: {}", id);
                        return new ResourceNotFoundException("Availability", "id", id);
                    });
            return toDTO(availability);
        } catch (Exception e) {
            logger.error("Error getting availability with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public AvailabilityDTO createAvailability(AvailabilityCreateDTO createDTO) {
        logger.info("Creating new availability for stylist ID: {}", createDTO.getStylistId());
        try {
            User stylist = userRepository.findById(createDTO.getStylistId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", createDTO.getStylistId()));
            
            if (stylist.getRole() != User.Role.ESTILISTA) {
                throw new BadRequestException("El usuario especificado no es un estilista");
            }

            if (createDTO.getEndTime().isBefore(createDTO.getStartTime()) || 
                createDTO.getEndTime().equals(createDTO.getStartTime())) {
                throw new BadRequestException("La hora de fin debe ser posterior a la hora de inicio");
            }

            // Verificar que no exista ya una disponibilidad para el mismo día
            List<Availability> existing = availabilityRepository
                    .findByStylistAndDayOfWeek(stylist, createDTO.getDayOfWeek());
            if (!existing.isEmpty()) {
                throw new BadRequestException("Ya existe una disponibilidad para este día de la semana");
            }

            Availability availability = new Availability();
            availability.setStylist(stylist);
            availability.setDayOfWeek(createDTO.getDayOfWeek());
            availability.setStartTime(createDTO.getStartTime());
            availability.setEndTime(createDTO.getEndTime());

            Availability saved = availabilityRepository.save(availability);
            logger.info("Availability created successfully with ID: {}", saved.getAvailabilityId());
            return toDTO(saved);
        } catch (BadRequestException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating availability: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public AvailabilityDTO updateAvailability(Long id, AvailabilityCreateDTO createDTO) {
        logger.info("Updating availability with ID: {}", id);
        try {
            Availability availability = availabilityRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Availability", "id", id));

            if (createDTO.getEndTime().isBefore(createDTO.getStartTime()) || 
                createDTO.getEndTime().equals(createDTO.getStartTime())) {
                throw new BadRequestException("La hora de fin debe ser posterior a la hora de inicio");
            }

            availability.setDayOfWeek(createDTO.getDayOfWeek());
            availability.setStartTime(createDTO.getStartTime());
            availability.setEndTime(createDTO.getEndTime());

            Availability updated = availabilityRepository.save(availability);
            logger.info("Availability with ID {} updated successfully", id);
            return toDTO(updated);
        } catch (Exception e) {
            logger.error("Error updating availability with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void deleteAvailability(Long id) {
        logger.info("Deleting availability with ID: {}", id);
        try {
            Availability availability = availabilityRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Availability", "id", id));
            availabilityRepository.delete(availability);
            logger.info("Availability with ID {} deleted successfully", id);
        } catch (Exception e) {
            logger.error("Error deleting availability with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    private AvailabilityDTO toDTO(Availability availability) {
        AvailabilityDTO dto = new AvailabilityDTO();
        dto.setAvailabilityId(availability.getAvailabilityId());
        dto.setStylistId(availability.getStylist().getUserId());
        dto.setStylistName(availability.getStylist().getName());
        dto.setDayOfWeek(availability.getDayOfWeek());
        dto.setStartTime(availability.getStartTime());
        dto.setEndTime(availability.getEndTime());
        return dto;
    }
}





