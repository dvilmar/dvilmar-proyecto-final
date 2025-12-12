package com.bookmycut.service;

import com.bookmycut.dto.ServiceOfferCreateDTO;
import com.bookmycut.dto.ServiceOfferDTO;
import com.bookmycut.exception.ResourceNotFoundException;
import com.bookmycut.mappers.ServiceOfferMapper;
import com.bookmycut.entities.ServiceOffer;
import com.bookmycut.repositories.ServiceOfferRepository;
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

/**
 * Service responsible for business logic related to service offers.
 * Manages creation, update, query and deletion of service offers.
 */
@Service
public class ServiceOfferService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceOfferService.class);

    @Autowired
    private ServiceOfferRepository serviceOfferRepository;

    @Autowired
    private ServiceOfferMapper serviceOfferMapper;

    /**
     * Gets all available service offers.
     *
     * @return List of ServiceOfferDTO.
     */
    public List<ServiceOfferDTO> getAllServiceOffers() {
        logger.info("Requesting all service offers");
        try {
            List<ServiceOffer> serviceOffers = serviceOfferRepository.findAll();
            logger.info("Found {} service offers", serviceOffers.size());
            return serviceOfferMapper.toDTOList(serviceOffers);
        } catch (Exception e) {
            logger.error("Error getting all service offers: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets all available service offers with pagination.
     *
     * @param page Page number (0-indexed).
     * @param size Page size.
     * @return Page of ServiceOfferDTO.
     */
    public Page<ServiceOfferDTO> getAllServiceOffers(int page, int size) {
        logger.info("Requesting all service offers - page: {}, size: {}", page, size);
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
            Page<ServiceOffer> serviceOffers = serviceOfferRepository.findAll(pageable);
            logger.info("Found {} service offers", serviceOffers.getTotalElements());
            return serviceOffers.map(serviceOfferMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error getting paginated service offers: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets a service offer by its ID.
     *
     * @param id Unique identifier of the service offer.
     * @return ServiceOfferDTO of the found service offer.
     * @throws ResourceNotFoundException If the service offer does not exist.
     */
    public ServiceOfferDTO getServiceOfferById(Long id) {
        logger.info("Searching for service offer with ID: {}", id);
        try {
            ServiceOffer serviceOffer = serviceOfferRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Service offer not found with ID: {}", id);
                        return new ResourceNotFoundException("ServiceOffer", "id", id);
                    });
            logger.debug("Service offer with ID {} found", id);
            return serviceOfferMapper.toDTO(serviceOffer);
        } catch (Exception e) {
            logger.error("Error getting service offer with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Creates a new service offer in the system.
     *
     * @param createDTO DTO with service offer data to create.
     * @return ServiceOfferDTO of the created service offer.
     */
    @Transactional
    public ServiceOfferDTO createServiceOffer(ServiceOfferCreateDTO createDTO) {
        logger.info("Creating new service offer: {}", createDTO.getName());
        try {
            ServiceOffer serviceOffer = serviceOfferMapper.toEntity(createDTO);
            ServiceOffer savedServiceOffer = serviceOfferRepository.save(serviceOffer);
            logger.info("Service offer created successfully with ID: {}", savedServiceOffer.getServiceId());
            return serviceOfferMapper.toDTO(savedServiceOffer);
        } catch (Exception e) {
            logger.error("Error creating service offer: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Updates an existing service offer.
     *
     * @param id ID of the service offer to update.
     * @param createDTO DTO with new service offer data.
     * @return ServiceOfferDTO of the updated service offer.
     * @throws ResourceNotFoundException If the service offer does not exist.
     */
    @Transactional
    public ServiceOfferDTO updateServiceOffer(Long id, ServiceOfferCreateDTO createDTO) {
        logger.info("Updating service offer with ID: {}", id);
        try {
            ServiceOffer serviceOffer = serviceOfferRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Service offer not found with ID: {}", id);
                        return new ResourceNotFoundException("ServiceOffer", "id", id);
                    });

            serviceOfferMapper.updateEntity(serviceOffer, createDTO);
            ServiceOffer updatedServiceOffer = serviceOfferRepository.save(serviceOffer);
            logger.info("Service offer with ID {} updated successfully", id);
            return serviceOfferMapper.toDTO(updatedServiceOffer);
        } catch (Exception e) {
            logger.error("Error updating service offer with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Deletes a service offer by its ID.
     *
     * @param id Unique identifier of the service offer.
     * @throws ResourceNotFoundException If the service offer does not exist.
     */
    @Transactional
    public void deleteServiceOffer(Long id) {
        logger.info("Deleting service offer with ID: {}", id);
        try {
            ServiceOffer serviceOffer = serviceOfferRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Service offer not found with ID: {}", id);
                        return new ResourceNotFoundException("ServiceOffer", "id", id);
                    });

            serviceOfferRepository.delete(serviceOffer);
            logger.info("Service offer with ID {} deleted successfully", id);
        } catch (Exception e) {
            logger.error("Error deleting service offer with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}

