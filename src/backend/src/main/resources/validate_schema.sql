-- ============================================
-- Script de Validación del Schema - BookMyCut
-- ============================================
-- Este script valida que todas las tablas y relaciones
-- están correctamente creadas según el modelo E-R.
-- ============================================

USE bookmycut;

-- Verificar que todas las tablas existen
SELECT 'Validación de Tablas Existentes' AS 'VALIDACIÓN';

SELECT 
    TABLE_NAME AS 'Tabla',
    TABLE_ROWS AS 'Filas',
    TABLE_COMMENT AS 'Comentario'
FROM 
    information_schema.TABLES 
WHERE 
    TABLE_SCHEMA = 'bookmycut'
ORDER BY 
    TABLE_NAME;

-- Verificar índices
SELECT 'Validación de Índices' AS 'VALIDACIÓN';

SELECT 
    TABLE_NAME AS 'Tabla',
    INDEX_NAME AS 'Índice',
    COLUMN_NAME AS 'Columna',
    SEQ_IN_INDEX AS 'Orden'
FROM 
    information_schema.STATISTICS
WHERE 
    TABLE_SCHEMA = 'bookmycut'
ORDER BY 
    TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- Verificar foreign keys
SELECT 'Validación de Foreign Keys' AS 'VALIDACIÓN';

SELECT 
    TABLE_NAME AS 'Tabla',
    CONSTRAINT_NAME AS 'FK',
    COLUMN_NAME AS 'Columna',
    REFERENCED_TABLE_NAME AS 'Tabla Referenciada',
    REFERENCED_COLUMN_NAME AS 'Columna Referenciada'
FROM 
    information_schema.KEY_COLUMN_USAGE
WHERE 
    TABLE_SCHEMA = 'bookmycut'
    AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY 
    TABLE_NAME, CONSTRAINT_NAME;

-- Contar registros en cada tabla
SELECT 'Conteo de Registros' AS 'VALIDACIÓN';

SELECT 'usuarios' AS Tabla, COUNT(*) AS Registros FROM usuarios
UNION ALL
SELECT 'clientes', COUNT(*) FROM clientes
UNION ALL
SELECT 'estilistas', COUNT(*) FROM estilistas
UNION ALL
SELECT 'administradores', COUNT(*) FROM administradores
UNION ALL
SELECT 'servicios', COUNT(*) FROM servicios
UNION ALL
SELECT 'disponibilidades', COUNT(*) FROM disponibilidades
UNION ALL
SELECT 'excepciones_horario', COUNT(*) FROM excepciones_horario
UNION ALL
SELECT 'citas', COUNT(*) FROM citas
UNION ALL
SELECT 'cita_servicio', COUNT(*) FROM cita_servicio
UNION ALL
SELECT 'estilista_servicio', COUNT(*) FROM estilista_servicio
UNION ALL
SELECT 'notificaciones', COUNT(*) FROM notificaciones;

-- Validar integridad referencial básica
SELECT 'Validación de Integridad Referencial' AS 'VALIDACIÓN';

-- Verificar que todos los clientes tienen un usuario válido
SELECT 
    'clientes sin usuario' AS Error,
    COUNT(*) AS Cantidad
FROM 
    clientes c
    LEFT JOIN usuarios u ON c.usuario_id = u.usuario_id
WHERE 
    u.usuario_id IS NULL;

-- Verificar que todos los estilistas tienen un usuario válido
SELECT 
    'estilistas sin usuario' AS Error,
    COUNT(*) AS Cantidad
FROM 
    estilistas e
    LEFT JOIN usuarios u ON e.usuario_id = u.usuario_id
WHERE 
    u.usuario_id IS NULL;

-- Verificar que todas las citas tienen cliente y estilista válidos
SELECT 
    'citas sin cliente válido' AS Error,
    COUNT(*) AS Cantidad
FROM 
    citas c
    LEFT JOIN clientes cl ON c.cliente_id = cl.usuario_id
WHERE 
    cl.usuario_id IS NULL;

SELECT 
    'citas sin estilista válido' AS Error,
    COUNT(*) AS Cantidad
FROM 
    citas c
    LEFT JOIN estilistas e ON c.estilista_id = e.usuario_id
WHERE 
    e.usuario_id IS NULL;

SELECT 'Validación completada' AS 'ESTADO';




