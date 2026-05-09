# cipolflo-server

Backend del sistema de gestión del Club CIPOLFLO. Administra clientes, socios, reservas, servicios y finanzas a través de una API REST.

---

## Stack

- **Java 21**
- **Spring Boot 3.x**
- **Spring Data JPA + Hibernate**
- **Spring Security**
- **PostgreSQL**
- **Gradle (Kotlin DSL)**
- **Lombok**

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

| Archivo | Se versiona | Contiene |
|---|---|---|
| `.env.example` | Sí | Plantilla con valores de ejemplo — sin credenciales reales |
| `.env` | **No** (está en `.gitignore`) | Tus credenciales reales — nunca commitear |

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

Postgres queda corriendo en `localhost:5433`. El schema se crea automáticamente la primera vez desde `docker/init.sql`.

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

### Comandos útiles

```bash
# Compilar
./gradlew build

# Compilar sin tests
./gradlew build -x test
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

| Clase / Enum | Rol |
|---|---|
| `AuditableEntity` | Superclase abstracta con `createdAt` y `updatedAt` automáticos. Todas las entidades la extienden. |
| `HealthController` | Endpoint `GET /api/health` para verificar que el servidor responde. |
| `FormaPago` | Enum: `EFECTIVO`, `TRANSFERENCIA`, `DEBITO`, `CREDITO` |
| `Procedencia` | Enum: `SEDE`, `CAMPING` |

---

### `clientes`

Gestión de clientes particulares y socios del club.

**Herencia de entidades** — estrategia `SINGLE_TABLE` con columna discriminadora `tipo`:

```
Cliente (abstracta)
├── Particular   (tipo = "PARTICULAR")
└── Socio        (tipo = "SOCIO")
```

`Socio` incluye lógica de negocio: control de meses sin pagar, cambio automático de estado a `INACTIVO` al superar 3 meses, y baja.

| Clase | Rol |
|---|---|
| `Cliente` | Entidad base: cédula, nombre, teléfono, mail, notas |
| `Particular` | Extiende Cliente sin campos adicionales |
| `Socio` | Extiende Cliente con número de socio, fecha de nacimiento, estado, domicilio, cuotas |
| `PagoCuota` | Registro de pago de cuota mensual de un socio |
| `EstadoSocio` | Enum: `AL_DIA`, `INACTIVO`, `DE_BAJA` |
| `MetodoCobro` | Enum: `EN_SEDE`, `DESCUENTO_SALARIO`, `TRANSFERENCIA` |
| `ClienteRepository` | `JpaRepository<Cliente, Long>` |
| `IClienteService` / `ClienteService` | Interfaz + implementación del servicio |
| `ClienteRequestDto` / `ClienteResponseDto` | DTOs de entrada y salida |
| `ClienteController` | `@RestController` — base: `/api/v1/clientes` |

---

### `servicios`

Catálogo de servicios que el club ofrece (actividades, instalaciones, etc.).

| Clase | Rol |
|---|---|
| `Servicio` | Entidad: nombre, procedencia, precio por tipo de cliente, modalidad, capacidad, habilitado |
| `ModalidadPrecio` | Enum: `POR_DIA`, `POR_PERSONA`, `POR_DIA_POR_PERSONA`, `POR_UNIDAD`, `POR_HORA` |
| `ServicioRepository` | `JpaRepository<Servicio, Long>` |
| `IServicioService` / `ServicioService` | Interfaz + implementación del servicio |
| `ServicioRequestDto` / `ServicioResponseDto` | DTOs de entrada y salida |
| `ServicioController` | `@RestController` — base: `/api/v1/servicios` |

---

### `reservas`

Gestión del ciclo de vida de las reservas con máquina de estados.

**Flujo de estados:**

```
PENDIENTE → CONFIRMADA → EN_CURSO → FINALIZADA
```

La reserva pasa a `CONFIRMADA` automáticamente cuando se registran tanto el pago como la documentación. La entidad usa un **factory method estático** `Reserva.crear(...)` en lugar de constructor público.

| Clase | Rol |
|---|---|
| `Reserva` | Entidad central: cliente, servicio, fechas, importe, estado, pago, documentación |
| `EstadoReserva` | Enum: `PENDIENTE`, `CONFIRMADA`, `EN_CURSO`, `FINALIZADA`, `CANCELADA` |
| `ReservaRepository` | `JpaRepository<Reserva, Long>` |
| `IReservaService` / `ReservaService` | Interfaz + implementación del servicio |
| `ReservaRequestDto` / `ReservaResponseDto` | DTOs de entrada y salida |
| `ReservaController` | `@RestController` — base: `/api/v1/reservas` |

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

| Clase | Rol |
|---|---|
| `Finanza` | Entidad base: fecha, importe, concepto, forma de pago, notas |
| `Ingreso` | Extiende Finanza con procedencia y referencia opcional a reserva |
| `Egreso` | Extiende Finanza sin campos adicionales |
| `FinanzaRepository` | `JpaRepository<Finanza, Long>` |
| `IFinanzaService` / `FinanzaService` | Interfaz + implementación del servicio |
| `FinanzaRequestDto` / `FinanzaResponseDto` | DTOs de entrada y salida |
| `FinanzaController` | `@RestController` — base: `/api/v1/finanzas` |

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
2. **Repository** — si se necesitan queries personalizadas, agregarlas en `{modulo}/repository/` extendiendo o anotando el repositorio existente.
3. **DTOs** — definir los campos de entrada y salida en `{modulo}/dto/`.
4. **Servicio** — declarar el método en `I{Modulo}Service` e implementarlo en `{Modulo}Service`.
5. **Controller** — exponer el endpoint en `{Modulo}Controller` mapeando DTO ↔ entidad.
6. **Test** — escribir el test en `src/test/.../{modulo}/` al mismo nivel que la clase testeada.

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

| Método | Ruta | Módulo | Estado |
|---|---|---|---|
| GET | `/api/health` | shared | Activo |
| * | `/api/v1/clientes/**` | clientes | Por implementar |
| * | `/api/v1/servicios/**` | servicios | Por implementar |
| * | `/api/v1/reservas/**` | reservas | Por implementar |
| * | `/api/v1/finanzas/**` | finanzas | Por implementar |
