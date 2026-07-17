# cipolflo-server

Backend del sistema de gestión del Club CIPOLFLO. Administra clientes, socios, reservas, servicios y finanzas a través de una API REST.

---

## Stack

- **Java 21**
- **Spring Boot 3.x**
- **Spring Data JPA + Hibernate**
- **Spring Security**
- **PostgreSQL**
- **Liquibase**
- **Gradle (Kotlin DSL)**
- **Lombok**
- **Springdoc OpenAPI (Swagger UI)**

---

## Configuración inicial (primer uso)

Seguir estos pasos **una sola vez** al clonar el proyecto por primera vez.

---

### 1. Instalar Docker Desktop (Windows)

1. Ir a [https://www.docker.com/products/docker-desktop/](https://www.docker.com/products/docker-desktop/) y descargar el instalador para Windows.
2. Ejecutar el instalador y seguir los pasos (dejá las opciones por defecto).
3. Reiniciar la PC si lo pide.
4. Abrir Docker Desktop y esperar a que el ícono de la ballena en la barra de tareas quede en verde — eso indica que Docker está corriendo.

> Docker Desktop debe estar abierto y corriendo **cada vez** que trabajés con el proyecto.

---

### 2. Instalar JDK 21

Si ya tenés JDK 21 instalado, saltear este paso.

1. Ir a [https://www.oracle.com/java/technologies/downloads/#java21](https://www.oracle.com/java/technologies/downloads/#java21) y descargar el instalador para Windows.
2. Ejecutar el instalador con las opciones por defecto.
3. Verificar la instalación abriendo una terminal y ejecutando:

```
java -version
```

Debe mostrar `java version "21..."`.

---

### 3. Clonar el repositorio

```bash
git clone https://github.com/CIPOLFLO/cipolflo-server.git
cd cipolflo-server
```

---

### 4. Crear el archivo `.env` con las credenciales

En la raíz del proyecto hay un archivo `.env.example` con valores de ejemplo. Hay que crear una copia con el nombre `.env` y completar las credenciales reales.

**En PowerShell:**

```powershell
Copy-Item .env.example .env
```

**O manualmente:** copiar el archivo `.env.example`, pegarlo en la misma carpeta y renombrarlo a `.env` (sin ninguna extensión adicional).

Luego abrir `.env` con cualquier editor de texto y reemplazar los valores de ejemplo:

```env
POSTGRES_USER=admin
POSTGRES_PASSWORD=elegí_una_contraseña_segura
POSTGRES_DB=CIPOLFLO_BD

DB_USERNAME=admin
DB_PASSWORD=elegí_una_contraseña_segura
```

> `DB_PASSWORD` y `POSTGRES_PASSWORD` deben tener el mismo valor.

| Archivo        | Se versiona                   | Contiene                                                   |
| -------------- | ----------------------------- | ---------------------------------------------------------- |
| `.env.example` | Sí                            | Plantilla con valores de ejemplo — sin credenciales reales |
| `.env`         | **No** (está en `.gitignore`) | Tus credenciales reales — nunca commitear                  |

---

### 5. Instalar el plugin EnvFile en IntelliJ IDEA

El plugin EnvFile permite que IntelliJ inyecte automáticamente las variables del `.env` al correr la app.

1. Abrir IntelliJ IDEA.
2. Ir a `File` → `Settings` → `Plugins`.
3. En la pestaña `Marketplace`, buscar **"EnvFile"**.
4. Hacer clic en **Install** y luego en **Restart IDE** cuando lo pida.

---

### 6. Configurar el Run Configuration para leer el `.env`

1. En la barra superior de IntelliJ, hacer clic en el menú desplegable al lado del botón ▶ (Play) → **"Edit Configurations..."**.
2. En el panel izquierdo, seleccionar **Spring Boot → ServerApplication**.
3. En el panel derecho, tildar la casilla **"Enable EnvFile"**.
4. Tildar también **"Substitute Environment Variables"**.
5. Hacer clic en el **`+`** que aparece debajo de las casillas → seleccionar **".env file"**.
6. Navegar hasta la raíz del proyecto y seleccionar el archivo **`.env`**.
7. Hacer clic en **Apply** y luego **OK**.

---

## Levantar el proyecto (uso diario)

### 1. Levantar la base de datos

Asegurarse de que Docker Desktop esté abierto y corriendo, luego ejecutar en la terminal:

```bash
docker compose up -d
```

Postgres queda corriendo en `localhost:5433`. Liquibase aplica automáticamente las migraciones pendientes al iniciar la app.

Para detenerla sin borrar los datos:

```bash
docker compose stop
```

Para detenerla y borrar el volumen (reset completo de la DB):

```bash
docker compose down -v
```

### 2. Correr la app

Presionar el botón **▶** en IntelliJ o ejecutar:

```bash
./gradlew bootRun
```

La API queda disponible en `http://localhost:8080`.

> **¿Por qué aparece una pantalla de login en el navegador?**
> El proyecto incluye Spring Security, que por defecto protege todos los endpoints. Eso es comportamiento esperado — la configuración de seguridad se completará cuando se implementen los endpoints de autenticación. Por ahora, el usuario por defecto es `user` y la contraseña generada aparece en la consola al iniciar la app, en una línea como: `Using generated security password: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`.

### Documentación de la API (Swagger)

La documentación de la API se genera automáticamente con **springdoc-openapi** a partir de los controllers y de las anotaciones `@Schema` de los DTOs.

| Recurso | Ruta |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Especificación OpenAPI (JSON) | `http://localhost:8080/v3/api-docs` |

> **Estas rutas están protegidas por Spring Security**, igual que el resto de la API. No están incluidas en las rutas públicas de `SecurityConfig`, así que acceder sin un JWT válido devuelve `401`. Para explorar la doc hay que autenticarse enviando el token en el header `Authorization: Bearer <token>` (por ejemplo desde un cliente HTTP), o —solo en un entorno de desarrollo— agregar temporalmente `"/swagger-ui/**"`, `"/swagger-ui.html"` y `"/v3/api-docs/**"` a `permitAll()` en `SecurityConfig`.

> **Importante:** para documentar campos de DTOs usar `@Schema` (de `io.swagger.v3.oas.annotations.media`), **no** `@Description` de `jdk.jfr` —esta última no tiene efecto sobre la doc de la API.

### Comandos útiles

```bash
# Compilar
./gradlew build

# Compilar sin tests
./gradlew build -x test
```

### Verificación de dependencias

El archivo `gradle/verification-metadata.xml` contiene los checksums SHA-256 de todas las dependencias. Gradle los verifica en cada build para detectar artefactos modificados o comprometidos.

**Cada vez que agregues, elimines o actualices una dependencia en `build.gradle.kts`, regenerá el archivo y commitealo junto con el cambio:**

```bash
./gradlew --write-verification-metadata sha256 help
```

---

## Migraciones de base de datos

El schema de la base de datos se gestiona con **Liquibase**. Al iniciar la app, Liquibase compara las migraciones ya aplicadas (registradas en la tabla `DATABASECHANGELOG`) con las del proyecto y ejecuta solo las pendientes.

### Estructura

Las migraciones se organizan en **una carpeta por ticket**. Cada carpeta tiene su propio
changelog "general" que incluye los SQL de esa carpeta, y el master referencia ese
changelog de carpeta (nunca los SQL sueltos).

```
src/main/resources/db/changelog/
├── db.changelog-master.yaml              ← referencia el changelog de cada carpeta
└── migrations/
    ├── 001_initial_schema.sql            ← schema inicial
    └── DEV-117/                          ← una carpeta por ticket
        ├── db.changelog-DEV-117.yaml     ← changelog general: incluye los SQL de la carpeta
        └── alter_tipo_fechas_columnas_reserva.sql
```

### Cómo agregar una nueva migración

Seguir siempre este proceso al introducir cambios en el schema (nueva tabla, columna, índice, constraint, etc.):

**1. Crear la carpeta del ticket (si no existe) y el archivo SQL de la migración**

Crear la carpeta `src/main/resources/db/changelog/migrations/{TICKET}/` (ej. `DEV-117/`) y, dentro,
un archivo SQL con **nombre descriptivo** de lo que hace (en minúsculas con guiones bajos), no numérico:

```
migrations/DEV-117/alter_tipo_fechas_columnas_reserva.sql
```

El contenido del archivo debe seguir el formato de Liquibase para SQL:

```sql
--liquibase formatted sql

--changeset cipolflo:DEV-117-descripcion-breve
-- DDL aquí
ALTER TABLE public.cliente ADD COLUMN fecha_baja date;

--rollback ALTER TABLE public.cliente DROP COLUMN fecha_baja;
```

Reglas del formato:

- La primera línea del archivo debe ser siempre `--liquibase formatted sql`.
- Cada changeset necesita `--changeset autor:id`. El `id` debe ser único en todo el proyecto — usar el ticket como prefijo (`cipolflo:DEV-117-...`) garantiza esto.
- Agregar siempre `--rollback` con el SQL inverso. Si el rollback es imposible (ej: `DROP TABLE`), usar `--rollback empty`.
- Un changeset **nunca se modifica** una vez aplicado. Si hay un error, corregirlo en una migración nueva.

**2. Crear (o actualizar) el changelog general de la carpeta**

Cada carpeta de ticket tiene un `db.changelog-{TICKET}.yaml` que incluye, en orden, todos los SQL de esa carpeta:

```yaml
# migrations/DEV-117/db.changelog-DEV-117.yaml
databaseChangeLog:
  - include:
      file: db/changelog/migrations/DEV-117/alter_tipo_fechas_columnas_reserva.sql
```

Si agregás otra migración al mismo ticket, sumás su `include` a este archivo (no se toca el master).

**3. Registrar la carpeta en el changelog maestro**

Solo la **primera** migración de un ticket requiere tocar el master. Abrir
`src/main/resources/db/changelog/db.changelog-master.yaml` y agregar el `include` al changelog de la carpeta:

```yaml
databaseChangeLog:
  - include:
      file: db/changelog/migrations/001_initial_schema.sql
  - include:
      file: db/changelog/migrations/DEV-117/db.changelog-DEV-117.yaml # ← agregar acá
```

**4. Verificar localmente**

Reiniciar la app. Liquibase aplica la migración al arrancar y lo registra en `DATABASECHANGELOG`. Si hay algún error en el SQL, la app no levanta y el mensaje de error indica el changeset fallido.

### Reset completo de la base de datos

Si necesitás partir de cero (ej: al trabajar en migraciones en desarrollo):

```bash
docker compose down -v   # elimina el volumen — borra todos los datos
docker compose up -d     # recrea el contenedor
# al correr la app, Liquibase aplica todas las migraciones desde cero
```

---

## Correr los tests

```bash
# Correr todos los tests
./gradlew test

# Correr tests de un módulo específico (ejemplo: clientes)
./gradlew test --tests "com.cipolflo.server.clientes.*"

# Correr un test puntual
./gradlew test --tests "com.cipolflo.server.clientes.service.ClienteServiceTest"
```

---

## Arquitectura general

El proyecto usa **arquitectura en capas por módulo de dominio**. Cada módulo agrupa todas sus capas internamente, en lugar de tener carpetas globales por tipo de capa.

```
src/main/java/com/cipolflo/server/
├── shared/                  ← Utilidades transversales a todos los módulos
├── clientes/                ← Módulo de clientes y socios
├── servicios/               ← Módulo de servicios ofrecidos
├── reservas/                ← Módulo de reservas
└── finanzas/                ← Módulo de ingresos y egresos
```

Dentro de cada módulo, la estructura de capas es:

```
{modulo}/
├── domain/          ← Entidades JPA y enums del dominio
│   └── enums/
├── repository/      ← Interfaces JpaRepository
├── service/         ← Interfaz + implementación de lógica de negocio
├── dto/             ← Request y Response DTOs
└── controller/      ← Endpoints REST (@RestController)
```

---

## Módulos

### `shared`

Clases base y enums usados por todos los módulos.

| Clase / Enum       | Rol                                                                                               |
| ------------------ | ------------------------------------------------------------------------------------------------- |
| `AuditableEntity`  | Superclase abstracta con `createdAt` y `updatedAt` automáticos. Todas las entidades la extienden. |
| `HealthController` | Endpoint `GET /api/health` para verificar que el servidor responde.                               |
| `FormaPago`        | Enum: `EFECTIVO`, `TRANSFERENCIA`, `DEBITO`, `CREDITO`                                            |
| `Procedencia`      | Enum: `SEDE`, `CAMPING`                                                                           |

---

### `clientes`

Gestión de clientes particulares y socios del club.

**Herencia de entidades** — estrategia `SINGLE_TABLE` con columna discriminadora `tipo`:

```
Cliente (abstracta)
├── Particular   (tipo = "PARTICULAR")
└── Socio        (tipo = "SOCIO")
```

`Socio` incluye lógica de negocio: cambio automático de estado a `INACTIVO` al acumular 3 meses de cuotas adeudadas (calculado a partir de `pago_cuota`, sin contador persistido), y baja.

| Clase                                      | Rol                                                                                  |
| ------------------------------------------ | ------------------------------------------------------------------------------------ |
| `Cliente`                                  | Entidad base: cédula, nombre, teléfono, mail, notas                                  |
| `Particular`                               | Extiende Cliente sin campos adicionales                                              |
| `Socio`                                    | Extiende Cliente con número de socio, fecha de nacimiento, estado, domicilio, cuotas |
| `PagoCuota`                                | Registro de pago de cuota mensual de un socio                                        |
| `EstadoSocio`                              | Enum: `ACTIVO`, `INACTIVO`, `DE_BAJA`                                                |
| `MetodoCobro`                              | Enum: `EN_SEDE`, `DESCUENTO_SALARIO`, `TRANSFERENCIA`                                |
| `ClienteRepository`                        | `JpaRepository<Cliente, Long>`                                                       |
| `IClienteService` / `ClienteService`       | Interfaz + implementación del servicio                                               |
| `ClienteRequestDto` / `ClienteResponseDto` | DTOs de entrada y salida                                                             |
| `ClienteController`                        | `@RestController` — base: `/api/v1/clientes`                                         |

---

### `servicios`

Catálogo de servicios que el club ofrece (actividades, instalaciones, etc.).

| Clase                                        | Rol                                                                                                               |
| -------------------------------------------- | ----------------------------------------------------------------------------------------------------------------- |
| `Servicio`                                   | Entidad: nombre, procedencia, precio por tipo de cliente, modalidad, capacidad, habilitado, requiereDocumentacion |
| `ModalidadPrecio`                            | Enum: `POR_DIA`, `POR_PERSONA`, `POR_DIA_POR_PERSONA`, `POR_UNIDAD`, `POR_HORA`                                   |
| `ServicioRepository`                         | `JpaRepository<Servicio, Long>`                                                                                   |
| `IServicioService` / `ServicioService`       | Interfaz + implementación del servicio                                                                            |
| `ServicioRequestDto` / `ServicioResponseDto` | DTOs de entrada y salida                                                                                          |
| `ServicioController`                         | `@RestController` — base: `/api/v1/servicios`                                                                     |

---

### `reservas`

Gestión del ciclo de vida de las reservas con máquina de estados.

**Tipos de reserva (`TipoReserva`):**

- `COMUN` — reserva estándar. Inicia en `PENDIENTE`.
- `COLABORACION_SIN_FINES_DE_LUCRO` — no requiere pago (importe = 0) y se identifica por RUT de organización en lugar de cliente del sistema. Inicia directamente en `CONFIRMADA`.

**Flujo de estados:**

```
PENDIENTE → CONFIRMADA → EN_CURSO → FINALIZADA
     └──────────────────────────────→ CANCELADA
```

La transición `PENDIENTE → CONFIRMADA` se dispara automáticamente cuando se cumplen **ambas** condiciones: pago registrado (`pago = true`) y documentación recibida (`tieneDocumentacion = true`). Para reservas que no requieren documentación, este flujo se resolverá cuando se implemente el método de confirmación manual (ver TODO en `Reserva.java`).

Las reservas de tipo `COLABORACION_SIN_FINES_DE_LUCRO` no tienen `clienteId`; en su lugar llevan un campo `rut` con el identificador de la organización.

La entidad usa un **factory method estático** `Reserva.crear(...)` en lugar de constructor público.

| Clase                                                              | Rol                                                                                                                         |
| ------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------- |
| `Reserva`                                                          | Entidad central: tipoReserva, clienteId (nullable), servicio, fechas, cantidades, rut, importe, estado, pago, documentación |
| `EstadoReserva`                                                    | Enum: `PENDIENTE`, `CONFIRMADA`, `EN_CURSO`, `FINALIZADA`, `CANCELADA`                                                      |
| `TipoReserva`                                                      | Enum: `COMUN`, `COLABORACION_SIN_FINES_DE_LUCRO`                                                                            |
| `ReservaRepository`                                                | `JpaRepository<Reserva, Long>`                                                                                              |
| `IReservaService` / `ReservaService`                               | Interfaz + implementación del servicio                                                                                      |
| `IRegistroParticularService` / `RegistroParticularService`         | Servicio dedicado para registrar clientes particulares (usado durante la creación de reserva)                               |
| `ReservaCreacionValidator`                                         | Valida fechas, disponibilidad del servicio, solapamiento y datos de cliente antes de crear la reserva                       |
| `IServicioRequiereDocumentacion` / `ServicioRequiereDocumentacion` | Consulta si un servicio requiere documentación previa                                                                       |
| `ReservaCreacionRequestDto` / `ReservaCreacionResponseDto`         | DTOs de entrada y salida para creación                                                                                      |
| `ReservaController`                                                | `@RestController` — base: `/api/v1/reservas`                                                                                |

---

### `finanzas`

Registro de ingresos y egresos del club.

**Herencia de entidades** — estrategia `SINGLE_TABLE` con columna discriminadora `tipo`:

```
Finanza (abstracta)
├── Ingreso   (tipo = "INGRESO")
└── Egreso    (tipo = "EGRESO")
```

`Ingreso` puede vincularse opcionalmente a una `Reserva` mediante `reservaId`.

| Clase                                      | Rol                                                              |
| ------------------------------------------ | ---------------------------------------------------------------- |
| `Finanza`                                  | Entidad base: fecha, importe, concepto, forma de pago, notas     |
| `Ingreso`                                  | Extiende Finanza con procedencia y referencia opcional a reserva |
| `Egreso`                                   | Extiende Finanza sin campos adicionales                          |
| `FinanzaRepository`                        | `JpaRepository<Finanza, Long>`                                   |
| `IFinanzaService` / `FinanzaService`       | Interfaz + implementación del servicio                           |
| `FinanzaRequestDto` / `FinanzaResponseDto` | DTOs de entrada y salida                                         |
| `FinanzaController`                        | `@RestController` — base: `/api/v1/finanzas`                     |

---

## Ubicación de los tests

Los tests se ubican **espejando la estructura de `src/main`** dentro de `src/test`. El test de una clase vive en el mismo paquete que esa clase, pero bajo el árbol de test.

**Ejemplo para `ClienteService`:**

```
src/main/java/com/cipolflo/server/clientes/service/ClienteService.java
src/test/java/com/cipolflo/server/clientes/service/ClienteServiceTest.java
```

**Ejemplo para `Reserva`:**

```
src/main/java/com/cipolflo/server/reservas/domain/Reserva.java
src/test/java/com/cipolflo/server/reservas/domain/ReservaTest.java
```

**Estructura completa esperada de tests:**

```
src/test/java/com/cipolflo/server/
├── ServerApplicationTests.java         ← smoke test (ya existe)
├── clientes/
│   ├── domain/
│   │   └── SocioTest.java              ← lógica de negocio del socio
│   └── service/
│       └── ClienteServiceTest.java
├── servicios/
│   └── service/
│       └── ServicioServiceTest.java
├── reservas/
│   ├── domain/
│   │   └── ReservaTest.java            ← máquina de estados
│   └── service/
│       └── ReservaServiceTest.java
└── finanzas/
    └── service/
        └── FinanzaServiceTest.java
```

Priorizar tests unitarios en la capa de dominio (sin Spring context) y tests de integración en la capa de servicio.

---

## Cómo implementar una nueva funcionalidad

Seguir este orden dentro del módulo correspondiente:

1. **Dominio** — agregar o modificar la entidad en `{modulo}/domain/`. Si hay enums nuevos, agregarlos en `{modulo}/domain/enums/`.
2. **Migración** — si el cambio implica modificar el schema (nueva tabla, columna, índice, etc.), crear la migración correspondiente antes de tocar la entidad. Ver la sección [Migraciones de base de datos](#migraciones-de-base-de-datos).
3. **Repository** — si se necesitan queries personalizadas, agregarlas en `{modulo}/repository/` extendiendo o anotando el repositorio existente.
4. **DTOs** — definir los campos de entrada y salida en `{modulo}/dto/`.
5. **Servicio** — declarar el método en `I{Modulo}Service` e implementarlo en `{Modulo}Service`.
6. **Controller** — exponer el endpoint en `{Modulo}Controller` mapeando DTO ↔ entidad.
7. **Test** — escribir el test en `src/test/.../{modulo}/` al mismo nivel que la clase testeada.

Si la funcionalidad involucra un concepto transversal a varios módulos (ej. un nuevo enum de estado, una nueva forma de pago), va en `shared/`.

---

## Antes de abrir un PR

Correr la skill de code review sobre la rama antes de crear el PR:

```
/code-review
```

La skill revisa convenciones, ejecuta los tests del módulo afectado y genera un archivo
`code_review_<nombre-rama>.md` en la raíz con los hallazgos. Si detecta errores, activa
automáticamente el flujo interactivo de sugerencias.

Los archivos `code_review_*.md` son locales y no deben pushearse. Agregar al `.gitignore` si no está ya.

---

## Endpoints disponibles

| Método                | Ruta                    | Módulo    | Estado                 |
| --------------------- | ----------------------- | --------- | ---------------------- |
| GET                   | `/api/health`           | shared    | Activo                 |
| GET, POST, PUT, PATCH | `/api/v1/clientes/**`   | clientes  | Activo                 |
| GET, POST, PUT, PATCH | `/api/v1/servicios/**`  | servicios | Activo                 |
| POST                  | `POST /api/v1/reservas` | reservas  | Activo — crear reserva |
| POST                  | `/api/v1/finanzas`      | finanzas  | Activo                 |
