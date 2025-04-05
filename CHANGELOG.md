# Changelog

Todos los cambios notables en este proyecto serán documentados en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Soporte para caché con Caffeine
- Documentación OpenAPI (Swagger UI)
- Exclusión de tarjetas prepagas como método de pago
- Nuevo endpoint para verificación de pagos (/api/tesoreria/mercadopago/checking/11/12/2024)
- Funcionalidad para verificación masiva de pagos

### Changed
- Actualización de Spring Boot a 3.4.4
- Actualización de Spring Cloud a 2024.0.1
- Actualización de Springdoc OpenAPI a 2.8.6
- Actualización de MercadoPago SDK Java a 2.2.0
- Mejoras en el servicio de verificación de pagos

### Deprecated
- N/A

### Removed
- N/A

### Fixed
- N/A

### Security
- N/A

## [0.0.1] - 2024-02-04

### Added
- Integración inicial con MercadoPago
- Gestión de preferencias de pago
- Manejo de webhooks
- Integración con el sistema de facturación de UM Tesorería

### Changed
- N/A

### Deprecated
- N/A

### Removed
- N/A

### Fixed
- N/A

### Security
- Implementación de validación de webhooks
- Manejo seguro de tokens de acceso 