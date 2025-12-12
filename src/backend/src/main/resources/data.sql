-- ============================================
-- Script de Datos Iniciales - BookMyCut
-- ============================================
-- Este script pobla la base de datos con datos de prueba.
-- Basado en el esquema definido en schema.sql
--
-- Ejecución:
--   mysql -u root -p bookmycut < data.sql
--   O desde mysql: source data.sql;
--
-- IMPORTANTE: Ejecutar primero schema.sql
-- ============================================

-- Asegurarse de usar la base de datos correcta
USE bookmycut;

-- Asegurar que la conexión use UTF-8
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ============================================
-- Usuarios de Prueba
-- ============================================

-- Administrador por defecto (se crea automáticamente)
-- Username: admin, Password: admin
-- NOTA: Contraseñas en texto plano (solo para desarrollo)
INSERT INTO usuarios (name, username, password, role, active, created_date, last_modified_date) VALUES
('Administrador Principal', 'admin', 'admin', 'ADMINISTRADOR', TRUE, NOW(), NOW());

INSERT INTO administradores (usuario_id) VALUES (LAST_INSERT_ID());

-- Cliente de prueba
-- Password: password123
INSERT INTO usuarios (name, email, password, role, active, created_date, last_modified_date) VALUES
('Juan Pérez', 'cliente@bookmycut.com', 'password123', 'CLIENTE', TRUE, NOW(), NOW());

INSERT INTO clientes (usuario_id, phone) VALUES (LAST_INSERT_ID(), '123456789');

-- Estilista de prueba (con username)
-- Password: password123
INSERT INTO usuarios (name, username, password, role, active, created_date, last_modified_date) VALUES
('María García', 'maria.garcia', 'password123', 'ESTILISTA', TRUE, NOW(), NOW());

INSERT INTO estilistas (usuario_id) VALUES (LAST_INSERT_ID());

-- Otro estilista (con username)
-- Password: password123
INSERT INTO usuarios (name, username, password, role, active, created_date, last_modified_date) VALUES
('Carlos López', 'carlos.lopez', 'password123', 'ESTILISTA', TRUE, NOW(), NOW());

INSERT INTO estilistas (usuario_id) VALUES (LAST_INSERT_ID());

-- ============================================
-- Servicios
-- ============================================
INSERT INTO servicios (name, description, duration, precio_unitario, created_date, last_modified_date) VALUES
('Corte de Pelo', 'Corte de pelo clásico para hombre o mujer', 30, 15.00, NOW(), NOW()),
('Corte y Peinado', 'Corte de pelo con peinado profesional', 45, 25.00, NOW(), NOW()),
('Tinte Completo', 'Tinte completo del cabello', 120, 50.00, NOW(), NOW()),
('Mechas', 'Aplicación de mechas', 90, 45.00, NOW(), NOW()),
('Lavado y Secado', 'Lavado profesional y secado', 30, 12.00, NOW(), NOW()),
('Tratamiento Capilar', 'Tratamiento nutritivo y reparador', 45, 30.00, NOW(), NOW()),
('Barba y Bigote', 'Arreglo de barba y bigote', 20, 10.00, NOW(), NOW()),
('Manicura', 'Cuidado y esmaltado de uñas', 45, 20.00, NOW(), NOW());

-- ============================================
-- Asignar Servicios a Estilistas
-- ============================================
-- María García ofrece todos los servicios (usuario_id = 3)
INSERT INTO estilista_servicio (estilista_id, servicio_id) 
SELECT (SELECT usuario_id FROM usuarios WHERE username = 'maria.garcia'), servicio_id FROM servicios;

-- Carlos López ofrece algunos servicios (usuario_id = 4)
INSERT INTO estilista_servicio (estilista_id, servicio_id) 
SELECT (SELECT usuario_id FROM usuarios WHERE username = 'carlos.lopez'), servicio_id FROM servicios 
WHERE servicio_id IN (1, 2, 5, 7); -- Corte de Pelo, Corte y Peinado, Lavado y Secado, Barba y Bigote

-- ============================================
-- Disponibilidades de Estilistas
-- ============================================
-- Disponibilidad por defecto para todos los estilistas: Lunes a Viernes, 10:00 - 18:30
-- María García: Lunes a Viernes, 10:00 - 18:30
INSERT INTO disponibilidades (estilista_id, dia_semana, hora_inicio, hora_fin) 
SELECT (SELECT usuario_id FROM usuarios WHERE username = 'maria.garcia'), 'MONDAY', '10:00:00', '18:30:00'
UNION ALL SELECT (SELECT usuario_id FROM usuarios WHERE username = 'maria.garcia'), 'TUESDAY', '10:00:00', '18:30:00'
UNION ALL SELECT (SELECT usuario_id FROM usuarios WHERE username = 'maria.garcia'), 'WEDNESDAY', '10:00:00', '18:30:00'
UNION ALL SELECT (SELECT usuario_id FROM usuarios WHERE username = 'maria.garcia'), 'THURSDAY', '10:00:00', '18:30:00'
UNION ALL SELECT (SELECT usuario_id FROM usuarios WHERE username = 'maria.garcia'), 'FRIDAY', '10:00:00', '18:30:00';

-- Carlos López: Lunes a Viernes, 10:00 - 18:30
INSERT INTO disponibilidades (estilista_id, dia_semana, hora_inicio, hora_fin) 
SELECT (SELECT usuario_id FROM usuarios WHERE username = 'carlos.lopez'), 'MONDAY', '10:00:00', '18:30:00'
UNION ALL SELECT (SELECT usuario_id FROM usuarios WHERE username = 'carlos.lopez'), 'TUESDAY', '10:00:00', '18:30:00'
UNION ALL SELECT (SELECT usuario_id FROM usuarios WHERE username = 'carlos.lopez'), 'WEDNESDAY', '10:00:00', '18:30:00'
UNION ALL SELECT (SELECT usuario_id FROM usuarios WHERE username = 'carlos.lopez'), 'THURSDAY', '10:00:00', '18:30:00'
UNION ALL SELECT (SELECT usuario_id FROM usuarios WHERE username = 'carlos.lopez'), 'FRIDAY', '10:00:00', '18:30:00';

-- Más clientes de prueba
-- Password: password123
INSERT INTO usuarios (name, email, password, role, active, created_date, last_modified_date) VALUES
('Ana Martínez', 'ana.martinez@bookmycut.com', 'password123', 'CLIENTE', TRUE, NOW(), NOW());

INSERT INTO clientes (usuario_id, phone) VALUES (LAST_INSERT_ID(), '987654321');

-- Password: password123
INSERT INTO usuarios (name, email, password, role, active, created_date, last_modified_date) VALUES
('Luis Rodríguez', 'luis.rodriguez@bookmycut.com', 'password123', 'CLIENTE', TRUE, NOW(), NOW());

INSERT INTO clientes (usuario_id, phone) VALUES (LAST_INSERT_ID(), '555123456');

-- Otro estilista
-- Password: password123
INSERT INTO usuarios (name, username, password, role, active, created_date, last_modified_date) VALUES
('Laura Sánchez', 'laura.sanchez', 'password123', 'ESTILISTA', TRUE, NOW(), NOW());

INSERT INTO estilistas (usuario_id) VALUES (LAST_INSERT_ID());

-- Asignar servicios a Laura Sánchez
INSERT INTO estilista_servicio (estilista_id, servicio_id) 
SELECT (SELECT usuario_id FROM usuarios WHERE username = 'laura.sanchez'), servicio_id FROM servicios 
WHERE servicio_id IN (1, 2, 3, 4, 5); -- Corte de Pelo, Corte y Peinado, Tinte Completo, Mechas, Lavado y Secado

-- Disponibilidades de Laura Sánchez: Lunes a Viernes, 10:00 - 18:30
INSERT INTO disponibilidades (estilista_id, dia_semana, hora_inicio, hora_fin) 
SELECT (SELECT usuario_id FROM usuarios WHERE username = 'laura.sanchez'), 'MONDAY', '10:00:00', '18:30:00'
UNION ALL SELECT (SELECT usuario_id FROM usuarios WHERE username = 'laura.sanchez'), 'TUESDAY', '10:00:00', '18:30:00'
UNION ALL SELECT (SELECT usuario_id FROM usuarios WHERE username = 'laura.sanchez'), 'WEDNESDAY', '10:00:00', '18:30:00'
UNION ALL SELECT (SELECT usuario_id FROM usuarios WHERE username = 'laura.sanchez'), 'THURSDAY', '10:00:00', '18:30:00'
UNION ALL SELECT (SELECT usuario_id FROM usuarios WHERE username = 'laura.sanchez'), 'FRIDAY', '10:00:00', '18:30:00';

-- ============================================
-- Citas de Ejemplo
-- ============================================
-- Cita confirmada para mañana (Juan Pérez con María García)
INSERT INTO citas (cliente_id, estilista_id, status, date, hora_inicio, hora_fin, telefono_cliente, precio_total, created_date, last_modified_date) 
SELECT 
    (SELECT usuario_id FROM usuarios WHERE email = 'cliente@bookmycut.com'),
    (SELECT usuario_id FROM usuarios WHERE username = 'maria.garcia'),
    'CONFIRMADA', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00', '11:00:00', '123456789', 25.00, NOW(), NOW();

-- Asignar servicios a la cita (Corte y Peinado)
INSERT INTO cita_servicio (cita_id, servicio_id) 
SELECT LAST_INSERT_ID(), servicio_id FROM servicios WHERE name = 'Corte y Peinado';

-- Cita confirmada para pasado mañana (Juan Pérez con Carlos López)
INSERT INTO citas (cliente_id, estilista_id, status, date, hora_inicio, hora_fin, telefono_cliente, precio_total, created_date, last_modified_date) 
SELECT 
    (SELECT usuario_id FROM usuarios WHERE email = 'cliente@bookmycut.com'),
    (SELECT usuario_id FROM usuarios WHERE username = 'carlos.lopez'),
    'CONFIRMADA', DATE_ADD(CURDATE(), INTERVAL 2 DAY), '14:00:00', '14:30:00', '123456789', 15.00, NOW(), NOW();

INSERT INTO cita_servicio (cita_id, servicio_id) 
SELECT LAST_INSERT_ID(), servicio_id FROM servicios WHERE name = 'Corte de Pelo';

-- Citas pasadas para historial (hace 7 días) - Juan Pérez con María García
INSERT INTO citas (cliente_id, estilista_id, status, date, hora_inicio, hora_fin, telefono_cliente, precio_total, created_date, last_modified_date) 
SELECT 
    (SELECT usuario_id FROM usuarios WHERE email = 'cliente@bookmycut.com'),
    (SELECT usuario_id FROM usuarios WHERE username = 'maria.garcia'),
    'FINALIZADA', DATE_SUB(CURDATE(), INTERVAL 7 DAY), '11:00:00', '12:30:00', '123456789', 95.00, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY);

INSERT INTO cita_servicio (cita_id, servicio_id) 
SELECT LAST_INSERT_ID(), servicio_id FROM servicios WHERE name IN ('Tinte Completo', 'Lavado y Secado');

-- Cita pasada (hace 14 días) - Juan Pérez con Carlos López
INSERT INTO citas (cliente_id, estilista_id, status, date, hora_inicio, hora_fin, telefono_cliente, precio_total, created_date, last_modified_date) 
SELECT 
    (SELECT usuario_id FROM usuarios WHERE email = 'cliente@bookmycut.com'),
    (SELECT usuario_id FROM usuarios WHERE username = 'carlos.lopez'),
    'FINALIZADA', DATE_SUB(CURDATE(), INTERVAL 14 DAY), '15:00:00', '15:30:00', '123456789', 15.00, DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY);

INSERT INTO cita_servicio (cita_id, servicio_id) 
SELECT LAST_INSERT_ID(), servicio_id FROM servicios WHERE name = 'Corte de Pelo';

-- Cita pasada (hace 30 días) - cancelada - Juan Pérez con María García
INSERT INTO citas (cliente_id, estilista_id, status, date, hora_inicio, hora_fin, telefono_cliente, precio_total, created_date, last_modified_date) 
SELECT 
    (SELECT usuario_id FROM usuarios WHERE email = 'cliente@bookmycut.com'),
    (SELECT usuario_id FROM usuarios WHERE username = 'maria.garcia'),
    'CANCELADA', DATE_SUB(CURDATE(), INTERVAL 30 DAY), '10:00:00', '11:00:00', '123456789', 25.00, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 29 DAY);

INSERT INTO cita_servicio (cita_id, servicio_id) 
SELECT LAST_INSERT_ID(), servicio_id FROM servicios WHERE name = 'Corte y Peinado';

-- Citas para otro cliente (Ana Martínez con Laura Sánchez)
INSERT INTO citas (cliente_id, estilista_id, status, date, hora_inicio, hora_fin, telefono_cliente, precio_total, created_date, last_modified_date) 
SELECT 
    (SELECT usuario_id FROM usuarios WHERE email = 'ana.martinez@bookmycut.com'),
    (SELECT usuario_id FROM usuarios WHERE username = 'laura.sanchez'),
    'FINALIZADA', DATE_SUB(CURDATE(), INTERVAL 10 DAY), '09:00:00', '11:00:00', '987654321', 70.00, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY);

INSERT INTO cita_servicio (cita_id, servicio_id) 
SELECT LAST_INSERT_ID(), servicio_id FROM servicios WHERE name IN ('Mechas', 'Lavado y Secado');

-- Cita futura para Ana (con Laura Sánchez)
INSERT INTO citas (cliente_id, estilista_id, status, date, hora_inicio, hora_fin, telefono_cliente, precio_total, created_date, last_modified_date) 
SELECT 
    (SELECT usuario_id FROM usuarios WHERE email = 'ana.martinez@bookmycut.com'),
    (SELECT usuario_id FROM usuarios WHERE username = 'laura.sanchez'),
    'CONFIRMADA', DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:00:00', '12:00:00', '987654321', 95.00, NOW(), NOW();

INSERT INTO cita_servicio (cita_id, servicio_id) 
SELECT LAST_INSERT_ID(), servicio_id FROM servicios WHERE name IN ('Tinte Completo', 'Lavado y Secado');

-- Más citas pasadas para tener un historial rico - Juan Pérez con María García
INSERT INTO citas (cliente_id, estilista_id, status, date, hora_inicio, hora_fin, telefono_cliente, precio_total, created_date, last_modified_date) 
SELECT 
    (SELECT usuario_id FROM usuarios WHERE email = 'cliente@bookmycut.com'),
    (SELECT usuario_id FROM usuarios WHERE username = 'maria.garcia'),
    'FINALIZADA', DATE_SUB(CURDATE(), INTERVAL 21 DAY), '14:00:00', '14:45:00', '123456789', 25.00, DATE_SUB(NOW(), INTERVAL 21 DAY), DATE_SUB(NOW(), INTERVAL 21 DAY);

INSERT INTO cita_servicio (cita_id, servicio_id) 
SELECT LAST_INSERT_ID(), servicio_id FROM servicios WHERE name = 'Corte y Peinado';

-- Notificaciones de ejemplo
-- Nota: Se insertan sin relacionar con citas específicas para evitar dependencias complejas
INSERT INTO notificaciones (usuario_id, title, message, type, is_read, created_date) 
SELECT 
    (SELECT usuario_id FROM usuarios WHERE email = 'cliente@bookmycut.com'),
    'Cita confirmada',
    'Tu cita para mañana a las 10:00 ha sido confirmada',
    'APPOINTMENT_CONFIRMED',
    FALSE,
    NOW()
UNION ALL
SELECT 
    (SELECT usuario_id FROM usuarios WHERE email = 'cliente@bookmycut.com'),
    'Recordatorio de cita',
    'Tienes una cita mañana a las 10:00 con María García',
    'APPOINTMENT_REMINDER',
    FALSE,
    DATE_SUB(NOW(), INTERVAL 1 DAY)
UNION ALL
SELECT 
    (SELECT usuario_id FROM usuarios WHERE email = 'ana.martinez@bookmycut.com'),
    'Cita finalizada',
    CONCAT('Tu cita del ', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 10 DAY), '%d/%m/%Y'), ' ha sido finalizada'),
    'INFO',
    TRUE,
    DATE_SUB(NOW(), INTERVAL 10 DAY);

-- ============================================
-- Notas sobre Contraseñas
-- ============================================
-- Contraseñas por defecto:
--   - Admin: username="admin", password="admin"
--   - Otros usuarios de prueba: password="password123"
-- En producción, estas deben ser cambiadas inmediatamente
-- NOTA: Las contraseñas están en texto plano (solo para desarrollo, NO usar en producción)






