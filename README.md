# UM.tesoreria.mercadopago-service

[![UM.tesoreria.mercadopago-service CI](https://github.com/UM-services/UM.tesoreria.mercadopago-service/actions/workflows/maven.yml/badge.svg)](https://github.com/UM-services/UM.tesoreria.mercadopago-service/actions/workflows/maven.yml)
[![Deploy GitHub Pages](https://github.com/UM-services/UM.tesoreria.mercadopago-service/actions/workflows/pages.yml/badge.svg)](https://github.com/UM-services/UM.tesoreria.mercadopago-service/actions/workflows/pages.yml)

[![Java](https://img.shields.io/badge/Java-21-red?logo=java)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.1-blue?logo=spring)](https://spring.io/projects/spring-cloud)
[![MercadoPago](https://img.shields.io/badge/MercadoPago%20SDK-2.4.0-lightblue?logo=mercadopago)](https://www.mercadopago.com.ar/developers/es)
[![OpenAPI](https://img.shields.io/badge/OpenAPI-2.8.8-green?logo=openapi-initiative)](https://www.openapis.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-purple?logo=apache-maven)](https://maven.apache.org/)

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
- Spring Boot 3.4.5
- Spring Cloud 2024.0.1
- MercadoPago SDK Java 2.4.0
- Caffeine (para caché)
- Springdoc OpenAPI 2.8.8 (Swagger UI)

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
- Tarjetas de crédito (configurable por tipo de chequera)

Los siguientes métodos están excluidos:
- Tarjetas prepagas
- Pagos con ticket

### Configuración de Tarjetas de Crédito

La configuración de tarjetas de crédito se realiza a través del tipo de chequera, permitiendo:
- Habilitar/deshabilitar tarjetas de crédito por tipo de chequera
- Configurar el número máximo de cuotas
- Establecer el número de cuotas por defecto

### Manejo de Errores

El servicio incluye un sistema mejorado de logging para errores de MercadoPago que proporciona:
- Detalles completos de la respuesta de la API
- Códigos de estado HTTP
- Mensajes de error específicos
- Información de la solicitud que causó el error
