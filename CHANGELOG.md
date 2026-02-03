# Changelog

Todos los cambios notables en este proyecto serán documentados en este archivo.

## [0.3.1] - 2025-11-16

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
