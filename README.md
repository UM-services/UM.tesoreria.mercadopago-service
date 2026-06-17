# MercadoPago Service

Servicio de integración con MercadoPago para la gestión de pagos y preferencias.

## Versión del Proyecto

- **Versión actual:** 0.7.1 _(fuente: pom.xml)_

## Características Principales

- Gestión de preferencias de pago en MercadoPago (cuotas y reserva de vacantes)
- Arquitectura hexagonal con separación de responsabilidades
- Configuración de métodos de pago por tipo de chequera
- Soporte para tarjetas de crédito con configuración específica (cuotas, monto máximo)
- Manejo robusto de errores y logging detallado con serialización JSON centralizada
- Integración con sistema de chequeras y reserva de vacantes
- Soporte para copias de email en notificaciones
- Registro de última actualización de vencimiento de preferencias
- Envío automático de notificaciones de cuota para usuarios específicos
- Tareas programadas para verificación automática diaria
- Soporte para eventos Kafka con serialización Jackson

## Requisitos

- Java 25 _(fuente: pom.xml)_
- Spring Boot 4.1.0 _(fuente: pom.xml)_
- Spring Cloud 2025.1.2 _(fuente: pom.xml)_
- MercadoPago SDK Java 3.2.1 _(fuente: pom.xml)_
- Springdoc OpenAPI 3.0.3 _(fuente: pom.xml)_

## Configuración

### Variables de Entorno

```properties
# MercadoPago
MERCADOPAGO_ACCESS_TOKEN=your_access_token
MERCADOPAGO_PUBLIC_KEY=your_public_key

# Servicios (Feign Clients)
TESORERIA_CORE_SERVICE_URL=http://tesoreria-core-service:8080

# Kafka (opcional)
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## Uso

El servicio expone endpoints REST para la gestión de preferencias de pago y pagos:

### Preferencias (Cuotas)
- `POST /api/tesoreria/mercadopago/preference/create`: Crear una nueva preferencia de pago para cuota
- `GET /api/tesoreria/mercadopago/preference/retrieve/{preferenceId}`: Obtener una preferencia específica

### Preferencias (Vacantes)
- `POST /api/tesoreria/mercadopago/vacante/preference/create`: Crear una nueva preferencia de pago para reserva de vacante

### Pagos
- `POST /api/tesoreria/mercadopago/payment/listener`: Webhook de notificaciones de pago (IPN)
- `GET /api/tesoreria/mercadopago/payment/update/{dataId}`: Forzar actualización de un pago

### Chequeras
- `GET /api/tesoreria/mercadopago/chequera/create/context/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}`: Crear preferencias para cuotas pendientes

### Verificación
- `GET /api/tesoreria/mercadopago/checking/cuota/{chequeraCuotaId}`: Verificar/renovar cuota individual
- `GET /api/tesoreria/mercadopago/checking/all/active`: Verificar todas las preferencias activas

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

[![UM.tesoreria.mercadopago-service Build JVM Image](https://github.com/UM-services/UM.tesoreria.mercadopago-service/actions/workflows/maven.yml/badge.svg)](https://github.com/UM-services/UM.tesoreria.mercadopago-service/actions/workflows/maven.yml)
[![Generate Documentation](https://github.com/UM-services/UM.tesoreria.mercadopago-service/actions/workflows/generate-docs.yml/badge.svg)](https://github.com/UM-services/UM.tesoreria.mercadopago-service/actions/workflows/generate-docs.yml)

[![Java](https://img.shields.io/badge/Java-25-red?logo=java)](https://www.java.com)
[![Version](https://img.shields.io/badge/Version-0.7.1-orange)](https://github.com/UM-services/UM.tesoreria.mercadopago-service)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.2-blue?logo=spring)](https://spring.io/projects/spring-cloud)
[![MercadoPago](https://img.shields.io/badge/MercadoPago%20SDK-3.2.1-lightblue?logo=mercadopago)](https://www.mercadopago.com.ar/developers/es)
[![OpenAPI](https://img.shields.io/badge/OpenAPI-3.0.3-green?logo=openapi-initiative)](https://www.openapis.org/)
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

La documentación del proyecto se genera automáticamente con cada push a la rama `main` y se puede consultar en:

- [Documentación del Servicio](https://um-services.github.io/UM.tesoreria.mercadopago-service)
- [Wiki del Proyecto](https://github.com/UM-services/UM.tesoreria.mercadopago-service/wiki)
- [Documentación de MercadoPago](https://www.mercadopago.com.ar/developers/es/reference)

## Estado del Proyecto

El estado actual del proyecto, incluyendo issues activos y milestones, se puede consultar en la [documentación del servicio](https://um-services.github.io/UM.tesoreria.mercadopago-service).

## Tecnologías

- Java 25 _(fuente: pom.xml)_
- Spring Boot 4.1.0 _(fuente: pom.xml)_
- Spring Cloud 2025.1.2 _(fuente: pom.xml)_
- MercadoPago SDK Java 3.2.1 _(fuente: pom.xml)_
- Caffeine (para caché)
- Springdoc OpenAPI 3.0.3 _(fuente: pom.xml)_

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

- Java 25
- Maven 3.9+
- Token de acceso de MercadoPago

## CI/CD

El proyecto utiliza GitHub Actions para:
- Compilación y pruebas continuas
- Análisis de código con SonarCloud
- Generación y despliegue automático de documentación
- Construcción y publicación de imágenes Docker

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

- Todas las versiones indicadas provienen directamente del archivo `pom.xml` y pueden ser verificadas allí
- Los cambios recientes en las versiones están documentados en el [CHANGELOG.md](CHANGELOG.md)
- La información de commits y cambios está disponible en el historial de git
- Si alguna dependencia cambia, por favor actualizar este README y referenciar la fuente