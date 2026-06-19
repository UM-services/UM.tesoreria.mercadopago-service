# Changelog

Todos los cambios notables en este proyecto serán documentados en este archivo.

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
- fix(service): Eliminada lógica de envío de cuota para `personaId` específico en `PreferenceService` (fuente: `src/main/java/um/tesoreria/mercadopago/service/service/PreferenceService.java`, `git diff HEAD`)

### Changed
- refactor(dto): Añadidas anotaciones `@Builder.Default` a campos en `MercadoPagoContextDto` y `TipoChequeraDto` para inicialización por defecto (fuente: `src/main/java/um/tesoreria/mercadopago/service/domain/dto/MercadoPagoContextDto.java`, `src/main/java/um/tesoreria/mercadopago/service/domain/dto/TipoChequeraDto.java`, `git diff HEAD`)
- refactor(logging): Ajustes en mensajes de logging en `ChequeraService` y `PreferenceService` (fuente: `src/main/java/um/tesoreria/mercadopago/service/service/ChequeraService.java`, `src/main/java/um/tesoreria/mercadopago/service/service/PreferenceService.java`, `git diff HEAD`)

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

## [0.4.1] - 2026-02-05

### Fixed
- fix(service): Restablecida validación de firma en PaymentService (fuente: `src/main/java/um/tesoreria/mercadopago/service/service/PaymentService.java`, `git diff HEAD`)

### Changed
- refactor(service): Añadido logging de debug para UMPreferenceMPDto en PreferenceService (fuente: `src/main/java/um/tesoreria/mercadopago/service/service/PreferenceService.java`, `git diff HEAD`)

## [0.4.0] - 2026-02-03

### Changed
- chore(deps): Actualización de Spring Boot de 3.5.8 a 4.0.2 (fuente: `pom.xml`, `git diff HEAD`)
- chore(deps): Actualización de Spring Cloud de 2025.0.0 a 2025.1.0 (fuente: `pom.xml`, `git diff HEAD`)
- chore(deps): Actualización de MercadoPago SDK de 2.5.0 a 2.8.0 (fuente: `pom.xml`, `git diff HEAD`)
- chore(deps): Actualización de SpringDoc OpenAPI de 2.8.10 a 3.0.1 (fuente: `pom.xml`, `git diff HEAD`)
- chore(deps): Actualización de commons-fileupload de 1.5 a 1.6.0 (fuente: `pom.xml`, `git diff HEAD`)
- chore(deps): Actualización de commons-beanutils de 1.9.4 a 1.11.0 (fuente: `pom.xml`, `git diff HEAD`)
- chore(deps): Actualización de commons-lang3 de 3.18.0 a 3.20.0 (fuente: `pom.xml`, `git diff HEAD`)

#### Fuente de la información:
- Los cambios y fechas provienen del análisis del código (`git diff HEAD`) y del historial (`git log`).
- Las versiones de dependencias provienen del archivo `pom.xml`.
