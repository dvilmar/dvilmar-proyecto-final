package com.bookmycut.mappers;

import com.bookmycut.dto.ServiceOfferCreateDTO;
import com.bookmycut.dto.ServiceOfferDTO;
import com.bookmycut.entities.ServiceOffer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Component responsible for mapping {@link ServiceOffer} entities to DTOs
 * and vice versa, to facilitate data transfer between application layers.
 */
@Component
public class ServiceOfferMapper {

    /**
     * Converts a {@link ServiceOffer} entity to a {@link ServiceOfferDTO}.
     *
     * @param serviceOffer The {@link ServiceOffer} entity to map.
     * @return A {@link ServiceOfferDTO} object with mapped data.
     */
    public ServiceOfferDTO toDTO(ServiceOffer serviceOffer) {
        if (serviceOffer == null) {
            return null;
        }

        ServiceOfferDTO dto = new ServiceOfferDTO();
        dto.setServiceId(serviceOffer.getServiceId());
        dto.setName(serviceOffer.getName());
        dto.setDescription(serviceOffer.getDescription());
        dto.setDuration(serviceOffer.getDuration());
        dto.setUnitPrice(serviceOffer.getUnitPrice());

        return dto;
    }

    /**
     * Converts a {@link ServiceOfferCreateDTO} object to a {@link ServiceOffer} entity.
     *
     * @param createDTO The DTO with data needed to create the service.
     * @return A {@link ServiceOffer} entity ready to be persisted.
     */
    public ServiceOffer toEntity(ServiceOfferCreateDTO createDTO) {
        if (createDTO == null) {
            return null;
        }

        ServiceOffer serviceOffer = new ServiceOffer();
        serviceOffer.setName(createDTO.getName());
        serviceOffer.setDescription(createDTO.getDescription());
        serviceOffer.setDuration(createDTO.getDuration());
        serviceOffer.setUnitPrice(createDTO.getUnitPrice());

        return serviceOffer;
    }

    /**
     * Updates a {@link ServiceOffer} entity with data from a {@link ServiceOfferCreateDTO}.
     *
     * @param serviceOffer The entity to update.
     * @param createDTO The DTO with new data.
     */
    public void updateEntity(ServiceOffer serviceOffer, ServiceOfferCreateDTO createDTO) {
        if (serviceOffer == null || createDTO == null) {
            return;
        }

        serviceOffer.setName(createDTO.getName());
        serviceOffer.setDescription(createDTO.getDescription());
        serviceOffer.setDuration(createDTO.getDuration());
        serviceOffer.setUnitPrice(createDTO.getUnitPrice());
    }

    /**
     * Converts a list of {@link ServiceOffer} entities to a list of {@link ServiceOfferDTO}.
     *
     * @param serviceOffers List of {@link ServiceOffer} entities.
     * @return List of {@link ServiceOfferDTO}.
     */
    public List<ServiceOfferDTO> toDTOList(List<ServiceOffer> serviceOffers) {
        if (serviceOffers == null) {
            return null;
        }
        return serviceOffers.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
