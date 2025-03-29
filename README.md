# UM.tesoreria.mercadopago-service

[![UM.tesoreria.mercadopago-service CI](https://github.com/UM-services/UM.tesoreria.mercadopago-service/actions/workflows/maven.yml/badge.svg)](https://github.com/UM-services/UM.tesoreria.mercadopago-service/actions/workflows/maven.yml)
[![Deploy GitHub Pages](https://github.com/UM-services/UM.tesoreria.mercadopago-service/actions/workflows/pages.yml/badge.svg)](https://github.com/UM-services/UM.tesoreria.mercadopago-service/actions/workflows/pages.yml)

## Autor ✍️

- Daniel Quinteros

## Descripción

Servicio de integración con MercadoPago para la arquitectura de microservicios de UM Tesorería. Este servicio proporciona funcionalidades para:
- Procesamiento de pagos
- Gestión de preferencias de pago
- Manejo de webhooks de MercadoPago
- Integración con el sistema de facturación

## Documentación

- [Documentación del Servicio](https://um-services.github.io/UM.tesoreria.mercadopago-service)
- [Wiki del Proyecto](https://github.com/UM-services/UM.tesoreria.mercadopago-service/wiki)
- [Documentación de MercadoPago](https://www.mercadopago.com.ar/developers/es/reference)

## Estado del Proyecto

El estado actual del proyecto, incluyendo issues activos y milestones, se puede consultar en:
- [Estado de Issues](https://github.com/UM-services/UM.tesoreria.mercadopago-service/issues)
- [Milestones](https://github.com/UM-services/UM.tesoreria.mercadopago-service/milestones)
- [Documentación Detallada del Proyecto](https://um-services.github.io/UM.tesoreria.mercadopago-service/project-documentation.html)

## Tecnologías

- Java 21
- Spring Boot 3.4.4
- Spring Cloud 2024.0.1
- MercadoPago SDK Java 2.1.29
- Caffeine (para caché)
- Springdoc OpenAPI 2.8.6 (Swagger UI)

## Configuración

El servicio requiere las siguientes variables de entorno:
- `MERCADOPAGO_ACCESS_TOKEN`: Token de acceso de MercadoPago
- `MERCADOPAGO_WEBHOOK_URL`: URL para webhooks de MercadoPago
- `APP_SECRET_KEY`: Clave secreta para validación de webhooks

## Endpoints

La documentación de los endpoints está disponible en:
- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`

## Desarrollo

Para ejecutar el servicio localmente:

```bash
mvn spring-boot:run
```

### Requisitos

- Java 21
- Maven 3.9+
- Token de acceso de MercadoPago

## CI/CD

El proyecto utiliza GitHub Actions para:
- Compilación y pruebas continuas
- Generación y despliegue automático de documentación
- Actualización automática de la wiki del proyecto

## Métodos de Pago

El servicio acepta los siguientes métodos de pago:
- Transferencias bancarias
- Pago en efectivo

Los siguientes métodos están excluidos:
- Tarjetas de crédito
- Tarjetas prepagas
- Pagos con ticket
