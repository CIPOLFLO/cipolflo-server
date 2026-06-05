---
name: jira-ticket
description: Usá esta skill cuando el usuario pida crear, generar, redactar o escribir la descripción de un ticket de Jira (o "ticket para jira", "descripción de ticket", "ticket de desarrollo") para cualquier funcionalidad de este proyecto. Ejemplos de frases que deben activarla: "crea la descripción de un ticket jira para...", "generame un ticket para jira de...", "haceme el ticket de...", "redactame la descripción del ticket para...", "necesito un ticket de jira para..."
argument-hint: <descripción de la funcionalidad>
---

Generá una descripción completa de ticket de Jira en español para la siguiente funcionalidad:

$ARGUMENTS

## Paso 1 — Entender el contexto

Antes de escribir el ticket, leé los archivos relevantes para entender las convenciones del proyecto. Como mínimo:

- `src/main/java/com/cipolflo/server/servicios/controller/ServicioController.java` — convenciones de endpoints
- `src/main/java/com/cipolflo/server/servicios/service/ServicioService.java` — patrón de la capa de servicio
- `src/main/java/com/cipolflo/server/shared/exception/GlobalExceptionHandler.java` — manejo de excepciones
- `src/main/java/com/cipolflo/server/clientes/controller/ClienteController.java` — convenciones adicionales de endpoints
- `src/main/java/com/cipolflo/server/clientes/service/ClienteService.java` — patrón de servicio con repositorio

Si la funcionalidad involucra un dominio específico (clientes, reservas, servicios, finanzas), también leé la entidad de dominio, el repositorio, el servicio y el controller de ese módulo para entender los patrones existentes y qué ya está implementado.

Usá Grep y Glob para encontrar archivos relevantes que no conocés de antemano.

## Paso 2 — Diseñar antes de escribir

Antes de redactar el ticket, resolvé internamente:
- ¿Qué método HTTP corresponde a esta operación?
- ¿Qué capa/s necesitan cambios? (controller, service, repository, dominio, DTOs, excepciones)
- ¿Qué validaciones aplican?
- ¿Qué excepciones nuevas se necesitan, si alguna?
- ¿Qué casos de error deben cubrirse?
- ¿Qué está fuera del alcance de este ticket?

## Paso 3 — Escribir el ticket

Usá exactamente esta estructura:

---

## [Título de la funcionalidad]

**Tipo:** Story | **Módulo:** [nombre del módulo]

---

### Descripción

[Contexto de negocio y qué hace la funcionalidad. Explicá el por qué, no solo el qué. Si hay un caso de uso que la motiva (pantalla, flujo, etc.), mencionalo.]

---

### Criterios de aceptación

[Criterios concretos y verificables. Organizalos por escenario si aplica. Incluí siempre:]
- Respuestas HTTP esperadas por escenario (éxito, no encontrado, validación inválida, etc.)
- Reglas de validación de campos
- Reglas de negocio
- Comportamiento ante casos borde relevantes
- **La documentación de `api-reference` debe actualizarse para reflejar el nuevo endpoint.**

---

### Notas técnicas

[Detalle de implementación por capa. Incluí solo las capas que realmente tienen cambios. Usá subsecciones por capa:]

**Endpoint**
URL y método HTTP.

**Controller — `NombreController`**
Qué agregar o modificar. Anotaciones relevantes.

**Service — `INombreService` / `NombreService`**
Firma del método nuevo. Flujo de ejecución paso a paso. Indicar `@Transactional` si aplica.

**Repository — `NombreRepository`**
Query nueva si se necesita, con firma sugerida.

**Dominio — `NombreEntidad`**
Método de dominio nuevo si aplica (la lógica de negocio vive en el dominio, no en el servicio).

**DTOs**
Nombres de los DTOs nuevos y sus campos con validaciones. Justificá si se reutiliza un DTO existente o se crea uno nuevo.

**Excepciones**
Nuevas excepciones, nuevos códigos de error en el enum correspondiente, y el handler a agregar en `GlobalExceptionHandler`.

**Tests**
- `NombreControllerTest`: escenarios HTTP a cubrir.
- `NombreServiceTest`: caminos de lógica de negocio a cubrir.
- Cualquier clase utilitaria nueva tiene su propio test.

---

### Fuera de alcance

[Lo que explícitamente NO entra en este ticket. Siempre incluir al menos un ítem.]

---

## Convenciones del proyecto a aplicar

### Métodos HTTP
- `GET` — consultas; retorna `200` (o `404` si el recurso no se encuentra cuando se espera uno)
- `POST` — creación; retorna `201`
- `PUT` — actualización completa del recurso; retorna `200` con el recurso actualizado
- `PATCH` — operaciones parciales o de estado específico (ej. baja, habilitación); retorna `200` o `204`

### URLs
`/api/v1/{módulo}/{id?}/{sub-recurso?}`

Para operaciones sobre subtipos (ej. solo socios), usar sub-ruta explícita: `/api/v1/clientes/socios/{id}`.

### Controller
- Clase: `@RestController`, `@Validated`, `@RequestMapping("/api/v1/...")`
- IDs: `@PathVariable @Positive(message = "El id de X debe ser un número positivo")`
- Body: `@Valid @RequestBody`
- Filtros/query params: `@Valid @ModelAttribute`
- Seguridad: `@PreAuthorize("isAuthenticated()")` en todos los endpoints
- Retorno: `ResponseEntity<T>`

### Service
- Siempre definir interfaz `IXxxService` y su implementación `XxxService`
- Inyección por constructor (sin `@Autowired`)
- Operaciones de escritura: `@Transactional`
- La lógica de negocio pertenece al dominio, no al servicio

### Dominio
- Lógica de negocio encapsulada en métodos del dominio (ej. `entidad.cancelar()`, `entidad.darDeBaja()`)
- Factory methods para creación (ej. `Reserva.crear(...)`)
- Transiciones de estado validadas dentro del dominio

### Excepciones
- Una clase de excepción por caso de error (ej. `XxxNotFoundException`)
- Enum `XxxCodigoError` por módulo con los códigos de error
- Todo manejado en `GlobalExceptionHandler`
- `404` para no encontrado; `400` para validaciones y reglas de negocio violadas

### DTOs
- DTOs separados para request y response
- Crear un DTO específico por caso de uso; no reutilizar DTOs de otros contextos salvo que sean semánticamente idénticos
- No incluir info de auditoría en DTOs de uso específico (guardarla para el detalle completo)
- DTOs de request: anotaciones de Bean Validation en los campos

### Herencia SINGLE_TABLE (clientes)
- Distinguir subtipos con `instanceof` al buscar por id
- Si el id existe pero el tipo no coincide con lo esperado, retornar `404`
- Separar endpoints por subtipo cuando los campos o validaciones difieren entre `Socio` y `Particular`

### Paginación
- Usar `PageRequestDto` + `PageResponse<T>` para endpoints de listado
- Filtros como `XxxListadoRequestDto` con `@Valid @ModelAttribute`

### Validación de cédula
- La regex `^\d+(\.\d+)*(-\d+)?$` y la normalización (eliminar puntos y guiones) están en `ClienteSpecification`
- Si se necesita en otro contexto, extraer a una clase utilitaria `CedulaUtils`
