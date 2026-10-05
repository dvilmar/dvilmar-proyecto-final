# Estructura del Proyecto BookMyCut

## 📁 Visión General

```
dvilmar-proyecto-final/
├── 📄 docker-compose.yml          # Configuración Docker Compose
├── 📄 .env.example                # Ejemplo de variables de entorno (Docker)
├── 📄 .dockerignore              # Archivos ignorados por Docker
├── 📄 README.md                  # Documentación principal
├── 📁 docs/                      # Documentación del proyecto
├── 📁 src/                       # Código fuente
│   ├── 📁 backend/               # Backend Spring Boot
│   └── 📁 frontend/              # Frontend Angular
└── 📁 .idea/                     # Configuración IDE (IntelliJ)
```

---

## 🎯 Backend - Spring Boot

### Ubicación: `src/backend/`

```
src/backend/
├── 📄 pom.xml                    # Configuración Maven
├── 📄 Dockerfile                 # Imagen Docker del backend
├── 📄 .dockerignore             # Archivos ignorados en Docker
├── 📄 .env.example              # Variables de entorno (ejecución local)
├── 📄 ENV_SETUP.md              # Guía de configuración de variables
├── 📄 setup-database.sql        # Script de base de datos (si aplica)
│
└── 📁 src/
    ├── 📁 main/
    │   ├── 📁 java/com/bookmycut/
    │   │   ├── 📄 BookMyCutApplication.java    # Clase principal Spring Boot
    │   │   │
    │   │   ├── 📁 config/                      # Configuraciones
    │   │   │   ├── CorsConfig.java             # Configuración CORS
    │   │   │   ├── DotenvConfig.java           # Carga de variables .env
    │   │   │   ├── OpenApiConfig.java          # Configuración Swagger/OpenAPI
    │   │   │   └── WebSocketConfig.java        # Configuración WebSockets
    │   │   │
    │   │   ├── 📁 controller/                  # Controladores REST
    │   │   │   ├── AppointmentController.java       # Gestión de citas
    │   │   │   ├── AuthController.java             # Autenticación
    │   │   │   ├── AvailabilityController.java     # Disponibilidad
    │   │   │   ├── NotificationController.java     # Notificaciones
    │   │   │   ├── ScheduleExceptionController.java # Excepciones de horario
    │   │   │   ├── ServiceOfferController.java     # Servicios ofertados
    │   │   │   ├── StylistServiceController.java   # Servicios de estilista
    │   │   │   └── UserController.java             # Gestión de usuarios
    │   │   │
    │   │   ├── 📁 dto/                          # Data Transfer Objects
    │   │   │   ├── AppointmentCreateDTO.java
    │   │   │   ├── AppointmentDTO.java
    │   │   │   ├── AuthResponse.java
    │   │   │   ├── AvailabilityCreateDTO.java
    │   │   │   ├── AvailabilityDTO.java
    │   │   │   ├── LoginRequest.java
    │   │   │   ├── NotificationCreateDTO.java
    │   │   │   ├── NotificationDTO.java
    │   │   │   ├── RegisterRequest.java
    │   │   │   ├── ScheduleExceptionCreateDTO.java
    │   │   │   ├── ScheduleExceptionDTO.java
    │   │   │   ├── ServiceOfferCreateDTO.java
    │   │   │   ├── ServiceOfferDTO.java
    │   │   │   └── UserDTO.java
    │   │   │
    │   │   ├── 📁 entities/                     # Entidades JPA
    │   │   │   ├── Appointment.java             # Citas
    │   │   │   ├── Availability.java            # Disponibilidad
    │   │   │   ├── Notification.java            # Notificaciones
    │   │   │   ├── ScheduleException.java       # Excepciones de horario
    │   │   │   ├── ServiceOffer.java            # Servicios ofertados
    │   │   │   └── User.java                    # Usuarios
    │   │   │
    │   │   ├── 📁 exception/                    # Manejo de excepciones
    │   │   │   ├── BadRequestException.java
    │   │   │   ├── ConflictException.java
    │   │   │   ├── ForbiddenException.java
    │   │   │   ├── GlobalExceptionHandler.java  # Manejador global
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   └── UnauthorizedException.java
    │   │   │
    │   │   ├── 📁 mappers/                      # Mappers DTO <-> Entity
    │   │   │   ├── AppointmentMapper.java
    │   │   │   ├── ServiceOfferMapper.java
    │   │   │   └── UserMapper.java
    │   │   │
    │   │   ├── 📁 repositories/                 # Repositorios JPA
    │   │   │   ├── AppointmentRepository.java
    │   │   │   ├── AvailabilityRepository.java
    │   │   │   ├── NotificationRepository.java
    │   │   │   ├── ScheduleExceptionRepository.java
    │   │   │   ├── ServiceOfferRepository.java
    │   │   │   └── UserRepository.java
    │   │   │
    │   │   ├── 📁 security/                     # Configuración de seguridad
    │   │   │   ├── CustomUserDetailsService.java
    │   │   │   ├── JwtAuthenticationFilter.java
    │   │   │   └── SecurityConfig.java
    │   │   │
    │   │   ├── 📁 service/                      # Lógica de negocio
    │   │   │   ├── AppointmentReminderScheduler.java  # Scheduler de recordatorios
    │   │   │   ├── AppointmentService.java
    │   │   │   ├── AuthService.java
    │   │   │   ├── AvailabilityService.java
    │   │   │   ├── NotificationService.java
    │   │   │   ├── ScheduleExceptionService.java
    │   │   │   └── ServiceOfferService.java
    │   │   │
    │   │   └── 📁 util/                         # Utilidades
    │   │       ├── JwtUtil.java                 # Utilidades JWT
    │   │       ├── PhoneNumberValidator.java    # Validador de teléfonos
    │   │       └── ValidPhoneNumber.java        # Anotación de validación
    │   │
    │   └── 📁 resources/                        # Recursos
    │       ├── application.properties           # Configuración Spring Boot
    │       ├── schema.sql                       # Esquema de base de datos
    │       └── data.sql                         # Datos iniciales
    │
    └── 📁 test/                                 # Tests
        └── 📁 java/com/bookmycut/service/
            ├── AppointmentServiceTest.java
            ├── AvailabilityServiceTest.java
            ├── NotificationServiceTest.java
            └── ServiceOfferServiceTest.java
```

### 📦 Tecnologías Backend

- **Framework**: Spring Boot 3.2.0
- **Lenguaje**: Java 17
- **Build Tool**: Maven
- **Base de Datos**: MySQL 8.0
- **ORM**: JPA/Hibernate
- **Seguridad**: Spring Security + JWT
- **API Docs**: SpringDoc OpenAPI (Swagger)
- **WebSockets**: Spring WebSocket (STOMP)
- **Logging**: SLF4J/Logback

---

## 🎨 Frontend - Angular

### Ubicación: `src/frontend/`

```
src/frontend/
├── 📄 package.json               # Dependencias npm
├── 📄 angular.json               # Configuración Angular CLI
├── 📄 tsconfig.json              # Configuración TypeScript
├── 📄 tsconfig.app.json          # Configuración TypeScript (app)
├── 📄 webpack.config.js          # Configuración Webpack
├── 📄 Dockerfile                 # Imagen Docker del frontend
├── 📄 nginx.conf                 # Configuración Nginx
├── 📄 .dockerignore             # Archivos ignorados en Docker
│
├── 📁 dist/                      # Build de producción (generado)
│   └── 📁 bookmycut/
│
├── 📁 node_modules/              # Dependencias (generado)
│
└── 📁 src/
    ├── 📄 index.html             # HTML principal
    ├── 📄 main.ts                # Punto de entrada
    ├── 📄 styles.css             # Estilos globales
    ├── 📄 favicon.ico            # Favicon
    │
    └── 📁 app/
        ├── 📄 app.component.ts   # Componente raíz
        ├── 📄 app.routes.ts      # Configuración de rutas
        │
        ├── 📁 components/        # Componentes Angular
        │   ├── 📁 admin/
        │   │   └── 📁 admin-dashboard/
        │   │       ├── admin-dashboard.component.html
        │   │       └── admin-dashboard.component.ts
        │   │
        │   ├── 📁 auth/          # Autenticación
        │   │   ├── 📁 login/
        │   │   │   ├── login.component.html
        │   │   │   └── login.component.ts
        │   │   └── 📁 register/
        │   │       ├── register.component.html
        │   │       └── register.component.ts
        │   │
        │   ├── 📁 calendario/    # Calendario de citas
        │   │   ├── calendario.component.html
        │   │   ├── calendario.component.css
        │   │   └── calendario.component.ts
        │   │
        │   ├── 📁 citas/         # Gestión de citas
        │   │   ├── 📁 cita-form/
        │   │   │   ├── cita-form.component.html
        │   │   │   └── cita-form.component.ts
        │   │   └── 📁 citas-list/
        │   │       ├── citas-list.component.html
        │   │       └── citas-list.component.ts
        │   │
        │   ├── 📁 dashboard/     # Panel principal
        │   │   ├── dashboard.component.html
        │   │   └── dashboard.component.ts
        │   │
        │   ├── 📁 disponibilidad/ # Gestión de disponibilidad
        │   │   ├── disponibilidad.component.html
        │   │   └── disponibilidad.component.ts
        │   │
        │   ├── 📁 excepciones/   # Excepciones de horario
        │   │   ├── excepciones.component.html
        │   │   └── excepciones.component.ts
        │   │
        │   ├── 📁 navbar/        # Barra de navegación
        │   │   ├── navbar.component.html
        │   │   ├── navbar.component.css
        │   │   └── navbar.component.ts
        │   │
        │   ├── 📁 notificaciones/ # Notificaciones
        │   │   ├── notificaciones.component.html
        │   │   └── notificaciones.component.ts
        │   │
        │   ├── 📁 perfil/        # Perfil de usuario
        │   │   ├── perfil.component.html
        │   │   └── perfil.component.ts
        │   │
        │   ├── 📁 servicios/     # Servicios ofertados
        │   │   ├── servicios.component.html
        │   │   └── servicios.component.ts
        │   │
        │   └── 📁 usuarios/      # Gestión de usuarios (admin)
        │       ├── usuarios.component.html
        │       ├── usuarios.component.ts
        │       └── 📁 usuario-form/
        │           ├── usuario-form.component.html
        │           └── usuario-form.component.ts
        │
        ├── 📁 guards/            # Guards de rutas
        │   ├── auth.guard.ts     # Guard de autenticación
        │   └── role.guard.ts     # Guard de roles
        │
        ├── 📁 services/          # Servicios Angular
        │   ├── api.service.ts    # Servicio API HTTP
        │   ├── auth.service.ts   # Servicio de autenticación
        │   └── notification.service.ts  # Servicio de notificaciones
        │
        └── 📁 types/             # Definiciones TypeScript
            └── sockjs-client.d.ts  # Tipo para sockjs-client
```

### 📦 Tecnologías Frontend

- **Framework**: Angular 17
- **Lenguaje**: TypeScript 5.2
- **Build Tool**: Angular CLI
- **Estilos**: Bootstrap 5.3 + CSS personalizado
- **HTTP**: HttpClient (Angular)
- **WebSockets**: @stomp/stompjs + sockjs-client
- **Routing**: Angular Router
- **Forms**: Angular Reactive Forms
- **Servidor Dev**: Angular Dev Server
- **Servidor Prod**: Nginx

---

## 🗂️ Documentación

### Ubicación: `docs/`

```
docs/
├── 📄 ANALISIS_MEJORAS_Y_FUNCIONALIDADES_FALTANTES.md
├── 📄 ANALISIS_PROGRESO_PROYECTO.md
├── 📄 API_REST_DOCUMENTATION.md
├── 📄 CAMBIOS_ENTIDADES_INGLES.md
├── 📄 COMPARACION_PROYECTOS.md
├── 📄 CONFIGURACION_CORS_ACTUALIZADA.md
├── 📄 DB_Schema_BookMyCut.drawio        # Diagrama de base de datos
├── 📄 DB_Schema_BookMyCut.pdf          # PDF del esquema
├── 📄 EJEMPLO_ENV.txt                  # Ejemplo de variables de entorno
├── 📄 ESTRUCTURA_PROYECTO.md           # Este archivo
├── 📄 INSTALACION_WEBSOCKET.md
├── 📄 MANUAL_INSTALACION.md
├── 📄 MEJORAS_COMPLETAS_ANALISIS.md
├── 📄 MEJORAS_IMPLEMENTADAS.md
├── 📄 MEJORAS_SEGURIDAD_Y_EXCEPCIONES.md
├── 📄 MEJORAS_SUGERIDAS.md
├── 📄 README.md
├── 📄 RESUMEN_DESPLIEGUE.md
├── 📄 RESUMEN_FINAL_RENOMBRADO.md
├── 📄 RESUMEN_IMPLEMENTACION_COMPLETA.md
├── 📄 RESUMEN_IMPLEMENTACION_FUNCIONALIDADES.md
├── 📄 RESUMEN_SWAGGER.md
├── 📄 REVISION_COMPLETA_PROYECTO.md
├── 📄 REVISION_FINAL_COMPLETA.md
├── 📄 SWAGGER_IMPLEMENTACION.md
└── 📄 TEMA_DISEÑO_IMPLEMENTADO.md
```

---

## 🐳 Docker

### Archivos de configuración:

```
dvilmar-proyecto-final/
├── 📄 docker-compose.yml        # Orquestación de contenedores
│                                # - MySQL 8.0
│                                # - Backend Spring Boot
│                                # - Frontend Angular (Nginx)
│
├── 📄 .env.example               # Variables de entorno (raíz - Docker)
│
src/backend/
├── 📄 Dockerfile                 # Multi-stage build: Maven + JRE
└── 📄 .env.example               # Variables de entorno (Local)
src/frontend/
├── 📄 Dockerfile                 # Multi-stage build: Node + Nginx
├── 📄 nginx.conf                 # Configuración Nginx para SPA
└───📄 .env.example               # Variables de entorno (Local)
```

---

## 🔧 Configuración

### Variables de Entorno

1. **`.env` (raíz)**: Para Docker Compose
   - Variables MySQL
   - Variables Backend
   - Variables Frontend

2. **`src/backend/.env`**: Para ejecución local del backend
   - Configuración de base de datos local
   - Configuración del servidor

### Archivos de Configuración

- **Backend**: `src/backend/src/main/resources/application.properties`
- **Frontend**: `src/frontend/angular.json`, `src/frontend/tsconfig.json`
- **Docker**: `docker-compose.yml`, `Dockerfile`s

---

## 📊 Arquitectura

### Patrón de Arquitectura

```
┌─────────────────────────────────────────┐
│           Frontend (Angular)            │
│  ┌──────────┐  ┌──────────┐            │
│  │Component │→ │ Service  │            │
│  └──────────┘  └────┬─────┘            │
└─────────────────────┼───────────────────┘
                      │ HTTP/WebSocket
┌─────────────────────┼───────────────────┐
│      Backend (Spring Boot)              │
│  ┌──────────┐  ┌──────────┐  ┌────────┐│
│  │Controller│→ │ Service  │→ │Repository││
│  └──────────┘  └──────────┘  └────┬───┘│
└───────────────────────────────────┼─────┘
                                    │ JPA
┌───────────────────────────────────┼─────┐
│         MySQL Database            │     │
└───────────────────────────────────┴─────┘
```

### Capas del Backend

1. **Controller**: Recibe peticiones HTTP, valida entrada, delega a servicios
2. **Service**: Lógica de negocio, validaciones, transformaciones
3. **Repository**: Acceso a datos (JPA), consultas personalizadas
4. **Entity**: Modelo de datos, mapeo ORM
5. **DTO**: Objetos de transferencia de datos (API)

### Componentes del Frontend

1. **Components**: Vista y lógica de presentación
2. **Services**: Comunicación con backend, estado de la aplicación
3. **Guards**: Protección de rutas (autenticación, roles)
4. **Types**: Definiciones TypeScript

---

## 🚀 Comandos Útiles

### Backend
```bash
# Desarrollo
cd src/backend
mvn spring-boot:run

# Build
mvn clean package

# Tests
mvn test
```

### Frontend
```bash
# Desarrollo
cd src/frontend
npm install
npm start

# Build producción
npm run build

# Tests
npm test
```

### Docker
```bash
# Desde la raíz del proyecto
docker-compose up -d --build
docker-compose down
docker-compose logs -f
```

---

## 📝 Notas

- Los archivos `.env` reales no están en el repositorio (`.gitignore`)
- Los archivos `node_modules/` y `target/` son generados y no están en el repositorio
- La documentación completa está en `docs/`
- El proyecto sigue una arquitectura de capas estándar
- Los tests están en `src/test/java/` (backend)

---

**Última actualización**: 2024




