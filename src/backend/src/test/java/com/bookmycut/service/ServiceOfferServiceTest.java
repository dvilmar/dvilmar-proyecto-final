package com.bookmycut.service;

import com.bookmycut.dto.ServiceOfferCreateDTO;
import com.bookmycut.dto.ServiceOfferDTO;
import com.bookmycut.entities.ServiceOffer;
import com.bookmycut.exception.ResourceNotFoundException;
import com.bookmycut.mappers.ServiceOfferMapper;
import com.bookmycut.repositories.ServiceOfferRepository;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios para ServiceOfferService")
class ServiceOfferServiceTest {

    @Mock
    private ServiceOfferRepository serviceOfferRepository;

    @Mock
    private ServiceOfferMapper serviceOfferMapper;

    @InjectMocks
    private ServiceOfferService serviceOfferService;

    private ServiceOffer serviceOffer;
    private ServiceOfferDTO serviceOfferDTO;
    private ServiceOfferCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        serviceOffer = new ServiceOffer();
        serviceOffer.setServiceId(1L);
        serviceOffer.setName("Corte de Pelo");
        serviceOffer.setDescription("Corte de pelo estándar");
        serviceOffer.setDuration(30);
        serviceOffer.setUnitPrice(new BigDecimal("25.00"));

        serviceOfferDTO = new ServiceOfferDTO(
                1L,
                "Corte de Pelo",
                "Corte de pelo estándar",
                30,
                new BigDecimal("25.00")
        );

        createDTO = new ServiceOfferCreateDTO(
                "Corte de Pelo",
                "Corte de pelo estándar",
                30,
                new BigDecimal("25.00")
        );
    }

    @Test
    @DisplayName("Debería obtener todos los servicios")
    void testGetAllServiceOffers() {
        // Given
        List<ServiceOffer> services = List.of(serviceOffer);
        when(serviceOfferRepository.findAll()).thenReturn(services);
        when(serviceOfferMapper.toDTOList(services)).thenReturn(List.of(serviceOfferDTO));

        // When
        List<ServiceOfferDTO> result = serviceOfferService.getAllServiceOffers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Corte de Pelo", result.get(0).getName());
        verify(serviceOfferRepository).findAll();
    }

    @Test
    @DisplayName("Debería obtener servicios paginados")
    void testGetAllServiceOffersPaginated() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ServiceOffer> servicePage = new PageImpl<>(List.of(serviceOffer), pageable, 1);
        when(serviceOfferRepository.findAll(pageable)).thenReturn(servicePage);
        when(serviceOfferMapper.toDTO(serviceOffer)).thenReturn(serviceOfferDTO);

        // When
        Page<ServiceOfferDTO> result = serviceOfferService.getAllServiceOffers(0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Corte de Pelo", result.getContent().get(0).getName());
        verify(serviceOfferRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Debería obtener servicio por ID")
    void testGetServiceOfferById() {
        // Given
        when(serviceOfferRepository.findById(1L)).thenReturn(Optional.of(serviceOffer));
        when(serviceOfferMapper.toDTO(serviceOffer)).thenReturn(serviceOfferDTO);

        // When
        ServiceOfferDTO result = serviceOfferService.getServiceOfferById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getServiceId());
        assertEquals("Corte de Pelo", result.getName());
        verify(serviceOfferRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando servicio no existe")
    void testGetServiceOfferByIdNotFound() {
        // Given
        when(serviceOfferRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, 
                () -> serviceOfferService.getServiceOfferById(999L));
    }

    @Test
    @DisplayName("Debería crear un nuevo servicio")
    void testCreateServiceOffer() {
        // Given
        when(serviceOfferMapper.toEntity(createDTO)).thenReturn(serviceOffer);
        when(serviceOfferRepository.save(serviceOffer)).thenReturn(serviceOffer);
        when(serviceOfferMapper.toDTO(serviceOffer)).thenReturn(serviceOfferDTO);

        // When
        ServiceOfferDTO result = serviceOfferService.createServiceOffer(createDTO);

        // Then
        assertNotNull(result);
        assertEquals("Corte de Pelo", result.getName());
        verify(serviceOfferRepository).save(serviceOffer);
    }

    @Test
    @DisplayName("Debería actualizar un servicio existente")
    void testUpdateServiceOffer() {
        // Given
        ServiceOfferCreateDTO updateDTO = new ServiceOfferCreateDTO(
                "Corte Premium",
                "Corte de pelo premium",
                45,
                new BigDecimal("35.00")
        );
        ServiceOfferDTO updatedDTO = new ServiceOfferDTO(
                1L,
                "Corte Premium",
                "Corte de pelo premium",
                45,
                new BigDecimal("35.00")
        );

        when(serviceOfferRepository.findById(1L)).thenReturn(Optional.of(serviceOffer));
        when(serviceOfferRepository.save(serviceOffer)).thenReturn(serviceOffer);
        when(serviceOfferMapper.toDTO(serviceOffer)).thenReturn(updatedDTO);

        // When
        ServiceOfferDTO result = serviceOfferService.updateServiceOffer(1L, updateDTO);

        // Then
        assertNotNull(result);
        assertEquals("Corte Premium", result.getName());
        verify(serviceOfferMapper).updateEntity(serviceOffer, updateDTO);
        verify(serviceOfferRepository).save(serviceOffer);
    }

    @Test
    @DisplayName("Debería eliminar un servicio")
    void testDeleteServiceOffer() {
        // Given
        when(serviceOfferRepository.findById(1L)).thenReturn(Optional.of(serviceOffer));
        doNothing().when(serviceOfferRepository).delete(serviceOffer);

        // When
        serviceOfferService.deleteServiceOffer(1L);

        // Then
        verify(serviceOfferRepository).findById(1L);
        verify(serviceOfferRepository).delete(serviceOffer);
    }
}





