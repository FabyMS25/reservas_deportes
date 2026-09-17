# Deportes GAMQ — backend

API de gestión de espacios deportivos del Gobierno Autónomo Municipal de Quillacollo. Administra complejos, canchas, horarios, tarifas, contribuyentes, organizaciones, estudiantes, reservas, mensualidades, pagos, notificaciones y reportes.

## Arquitectura

```text
deportes-fe (Angular)
  └─ deportes-be (API, permisos y reglas del negocio)
       ├─ PostgreSQL / PostGIS: datos del sistema
       ├─ Usuarios: autenticación y permisos del personal municipal
       └─ ruat-be: acceso compartido a los servicios externos de RUAT
```

Java 11, Spring Boot 2.7.15, Gradle Wrapper 8.2.1, Spring Data JPA, Spring Security, WebSocket/STOMP y JasperReports. `ruat-be` es otro proyecto y proceso; no comparte la base de datos de Deportes.

## Funcionalidades

| Área | Funciones |
| --- | --- |
| Infraestructura | Complejos, canchas, superficies, categorías, encargados, horarios fijos y excepciones. |
| Tarifas | Tarifas por cancha/categoría, conceptos, clasificadores RUAT y detalle tarifario de reservas. |
| Contribuyentes | Consulta, registro, actualización, acceso de usuario y relación con RUAT. |
| Organizaciones | Clubes, asociaciones, organizaciones y escuelas municipales; varios representantes por organización y varias organizaciones por contribuyente. |
| Estudiantes | Identidad compartida entre inscripciones; escuela, tutor/responsable, matrícula, baja, reactivación e historial. |
| Reservas puntuales | Espacio, fecha, horario, disponibilidad, tarifa, validación, cambio de horario y anulación según permisos y políticas. |
| Reservas recurrentes | Período de fechas, horarios/sesiones, validación, cobros y tickets. |
| Mensualidades | Varios estudiantes y períodos por registro, selección de pagador e historial por inscripción. |
| Pagos y RUAT | Búsqueda/registro de contribuyentes, creación/anulación de tasas y consulta de pagos mediante el gateway. |
| Seguimiento | Auditoría general y de reservas, notificaciones por usuario y contador de no leídas. |
| Reportes | Tickets y listados de reservas puntuales, recurrentes, mensualidades y pagos. |
| Configuración | Políticas por clave, categoría, ámbito y referencia. |

## Usuarios y autorización

- **Administrador:** administra el sistema con las reglas de autorización del backend.
- **Personal/encargado:** requiere permisos de Usuarios y asignación a un complejo; el servidor comprueba su alcance.
- **Contribuyente:** usa su cuenta local y recibe un menú construido por el backend. Puede ser padre/tutor sin representar una organización.
- **Representante:** es una relación almacenada con una organización; no permite administrar cualquier escuela.

El token de Deportes contiene una instantánea firmada de permisos. El menú del frontend controla la presentación; los endpoints y operaciones comprueban autorización y alcance en el servidor. Modificar el almacenamiento del navegador no concede acceso. Después de cambiar permisos o representación, iniciar sesión nuevamente.

Definiciones: `src/main/resources/security/taxpayer-menu.json` y `permission-resources.json`. Ver [flujo de permisos](docs/permission-flow.md).

## Flujos del sistema

### Inicio de sesión

1. El frontend envía credenciales a `POST /api/auth/login`.
2. Deportes llama una vez a `ruat-be: POST /api/ruat/auth/check`. El gateway comprueba primero la clave compartida y el sistema solicitante.
3. Un rechazo del gateway o fallo de transporte bloquea el login. Si el gateway acepta pero RUAT municipal está indisponible, devuelve `available: false`; Deportes puede continuar autenticando y presentar el aviso.
4. Deportes valida al personal con Usuarios o al contribuyente con su cuenta local; resuelve permisos y asignación.
5. Devuelve token propio, usuario, menú, asignación cuando corresponda, `ruatStatus` y `ruatMessage`. Los tokens de Usuarios y RUAT no se entregan al navegador.

### Preparación y reservas

1. Registrar complejos, canchas, encargados, horarios y excepciones.
2. Configurar tarifas, clasificadores y políticas aplicables.
3. Seleccionar contribuyente y organización cuando corresponda.
4. Seleccionar reserva puntual o recurrente, espacio y horario disponible.
5. Aplicar tarifa, aprobación y pago conforme a las políticas. Las acciones disponibles dependen del permiso y estado del registro.
6. Consultar el pago RUAT cuando aplique y obtener el ticket. Anulaciones y cambios de horario tienen controles propios.

### Escuelas y estudiantes

1. El personal autorizado registra la escuela y sus representantes.
2. Puede inscribir un estudiante y asignar cualquier contribuyente como tutor/responsable, aunque no represente la escuela.
3. Un contribuyente solo puede crear inscripciones en escuelas que representa y bajo su propia responsabilidad; no puede seleccionar otro tutor o una escuela ajena.
4. Un estudiante puede tener relaciones con varias escuelas. La matrícula identifica una inscripción y no se permiten inscripciones activas duplicadas en la misma escuela.
5. La baja conserva identidad e historial; la reactivación crea otra inscripción y matrícula. Cambiar de escuela requiere otra inscripción.

### Mensualidades

1. Un contribuyente puede acceder a **Agregar mensualidad** sin representar una organización.
2. Debe tener estudiantes activos a su cargo o estudiantes de una escuela que representa. Si no los tiene, se muestra: “Su usuario no tiene estudiantes a cargo, por lo que no puede registrar mensualidades.”
3. Seleccionar escuela, reserva recurrente base aprobada/activa, estudiantes y períodos disponibles.
4. El padre/tutor puede seleccionar sus estudiantes aunque la reserva base pertenezca a otro contribuyente. Los representantes pueden gestionar los de su escuela; el personal opera conforme a sus permisos.
5. Elegir un pagador permitido, revisar importes y registrar tasa y mensualidad. Los períodos registrados se excluyen de la selección disponible.
6. Consultar pagos, tickets e historial desde mensualidades o la ficha del estudiante.

Crear la tasa RUAT y guardar la reserva son operaciones separadas. Si falla el segundo paso puede existir una tasa: verificar el resultado antes de repetir una creación.

## API e integración

Base local habitual: `http://localhost:8085/api`. Swagger: `/swagger-ui/index.html`; OpenAPI: `/v3/api-docs`. Los controladores y OpenAPI son la referencia completa de métodos y parámetros.

| Área | Ruta base |
| --- | --- |
| Acceso / personal | `/api/auth`, `/api/usuarios` |
| Contribuyentes / organizaciones | `/api/contribuyente`, `/api/organizaciones` |
| Estudiantes e inscripciones | `/api/estudiante-escuela` |
| Complejos / canchas / encargados | `/api/complejos`, `/api/campoDeportivo` (alias `/api/sports-fields`), `/api/encargado` |
| Configuración | `/api/configuracionSistema` (alias `/api/system-configurations`) |
| Reportes / notificaciones | `/api/reporte`, `/api/notificaciones` |
| Auditoría general / puente RUAT | `/api/sistema-auditorias`, `/api/ruat` |

WebSocket/SockJS: `/ws-notificaciones`. La conexión requiere token de Deportes y solo permite suscribirse a los canales del usuario autenticado.

| Deportes POST | ruat-be POST |
| --- | --- |
| `/api/ruat/login` | `/api/ruat/auth/check` |
| `/api/ruat/buscar` | `/api/ruat/taxpayers/search` |
| `/api/ruat/registro` | `/api/ruat/taxpayers` |
| `/api/ruat/creartasa` | `/api/ruat/taxes` |
| `/api/ruat/anulartasa` | `/api/ruat/taxes/annulments` |
| `/api/ruat/pago` | `/api/ruat/taxes/payments/query` |

Ver [integración y diagnóstico RUAT](docs/ruat-gateway.md).

## Configuración y ejecución

Requisitos: JDK 11, PostgreSQL/PostGIS con el esquema y disparadores requeridos, servicio de Usuarios y una instancia configurada de ruat-be. No se incluye un instalador completo del esquema; una base vacía no basta para todos los flujos, incluida la generación de matrículas.

Configurar `src/main/resources/application.properties`; no editar la copia generada en `build/resources`.

| Propiedades | Uso |
| --- | --- |
| `spring.datasource.*`, `spring.jpa.hibernate.ddl-auto` | Conexión y política del esquema. |
| `server.port`, `server.address` | Puerto e interfaz. |
| `app.cors.allowed-origin-patterns` | Orígenes HTTP/WebSocket permitidos. |
| `usuarios.base-url` | Servicio de Usuarios. |
| `deportes.application.*` | Nombre, código del módulo y menú de administrador. |
| `deportes.security.*` | Firma y duración del token. |
| `ruat.gateway.base-url` | URL de ruat-be, no del RUAT externo. |
| `ruat.gateway.internal-api-key` | Clave compartida del gateway. |
| `ruat.client.*` | Tiempos de conexión y lectura. |

```powershell
.\gradlew.bat bootRun
# Compilar y empaquetar sin ejecutar pruebas
.\gradlew.bat bootJar
java -jar build/libs/app.jar
```

En Linux usar `./gradlew`. El ejecutable es `app.jar`, utilizado también por el Dockerfile. El JAR simple y las tareas automatizadas de pruebas están deshabilitados; no se mantiene `src/test`.

`docker-compose.yml` contiene backend, PostGIS, pgAdmin y frontend. Revisar la configuración del entorno, disponer de la imagen del frontend y preparar el esquema. Usuarios y ruat-be se configuran aparte. El proxy público debe enrutar `/api` y `/ws-notificaciones` al backend.

## Organización del código

```text
src/main/java/com/gamq/deportes/
  controller/          HTTP y autorización por endpoint
  service/implement/   Reglas y operaciones del negocio
  repository/          Acceso a datos
  model/               Entidades y modelos de integración
  dto/mapper/          Contratos y conversiones
  security/            Tokens, permisos y alcance
  config/              Configuración HTTP y WebSocket
  utils/               Generación de reportes
src/main/resources/
  security/            Permisos y menú del contribuyente
  reportes/            Plantillas Jasper
  imagenes/            Recursos de reportes
docs/                  Flujos e integración
```

`build/`, `.gradle/` y los archivos del IDE son generados/locales. No son el lugar para modificar código o configuración.

## Verificación manual

Compilar con `bootJar`; luego revisar en un entorno preparado: login por rol, permisos, reservas puntuales/recurrentes, inscripción por personal y representante, tutor no representante pagando mensualidades, usuario sin estudiantes, consulta de pago, tickets, historial y notificaciones. La compilación no verifica la base ni realiza operaciones reales contra RUAT.

Frontend: [README de deportes-fe](../deportes-fe/README.md).

## Autoría

GAMQ · Fabiola Danny Morales Siles · moralessfaby.dev@gmail.com
