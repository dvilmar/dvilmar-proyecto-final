-- ============================================
-- Script de Creación de Base de Datos - BookMyCut
-- ============================================
-- Este script crea la base de datos y todas las tablas según el modelo E-R
-- Basado en el diagrama DB_Schema_BookMyCut.drawio
--
-- Ejecución:
--   mysql -u root -p < schema.sql
--   O desde mysql: source schema.sql;
--
-- Nota: La base de datos se crea si no existe
-- ============================================

-- Crear la base de datos si no existe
-- Nota: MySQL ya crea la base de datos automáticamente si MYSQL_DATABASE está definido
-- Este comando es por si acaso y no causará problemas si ya existe
CREATE DATABASE IF NOT EXISTS bookmycut 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Usar la base de datos
-- Nota: Cuando MySQL ejecuta scripts en docker-entrypoint-initdb.d, ya está en la BD correcta
-- pero es buena práctica incluir USE para ejecuciones manuales
USE bookmycut;

-- Eliminar tablas si existen (en orden inverso de dependencias)
DROP TABLE IF EXISTS notificaciones;
DROP TABLE IF EXISTS cita_servicio;
DROP TABLE IF EXISTS citas;
DROP TABLE IF EXISTS excepciones_horario;
DROP TABLE IF EXISTS disponibilidades;
DROP TABLE IF EXISTS estilista_servicio;
DROP TABLE IF EXISTS servicios;
DROP TABLE IF EXISTS clientes;
DROP TABLE IF EXISTS estilistas;
DROP TABLE IF EXISTS administradores;
DROP TABLE IF EXISTS usuarios;

-- ============================================
-- Tabla de Usuarios (tabla base)
-- ============================================
-- Esta es la tabla principal que almacena todos los usuarios del sistema.
-- Utiliza un campo 'rol' para diferenciar entre CLIENTE, ESTILISTA y ADMINISTRADOR.
-- Las tablas clientes, estilistas y administradores son tablas de relación
-- que referencian esta tabla principal.
-- ============================================
CREATE TABLE IF NOT EXISTS usuarios (
    usuario_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'Nombre completo del usuario',
    username VARCHAR(50) NULL UNIQUE COMMENT 'Username para login (requerido para ESTILISTA y ADMINISTRADOR, opcional para CLIENTE)',
    email VARCHAR(100) NULL UNIQUE COMMENT 'Email (opcional, principalmente para clientes)',
    password VARCHAR(255) NOT NULL COMMENT 'Contraseña (texto plano en desarrollo)',
    role ENUM('CLIENTE', 'ESTILISTA', 'ADMINISTRADOR') NOT NULL COMMENT 'Rol del usuario en el sistema',
    active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Indica si el usuario está activo en el sistema',
    phone VARCHAR(20) NULL COMMENT 'Teléfono del usuario (principalmente para clientes)',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de creación del registro',
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Fecha de última modificación',
    last_password_change_date TIMESTAMP NULL COMMENT 'Fecha del último cambio de contraseña',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Tabla principal de usuarios del sistema';

-- ============================================
-- Tabla de Clientes
-- ============================================
-- Tabla de relación que identifica qué usuarios son clientes.
-- Nota: El teléfono puede estar también en la tabla usuarios, pero esta tabla
-- permite tener información adicional específica de clientes si se necesita en el futuro.
-- ============================================
CREATE TABLE IF NOT EXISTS clientes (
    usuario_id BIGINT PRIMARY KEY COMMENT 'ID del usuario cliente (FK a usuarios)',
    phone VARCHAR(20) NULL COMMENT 'Teléfono del cliente (duplicado de usuarios.phone para compatibilidad)',
    FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tabla de relación para identificar usuarios clientes';

-- ============================================
-- Tabla de Estilistas
-- ============================================
-- Tabla de relación que identifica qué usuarios son estilistas.
-- ============================================
CREATE TABLE IF NOT EXISTS estilistas (
    usuario_id BIGINT PRIMARY KEY COMMENT 'ID del usuario estilista (FK a usuarios)',
    FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tabla de relación para identificar usuarios estilistas';

-- ============================================
-- Tabla de Administradores
-- ============================================
-- Tabla de relación que identifica qué usuarios son administradores.
-- ============================================
CREATE TABLE IF NOT EXISTS administradores (
    usuario_id BIGINT PRIMARY KEY COMMENT 'ID del usuario administrador (FK a usuarios)',
    FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tabla de relación para identificar usuarios administradores';

-- ============================================
-- Tabla de Servicios
-- ============================================
-- Almacena los servicios ofrecidos por la peluquería.
-- Los servicios pueden ser ofrecidos por múltiples estilistas (relación N:M).
-- ============================================
CREATE TABLE IF NOT EXISTS servicios (
    servicio_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID único del servicio',
    name VARCHAR(100) NOT NULL COMMENT 'Nombre del servicio',
    description VARCHAR(500) NULL COMMENT 'Descripción detallada del servicio',
    duration INT NOT NULL COMMENT 'Duración del servicio en minutos',
    precio_unitario DECIMAL(10, 2) NOT NULL COMMENT 'Precio unitario del servicio',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de creación del servicio',
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Fecha de última modificación',
    INDEX idx_name (name) COMMENT 'Índice para búsquedas por nombre'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tabla de servicios ofrecidos por la peluquería';

-- ============================================
-- Tabla de Disponibilidades (horarios de estilistas)
-- ============================================
-- Define los horarios de disponibilidad de cada estilista por día de la semana.
-- Un estilista puede tener múltiples disponibilidades (una por día de la semana).
-- ============================================
CREATE TABLE IF NOT EXISTS disponibilidades (
    disp_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID único de la disponibilidad',
    estilista_id BIGINT NOT NULL COMMENT 'ID del estilista (FK a estilistas)',
    dia_semana ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY') NOT NULL COMMENT 'Día de la semana',
    hora_inicio TIME NOT NULL COMMENT 'Hora de inicio de la disponibilidad',
    hora_fin TIME NOT NULL COMMENT 'Hora de fin de la disponibilidad',
    FOREIGN KEY (estilista_id) REFERENCES estilistas(usuario_id) ON DELETE CASCADE,
    INDEX idx_estilista_dia (estilista_id, dia_semana) COMMENT 'Índice compuesto para búsquedas por estilista y día'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tabla de disponibilidades horarias de los estilistas';

-- ============================================
-- Tabla de Excepciones de Horario
-- ============================================
-- Permite definir excepciones en los horarios regulares de los estilistas.
-- Puede ser para un estilista específico o para todos (estilista_id NULL).
-- Puede ser para un rango de horas o para todo el día (hora_inicio/hora_fin NULL).
-- ============================================
CREATE TABLE IF NOT EXISTS excepciones_horario (
    excep_horario_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID único de la excepción',
    estilista_id BIGINT NULL COMMENT 'ID del estilista (FK a estilistas). NULL significa para todos los estilistas',
    admin_id BIGINT NOT NULL COMMENT 'ID del administrador que crea la excepción (FK a administradores)',
    date DATE NOT NULL COMMENT 'Fecha de la excepción',
    hora_inicio TIME NULL COMMENT 'Hora de inicio (NULL significa día completo)',
    hora_fin TIME NULL COMMENT 'Hora de fin (NULL significa día completo)',
    type ENUM('DISPONIBLE', 'NO_DISPONIBLE') NOT NULL COMMENT 'Tipo de excepción: disponible o no disponible',
    reason VARCHAR(500) NULL COMMENT 'Motivo o descripción de la excepción',
    FOREIGN KEY (estilista_id) REFERENCES estilistas(usuario_id) ON DELETE CASCADE,
    FOREIGN KEY (admin_id) REFERENCES administradores(usuario_id) ON DELETE CASCADE,
    INDEX idx_date (date) COMMENT 'Índice para búsquedas por fecha',
    INDEX idx_estilista_date (estilista_id, date) COMMENT 'Índice compuesto para búsquedas por estilista y fecha',
    INDEX idx_type (type) COMMENT 'Índice para búsquedas por tipo'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tabla de excepciones de horario para estilistas';

-- ============================================
-- Tabla de Citas
-- ============================================
-- Almacena las citas/reservas realizadas por los clientes.
-- Una cita puede tener múltiples servicios asociados (relación N:M).
-- ============================================
CREATE TABLE IF NOT EXISTS citas (
    cita_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID único de la cita',
    cliente_id BIGINT NOT NULL COMMENT 'ID del cliente (FK a clientes)',
    estilista_id BIGINT NOT NULL COMMENT 'ID del estilista asignado (FK a estilistas)',
    status ENUM('CONFIRMADA', 'CANCELADA', 'FINALIZADA') NOT NULL DEFAULT 'CONFIRMADA' COMMENT 'Estado actual de la cita',
    date DATE NOT NULL COMMENT 'Fecha de la cita',
    hora_inicio TIME NOT NULL COMMENT 'Hora de inicio de la cita',
    hora_fin TIME NOT NULL COMMENT 'Hora de fin de la cita (calculada según duración de servicios)',
    telefono_cliente VARCHAR(20) NULL COMMENT 'Teléfono del cliente (puede diferir del registrado)',
    precio_total DECIMAL(10, 2) NOT NULL COMMENT 'Precio total de la cita (suma de servicios)',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de creación de la cita',
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Fecha de última modificación',
    FOREIGN KEY (cliente_id) REFERENCES clientes(usuario_id) ON DELETE CASCADE,
    FOREIGN KEY (estilista_id) REFERENCES estilistas(usuario_id) ON DELETE CASCADE,
    INDEX idx_cliente (cliente_id) COMMENT 'Índice para búsquedas por cliente',
    INDEX idx_estilista (estilista_id) COMMENT 'Índice para búsquedas por estilista',
    INDEX idx_date (date) COMMENT 'Índice para búsquedas por fecha',
    INDEX idx_status (status) COMMENT 'Índice para búsquedas por estado',
    INDEX idx_date_status (date, status) COMMENT 'Índice compuesto para búsquedas por fecha y estado',
    INDEX idx_estilista_date (estilista_id, date) COMMENT 'Índice compuesto para búsquedas de disponibilidad'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tabla de citas/reservas del sistema';

-- ============================================
-- Tabla de Relación Cita-Servicio (N:M)
-- ============================================
CREATE TABLE IF NOT EXISTS cita_servicio (
    cita_id BIGINT NOT NULL COMMENT 'ID de la cita (FK a citas)',
    servicio_id BIGINT NOT NULL COMMENT 'ID del servicio (FK a servicios)',
    PRIMARY KEY (cita_id, servicio_id),
    FOREIGN KEY (cita_id) REFERENCES citas(cita_id) ON DELETE CASCADE,
    FOREIGN KEY (servicio_id) REFERENCES servicios(servicio_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tabla de relación N:M entre citas y servicios';

-- ============================================
-- Tabla de Relación Estilista-Servicio (N:M)
-- ============================================
CREATE TABLE IF NOT EXISTS estilista_servicio (
    estilista_id BIGINT NOT NULL COMMENT 'ID del estilista (FK a estilistas)',
    servicio_id BIGINT NOT NULL COMMENT 'ID del servicio (FK a servicios)',
    PRIMARY KEY (estilista_id, servicio_id),
    FOREIGN KEY (estilista_id) REFERENCES estilistas(usuario_id) ON DELETE CASCADE,
    FOREIGN KEY (servicio_id) REFERENCES servicios(servicio_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tabla de relación N:M entre estilistas y servicios';

-- ============================================
-- Tabla de Notificaciones
-- ============================================
-- Almacena las notificaciones del sistema para cada usuario.
-- Puede estar relacionada con una cita específica (opcional).
-- ============================================
CREATE TABLE IF NOT EXISTS notificaciones (
    notificacion_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID único de la notificación',
    usuario_id BIGINT NOT NULL COMMENT 'ID del usuario destinatario (FK a usuarios)',
    title VARCHAR(255) NOT NULL COMMENT 'Título de la notificación',
    message VARCHAR(500) NULL COMMENT 'Mensaje detallado de la notificación',
    type ENUM('INFO', 'SUCCESS', 'WARNING', 'ERROR', 'APPOINTMENT_REMINDER', 'APPOINTMENT_CANCELLED', 'APPOINTMENT_CONFIRMED') NOT NULL DEFAULT 'INFO' COMMENT 'Tipo de notificación',
    is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Indica si la notificación ha sido leída',
    related_appointment_id BIGINT NULL COMMENT 'ID de la cita relacionada (FK a citas, opcional)',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de creación de la notificación',
    FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id) ON DELETE CASCADE,
    FOREIGN KEY (related_appointment_id) REFERENCES citas(cita_id) ON DELETE SET NULL,
    INDEX idx_usuario (usuario_id),
    INDEX idx_read (is_read),
    INDEX idx_type (type),
    INDEX idx_fecha (created_date),
    INDEX idx_usuario_read (usuario_id, is_read) COMMENT 'Índice compuesto para búsquedas de notificaciones no leídas por usuario'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tabla de notificaciones del sistema';

-- ============================================
-- Script Completado
-- ============================================
-- Todas las tablas han sido creadas según el modelo E-R.
-- 
-- Estructura del modelo:
--   - usuarios (tabla base)
--   - clientes, estilistas, administradores (tablas de relación)
--   - servicios
--   - disponibilidades (horarios de estilistas)
--   - excepciones_horario (excepciones en horarios)
--   - citas
--   - cita_servicio (relación N:M)
--   - estilista_servicio (relación N:M)
--   - notificaciones
--
-- Para poblar la base de datos con datos de prueba, ejecutar:
--   source data.sql;
-- O
--   mysql -u root -p bookmycut < data.sql;
-- ============================================




