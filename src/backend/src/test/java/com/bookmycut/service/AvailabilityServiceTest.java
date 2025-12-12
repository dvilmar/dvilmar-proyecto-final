package com.bookmycut.service;

import com.bookmycut.dto.AvailabilityCreateDTO;
import com.bookmycut.dto.AvailabilityDTO;
import com.bookmycut.entities.Availability;
import com.bookmycut.entities.User;
import com.bookmycut.exception.BadRequestException;
import com.bookmycut.exception.ResourceNotFoundException;
import com.bookmycut.repositories.AvailabilityRepository;
import com.bookmycut.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios para AvailabilityService")
class AvailabilityServiceTest {

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    private User stylist;
    private Availability availability;
    private AvailabilityCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        stylist = new User();
        stylist.setUserId(1L);
        stylist.setName("Estilista Test");
        stylist.setEmail("estilista@test.com");
        stylist.setRole(User.Role.ESTILISTA);

        availability = new Availability();
        availability.setAvailabilityId(1L);
        availability.setStylist(stylist);
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(18, 0));

        createDTO = new AvailabilityCreateDTO(
                1L,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(18, 0)
        );
    }

    @Test
    @DisplayName("Debería obtener todas las disponibilidades")
    void testGetAllAvailabilities() {
        // Given
        when(availabilityRepository.findAll()).thenReturn(List.of(availability));

        // When
        List<AvailabilityDTO> result = availabilityService.getAllAvailabilities(null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(availabilityRepository).findAll();
    }

    @Test
    @DisplayName("Debería obtener disponibilidades paginadas")
    void testGetAllAvailabilitiesPaginated() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Availability> availabilityPage = new PageImpl<>(List.of(availability), pageable, 1);
        when(availabilityRepository.findAll(pageable)).thenReturn(availabilityPage);

        // When
        Page<AvailabilityDTO> result = availabilityService.getAllAvailabilities(null, 0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(availabilityRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Debería obtener disponibilidad por ID")
    void testGetAvailabilityById() {
        // Given
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));

        // When
        AvailabilityDTO result = availabilityService.getAvailabilityById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getAvailabilityId());
        verify(availabilityRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería crear disponibilidad correctamente")
    void testCreateAvailability() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(stylist));
        when(availabilityRepository.findByStylistAndDayOfWeek(stylist, DayOfWeek.MONDAY))
                .thenReturn(List.of());
        when(availabilityRepository.save(any(Availability.class))).thenReturn(availability);

        // When
        AvailabilityDTO result = availabilityService.createAvailability(createDTO);

        // Then
        assertNotNull(result);
        assertEquals(DayOfWeek.MONDAY, result.getDayOfWeek());
        verify(availabilityRepository).save(any(Availability.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción si el usuario no es estilista")
    void testCreateAvailabilityNotStylist() {
        // Given
        User client = new User();
        client.setUserId(2L);
        client.setRole(User.Role.CLIENTE);
        AvailabilityCreateDTO invalidDTO = new AvailabilityCreateDTO(
                2L, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0)
        );
        when(userRepository.findById(2L)).thenReturn(Optional.of(client));

        // When/Then
        assertThrows(BadRequestException.class, 
                () -> availabilityService.createAvailability(invalidDTO));
    }

    @Test
    @DisplayName("Debería lanzar excepción si hora fin es anterior a hora inicio")
    void testCreateAvailabilityInvalidTime() {
        // Given
        AvailabilityCreateDTO invalidDTO = new AvailabilityCreateDTO(
                1L, DayOfWeek.MONDAY, LocalTime.of(18, 0), LocalTime.of(9, 0)
        );
        when(userRepository.findById(1L)).thenReturn(Optional.of(stylist));

        // When/Then
        assertThrows(BadRequestException.class, 
                () -> availabilityService.createAvailability(invalidDTO));
    }

    @Test
    @DisplayName("Debería actualizar disponibilidad correctamente")
    void testUpdateAvailability() {
        // Given
        AvailabilityCreateDTO updateDTO = new AvailabilityCreateDTO(
                1L, DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(19, 0)
        );
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));
        when(availabilityRepository.save(availability)).thenReturn(availability);

        // When
        AvailabilityDTO result = availabilityService.updateAvailability(1L, updateDTO);

        // Then
        assertNotNull(result);
        verify(availabilityRepository).save(availability);
    }

    @Test
    @DisplayName("Debería eliminar disponibilidad")
    void testDeleteAvailability() {
        // Given
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));
        doNothing().when(availabilityRepository).delete(availability);

        // When
        availabilityService.deleteAvailability(1L);

        // Then
        verify(availabilityRepository).delete(availability);
    }
}





