# Changelog

Todos los cambios notables en este proyecto serán documentados en este archivo.

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

## [Unreleased]

### Versiones de dependencias principales (fuente: pom.xml)

- Spring Boot: 3.5.4
- Spring Cloud: 2025.0.0
- Java: 24
- MercadoPago SDK: 2.5.0
- SpringDoc OpenAPI: 2.8.9

#### Fuente de la información:
- Los cambios y fechas provienen del análisis del código (`git diff HEAD`) y del historial (`git log`).
- Las versiones de dependencias provienen del archivo `pom.xml`.
- La versión actual del proyecto propuesta para release es 0.1.1.
