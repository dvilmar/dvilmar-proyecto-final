package com.bookmycut.service;

import com.bookmycut.dto.AppointmentCreateDTO;
import com.bookmycut.dto.AppointmentDTO;
import com.bookmycut.entities.Appointment;
import com.bookmycut.entities.Availability;
import com.bookmycut.entities.ScheduleException;
import com.bookmycut.entities.ServiceOffer;
import com.bookmycut.entities.User;
import com.bookmycut.exception.BadRequestException;
import com.bookmycut.exception.ConflictException;
import com.bookmycut.exception.ResourceNotFoundException;
import com.bookmycut.mappers.AppointmentMapper;
import com.bookmycut.repositories.AppointmentRepository;
import com.bookmycut.repositories.AvailabilityRepository;
import com.bookmycut.repositories.ScheduleExceptionRepository;
import com.bookmycut.repositories.ServiceOfferRepository;
import com.bookmycut.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios para AppointmentService")
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ServiceOfferRepository serviceOfferRepository;

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private ScheduleExceptionRepository scheduleExceptionRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @InjectMocks
    private AppointmentService appointmentService;

    private User client;
    private User stylist;
    private AppointmentCreateDTO createDTO;
    private Appointment appointment;
    private AppointmentDTO appointmentDTO;
    private List<ServiceOffer> services;
    private Availability availability;

    @BeforeEach
    void setUp() {
        // Configurar cliente
        client = new User();
        client.setUserId(1L);
        client.setEmail("cliente@test.com");
        client.setName("Cliente Test");
        client.setRole(User.Role.CLIENTE);

        // Configurar estilista
        stylist = new User();
        stylist.setUserId(2L);
        stylist.setEmail("estilista@test.com");
        stylist.setName("Estilista Test");
        stylist.setRole(User.Role.ESTILISTA);

        // Configurar disponibilidad
        availability = new Availability();
        availability.setAvailabilityId(1L);
        availability.setStylist(stylist);
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(18, 0));

        // Configurar servicios
        ServiceOffer service1 = new ServiceOffer();
        service1.setServiceId(1L);
        service1.setName("Corte de pelo");
        service1.setUnitPrice(new BigDecimal("25.00"));

        ServiceOffer service2 = new ServiceOffer();
        service2.setServiceId(2L);
        service2.setName("Barba");
        service2.setUnitPrice(new BigDecimal("15.00"));

        services = List.of(service1, service2);

        // Configurar DTO de creación
        createDTO = new AppointmentCreateDTO();
        createDTO.setClientId(1L);
        createDTO.setStylistId(2L);
        createDTO.setDate(LocalDate.of(2024, 12, 16)); // Lunes
        createDTO.setStartTime(LocalTime.of(10, 0));
        createDTO.setEndTime(LocalTime.of(11, 0));
        createDTO.setServiceIds(List.of(1L, 2L));

        // Configurar cita
        appointment = new Appointment();
        appointment.setAppointmentId(1L);
        appointment.setClient(client);
        appointment.setStylist(stylist);
        appointment.setDate(createDTO.getDate());
        appointment.setStartTime(createDTO.getStartTime());
        appointment.setEndTime(createDTO.getEndTime());
        appointment.setTotalPrice(new BigDecimal("40.00"));
        appointment.setServices(services);
        appointment.setStatus(Appointment.AppointmentStatus.CONFIRMADA);

        // Configurar DTO
        appointmentDTO = new AppointmentDTO();
        appointmentDTO.setAppointmentId(1L);
        appointmentDTO.setClientId(1L);
        appointmentDTO.setStylistId(2L);
        appointmentDTO.setDate(createDTO.getDate());
        appointmentDTO.setStartTime(createDTO.getStartTime());
        appointmentDTO.setEndTime(createDTO.getEndTime());
        appointmentDTO.setTotalPrice(new BigDecimal("40.00"));
    }

    @Test
    @DisplayName("Debería crear una cita exitosamente cuando todos los datos son válidos")
    void shouldCreateAppointment_WhenValidData() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(2L)).thenReturn(Optional.of(stylist));
        when(availabilityRepository.findByStylistAndDayOfWeek(stylist, DayOfWeek.MONDAY))
                .thenReturn(List.of(availability));
        when(scheduleExceptionRepository.findByDateAndStylistOrNull(any(), eq(stylist)))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(serviceOfferRepository.findAllById(anyList())).thenReturn(services);
        when(appointmentMapper.toEntity(any(), eq(client), eq(stylist), eq(services)))
                .thenReturn(appointment);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toDTO(any(Appointment.class))).thenReturn(appointmentDTO);

        // When
        AppointmentDTO result = appointmentService.createAppointment(createDTO);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getAppointmentId());
        verify(userRepository, times(2)).findById(anyLong());
        verify(availabilityRepository).findByStylistAndDayOfWeek(any(), any());
        verify(scheduleExceptionRepository).findByDateAndStylistOrNull(any(), any());
        verify(appointmentRepository).findOverlappingAppointments(any(), any(), any(), any());
        verify(serviceOfferRepository).findAllById(anyList());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Debería calcular el precio total automáticamente desde los servicios")
    void shouldCalculateTotalPrice_AutomaticallyFromServices() {
        // Given
        createDTO.setTotalPrice(null); // No proporcionar precio

        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(2L)).thenReturn(Optional.of(stylist));
        when(availabilityRepository.findByStylistAndDayOfWeek(stylist, DayOfWeek.MONDAY))
                .thenReturn(List.of(availability));
        when(scheduleExceptionRepository.findByDateAndStylistOrNull(any(), eq(stylist)))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(serviceOfferRepository.findAllById(anyList())).thenReturn(services);
        when(appointmentMapper.toEntity(any(), eq(client), eq(stylist), eq(services)))
                .thenReturn(appointment);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toDTO(any(Appointment.class))).thenReturn(appointmentDTO);

        // When
        AppointmentDTO result = appointmentService.createAppointment(createDTO);

        // Then
        assertNotNull(result);
        // Verificar que el precio calculado es 25 + 15 = 40
        verify(appointmentMapper).toEntity(argThat(dto -> {
            AppointmentCreateDTO dtoArg = (AppointmentCreateDTO) dto;
            return dtoArg.getTotalPrice().compareTo(new BigDecimal("40.00")) == 0;
        }), eq(client), eq(stylist), eq(services));
    }

    @Test
    @DisplayName("Debería lanzar BadRequestException cuando el cliente no existe")
    void shouldThrowBadRequestException_WhenClientNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            appointmentService.createAppointment(createDTO);
        });
        verify(userRepository).findById(1L);
        verify(serviceOfferRepository, never()).findAllById(anyList());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar BadRequestException cuando el usuario no es un cliente")
    void shouldThrowBadRequestException_WhenUserIsNotClient() {
        // Given
        client.setRole(User.Role.ESTILISTA);
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            appointmentService.createAppointment(createDTO);
        });
        assertEquals("El usuario especificado no es un cliente", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar BadRequestException cuando el usuario no es un estilista")
    void shouldThrowBadRequestException_WhenUserIsNotStylist() {
        // Given
        stylist.setRole(User.Role.CLIENTE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(2L)).thenReturn(Optional.of(stylist));

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            appointmentService.createAppointment(createDTO);
        });
        assertEquals("El usuario especificado no es un estilista", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar BadRequestException cuando la hora de fin es anterior a la de inicio")
    void shouldThrowBadRequestException_WhenEndTimeBeforeStartTime() {
        // Given
        createDTO.setStartTime(LocalTime.of(11, 0));
        createDTO.setEndTime(LocalTime.of(10, 0));

        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(2L)).thenReturn(Optional.of(stylist));

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            appointmentService.createAppointment(createDTO);
        });
        assertEquals("La hora de fin debe ser posterior a la hora de inicio", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar BadRequestException cuando el estilista no tiene disponibilidad")
    void shouldThrowBadRequestException_WhenStylistHasNoAvailability() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(2L)).thenReturn(Optional.of(stylist));
        when(availabilityRepository.findByStylistAndDayOfWeek(stylist, DayOfWeek.MONDAY))
                .thenReturn(Collections.emptyList());

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            appointmentService.createAppointment(createDTO);
        });
        assertTrue(exception.getMessage().contains("no tiene disponibilidad configurada"));
    }

    @Test
    @DisplayName("Debería lanzar BadRequestException cuando la cita está fuera del horario disponible")
    void shouldThrowBadRequestException_WhenAppointmentOutsideAvailableHours() {
        // Given
        createDTO.setStartTime(LocalTime.of(19, 0)); // Fuera del horario (9:00-18:00)
        createDTO.setEndTime(LocalTime.of(20, 0));

        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(2L)).thenReturn(Optional.of(stylist));
        when(availabilityRepository.findByStylistAndDayOfWeek(stylist, DayOfWeek.MONDAY))
                .thenReturn(List.of(availability));
        when(scheduleExceptionRepository.findByDateAndStylistOrNull(any(), eq(stylist)))
                .thenReturn(Collections.emptyList());

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            appointmentService.createAppointment(createDTO);
        });
        assertTrue(exception.getMessage().contains("fuera del horario disponible"));
    }

    @Test
    @DisplayName("Debería lanzar ConflictException cuando hay una excepción de horario (día cerrado)")
    void shouldThrowConflictException_WhenScheduleExceptionExists() {
        // Given
        ScheduleException exception = new ScheduleException();
        exception.setType(ScheduleException.ExceptionType.NO_DISPONIBLE);
        exception.setDate(createDTO.getDate());
        exception.setStylist(stylist);

        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(2L)).thenReturn(Optional.of(stylist));
        when(availabilityRepository.findByStylistAndDayOfWeek(stylist, DayOfWeek.MONDAY))
                .thenReturn(List.of(availability));
        when(scheduleExceptionRepository.findByDateAndStylistOrNull(any(), eq(stylist)))
                .thenReturn(List.of(exception));

        // When & Then
        ConflictException conflictException = assertThrows(ConflictException.class, () -> {
            appointmentService.createAppointment(createDTO);
        });
        assertTrue(conflictException.getMessage().contains("no está disponible"));
    }

    @Test
    @DisplayName("Debería lanzar ConflictException cuando hay solapamiento de citas")
    void shouldThrowConflictException_WhenAppointmentOverlaps() {
        // Given
        Appointment overlappingAppointment = new Appointment();
        overlappingAppointment.setAppointmentId(99L);
        overlappingAppointment.setStylist(stylist);
        overlappingAppointment.setDate(createDTO.getDate());
        overlappingAppointment.setStartTime(LocalTime.of(10, 30));
        overlappingAppointment.setEndTime(LocalTime.of(11, 30));
        overlappingAppointment.setStatus(Appointment.AppointmentStatus.CONFIRMADA);

        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(2L)).thenReturn(Optional.of(stylist));
        when(availabilityRepository.findByStylistAndDayOfWeek(stylist, DayOfWeek.MONDAY))
                .thenReturn(List.of(availability));
        when(scheduleExceptionRepository.findByDateAndStylistOrNull(any(), eq(stylist)))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any(), any()))
                .thenReturn(List.of(overlappingAppointment));

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            appointmentService.createAppointment(createDTO);
        });
        assertTrue(exception.getMessage().contains("Ya existe una cita en este horario"));
    }

    @Test
    @DisplayName("Debería lanzar ResourceNotFoundException cuando los servicios no existen")
    void shouldThrowResourceNotFoundException_WhenServicesNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(2L)).thenReturn(Optional.of(stylist));
        when(availabilityRepository.findByStylistAndDayOfWeek(stylist, DayOfWeek.MONDAY))
                .thenReturn(List.of(availability));
        when(scheduleExceptionRepository.findByDateAndStylistOrNull(any(), eq(stylist)))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(serviceOfferRepository.findAllById(anyList())).thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            appointmentService.createAppointment(createDTO);
        });
        verify(serviceOfferRepository).findAllById(anyList());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar BadRequestException cuando no hay servicios ni precio total")
    void shouldThrowBadRequestException_WhenNoServicesAndNoPrice() {
        // Given
        createDTO.setServiceIds(null);
        createDTO.setTotalPrice(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(2L)).thenReturn(Optional.of(stylist));
        when(availabilityRepository.findByStylistAndDayOfWeek(stylist, DayOfWeek.MONDAY))
                .thenReturn(List.of(availability));
        when(scheduleExceptionRepository.findByDateAndStylistOrNull(any(), eq(stylist)))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            appointmentService.createAppointment(createDTO);
        });
        assertTrue(exception.getMessage().contains("Debe proporcionarse un precio total o una lista de servicios"));
    }

    @Test
    @DisplayName("Debería obtener una cita por ID exitosamente")
    void shouldGetAppointmentById_Successfully() {
        // Given
        Long appointmentId = 1L;
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentMapper.toDTO(appointment)).thenReturn(appointmentDTO);

        // When
        AppointmentDTO result = appointmentService.getAppointmentById(appointmentId);

        // Then
        assertNotNull(result);
        assertEquals(appointmentId, result.getAppointmentId());
        verify(appointmentRepository).findById(appointmentId);
        verify(appointmentMapper).toDTO(appointment);
    }

    @Test
    @DisplayName("Debería lanzar ResourceNotFoundException cuando la cita no existe")
    void shouldThrowResourceNotFoundException_WhenAppointmentNotFound() {
        // Given
        Long appointmentId = 999L;
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            appointmentService.getAppointmentById(appointmentId);
        });
        verify(appointmentRepository).findById(appointmentId);
        verify(appointmentMapper, never()).toDTO(any());
    }
}





