# MercadoPago Service

Servicio de integración con MercadoPago para la gestión de pagos y preferencias.

## Versión del Proyecto

- **Versión actual:** 0.0.1-SNAPSHOT _(según pom.xml)_

## Características Principales

- Gestión de preferencias de pago en MercadoPago
- Configuración de métodos de pago por tipo de chequera
- Soporte para tarjetas de crédito con configuración específica
- Manejo robusto de errores y logging detallado
- Integración con sistema de chequeras
- Soporte para copias de email en notificaciones

## Requisitos

- Java 24
- Spring Boot 3.5.0 _(según pom.xml)_
- MercadoPago SDK 2.4.0 _(según pom.xml)_
- Springdoc OpenAPI 2.8.8 _(según pom.xml)_
- Base de datos PostgreSQL

## Configuración

### Variables de Entorno

```properties
# MercadoPago
MERCADOPAGO_ACCESS_TOKEN=your_access_token
MERCADOPAGO_PUBLIC_KEY=your_public_key

# Base de Datos
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mercadopago_db
DB_USER=user
DB_PASSWORD=password

# Servicios
CHEQUERA_SERVICE_URL=http://localhost:8080
```

## Uso

El servicio expone endpoints REST para la gestión de preferencias de pago:

- `POST /api/v1/preferences`: Crear una nueva preferencia
- `PUT /api/v1/preferences/{id}`: Actualizar una preferencia existente
- `GET /api/v1/preferences/{id}`: Obtener una preferencia específica

### Ejemplo de Uso

```java
// Crear una preferencia
PreferenceRequest request = PreferenceRequest.builder()
    .items(List.of(item))
    .payer(payer)
    .paymentMethods(paymentMethods)
    .build();

Preference preference = preferenceService.createPreference(request);
```

## Logging

El servicio implementa logging detallado para:
- Configuración de tipo de chequera
- Métodos de pago configurados
- Errores de integración con MercadoPago
- Operaciones de preferencias

## Contribución

1. Fork el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'feat: add some amazing feature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

# UM.tesoreria.mercadopago-service

[![UM.tesoreria.mercadopago-service CI](https://github.com/UM-services/UM.tesoreria.mercadopago-service/actions/workflows/maven.yml/badge.svg)](https://github.com/UM-services/UM.tesoreria.mercadopago-service/actions/workflows/maven.yml)
[![Deploy GitHub Pages](https://github.com/UM-services/UM.tesoreria.mercadopago-service/actions/workflows/pages.yml/badge.svg)](https://github.com/UM-services/UM.tesoreria.mercadopago-service/actions/workflows/pages.yml)

[![Java](https://img.shields.io/badge/Java-24-red?logo=java)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.0.0-blue?logo=spring)](https://spring.io/projects/spring-cloud)
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

- Java 24
- Spring Boot 3.5.0 _(pom.xml)_
- Spring Cloud 2025.0.0 _(pom.xml, variable referenciada)_
- MercadoPago SDK Java 2.4.0 _(pom.xml)_
- Caffeine (para caché)
- Springdoc OpenAPI 2.8.8 _(pom.xml)_

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

- Java 24
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

## Notas sobre versiones y dependencias

- Todas las versiones indicadas provienen directamente del archivo `pom.xml` y pueden ser verificadas allí.
- Si alguna dependencia cambia, por favor actualizar este README y referenciar la fuente.
