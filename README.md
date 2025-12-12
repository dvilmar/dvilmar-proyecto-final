# BookMyCut

#### Curso Escolar 2025-2026
#### Autor: [https://github.com/dvilmar]
#### Tutor: [https://github.com/leyvapaco]
#### Fecha de Inicio: 15-09-2025
#### Fecha de Finalización: 01-12-2025

## Breve descripción del proyecto

Como propuesta de proyecto tengo el objetivo de desarrollar una aplicación web llamada BookMyCut se trata de una aplicación pensada para que peluquerías o centros de estética sean capaces de gestionar sus reservas de manera online y también proporcionar información sobre los servicios que ofrezcan.

La aplicación permitirá a los clientes reservar citas con estilistas, gestionar su perfil, recibir recordatorios de citas próximas y etc. El sistema contará con diferentes roles, como clientes, estilistas y administradores, cada uno con acceso a funcionalidades específicas. Los administradores tendrán la capacidad de gestionar al resto de usuarios, las citas y la disponibilidad de los estilistas, las fechas disponibles de la peluquería y más. Los clientes podrán gestionar sus citas, cancelarlas y modificar sus datos personales. Los estilistas podrán especificar  los servicios que ofrecen y consultar las citas programadas en el calendario.

El sistema estará diseñado para ser sencillo, fácil de usar y práctico. La interfaz de usuario tendrá un diseño responsive y se basará en un estilo minimalista y elegante utilizando sobre todo los colores blanco y negro. Para mejorar la experiencia del usuario se añadirán notificaciones para recordar las citas programadas y más funcionalidades para mejorar la accesibilidad.

He elegido está idea para mi proyecto final porque mi principal objetivo era crear una aplicación que pudiera usarse en una empresa real, que fuera sencilla y sin mucha complejidad. Muchas de las aplicaciones con aplicaciones reales ya existen de una manera profesional, y la mayoría se escapan de la complejidad de este proyecto. El concepto de una aplicación para reservar citas, en este caso una peluquería, es simple y podría utilizarse en algunos pequeños negocios particulares que requieren poca cosa. 

## Objetivo de la aplicación

BookMyCut es una aplicación web pensada para que peluquerías y centros de estética puedan organizar sus citas de forma online. Permitirá a los clientes reservar fácilmente día, hora, servicio y estilista desde internet, mientras que los negocios podrán llevar su agenda al día sin complicaciones.

El principal atractivo de la app es que es sencilla y práctica, pensada para pequeños negocios que no necesitan sistemas costosos ni complejos. Incluye funciones útiles como recordatorios automáticos de citas, un calendario visual y la posibilidad de que cada estilista gestione sus propios horarios.

El problema que resuelve es el desorden típico de llevar las citas en papel o por teléfono, evitando malentendidos, citas que se pierden y confusiones. En resumen, cubre la necesidad de dar a las peluquerías una herramienta clara para organizar sus horarios y a los clientes una forma rápida y cómoda de reservar sin tener que llamar o desplazarse.

## Stack Tecnológico

- **Backend:** Spring Boot 3.2.0 (Java 17)
- **Frontend:** Angular 17 (TypeScript)
- **Base de Datos:** MySQL 8.0
- **Autenticación:** JWT (JSON Web Tokens)
- **Estilos:** Bootstrap 5.3
- **Despliegue:** Docker & Docker Compose
- **Comunicación:** API RESTful
- **Tiempo Real:** WebSockets (preparado para implementación futura)

## Estructura del Proyecto

```
dvilmar-proyecto-final/
├── src/
│   ├── backend/              # Aplicación Spring Boot
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/bookmycut/
│   │   │   │   │   ├── model/          # Entidades JPA
│   │   │   │   │   ├── repository/     # Repositorios JPA
│   │   │   │   │   ├── service/        # Lógica de negocio
│   │   │   │   │   ├── controller/     # Controladores REST
│   │   │   │   │   ├── dto/            # Data Transfer Objects
│   │   │   │   │   ├── security/       # Configuración de seguridad
│   │   │   │   │   └── util/           # Utilidades (JWT)
│   │   │   │   └── resources/
│   │   │   │       └── application.properties
│   │   │   └── pom.xml
│   │   └── Dockerfile
│   └── frontend/             # Aplicación Angular
│       ├── src/
│       │   ├── app/
│       │   │   ├── components/         # Componentes Angular
│       │   │   ├── services/           # Servicios HTTP
│       │   │   ├── guards/             # Guards de rutas
│       │   │   └── app.routes.ts
│       │   ├── styles.css
│       │   └── index.html
│       ├── package.json
│       ├── angular.json
│       └── Dockerfile
├── docs/                      # Documentación
│   ├── API_REST_DOCUMENTATION.md
│   └── DB_Schema_BookMyCut.drawio
├── docker-compose.yml         # Configuración Docker
└── README.md
```

## Requisitos Previos

### Para Desarrollo Local

- Java 17 o superior
- Node.js 18 o superior
- MySQL 8.0 o superior
- Maven 3.9 o superior

### Para Despliegue con Docker (Recomendado)

- **Docker**: Versión 20.10 o superior
- **Docker Compose**: Versión 1.29 o superior

Verifica tu instalación:
```bash
docker --version
docker-compose --version
```

## Instalación y Configuración

### 🐳 Opción 1: Despliegue con Docker (Recomendado)

Esta es la forma más sencilla de desplegar la aplicación completa. Todos los servicios se ejecutan en contenedores Docker.

#### Pasos Rápidos

1. **Clonar el repositorio** (si aún no lo has hecho):
```bash
git clone <URL_DEL_REPOSITORIO>
cd dvilmar-proyecto-final
```

2. **Crear archivo de configuración**:
```bash
# Crear archivo .env con las variables de entorno
# Ver sección de configuración más abajo o consultar docs/MANUAL_INSTALACION.md
```

3. **Construir y levantar todos los servicios**:
```bash
docker-compose up -d --build
```

4. **Verificar que todo está funcionando**:
```bash
docker-compose ps
```

5. **Acceder a la aplicación**:
- **Frontend**: http://localhost:4200
- **Backend API**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **MySQL**: localhost:3306

#### Configuración con Variables de Entorno

Crea un archivo `.env` en la raíz del proyecto:

```properties
# Base de Datos
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=bookmycut
MYSQL_USER=bookmycut
MYSQL_PASSWORD=bookmycut

# Puertos
BACKEND_PORT=8080
FRONTEND_PORT=4200
MYSQL_PORT=3306

# JWT
JWT_SECRET=bookmycut-secret
JWT_EXPIRATION=86400000

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:4200
```

> **📖 Manual Completo**: Para instrucciones detalladas, troubleshooting y configuración avanzada, consulta el [Manual de Instalación](docs/MANUAL_INSTALACION.md).

#### Comandos Útiles

```bash
# Iniciar servicios
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose stop

# Detener y eliminar contenedores
docker-compose down

# Reiniciar un servicio
docker-compose restart backend

# Ver estado de los servicios
docker-compose ps
```

### 💻 Opción 2: Desarrollo Local (Sin Docker)

Esta opción es útil para desarrollo, pero requiere configurar manualmente todos los servicios.

#### Backend

1. **Crear la base de datos MySQL**:
```sql
CREATE DATABASE bookmycut;
```

2. **Configurar** `src/backend/src/main/resources/application.properties` con tus credenciales de MySQL.

3. **Compilar y ejecutar**:
```bash
cd src/backend
mvn clean install
mvn spring-boot:run
```

El backend estará disponible en `http://localhost:8080/api`

#### Frontend

1. **Instalar dependencias**:
```bash
cd src/frontend
npm install
```

2. **Ejecutar en modo desarrollo**:
```bash
npm start
```

El frontend estará disponible en `http://localhost:4200`

> **Nota**: Asegúrate de que el backend esté corriendo antes de iniciar el frontend, ya que el frontend necesita comunicarse con la API.

## Documentación

### Documentación de la API

La documentación completa de la API REST está disponible en:
- [docs/API_REST_DOCUMENTATION.md](docs/API_REST_DOCUMENTATION.md)

### Manual de Instalación

Para instrucciones detalladas de instalación y despliegue:
- [docs/MANUAL_INSTALACION.md](docs/MANUAL_INSTALACION.md)

### Otra Documentación

- [API REST Documentation](docs/API_REST_DOCUMENTATION.md)
- [Esquema de Base de Datos](docs/DB_Schema_BookMyCut.pdf)
- [Mejoras Sugeridas](docs/MEJORAS_SUGERIDAS.md)

## Funcionalidades Implementadas

### Entrega II

✅ **Proceso de autenticación**
- Login con JWT
- Registro de usuarios (Cliente, Estilista, Administrador)
- Logout
- Protección de rutas con guards

✅ **Dashboard inicial**
- Dashboard para usuarios (clientes y estilistas)
- Dashboard de administrador
- Visualización de citas según rol
- Gestión de servicios (admin)

✅ **Modelado de datos completo**
- Entidades JPA implementadas
- Relaciones entre entidades
- Validaciones en modelos

✅ **API REST**
- Endpoints GET, POST, PUT, PATCH implementados
- Validaciones en controladores
- Manejo de errores
- Documentación completa

✅ **Frontend**
- Componentes de autenticación
- Dashboards responsivos
- Integración con Bootstrap
- Servicios HTTP para comunicación con API

✅ **Docker**
- Configuración de contenedores
- Docker Compose para orquestación
- Configuración de nginx para frontend

## Próximas Implementaciones

- [ ] Sistema de recordatorios de citas
- [ ] Calendario interactivo para reservas
- [ ] Gestión de disponibilidad de estilistas
- [ ] Excepciones de horario
- [ ] WebSockets para actualizaciones en tiempo real
- [ ] Cambio de idioma (i18n)
- [ ] Modo claro/oscuro
- [ ] Historial de citas para clientes

## Licencia

Este proyecto es parte de un trabajo académico.
