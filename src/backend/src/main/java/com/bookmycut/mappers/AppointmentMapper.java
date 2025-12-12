package com.bookmycut.mappers;

import com.bookmycut.dto.AppointmentCreateDTO;
import com.bookmycut.dto.AppointmentDTO;
import com.bookmycut.entities.Appointment;
import com.bookmycut.entities.User;
import com.bookmycut.entities.ServiceOffer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Component responsible for mapping {@link Appointment} entities to DTOs
 * and vice versa, to facilitate data transfer between application layers.
 */
@Component
public class AppointmentMapper {

    /**
     * Converts an {@link Appointment} entity to an {@link AppointmentDTO}.
     *
     * @param appointment The {@link Appointment} entity to map.
     * @return An {@link AppointmentDTO} object with mapped data.
     */
    public AppointmentDTO toDTO(Appointment appointment) {
        if (appointment == null) {
            return null;
        }

        AppointmentDTO dto = new AppointmentDTO();
        dto.setAppointmentId(appointment.getAppointmentId());
        dto.setDate(appointment.getDate());
        dto.setStartTime(appointment.getStartTime());
        dto.setEndTime(appointment.getEndTime());
        dto.setStatus(appointment.getStatus().name());
        dto.setClientPhone(appointment.getClientPhone());
        dto.setTotalPrice(appointment.getTotalPrice());

        // Map client
        if (appointment.getClient() != null) {
            dto.setClientId(appointment.getClient().getUserId());
            dto.setClientName(appointment.getClient().getName());
        }

        // Map stylist
        if (appointment.getStylist() != null) {
            dto.setStylistId(appointment.getStylist().getUserId());
            dto.setStylistName(appointment.getStylist().getName());
        }

        // Map services
        if (appointment.getServices() != null) {
            dto.setServices(appointment.getServices().stream()
                    .map(service -> {
                        AppointmentDTO.ServiceOfferDTO serviceDTO = new AppointmentDTO.ServiceOfferDTO();
                        serviceDTO.setServiceId(service.getServiceId());
                        serviceDTO.setName(service.getName());
                        serviceDTO.setUnitPrice(service.getUnitPrice());
                        serviceDTO.setDuration(service.getDuration());
                        return serviceDTO;
                    })
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    /**
     * Converts an {@link AppointmentCreateDTO} object to an {@link Appointment} entity.
     *
     * @param createDTO The DTO with data needed to create the appointment.
     * @param client The client associated with the appointment.
     * @param stylist The stylist associated with the appointment.
     * @param services The list of services associated with the appointment.
     * @return An {@link Appointment} entity ready to be persisted.
     */
    public Appointment toEntity(AppointmentCreateDTO createDTO, User client, User stylist, List<ServiceOffer> services) {
        if (createDTO == null) {
            return null;
        }

        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setStylist(stylist);
        appointment.setDate(createDTO.getDate());
        appointment.setStartTime(createDTO.getStartTime());
        appointment.setEndTime(createDTO.getEndTime());
        appointment.setClientPhone(createDTO.getClientPhone());
        appointment.setTotalPrice(createDTO.getTotalPrice());
        appointment.setStatus(Appointment.AppointmentStatus.CONFIRMADA);

        if (services != null) {
            appointment.setServices(services);
        }

        return appointment;
    }

    /**
     * Converts a list of {@link Appointment} entities to a list of {@link AppointmentDTO}.
     *
     * @param appointments List of {@link Appointment} entities.
     * @return List of {@link AppointmentDTO}.
     */
    public List<AppointmentDTO> toDTOList(List<Appointment> appointments) {
        if (appointments == null) {
            return null;
        }
        return appointments.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
