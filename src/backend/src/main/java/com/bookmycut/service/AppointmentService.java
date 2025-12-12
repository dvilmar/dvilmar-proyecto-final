package com.bookmycut.service;

import com.bookmycut.dto.AppointmentCreateDTO;
import com.bookmycut.dto.AppointmentDTO;
import com.bookmycut.exception.BadRequestException;
import com.bookmycut.exception.ConflictException;
import com.bookmycut.exception.ResourceNotFoundException;
import com.bookmycut.mappers.AppointmentMapper;
import com.bookmycut.entities.Appointment;
import com.bookmycut.entities.Availability;
import com.bookmycut.entities.ScheduleException;
import com.bookmycut.entities.User;
import com.bookmycut.entities.ServiceOffer;
import com.bookmycut.repositories.AppointmentRepository;
import com.bookmycut.repositories.AvailabilityRepository;
import com.bookmycut.repositories.ScheduleExceptionRepository;
import com.bookmycut.repositories.UserRepository;
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

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service responsible for business logic related to appointments.
 * Manages creation, update, query and deletion of appointments.
 */
@Service
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceOfferRepository serviceOfferRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private ScheduleExceptionRepository scheduleExceptionRepository;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired(required = false)
    private NotificationService notificationService;

    /**
     * Gets all appointments in the system.
     *
     * @return List of AppointmentDTO.
     */
    public List<AppointmentDTO> getAllAppointments() {
        logger.info("Requesting all appointments");
        try {
            List<Appointment> appointments = appointmentRepository.findAll();
            logger.info("Found {} appointments", appointments.size());
            return appointmentMapper.toDTOList(appointments);
        } catch (Exception e) {
            logger.error("Error getting all appointments: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets all appointments with pagination.
     *
     * @param page Page number (0-indexed).
     * @param size Page size.
     * @return Page of AppointmentDTO.
     */
    public Page<AppointmentDTO> getAllAppointments(int page, int size) {
        logger.info("Requesting all appointments - page: {}, size: {}", page, size);
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date", "startTime"));
            Page<Appointment> appointments = appointmentRepository.findAll(pageable);
            logger.info("Found {} appointments", appointments.getTotalElements());
            return appointments.map(appointmentMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error getting paginated appointments: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets an appointment by its ID.
     *
     * @param id Unique identifier of the appointment.
     * @return AppointmentDTO of the found appointment.
     * @throws ResourceNotFoundException If the appointment does not exist.
     */
    public AppointmentDTO getAppointmentById(Long id) {
        logger.info("Searching for appointment with ID: {}", id);
        try {
            Appointment appointment = appointmentRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Appointment not found with ID: {}", id);
                        return new ResourceNotFoundException("Appointment", "id", id);
                    });
            logger.debug("Appointment with ID {} found", id);
            return appointmentMapper.toDTO(appointment);
        } catch (Exception e) {
            logger.error("Error getting appointment with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets appointments for a specific client.
     *
     * @param clientId Client ID.
     * @return List of AppointmentDTO for the client.
     */
    public List<AppointmentDTO> getAppointmentsByClient(Long clientId) {
        logger.info("Searching for appointments for client ID: {}", clientId);
        try {
            User client = userRepository.findById(clientId)
                    .orElseThrow(() -> {
                        logger.warn("Client not found with ID: {}", clientId);
                        return new ResourceNotFoundException("User", "id", clientId);
                    });
            List<Appointment> appointments = appointmentRepository.findByClient(client);
            logger.info("Found {} appointments for client ID: {}", appointments.size(), clientId);
            return appointmentMapper.toDTOList(appointments);
        } catch (Exception e) {
            logger.error("Error getting appointments for client ID {}: {}", clientId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets appointments for a specific client with pagination.
     *
     * @param clientId Client ID.
     * @param page Page number (0-indexed).
     * @param size Page size.
     * @return Page of AppointmentDTO for the client.
     */
    public Page<AppointmentDTO> getAppointmentsByClient(Long clientId, int page, int size) {
        logger.info("Searching for appointments for client ID: {} - page: {}, size: {}", clientId, page, size);
        try {
            User client = userRepository.findById(clientId)
                    .orElseThrow(() -> {
                        logger.warn("Client not found with ID: {}", clientId);
                        return new ResourceNotFoundException("User", "id", clientId);
                    });
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date", "startTime"));
            Page<Appointment> appointments = appointmentRepository.findByClient(client, pageable);
            logger.info("Found {} appointments for client ID: {}", appointments.getTotalElements(), clientId);
            return appointments.map(appointmentMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error getting paginated appointments for client ID {}: {}", clientId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets appointments for a specific stylist.
     *
     * @param stylistId Stylist ID.
     * @return List of AppointmentDTO for the stylist.
     */
    public List<AppointmentDTO> getAppointmentsByStylist(Long stylistId) {
        logger.info("Searching for appointments for stylist ID: {}", stylistId);
        try {
            User stylist = userRepository.findById(stylistId)
                    .orElseThrow(() -> {
                        logger.warn("Stylist not found with ID: {}", stylistId);
                        return new ResourceNotFoundException("User", "id", stylistId);
                    });
            List<Appointment> appointments = appointmentRepository.findByStylist(stylist);
            logger.info("Found {} appointments for stylist ID: {}", appointments.size(), stylistId);
            return appointmentMapper.toDTOList(appointments);
        } catch (Exception e) {
            logger.error("Error getting appointments for stylist ID {}: {}", stylistId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets appointments for a specific stylist with pagination.
     *
     * @param stylistId Stylist ID.
     * @param page Page number (0-indexed).
     * @param size Page size.
     * @return Page of AppointmentDTO for the stylist.
     */
    public Page<AppointmentDTO> getAppointmentsByStylist(Long stylistId, int page, int size) {
        logger.info("Searching for appointments for stylist ID: {} - page: {}, size: {}", stylistId, page, size);
        try {
            User stylist = userRepository.findById(stylistId)
                    .orElseThrow(() -> {
                        logger.warn("Stylist not found with ID: {}", stylistId);
                        return new ResourceNotFoundException("User", "id", stylistId);
                    });
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date", "startTime"));
            Page<Appointment> appointments = appointmentRepository.findByStylist(stylist, pageable);
            logger.info("Found {} appointments for stylist ID: {}", appointments.getTotalElements(), stylistId);
            return appointments.map(appointmentMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error getting paginated appointments for stylist ID {}: {}", stylistId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets appointments for a specific date.
     *
     * @param date Date to query.
     * @return List of AppointmentDTO for the date.
     */
    public List<AppointmentDTO> getAppointmentsByDate(LocalDate date) {
        logger.info("Searching for appointments for date: {}", date);
        try {
            List<Appointment> appointments = appointmentRepository.findByDate(date);
            logger.info("Found {} appointments for date: {}", appointments.size(), date);
            return appointmentMapper.toDTOList(appointments);
        } catch (Exception e) {
            logger.error("Error getting appointments for date {}: {}", date, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets appointments for a specific stylist and date.
     * Used for checking availability when creating public appointments.
     *
     * @param stylistId Stylist ID.
     * @param date Date to query.
     * @return List of AppointmentDTO for the stylist and date.
     */
    public List<AppointmentDTO> getAppointmentsByStylistAndDate(Long stylistId, LocalDate date) {
        logger.info("Searching for appointments for stylist ID: {} and date: {}", stylistId, date);
        try {
            User stylist = userRepository.findById(stylistId)
                    .orElseThrow(() -> {
                        logger.warn("Stylist not found with ID: {}", stylistId);
                        return new ResourceNotFoundException("User", "id", stylistId);
                    });
            List<Appointment> appointments = appointmentRepository.findByStylistAndDate(stylist, date);
            logger.info("Found {} appointments for stylist ID: {} and date: {}", appointments.size(), stylistId, date);
            return appointmentMapper.toDTOList(appointments);
        } catch (Exception e) {
            logger.error("Error getting appointments for stylist ID {} and date {}: {}", stylistId, date, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Busca citas con filtros avanzados (sin paginación).
     *
     * @param clientName Nombre del cliente (opcional, busca parcial case-insensitive).
     * @param stylistName Nombre del estilista (opcional, busca parcial case-insensitive).
     * @param serviceName Nombre del servicio (opcional, busca parcial case-insensitive).
     * @param date Fecha específica (opcional).
     * @param status Estado de la cita (opcional).
     * @return Lista de AppointmentDTO que coinciden con los filtros.
     */
    public List<AppointmentDTO> searchAppointmentsWithFilters(
            String clientName, String stylistName, String serviceName, 
            LocalDate date, Appointment.AppointmentStatus status) {
        logger.info("Searching appointments with filters - Client: {}, Stylist: {}, Service: {}, Date: {}, Status: {}", 
                clientName, stylistName, serviceName, date, status);
        try {
            List<Appointment> appointments = appointmentRepository.findWithAdvancedFilters(
                    clientName, stylistName, serviceName, date, status);
            logger.info("Found {} appointments matching filters", appointments.size());
            return appointmentMapper.toDTOList(appointments);
        } catch (Exception e) {
            logger.error("Error searching appointments with filters: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Busca citas con filtros avanzados (con paginación).
     *
     * @param clientName Nombre del cliente (opcional, busca parcial case-insensitive).
     * @param stylistName Nombre del estilista (opcional, busca parcial case-insensitive).
     * @param serviceName Nombre del servicio (opcional, busca parcial case-insensitive).
     * @param date Fecha específica (opcional).
     * @param status Estado de la cita (opcional).
     * @param page Número de página (0-indexed).
     * @param size Tamaño de página.
     * @return Página de AppointmentDTO que coinciden con los filtros.
     */
    public Page<AppointmentDTO> searchAppointmentsWithFilters(
            String clientName, String stylistName, String serviceName, 
            LocalDate date, Appointment.AppointmentStatus status,
            int page, int size) {
        logger.info("Searching appointments with filters (paginated) - Client: {}, Stylist: {}, Service: {}, Date: {}, Status: {}, Page: {}, Size: {}", 
                clientName, stylistName, serviceName, date, status, page, size);
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date", "startTime"));
            Page<Appointment> appointments = appointmentRepository.findWithAdvancedFilters(
                    clientName, stylistName, serviceName, date, status, pageable);
            logger.info("Found {} appointments matching filters (total: {})", 
                    appointments.getNumberOfElements(), appointments.getTotalElements());
            return appointments.map(appointmentMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error searching appointments with filters (paginated): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Creates a new appointment in the system.
     * Includes validations for availability, overlapping appointments, and automatic price calculation.
     *
     * @param createDTO DTO with appointment data to create.
     * @return AppointmentDTO of the created appointment.
     * @throws ResourceNotFoundException If client, stylist or services do not exist.
     * @throws BadRequestException If validations fail.
     * @throws ConflictException If appointment overlaps with existing one.
     */
    @Transactional
    public AppointmentDTO createAppointment(AppointmentCreateDTO createDTO) {
        logger.info("Creating new appointment for client ID: {} and stylist ID: {}", 
                createDTO.getClientId(), createDTO.getStylistId());
        
        try {
            // Validar cliente
            User client = userRepository.findById(createDTO.getClientId())
                    .orElseThrow(() -> {
                        logger.warn("Client not found with ID: {}", createDTO.getClientId());
                        return new ResourceNotFoundException("User", "id", createDTO.getClientId());
                    });
            
            if (client.getRole() != User.Role.CLIENTE) {
                logger.warn("User with ID {} is not a client", createDTO.getClientId());
                throw new BadRequestException("El usuario especificado no es un cliente");
            }

            // Validar estilista
            User stylist = userRepository.findById(createDTO.getStylistId())
                    .orElseThrow(() -> {
                        logger.warn("Stylist not found with ID: {}", createDTO.getStylistId());
                        return new ResourceNotFoundException("User", "id", createDTO.getStylistId());
                    });
            
            if (stylist.getRole() != User.Role.ESTILISTA) {
                logger.warn("User with ID {} is not a stylist", createDTO.getStylistId());
                throw new BadRequestException("El usuario especificado no es un estilista");
            }

            // Validar que la hora de fin sea posterior a la de inicio
            if (createDTO.getEndTime().isBefore(createDTO.getStartTime()) || 
                createDTO.getEndTime().equals(createDTO.getStartTime())) {
                logger.warn("End time must be after start time");
                throw new BadRequestException("La hora de fin debe ser posterior a la hora de inicio");
            }

            // Validar que no se creen citas en el pasado
            LocalDate nowDate = LocalDate.now();
            java.time.LocalTime nowTime = java.time.LocalTime.now();
            if (createDTO.getDate().isBefore(nowDate) || 
                (createDTO.getDate().equals(nowDate) && createDTO.getStartTime().isBefore(nowTime))) {
                logger.warn("Attempt to create appointment in the past - Date: {}, Time: {}", 
                        createDTO.getDate(), createDTO.getStartTime());
                throw new BadRequestException("No se pueden crear citas en el pasado");
            }

            // Validar disponibilidad del estilista
            validateStylistAvailability(stylist, createDTO.getDate(), 
                    createDTO.getStartTime(), createDTO.getEndTime());

            // Validar solapamiento de citas
            validateNoOverlappingAppointments(stylist, createDTO.getDate(), 
                    createDTO.getStartTime(), createDTO.getEndTime(), null);

            // Obtener servicios
            List<ServiceOffer> services = null;
            if (createDTO.getServiceIds() != null && !createDTO.getServiceIds().isEmpty()) {
                services = serviceOfferRepository.findAllById(createDTO.getServiceIds());
                if (services.size() != createDTO.getServiceIds().size()) {
                    logger.warn("Some services were not found");
                    throw new ResourceNotFoundException("Services", "ids", createDTO.getServiceIds());
                }
                logger.debug("Associated {} services to appointment", services.size());
            }

            // Calcular precio automáticamente si no se proporciona o si hay servicios
            BigDecimal totalPrice = createDTO.getTotalPrice();
            if (services != null && !services.isEmpty()) {
                totalPrice = calculateTotalPrice(services);
                logger.debug("Calculated total price: {} from {} services", totalPrice, services.size());
            } else if (totalPrice == null) {
                throw new BadRequestException("Debe proporcionarse un precio total o una lista de servicios");
            }

            // Crear la cita con el precio calculado
            AppointmentCreateDTO dtoWithPrice = new AppointmentCreateDTO(
                    createDTO.getClientId(),
                    createDTO.getStylistId(),
                    createDTO.getDate(),
                    createDTO.getStartTime(),
                    createDTO.getEndTime(),
                    createDTO.getClientPhone(),
                    totalPrice,
                    createDTO.getServiceIds()
            );

            Appointment appointment = appointmentMapper.toEntity(dtoWithPrice, client, stylist, services);
            Appointment savedAppointment = appointmentRepository.save(appointment);
            logger.info("Appointment created successfully with ID: {}", savedAppointment.getAppointmentId());
            
            // Enviar notificaciones de confirmación
            if (notificationService != null) {
                try {
                    com.bookmycut.dto.NotificationCreateDTO clientNotif = new com.bookmycut.dto.NotificationCreateDTO();
                    clientNotif.setUserId(client.getUserId());
                    clientNotif.setTitle("Cita confirmada");
                    // Formatear fecha usando formato español para evitar problemas
                    String fechaFormateada = createDTO.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    clientNotif.setMessage(String.format("Tu cita con %s ha sido confirmada para el %s a las %s", 
                        stylist.getName(), fechaFormateada, createDTO.getStartTime()));
                    clientNotif.setType(com.bookmycut.entities.Notification.NotificationType.APPOINTMENT_CONFIRMED);
                    clientNotif.setRelatedAppointmentId(savedAppointment.getAppointmentId());
                    notificationService.createNotification(clientNotif);

                    com.bookmycut.dto.NotificationCreateDTO stylistNotif = new com.bookmycut.dto.NotificationCreateDTO();
                    stylistNotif.setUserId(stylist.getUserId());
                    stylistNotif.setTitle("Nueva cita asignada");
                    // Formatear fecha usando formato español para evitar problemas
                    String fechaFormateadaEstilista = createDTO.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    stylistNotif.setMessage(String.format("Tienes una nueva cita con %s el %s a las %s", 
                        client.getName(), fechaFormateadaEstilista, createDTO.getStartTime()));
                    stylistNotif.setType(com.bookmycut.entities.Notification.NotificationType.APPOINTMENT_CONFIRMED);
                    stylistNotif.setRelatedAppointmentId(savedAppointment.getAppointmentId());
                    notificationService.createNotification(stylistNotif);
                } catch (Exception e) {
                    logger.warn("Error sending notifications for appointment creation: {}", e.getMessage());
                }
            }
            
            return appointmentMapper.toDTO(savedAppointment);
        } catch (BadRequestException | ConflictException | ResourceNotFoundException e) {
            logger.warn("Validation error creating appointment: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating appointment: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Valida que el estilista esté disponible en la fecha y horario especificados.
     *
     * @param stylist Estilista a validar.
     * @param date Fecha de la cita.
     * @param startTime Hora de inicio.
     * @param endTime Hora de fin.
     * @throws BadRequestException Si el estilista no está disponible.
     */
    private void validateStylistAvailability(User stylist, LocalDate date, 
                                            java.time.LocalTime startTime, 
                                            java.time.LocalTime endTime) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        
        // Verificar excepciones de horario (días cerrados, vacaciones)
        List<ScheduleException> exceptions = scheduleExceptionRepository
                .findByDateAndStylistOrNull(date, stylist);
        
        for (ScheduleException exception : exceptions) {
            if (exception.getType() == ScheduleException.ExceptionType.NO_DISPONIBLE) {
                // Si la excepción es para todo el día (sin horas específicas)
                if (exception.getStartTime() == null || exception.getEndTime() == null) {
                    logger.warn("Stylist {} not available on {} due to schedule exception", 
                            stylist.getUserId(), date);
                    throw new ConflictException("El estilista no está disponible en esta fecha");
                }
                // Si la excepción es para un rango de horas específico
                if (!(endTime.isBefore(exception.getStartTime()) || 
                      startTime.isAfter(exception.getEndTime()))) {
                    logger.warn("Stylist {} not available on {} from {} to {} due to schedule exception", 
                            stylist.getUserId(), date, exception.getStartTime(), exception.getEndTime());
                    throw new ConflictException("El estilista no está disponible en este horario");
                }
            }
        }
        
        // Verificar horario regular de disponibilidad
        List<Availability> availabilities = availabilityRepository
                .findByStylistAndDayOfWeek(stylist, dayOfWeek);
        
        if (availabilities.isEmpty()) {
            logger.warn("Stylist {} has no availability configured for {}", 
                    stylist.getUserId(), dayOfWeek);
            throw new BadRequestException("El estilista no tiene disponibilidad configurada para los " + 
                    getDayOfWeekName(dayOfWeek));
        }
        
        // Verificar que el horario esté dentro de alguna disponibilidad
        boolean isAvailable = availabilities.stream().anyMatch(availability -> 
            !startTime.isBefore(availability.getStartTime()) && 
            !endTime.isAfter(availability.getEndTime())
        );
        
        if (!isAvailable) {
            logger.warn("Appointment time {} - {} is outside stylist {} availability on {}", 
                    startTime, endTime, stylist.getUserId(), dayOfWeek);
            throw new BadRequestException("La cita está fuera del horario disponible del estilista");
        }
    }

    /**
     * Valida que no haya citas solapadas para el mismo estilista en la misma fecha y horario.
     *
     * @param stylist Estilista a validar.
     * @param date Fecha de la cita.
     * @param startTime Hora de inicio.
     * @param endTime Hora de fin.
     * @param excludeAppointmentId ID de cita a excluir (para actualizaciones, puede ser null).
     * @throws ConflictException Si hay solapamiento.
     */
    private void validateNoOverlappingAppointments(User stylist, LocalDate date, 
                                                  java.time.LocalTime startTime, 
                                                  java.time.LocalTime endTime,
                                                  Long excludeAppointmentId) {
        List<Appointment> overlapping;
        
        if (excludeAppointmentId != null) {
            overlapping = appointmentRepository.findOverlappingAppointmentsExcluding(
                    stylist, date, startTime, endTime, excludeAppointmentId);
        } else {
            overlapping = appointmentRepository.findOverlappingAppointments(
                    stylist, date, startTime, endTime);
        }
        
        if (!overlapping.isEmpty()) {
            logger.warn("Found {} overlapping appointments for stylist {} on {} from {} to {}", 
                    overlapping.size(), stylist.getUserId(), date, startTime, endTime);
            throw new ConflictException("Ya existe una cita en este horario para el estilista");
        }
    }

    /**
     * Calcula el precio total sumando los precios de los servicios.
     *
     * @param services Lista de servicios.
     * @return Precio total calculado.
     */
    private BigDecimal calculateTotalPrice(List<ServiceOffer> services) {
        return services.stream()
                .map(ServiceOffer::getUnitPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Obtiene el nombre del día de la semana en español.
     *
     * @param dayOfWeek Día de la semana.
     * @return Nombre del día en español.
     */
    private String getDayOfWeekName(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "lunes";
            case TUESDAY -> "martes";
            case WEDNESDAY -> "miércoles";
            case THURSDAY -> "jueves";
            case FRIDAY -> "viernes";
            case SATURDAY -> "sábado";
            case SUNDAY -> "domingo";
        };
    }

    /**
     * Updates an existing appointment.
     *
     * @param id ID of the appointment to update.
     * @param updates Map with fields to update.
     * @return AppointmentDTO of the updated appointment.
     * @throws ResourceNotFoundException If the appointment does not exist.
     */
    @Transactional
    public AppointmentDTO updateAppointment(Long id, Map<String, Object> updates) {
        logger.info("Updating appointment with ID: {}", id);
        
        try {
            Appointment appointment = appointmentRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Appointment not found with ID: {}", id);
                        return new ResourceNotFoundException("Appointment", "id", id);
                    });

            LocalDate newDate = appointment.getDate();
            java.time.LocalTime newStartTime = appointment.getStartTime();
            java.time.LocalTime newEndTime = appointment.getEndTime();
            boolean dateOrTimeChanged = false;

            Appointment.AppointmentStatus oldStatus = appointment.getStatus();
            if (updates.containsKey("estado")) {
                String statusStr = updates.get("estado").toString();
                appointment.setStatus(Appointment.AppointmentStatus.valueOf(statusStr));
                logger.debug("Appointment status updated to: {}", statusStr);
                
                // Enviar notificación si se cancela la cita
                if (notificationService != null && 
                    appointment.getStatus() == Appointment.AppointmentStatus.CANCELADA &&
                    oldStatus != Appointment.AppointmentStatus.CANCELADA) {
                    try {
                        com.bookmycut.dto.NotificationCreateDTO clientNotif = new com.bookmycut.dto.NotificationCreateDTO();
                        clientNotif.setUserId(appointment.getClient().getUserId());
                        clientNotif.setTitle("Cita cancelada");
                        String fechaCancelada = appointment.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        clientNotif.setMessage(String.format("Tu cita con %s del %s a las %s ha sido cancelada", 
                            appointment.getStylist().getName(), fechaCancelada, appointment.getStartTime()));
                        clientNotif.setType(com.bookmycut.entities.Notification.NotificationType.APPOINTMENT_CANCELLED);
                        clientNotif.setRelatedAppointmentId(appointment.getAppointmentId());
                        notificationService.createNotification(clientNotif);

                        com.bookmycut.dto.NotificationCreateDTO stylistNotif = new com.bookmycut.dto.NotificationCreateDTO();
                        stylistNotif.setUserId(appointment.getStylist().getUserId());
                        stylistNotif.setTitle("Cita cancelada");
                        String fechaCanceladaEstilista = appointment.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        stylistNotif.setMessage(String.format("La cita con %s del %s a las %s ha sido cancelada", 
                            appointment.getClient().getName(), fechaCanceladaEstilista, appointment.getStartTime()));
                        stylistNotif.setType(com.bookmycut.entities.Notification.NotificationType.APPOINTMENT_CANCELLED);
                        stylistNotif.setRelatedAppointmentId(appointment.getAppointmentId());
                        notificationService.createNotification(stylistNotif);
                    } catch (Exception e) {
                        logger.warn("Error sending cancellation notifications: {}", e.getMessage());
                    }
                }
            }

            if (updates.containsKey("fecha")) {
                newDate = java.time.LocalDate.parse(updates.get("fecha").toString());
                appointment.setDate(newDate);
                dateOrTimeChanged = true;
                logger.debug("Appointment date updated");
            }

            if (updates.containsKey("horaInicio")) {
                newStartTime = java.time.LocalTime.parse(updates.get("horaInicio").toString());
                appointment.setStartTime(newStartTime);
                dateOrTimeChanged = true;
                logger.debug("Appointment start time updated");
            }

            if (updates.containsKey("horaFin")) {
                newEndTime = java.time.LocalTime.parse(updates.get("horaFin").toString());
                appointment.setEndTime(newEndTime);
                dateOrTimeChanged = true;
                logger.debug("Appointment end time updated");
            }

            // Validar que la hora de fin sea posterior a la de inicio
            if (newEndTime.isBefore(newStartTime) || newEndTime.equals(newStartTime)) {
                throw new BadRequestException("La hora de fin debe ser posterior a la hora de inicio");
            }

            // Validar que no se actualice a fechas/horas en el pasado
            if (dateOrTimeChanged) {
                LocalDate nowDate = LocalDate.now();
                java.time.LocalTime nowTime = java.time.LocalTime.now();
                if (newDate.isBefore(nowDate) || 
                    (newDate.equals(nowDate) && newStartTime.isBefore(nowTime))) {
                    logger.warn("Attempt to update appointment to past date/time - Date: {}, Time: {}", 
                            newDate, newStartTime);
                    throw new BadRequestException("No se pueden actualizar citas a fechas u horas en el pasado");
                }
            }

            // Si cambió la fecha u horario, validar disponibilidad y solapamiento
            if (dateOrTimeChanged) {
                validateStylistAvailability(appointment.getStylist(), newDate, newStartTime, newEndTime);
                validateNoOverlappingAppointments(appointment.getStylist(), newDate, 
                        newStartTime, newEndTime, appointment.getAppointmentId());
            }

            Appointment updatedAppointment = appointmentRepository.save(appointment);
            logger.info("Appointment with ID {} updated successfully", id);
            return appointmentMapper.toDTO(updatedAppointment);
        } catch (BadRequestException | ConflictException | ResourceNotFoundException e) {
            logger.warn("Validation error updating appointment: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating appointment with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Deletes an appointment by its ID.
     *
     * @param id Unique identifier of the appointment.
     * @throws ResourceNotFoundException If the appointment does not exist.
     */
    @Transactional
    public void deleteAppointment(Long id) {
        logger.info("Deleting appointment with ID: {}", id);
        
        try {
            Appointment appointment = appointmentRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Appointment not found with ID: {}", id);
                        return new ResourceNotFoundException("Appointment", "id", id);
                    });

            appointmentRepository.delete(appointment);
            logger.info("Appointment with ID {} deleted successfully", id);
        } catch (Exception e) {
            logger.error("Error deleting appointment with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}

