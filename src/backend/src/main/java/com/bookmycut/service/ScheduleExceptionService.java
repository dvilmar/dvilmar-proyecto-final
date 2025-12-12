package com.bookmycut.service;

import com.bookmycut.dto.ScheduleExceptionCreateDTO;
import com.bookmycut.dto.ScheduleExceptionDTO;
import com.bookmycut.entities.ScheduleException;
import com.bookmycut.entities.User;
import com.bookmycut.exception.BadRequestException;
import com.bookmycut.exception.ResourceNotFoundException;
import com.bookmycut.repositories.ScheduleExceptionRepository;
import com.bookmycut.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleExceptionService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleExceptionService.class);

    @Autowired
    private ScheduleExceptionRepository scheduleExceptionRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ScheduleExceptionDTO> getAllExceptions(Long stylistId, LocalDate date) {
        logger.info("Requesting schedule exceptions - stylistId: {}, date: {}", stylistId, date);
        try {
            List<ScheduleException> exceptions;
            if (stylistId != null && date != null) {
                User stylist = userRepository.findById(stylistId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", stylistId));
                exceptions = scheduleExceptionRepository.findByStylistAndDate(stylist, date);
            } else if (stylistId != null) {
                User stylist = userRepository.findById(stylistId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", stylistId));
                exceptions = scheduleExceptionRepository.findByStylist(stylist);
            } else if (date != null) {
                exceptions = scheduleExceptionRepository.findByDate(date);
            } else {
                exceptions = scheduleExceptionRepository.findAll();
            }
            return exceptions.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting schedule exceptions: {}", e.getMessage(), e);
            throw e;
        }
    }

    public ScheduleExceptionDTO getExceptionById(Long id) {
        logger.info("Searching for schedule exception with ID: {}", id);
        try {
            ScheduleException exception = scheduleExceptionRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Schedule exception not found with ID: {}", id);
                        return new ResourceNotFoundException("ScheduleException", "id", id);
                    });
            return toDTO(exception);
        } catch (Exception e) {
            logger.error("Error getting schedule exception with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public ScheduleExceptionDTO createException(ScheduleExceptionCreateDTO createDTO, User administrator) {
        logger.info("Creating new schedule exception for date: {}", createDTO.getDate());
        try {
            ScheduleException exception = new ScheduleException();
            exception.setAdministrator(administrator);
            exception.setDate(createDTO.getDate());
            exception.setStartTime(createDTO.getStartTime());
            exception.setEndTime(createDTO.getEndTime());
            exception.setType(createDTO.getType());
            exception.setReason(createDTO.getReason());

            if (createDTO.getStylistId() != null) {
                User stylist = userRepository.findById(createDTO.getStylistId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", createDTO.getStylistId()));
                exception.setStylist(stylist);
            }

            if (createDTO.getStartTime() != null && createDTO.getEndTime() != null) {
                if (createDTO.getEndTime().isBefore(createDTO.getStartTime())) {
                    throw new BadRequestException("La hora de fin debe ser posterior a la hora de inicio");
                }
            }

            ScheduleException saved = scheduleExceptionRepository.save(exception);
            logger.info("Schedule exception created successfully with ID: {}", saved.getScheduleExceptionId());
            return toDTO(saved);
        } catch (BadRequestException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating schedule exception: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public ScheduleExceptionDTO updateException(Long id, ScheduleExceptionCreateDTO createDTO) {
        logger.info("Updating schedule exception with ID: {}", id);
        try {
            ScheduleException exception = scheduleExceptionRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("ScheduleException", "id", id));

            exception.setDate(createDTO.getDate());
            exception.setStartTime(createDTO.getStartTime());
            exception.setEndTime(createDTO.getEndTime());
            exception.setType(createDTO.getType());
            exception.setReason(createDTO.getReason());

            if (createDTO.getStylistId() != null) {
                User stylist = userRepository.findById(createDTO.getStylistId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", createDTO.getStylistId()));
                exception.setStylist(stylist);
            } else {
                exception.setStylist(null);
            }

            if (createDTO.getStartTime() != null && createDTO.getEndTime() != null) {
                if (createDTO.getEndTime().isBefore(createDTO.getStartTime())) {
                    throw new BadRequestException("La hora de fin debe ser posterior a la hora de inicio");
                }
            }

            ScheduleException updated = scheduleExceptionRepository.save(exception);
            logger.info("Schedule exception with ID {} updated successfully", id);
            return toDTO(updated);
        } catch (Exception e) {
            logger.error("Error updating schedule exception with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void deleteException(Long id) {
        logger.info("Deleting schedule exception with ID: {}", id);
        try {
            ScheduleException exception = scheduleExceptionRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("ScheduleException", "id", id));
            scheduleExceptionRepository.delete(exception);
            logger.info("Schedule exception with ID {} deleted successfully", id);
        } catch (Exception e) {
            logger.error("Error deleting schedule exception with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    private ScheduleExceptionDTO toDTO(ScheduleException exception) {
        ScheduleExceptionDTO dto = new ScheduleExceptionDTO();
        dto.setScheduleExceptionId(exception.getScheduleExceptionId());
        dto.setDate(exception.getDate());
        dto.setStartTime(exception.getStartTime());
        dto.setEndTime(exception.getEndTime());
        dto.setType(exception.getType());
        dto.setReason(exception.getReason());
        
        if (exception.getStylist() != null) {
            dto.setStylistId(exception.getStylist().getUserId());
            dto.setStylistName(exception.getStylist().getName());
        }
        
        dto.setAdministratorId(exception.getAdministrator().getUserId());
        dto.setAdministratorName(exception.getAdministrator().getName());
        
        return dto;
    }
}





