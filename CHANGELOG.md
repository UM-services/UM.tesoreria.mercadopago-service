# Changelog

Todos los cambios notables en este proyecto serán documentados en este archivo.

## [Unreleased]

### Cambios recientes (fuente: git log)

- 2024-03-19: Revertir actualización de Java a versión 24, volviendo a versión 21. (commit 38563b8)
- 2024-03-19: Actualización de Java a versión 24 y dependencias principales. (commit 02d7435)
- 2024-03-13: Mejora en la configuración de métodos de pago y cuotas. Manejo explícito de cuotas e inclusión de tipos de pago excluidos. (commit 88bfef2)
- 2024-03-13: Mejora en logging y configuración de TipoChequera. Se añade campo emailCopia y anotación @Builder a TipoChequeraDto. (commit 4e461ab)
- 2024-03-08: Actualización de dependencias y mejora en búsqueda de configuración de tarjetas de crédito. (commit be2dcb9)
- 2024-02-23: Mejora en el manejo de errores y configuración de pagos de MercadoPago. (commit e44b5ec)

### Versiones de dependencias principales (fuente: pom.xml)

- Spring Boot: 3.5.0
- Spring Cloud: 2025.0.0
- Java: 21 (revertido desde 24)
- MercadoPago SDK: 2.4.0
- SpringDoc OpenAPI: 2.8.8

#### Fuente de la información:
- Los cambios y fechas provienen exclusivamente de los mensajes de los commits (git log)
- Las versiones de dependencias provienen del archivo pom.xml
- La versión actual del proyecto es 0.0.1-SNAPSHOT (según pom.xml)
- Si existen cambios en el código que no están documentados en git, por favor indícalo para poder agregarlos