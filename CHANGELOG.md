# Changelog

Todos los cambios notables en este proyecto serán documentados en este archivo.

## [0.11.0] - 2026-10-20

### Added
- feat(hexagonal): Migración completa al slice único `um.tesoreria.mercadopago.hexagonal.mercadopago` con los subpaquetes `datos` (`chequeraCuota`, `contexto`, `tipoChequeraTarjeta`), `gateways` (`mercadoPago`, `mensajeria`), `procesos` (`checking`, `chequera`, `pago`, `preferenciaCuota`, `preferenciaVacante`) y `shared` (modelo, servicios, DTOs y mappers). Cada slice declara `domain/model`, `domain/ports/in`, `domain/ports/out`, `application/usecases` e `infrastructure` (fuente: `src/main/java/um/tesoreria/mercadopago/hexagonal/mercadopago/**`, `git diff HEAD`)
- feat(datos): Nuevos puertos de salida `ContextoRepository`, `ChequeraCuotaRepository` y `TipoChequeraTarjetaRepository`, implementados por `ContextoClientAdapter`, `ChequeraCuotaClientAdapter` y `TipoChequeraTarjetaClientAdapter` sobre los clientes Feign `ContextoCoreClient`, `ContextoQueryClient`, `ChequeraCuotaClient` y `TipoChequeraMercadoPagoCreditCardClient` (fuente: `src/main/java/um/tesoreria/mercadopago/hexagonal/mercadopago/datos/**`, `git diff HEAD`)
- feat(gateways): `MercadoPagoService` expone los casos de uso `CrearPreferenciaUseCase`, `ActualizarPreferenciaUseCase`, `RecuperarPreferenciaUseCase` y `RecuperarPagoUseCase` sobre el SDK 3.2.1, con los mappers `PreferenciaMapper`, `PagoMapper` y `SolicitudPreferenciaMapper` y la excepción `MercadoPagoException` (status + respuesta de la API) (fuente: `src/main/java/um/tesoreria/mercadopago/hexagonal/mercadopago/gateways/mercadoPago/**`, `git diff HEAD`)
- feat(mensajeria): `MensajeriaService`, `PublicarPagoProcesadoUseCaseImpl` y `PaymentEventProducerAdapter` publican `PagoProcesadoEvento` en el tópico `payment-processed` usando `paymentId` como clave (fuente: `src/main/java/um/tesoreria/mercadopago/hexagonal/mercadopago/gateways/mensajeria/**`, `KafkaContractTest.java`, `git diff HEAD`)
- feat(shared): Nuevo modelo de dominio `ContextoMercadoPago`, `ChequeraCuota`, `PreferenciaMercadoPago`, `PagoMercadoPago`, `ReservaVacante`, `SolicitudPreferencia`, `TipoChequeraTarjeta`, `ItemPreferencia`, `PagadorPreferencia`, `MediosPagoPreferencia`, `UrlsRetornoPreferencia` y `ContextoConCuota`, junto con `PreferenciaRequestBuilder` y `FechasMercadoPago` como servicios compartidos (fuente: `src/main/java/um/tesoreria/mercadopago/hexagonal/mercadopago/shared/**`, `git diff HEAD`)
- feat(pago): La verificación de firma del webhook se modela en dominio con `CabecerasWebhook`, `ComponentesFirma` y `FirmaWebhook`, y la referencia externa con `ReferenciaExterna.parse(...)` (fuente: `src/main/java/um/tesoreria/mercadopago/hexagonal/mercadopago/procesos/pago/domain/model/**`, `WireFormatContractTest.java`, `git diff HEAD`)
- test(contrato): Nuevos tests `ArquitecturaHexagonalTest`, `HttpContractTest` (9 endpoints), `FeignClientsContractTest`, `KafkaContractTest`, `PreferenceRequestContractTest`, `SchedulingContractTest` y `WireFormatContractTest`, además de tests por caso de uso en `hexagonal/**` (fuente: `src/test/java/um/tesoreria/mercadopago/**`, `git diff HEAD`)

### Changed
- refactor(paquetes): Eliminados los paquetes `client`, `controller`, `service`, `domain`, `producer`, `scheduled`, `util` y `hexagonal.preference`; `MercadoPagoApplication` conserva el escaneo base `um.tesoreria.mercadopago` (fuente: `git status`, `ArquitecturaHexagonalTest.losPaquetesLegadosYaNoExisten`)
- refactor(config): `@EnableFeignClients` ahora escanea `um.tesoreria.mercadopago.hexagonal` en lugar de `um.tesoreria.mercadopago.client` (fuente: `src/main/java/um/tesoreria/mercadopago/configuration/MercadoPagoConfiguration.java`, `git diff HEAD`)
- refactor(persistencia): `CrearPreferenciaCuotaUseCaseImpl` y `ActualizarPreferenciaCuotaUseCaseImpl` dejaron de escribir en el core; la persistencia queda a cargo de `ChequearCuotaUseCaseImpl` (un `updateContext`), `CrearContextosChequeraUseCaseImpl` (un `updateContext` por cuota) y `CrearContextosChequeraBulkUseCaseImpl` (un `updateContexts` batch), mientras que el endpoint `POST /preference/create` usa `PreferenciaCuotaService.crearYGuardar`. El detalle del diseño está en `docs/persistencia-contexto-mercadopago.md` (fuente: `src/main/java/um/tesoreria/mercadopago/hexagonal/mercadopago/procesos/**`, `docs/persistencia-contexto-mercadopago.md`, `git diff HEAD`)
- refactor(kafka): El productor usa un `JacksonJsonSerializer` explícito con `setAddTypeInfo(false)`, por lo que el evento ya no se publica con el header `__TypeId__`; se conservan el tópico `payment-processed`, la clave `paymentId` y las propiedades `acks=all` y `retries=3` (fuente: `src/main/java/um/tesoreria/mercadopago/configuration/KafkaConfiguration.java`, `KafkaContractTest.java`, `git diff HEAD`)
- refactor(controller): Los controladores reciben y devuelven DTOs, delegan en los servicios de `procesos` y usan `PreferenciaDtoMapper`/`ContextoMercadoPagoDtoMapper`, manteniendo las mismas rutas y verbos HTTP (fuente: `src/main/java/um/tesoreria/mercadopago/hexagonal/mercadopago/procesos/*/infrastructure/web/controller/**`, `HttpContractTest.java`, `git diff HEAD`)
- refactor(config): `app.secret-key` pasa a `${app.secret-key:}` para permitir el arranque sin la propiedad; una firma inválida se sigue registrando con `log.warn` y no aborta el webhook (fuente: `src/main/java/um/tesoreria/mercadopago/hexagonal/mercadopago/procesos/pago/application/usecases/ProcesarWebhookPagoUseCaseImpl.java`, `git diff HEAD`)

### Fixed
- fix(preferencia): `POST /api/tesoreria/mercadopago/preference/create` ahora persiste el contexto cuando llega con `changed == 1`, guardando `changed = 0` y `lastVencimientoUpdated`; antes ese camino no escribía en el core (fuente: `src/main/java/um/tesoreria/mercadopago/hexagonal/mercadopago/procesos/preferenciaCuota/application/service/PreferenciaCuotaService.java`, `PreferenciaCuotaServiceTest.java`, `git diff HEAD`)
- fix(persistencia): Los errores del core al persistir se registran (`Error updating context in Core...`) y ya no abortan el flujo de `checking`, chequera individual ni bulk (fuente: `ChequearCuotaUseCaseImpl.java`, `CrearContextosChequeraBulkUseCaseImpl.java`, `docs/persistencia-contexto-mercadopago.md`, `git diff HEAD`)
- fix(duplicación): Eliminada la doble escritura del contexto al crear una preferencia de cuota (de 2 PUT a 1 PUT en el flujo unitario y de N + 1 escrituras a un único batch en el flujo bulk) (fuente: `docs/persistencia-contexto-mercadopago.md`, `git diff HEAD`)

### Removed
- refactor: Eliminados `PaymentService`, el `PreferenceService` monolítico, `PaymentEventProducer`, `DateToolMP`, `ChequeraPagoDto` y el test suelto `src/test/java/SignatureTest.java` (fuente: `git status`, `git diff HEAD`)
- refactor(dto): Los modelos de vacante pasan de `ReservaVacante`/`Campanha` a los DTOs `ReservaVacanteDto`/`CampanhaDto`, y todos los DTOs se reubican en `shared.infrastructure.dto` con mappers dedicados (fuente: `src/main/java/um/tesoreria/mercadopago/hexagonal/mercadopago/shared/infrastructure/**`, `git diff HEAD`)

### Documentation
- docs: Nuevo `docs/persistencia-contexto-mercadopago.md` con la política de persistencia de contextos y las escrituras por flujo, enlazado desde el README (fuente: `docs/persistencia-contexto-mercadopago.md`, `README.md`, `git diff HEAD`)
- docs(diagramas): Actualizados los diagramas de secuencia de cuota, vacante y bulk a los nuevos casos de uso; `modelo-datos-core.mmd` elimina `ChequeraPagoDto` y renombra sus entidades a `ReservaVacanteDto` y `CampanhaDto`; nuevo `arquitectura-hexagonal.mmd` registrado en `generate-docs.yml` con escapado del contenido antes de inyectarlo con `innerHTML` (fuente: `docs/diagrams/**`, `.github/workflows/generate-docs.yml`, `git diff HEAD`)

### Breaking Changes
- Cambian los paquetes de todas las clases: cualquier consumidor Java que importe `um.tesoreria.mercadopago.client`, `.controller`, `.service`, `.domain`, `.producer`, `.scheduled`, `.util` o `.hexagonal.preference` debe migrar a `um.tesoreria.mercadopago.hexagonal.mercadopago.*`.
- `PaymentProcessedEvent` se mueve a `...gateways.mensajeria.infrastructure.kafka.event` y el contrato Kafka deja de enviar el header `__TypeId__`; los consumidores deben deserializar con el tipo target en lugar de depender del header.
- Los endpoints REST no cambian de ruta, verbo ni forma de payload.

## [0.10.1] - 2026-10-20

### Added
- ci(deploy): Nuevos workflows `deploy-develop.yml` y `deploy-staging.yml` que verifican la aplicación con `mvn -B verify`, construyen y publican la imagen Docker etiquetada con el SHA del commit y despliegan en runners self-hosted. El workflow de staging despliega en matriz sobre los entornos `staging` y `staging-new` (fuente: `.github/workflows/deploy-develop.yml`, `.github/workflows/deploy-staging.yml`, `git diff HEAD`)

### Changed
- chore(deps): Actualización de Spring Boot de 4.1.0 a 4.1.1 (fuente: `pom.xml`, `git diff HEAD`)
- chore(deps): Actualización de Spring Cloud de 2025.1.2 a 2025.1.3 (fuente: `pom.xml`, `git diff HEAD`)

### Fixed
- fix(logging): El nivel de log configurado en `bootstrap.yml` ahora aplica al paquete base `um.tesoreria.mercadopago` en lugar del paquete obsoleto `um.tesoreria.mercadopago.service`, restaurando el logging tras la migración de paquetes de la versión 0.7.0 (fuente: `src/main/resources/bootstrap.yml`, `git diff HEAD`)
- docs(diagramas): `despliegue.mmd` migrado de la sintaxis inválida `deploymentDiagram` a `flowchart TD`; `modelo-datos-core.mmd` ajustado para expresar las relaciones entre DTOs mediante relaciones ER en lugar de atributos de tipo objeto y se eliminó el atributo inexistente `mercadoPagoContextId` de `UMPreferenceMPDto` (fuente: `docs/diagrams/despliegue.mmd`, `docs/diagrams/modelo-datos-core.mmd`, `src/main/java/um/tesoreria/mercadopago/domain/dto/UMPreferenceMPDto.java`, `git diff HEAD`)
- docs(readme): Corregidas las referencias a Spring Boot (4.1.0 → 4.1.1) para que coincidan con `pom.xml` (fuente: `README.md`, `pom.xml`)
- docs(changelog): Restaurada la versión de Spring Cloud registrada para la release 0.6.0 (2025.1.2), coherente con el `pom.xml` de esa versión (fuente: `git show 8e5caa6:pom.xml`)

## [0.10.0] - 2026-07-19

### Added
- feat(chequera): Nuevo endpoint `GET /api/tesoreria/mercadopago/chequera/create/context/bulk/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}` para creación masiva de preferencias de pago de cuotas pendientes. El endpoint obtiene todos los contextos de una chequera del core-service en una sola llamada, crea las preferencias de MercadoPago y actualiza los contextos en batch, optimizando el rendimiento frente al endpoint existente que procesa cuota por cuota (fuente: `src/main/java/um/tesoreria/mercadopago/controller/ChequeraController.java:26-29`, `src/main/java/um/tesoreria/mercadopago/service/ChequeraService.java:51-80`, `git diff HEAD`)
- feat(client): Nuevos métodos Feign `makeContextsChequera()` (GET) y `updateContexts()` (PUT) en `MercadoPagoCoreClient` para soportar operaciones batch contra el core-service (fuente: `src/main/java/um/tesoreria/mercadopago/client/core/MercadoPagoCoreClient.java:19-31`, `git diff HEAD`)

### Changed
- refactor(chequera): `ChequeraService.createChequeraContextBulk()` acumula todos los contextos actualizados y realiza una única llamada `updateContexts()` al core-service, a diferencia del flujo existente que actualiza contexto por contexto (fuente: `src/main/java/um/tesoreria/mercadopago/service/ChequeraService.java:51-80`, `git diff HEAD`)

## [0.9.0] - 2026-07-05

### Added
- feat(payment): `reservaVacanteId` (UUID) en `PaymentProcessedEvent` para identificar pagos de reserva de vacantes en eventos publicados a Kafka (fuente: `src/main/java/um/tesoreria/mercadopago/domain/event/PaymentProcessedEvent.java:20`, `git diff HEAD`)
- feat(payment): Nuevo formato de `externalReference` de 6 partes (`UUID-mercadoPagoContextId`) en `PaymentService.parseExternalReference()` para soportar external references UUID del flujo de vacantes. El método distingue entre formato UUID (6 partes, para vacantes) y formato numérico (2-3 partes, para cuotas). Añadido `reservaVacanteId` al record `PaymentReferenceData` y propagado al evento `PaymentProcessedEvent` (fuente: `src/main/java/um/tesoreria/mercadopago/service/PaymentService.java:184-215`, `src/main/java/um/tesoreria/mercadopago/service/PaymentService.java:246`, `git diff HEAD`)

### Changed
- refactor(logging): Mejorados mensajes de log en `parseExternalReference()` para distinguir entre referencias de cuota y vacante (fuente: `src/main/java/um/tesoreria/mercadopago/service/PaymentService.java:195,205-207`, `git diff HEAD`)

## [0.8.2] - 2026-06-29

### Fixed
- fix(service): Corregido `parseExternalReference()` en `PaymentService` para aceptar external references de formato variable (2 o 3 partes). El método ahora soporta el formato corto `chequeraCuotaId-mercadoPagoContextId` (usado en flujo de cuotas) y el formato largo `prefijo-chequeraCuotaId-mercadoPagoContextId` (usado en flujo de vacantes). Eliminados casts redundantes `(Long)` en el parseo. (fuente: `src/main/java/um/tesoreria/mercadopago/service/PaymentService.java:183-203`, `git diff HEAD`)

## [0.8.1] - 2026-06-29

### Fixed
- fix(service): Simplificada llamada `client.get()` eliminando wrapper redundante `Long.valueOf()` en `PaymentService.retrieveAndPublishPayment()` (fuente: `src/main/java/um/tesoreria/mercadopago/service/PaymentService.java:100`, `git diff HEAD`)

### Changed
- refactor(service): Mejorado logging en `processPaymentContext()` y `parseExternalReference()` de `PaymentService` para depuración de externalReference, incluyendo datos crudos, número de partes esperadas y mensaje de error detallado (fuente: `src/main/java/um/tesoreria/mercadopago/service/PaymentService.java:174-199`, `git diff HEAD`)

## [0.8.0] - 2026-06-19

### Added
- feat(preference): Nuevo parámetro `sinRestricciones` en `PreferenceService.buildPreferenceRequest()` y `createPaymentMethodsRequest()`. Cuando es `true`, genera preferencias sin exclusiones de medios de pago (sin límite de cuotas, sin excluir ticket/prepaid_card) (fuente: `src/main/java/um/tesoreria/mercadopago/hexagonal/preference/service/PreferenceService.java`, `git diff HEAD`)
- feat(vacante): `PreferenceVacanteService` ahora crea preferencias sin restricciones de medios de pago (`sinRestricciones=true`) para el flujo de reserva de vacantes (fuente: `src/main/java/um/tesoreria/mercadopago/hexagonal/preference/vacante/application/service/PreferenceVacanteService.java`, `git diff HEAD`)

### Changed
- refactor(cuota): `PreferenceCuotaService` actualizado para usar `sinRestricciones=false`, preservando el comportamiento existente con exclusiones de medios de pago (fuente: `src/main/java/um/tesoreria/mercadopago/hexagonal/preference/cuota/application/service/PreferenceCuotaService.java`, `git diff HEAD`)

## [0.7.1] - 2026-06-16

### Fixed
- fix(service): Corregido `createExternalReference` en `PreferenceService` para usar `chequeraCuotaId` en el flujo de cuotas y `reservaVacanteId` en el flujo de vacantes (fuente: `src/main/java/um/tesoreria/mercadopago/hexagonal/preference/service/PreferenceService.java:124-132`, `git diff HEAD`)

## [0.7.0] - 2026-06-15

### Added
- feat(hexagonal): Nueva arquitectura hexagonal para gestión de preferencias con separación en `PreferenceCuotaService` (cuotas) y `PreferenceVacanteService` (vacantes) (fuente: `src/main/java/um/tesoreria/mercadopago/hexagonal/**`, `git diff HEAD`)
- feat(vacante): Nuevo endpoint `POST /api/tesoreria/mercadopago/vacante/preference/create` para creación de preferencias de reserva de vacantes (fuente: `src/main/java/um/tesoreria/mercadopago/hexagonal/preference/vacante/**`, `git diff HEAD`)
- feat(vacante): Nuevos modelos de dominio `ReservaVacante` y `Campanha` para el flujo de reserva de vacantes (fuente: `src/main/java/um/tesoreria/mercadopago/hexagonal/preference/vacante/domain/model/**`, `git diff HEAD`)
- feat(dto): Nuevo campo `reservaVacanteId` (UUID) en `MercadoPagoContextDto` para vincular contexto de pago con reserva de vacante (fuente: `src/main/java/um/tesoreria/mercadopago/domain/dto/MercadoPagoContextDto.java`, `git diff HEAD`)
- feat(dto): Nuevos campos `chequeraCuota` y `reservaVacante` en `MercadoPagoContextDto` (fuente: `src/main/java/um/tesoreria/mercadopago/domain/dto/MercadoPagoContextDto.java`, `git diff HEAD`)
- feat(dto): Nuevo campo `reservaVacante` en `UMPreferenceMPDto` (fuente: `src/main/java/um/tesoreria/mercadopago/domain/dto/UMPreferenceMPDto.java`, `git diff HEAD`)
- feat(kafka): Cambio de serializador Kafka de `JsonSerializer` a `JacksonJsonSerializer` (fuente: `src/main/java/um/tesoreria/mercadopago/configuration/KafkaConfiguration.java`, `git diff HEAD`)
- feat(dto): Añadidas anotaciones `@Builder.Default` en múltiples DTOs para valores por defecto en constructores builder (ArancelTipoDto, ChequeraCuotaDto, ChequeraSerieDto, ClaseChequeraDto, DomicilioDto, FacultadDto, GeograficaDto, LectivoDto, PersonaDto, ProductoDto) (fuente: `src/main/java/um/tesoreria/mercadopago/domain/dto/*.java`, `git diff HEAD`)
- feat(service): Actualización de `externalReference` para incluir `reservaVacanteId` en el nuevo flujo de preferencias (fuente: `src/main/java/um/tesoreria/mercadopago/hexagonal/preference/service/PreferenceService.java`, `git diff HEAD`)

### Changed
- refactor(package): Migración masiva del paquete base de `um.tesoreria.mercadopago.service` a `um.tesoreria.mercadopago` — todos los controladores, servicios, clientes, configuraciones, DTOs, eventos y utilidades reubicados (fuente: `git diff HEAD`, todos los archivos `.java`)
- refactor(service): `PreferenceService` monolítico original dividido en tres clases especializadas:
  - `PreferenceCuotaService` — lógica de preferencias para cuotas
  - `PreferenceVacanteService` — lógica de preferencias para vacantes
  - `PreferenceService` (hexagonal) — servicios comunes compartidos (buildPreferenceRequest, createAndLogPreference, etc.)
  (fuente: `src/main/java/um/tesoreria/mercadopago/hexagonal/**`, `git diff HEAD`)
- refactor(controller): `PreferenceController` reemplazado por `PreferenceCuotaController` con `@RequiredArgsConstructor` (fuente: `src/main/java/um/tesoreria/mercadopago/hexagonal/preference/cuota/infrastructure/web/controller/PreferenceCuotaController.java`, `git diff HEAD`)
- refactor(service): `CheckingService` y `ChequeraService` ahora inyectan `PreferenceCuotaService` en lugar del antiguo `PreferenceService` (fuente: `src/main/java/um/tesoreria/mercadopago/service/*.java`, `git diff HEAD`)
- refactor(config): `@EnableFeignClients` actualizado de `um.tesoreria.mercadopago.service.client` a `um.tesoreria.mercadopago.client` (fuente: `src/main/java/um/tesoreria/mercadopago/configuration/MercadoPagoConfiguration.java`, `git diff HEAD`)

### Removed
- refactor(controller): Eliminado `PreferenceController` antiguo (fuente: `src/main/java/um/tesoreria/mercadopago/service/controller/PreferenceController.java`, `git diff HEAD`)
- refactor(service): Eliminado `PreferenceService` monolítico antiguo (fuente: `src/main/java/um/tesoreria/mercadopago/service/service/PreferenceService.java`, `git diff HEAD`)

### Breaking Changes
- Todos los imports de `um.tesoreria.mercadopago.service.*` deben actualizarse a `um.tesoreria.mercadopago.*`. Cualquier código externo que importe clases del paquete `service` se verá afectado.
- `PreferenceService` ha sido reemplazado por `PreferenceCuotaService` (para cuotas) y `PreferenceVacanteService` (para vacantes).
- El endpoint `POST /api/tesoreria/mercadopago/preference/create` ahora es manejado por `PreferenceCuotaController` en lugar del antiguo `PreferenceController`.

## [0.6.1] - 2026-06-15

### Changed
- chore(deps): Actualización de MercadoPago SDK de 2.8.0 a 3.2.1 (fuente: `pom.xml`, `git diff HEAD`)
- chore(deps): Actualización de SpringDoc OpenAPI de 3.0.2 a 3.0.3 (fuente: `pom.xml`, `git diff HEAD`)
- chore(deps): Actualización de plexus-utils de 4.0.2 a 4.0.3 (fuente: `pom.xml`, `git diff HEAD`)

## [0.6.0] - 2026-06-14

### Changed
- chore(deps): Actualización de Spring Boot de 4.0.5 a 4.1.0 (fuente: `pom.xml`, `git diff HEAD`)
- chore(deps): Actualización de Spring Cloud de 2025.1.0 a 2025.1.2 (fuente: `pom.xml`, `git diff HEAD`)
- refactor(dto): Migración de `@Data` a `@Getter` + `@Setter` + `@Builder` en todos los DTOs para usar patrón builder explícito (fuente: `src/main/java/um/tesoreria/mercadopago/service/domain/dto/*.java`, `git diff HEAD`)
- fix(dto): Corrección de formato ISO 8601 en campos `OffsetDateTime` — cambio de patrón `Z` (RFC 822, ej: `+0000`) a `XX` (ISO 8601, ej: `+00:00`) (fuente: `src/main/java/um/tesoreria/mercadopago/service/domain/dto/*.java`, `git diff HEAD`)
- chore(actions): Actualización de `actions/upload-pages-artifact@v3` a `v4` (fuente: `.github/workflows/generate-docs.yml`, `git diff HEAD`)
- chore(banner): Limpieza del banner de la aplicación (fuente: `src/main/resources/banner.txt`, `git diff HEAD`)

### Breaking Changes
- Los DTOs ya no incluyen `toString()`, `equals()` y `hashCode()` generados por `@Data`. Ahora usan `@Getter` + `@Setter` + `@Builder`.
- El formato de fecha en JSON cambió de `yyyy-MM-dd'T'HH:mm:ssZ` (ej: `2026-06-14T12:00:00+0000`) a `yyyy-MM-dd'T'HH:mm:ssXX` (ej: `2026-06-14T12:00:00+00:00`). Los consumidores deben actualizar sus parsers.

## [0.5.1] - 2026-04-04

### Changed
- chore(deps): Actualización de Spring Boot de 4.0.2 a 4.0.5 (fuente: `pom.xml`, `git diff HEAD`)
- chore(deps): Actualización de SpringDoc OpenAPI de 3.0.1 a 3.0.2 (fuente: `pom.xml`, `git diff HEAD`)
- chore(deps): Añadida dependencia plexus-utils 4.0.2 para resolución de vulnerabilidades (fuente: `pom.xml`, `git diff HEAD`)
- chore(actions): Actualización de GitHub Actions a versiones recientes:
  - actions/checkout@v4 → v6
  - actions/setup-java@v4 → v5
  - actions/cache@v4 → v5
  - actions/deploy-pages@v4 → v5
  - docker/login-action@v3 → v4
  - docker/metadata-action@v5 → v6
  - docker/setup-buildx-action@v3 → v4
  - docker/build-push-action@v6 → v7

### Fixed
- fix(service): Corrección de tipado en PaymentService - cambio de `Long.parseLong` a `Long.valueOf(Long.parseLong(...))` para compatibilidad (fuente: `src/main/java/um/tesoreria/mercadopago/service/service/PaymentService.java:100`, `git diff HEAD`)
- fix(service): Corrección de cast en PaymentReferenceData para parseo de externalReference (fuente: `src/main/java/um/tesoreria/mercadopago/service/service/PaymentService.java:187-192`, `git diff HEAD`)

## [0.5.0] - 2026-03-14

### Added
- feat(dto): Nuevos campos de gestión de becas en `ChequeraSerieDto` (fuente: `src/main/java/um/tesoreria/mercadopago/service/domain/dto/ChequeraSerieDto.java`, `git diff HEAD`)
  - `hpum` (Byte): Indicador de Hilfe Programm UM
  - `becaPorcentaje` (BigDecimal): Porcentaje de beca
  - `becaResolucion` (String): Resolución de la beca
  - `becaFecha` (OffsetDateTime): Fecha de otorgamiento de beca
  - `becaUserId` (Long): ID del usuario que otorga la beca

### Changed
- refactor(service): Comented verification of signature in PaymentService for testing purposes (fuente: `src/main/java/um/tesoreria/mercadopago/service/service/PaymentService.java`, `git diff HEAD`)

### Removed
- refactor(controller): Eliminado endpoint `/faltantes` de `PaymentController` (fuente: `src/main/java/um/tesoreria/mercadopago/service/controller/PaymentController.java`, `git diff HEAD`)
- refactor(service): Eliminado método `fillFaltantes()` de `PaymentService` (fuente: `src/main/java/um/tesoreria/mercadopago/service/service/PaymentService.java`, `git diff HEAD`)

### Breaking Changes
- El endpoint `GET /faltantes` ha sido eliminado. Los métodos asociados `getIdsPart1()` a `getIdsPart8()` también han sido eliminados.

## [0.4.1] - 2026-02-05

### Fixed
- fix(service): Restablecida validación de firma en PaymentService (fuente: `src/main/java/um/tesoreria/mercadopago/service/service/PaymentService.java`, `git diff HEAD`)

### Changed
- refactor(service): Añadido logging de debug para UMPreferenceMPDto en PreferenceService (fuente: `src/main/java/um/tesoreria/mercadopago/service/service/PreferenceService.java`, `git diff HEAD`)

## [0.4.0] - 2026-02-03

### Fixed
- fix(service): Eliminada lógica de envío de cuota para `personaId` específico en `PreferenceService` (fuente: `src/main/java/um/tesoreria/mercadopago/service/service/PreferenceService.java`, `git diff HEAD`)

### Changed
- refactor(dto): Añadidas anotaciones `@Builder.Default` a campos en `MercadoPagoContextDto` y `TipoChequeraDto` para inicialización por defecto (fuente: `src/main/java/um/tesoreria/mercadopago/service/domain/dto/MercadoPagoContextDto.java`, `src/main/java/um/tesoreria/mercadopago/service/domain/dto/TipoChequeraDto.java`, `git diff HEAD`)
- refactor(logging): Ajustes en mensajes de logging en `ChequeraService` y `PreferenceService` (fuente: `src/main/java/um/tesoreria/mercadopago/service/service/ChequeraService.java`, `src/main/java/um/tesoreria/mercadopago/service/service/PreferenceService.java`, `git diff HEAD`)
- chore(deps): Actualización de Spring Boot de 3.5.8 a 4.0.2 (fuente: `pom.xml`, `git diff HEAD`)
- chore(deps): Actualización de Spring Cloud de 2025.0.0 a 2025.1.0 (fuente: `pom.xml`, `git diff HEAD`)
- chore(deps): Actualización de MercadoPago SDK de 2.5.0 a 2.8.0 (fuente: `pom.xml`, `git diff HEAD`)
- chore(deps): Actualización de SpringDoc OpenAPI de 2.8.10 a 3.0.1 (fuente: `pom.xml`, `git diff HEAD`)
- chore(deps): Actualización de commons-fileupload de 1.5 a 1.6.0 (fuente: `pom.xml`, `git diff HEAD`)
- chore(deps): Actualización de commons-beanutils de 1.9.4 a 1.11.0 (fuente: `pom.xml`, `git diff HEAD`)
- chore(deps): Actualización de commons-lang3 de 3.18.0 a 3.20.0 (fuente: `pom.xml`, `git diff HEAD`)

## [0.3.0] - 2025-11-14

### Added
- feat(client): nuevo cliente `ChequeraClient` para envío de notificaciones de cuota (fuente: `src/main/java/um/tesoreria/mercadopago/service/client/sender/ChequeraClient.java`, `git diff HEAD`)
- feat(scheduled): nueva tarea programada `CheckingScheduled` para verificación diaria automática (fuente: `src/main/java/um/tesoreria/mercadopago/service/scheduled/CheckingScheduled.java`, `git diff HEAD`)
- feat(service): lógica adicional en `PreferenceService` para envío automático de cuota basado en personaId específico (fuente: `src/main/java/um/tesoreria/mercadopago/service/service/PreferenceService.java`, `git diff HEAD`)

### Changed
- refactor(client): actualización de configuración Feign en clientes core para usar `contextId` y `path` separados (fuente: `src/main/java/um/tesoreria/mercadopago/service/client/core/*`, `git diff HEAD`)
- refactor(controller): migración a `@RequiredArgsConstructor` en `CheckingController` (fuente: `src/main/java/um/tesoreria/mercadopago/service/controller/CheckingController.java`, `git diff HEAD`)
- refactor(service): migración a `@RequiredArgsConstructor` en `CheckingService` y mejoras en logging (fuente: `src/main/java/um/tesoreria/mercadopago/service/service/CheckingService.java`, `git diff HEAD`)

## [0.2.0] - 2025-10-28

### Added
- feat(util): nueva clase `Jsonifier` para centralización de serialización JSON con configuración pretty-print (fuente: `src/main/java/um/tesoreria/mercadopago/service/util/Jsonifier.java`, `git diff HEAD`)
- feat(logging): mejoras en logging con prefijos de clase y uso de `Jsonifier` en servicios (fuente: `src/main/java/um/tesoreria/mercadopago/service/service/*`, `git diff HEAD`)

### Changed
- refactor(service): eliminación de constructor manual en `ChequeraService` usando `@RequiredArgsConstructor` (fuente: `src/main/java/um/tesoreria/mercadopago/service/service/ChequeraService.java`, `git diff HEAD`)
- refactor(diagrams): simplificación de etiquetas en diagramas de despliegue y modelo de datos (fuente: `docs/diagrams/despliegue.mmd`, `docs/diagrams/modelo-datos-core.mmd`, `git diff HEAD`)
- chore(deps): actualización de JDK de 24 a 25 en workflows y Dockerfile (fuente: `.github/workflows/maven.yml`, `Dockerfile`, `git diff HEAD`)

### Removed
- refactor(logging): eliminación de métodos de logging redundantes en `PaymentService` (logPayment, logMercadoPagoContext, logChequeraPago) (fuente: `src/main/java/um/tesoreria/mercadopago/service/service/PaymentService.java`, `git diff HEAD`)

## [0.1.2] - 2025-08-16

### Added
- feat(domain): añade campo `lastVencimientoUpdated` en `MercadoPagoContextDto` para rastrear última actualización de vencimiento (fuente: `src/.../domain/dto/MercadoPagoContextDto.java`, `git diff HEAD`)
- feat(service): actualiza `lastVencimientoUpdated` al modificar preferencias en `PreferenceService` (fuente: `src/.../service/PreferenceService.java`, `git diff HEAD`)

### Fixed
- fix(workflow): corrige typo en nombre del workflow de GitHub Actions (fuente: `.github/workflows/maven.yml`, `git diff HEAD`)

## [0.1.1] - 2025-08-14

### Changed
- chore(deps): Spring Boot `3.5.3` -> `3.5.4` (fuente: `pom.xml`, `git diff HEAD`)
- chore(schedule): cambia cron de verificación a las 04:00 (fuente: `src/.../controller/CheckingController.java`, `git diff HEAD`)

### Added
- feat(core-client): nuevo endpoint interno `GET /all/active/to/change` y uso en verificación (fuente: `src/.../client/core/MercadoPagoContextClient.java`, `src/.../service/CheckingService.java`, `git diff HEAD`)
- refactor(logging): serialización JSON centralizada (`JsonSerializer`, `JacksonJsonSerializer`) y métodos `jsonify` en DTOs (fuente: `src/.../serializer/*`, `src/.../domain/dto/*`, `git diff HEAD`)

### Fixed
- fix(docker): asegura ownership de `/app` para usuario no root (fuente: `Dockerfile`, `git diff HEAD`)

## [0.1.0] - 2025-07-20

### Added

- **Nueva documentación generada por GHActions:** Se ha implementado un nuevo sistema de generación de documentación utilizando GitHub Actions, que genera una página web estática con información del proyecto, diagramas y dependencias.
- **Soporte para Java 24:** Se ha actualizado la versión de Java de 21 a 24.
- **Plugins de Maven:** Se han añadido los plugins `maven-compiler-plugin` y `jacoco-maven-plugin` para mejorar el proceso de construcción y la calidad del código.

### Changed

- **Actualización de dependencias:**
  - Spring Boot: `3.5.0` -> `3.5.3`
  - MercadoPago SDK: `2.4.0` -> `2.5.0`
  - SpringDoc OpenAPI: `2.8.8` -> `2.8.9`
- **Dockerfile:** Se ha mejorado el `Dockerfile` para utilizar un build multi-etapa, resultando en una imagen Docker más pequeña y segura.
- **CI/CD:** Se han actualizado los workflows de GitHub Actions para reflejar los nuevos cambios en la construcción y despliegue de la aplicación.

### Removed

- **Antiguo sistema de documentación:** Se ha eliminado el antiguo sistema de documentación basado en Jekyll.

