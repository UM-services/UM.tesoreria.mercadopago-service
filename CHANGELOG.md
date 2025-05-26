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
- Soporte para configuración de tarjetas de crédito por tipo de chequera
- Integración con TipoChequeraMercadoPagoCreditCard para gestión de métodos de pago
- Added `emailCopia` field to `TipoChequeraDto` for email copy functionality
- Added detailed logging for `TipoChequeraContext` and `PaymentMethods` in `PreferenceService`

### Changed
- Actualización de Spring Boot a 3.4.5
- Actualización de Spring Cloud a 2024.0.1
- Actualización de Springdoc OpenAPI a 2.8.8
- Actualización de MercadoPago SDK Java a 2.4.0
- Modificación del endpoint de TipoChequeraMercadoPagoCreditCard para soportar búsqueda por tipoChequeraId y alternativaId
- Mejoras en el servicio de verificación de pagos
- Refactorización del servicio de preferencias para soportar configuración de tarjetas de crédito
- Eliminación del método checking_2024_11_12
- Mejora en el manejo de errores de MercadoPago con logging detallado
- Optimización de la configuración de métodos de pago excluidos
- Added `@Builder` annotation to `TipoChequeraDto` for better object construction
- Improved error handling and logging in MercadoPago integration
- Updated dependencies for better security and performance
- Enhanced credit card configuration search and validation

### Deprecated
- Endpoint `/tipoChequera/{tipoChequeraId}` en TipoChequeraMercadoPagoCreditCardClient (reemplazado por `/unique/{tipoChequeraId}/{alternativaId}`)

### Removed
- Método checking_2024_11_12 del CheckingService
- Campo defaultPaymentMethodId de TipoChequeraMercadoPagoCreditCardDto

### Fixed
- Improved error handling in MercadoPago payment configuration
- Enhanced logging for better debugging and monitoring

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