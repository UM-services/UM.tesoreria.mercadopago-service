# MercadoPago Service

Servicio de integración con MercadoPago para la gestión de pagos y preferencias.

## Versión del Proyecto

- **Versión actual:** 0.12.0 _(fuente: pom.xml)_

## Características Principales

- Gestión de preferencias de pago en MercadoPago (cuotas y reserva de vacantes)
- Arquitectura hexagonal por slices con puertos de entrada/salida y adaptadores Feign, SDK de MercadoPago y Kafka
- Configuración de métodos de pago por tipo de chequera
- Soporte para tarjetas de crédito con configuración específica (cuotas, monto máximo)
- Manejo robusto de errores y logging detallado con serialización JSON centralizada
- Integración con sistema de chequeras y reserva de vacantes
- Soporte para copias de email en notificaciones
- Registro de última actualización de vencimiento de preferencias
- Envío automático de notificaciones de cuota para usuarios específicos
- Renovación diaria (00:30 ART) de los enlaces de pago cuyo vencimiento cayó en las últimas 72 h
- Tareas programadas para verificación automática diaria
- Publicación del evento `payment-processed` en Kafka con payload JSON (sin header `__TypeId__`)

## Arquitectura

El código vive en un único slice `um.tesoreria.mercadopago.hexagonal.mercadopago` con cuatro áreas:

- `datos`: acceso al `tesoreria-core-service` (`chequeraCuota`, `contexto`, `tipoChequeraTarjeta`).
- `gateways`: salidas hacia MercadoPago (`mercadoPago`) y Kafka (`mensajeria`).
- `procesos`: casos de uso orquestadores (`checking`, `chequera`, `pago`, `preferenciaCuota`, `preferenciaVacante`, `renovacion`).
- `shared`: modelo de dominio, DTOs, mappers y servicios comunes.

Cada slice declara `domain/model`, `domain/ports/in`, `domain/ports/out`, `application/usecases` e `infrastructure`; la regla de dependencias (`domain` ← `application` ← `infrastructure` y `shared` sin depender de otras slices) se verifica en `ArquitecturaHexagonalTest`. Ver [diagrama de arquitectura hexagonal](docs/diagrams/arquitectura-hexagonal.mmd).

## Requisitos

- Java 25 _(fuente: pom.xml)_
- Spring Boot 4.1.1 _(fuente: pom.xml)_
- Spring Cloud 2025.1.3 _(fuente: pom.xml)_
- MercadoPago SDK Java 3.7.0 _(fuente: pom.xml)_
- Springdoc OpenAPI 3.1.1 _(fuente: pom.xml)_

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
- `POST /api/tesoreria/mercadopago/payment/listener`: Webhook de notificaciones de pago (IPN).
  - Acepta el formato v1 (`?data.id=<id>&type=payment`, valida `x-signature` con `app.secret-key`)
    y también el IPN legacy (`?id=<paymentId>&topic=payment`), que se procesa sin validar firma
    porque el pago se autentica al recuperarlo con las credenciales propias.
  - Las notificaciones legacy de `topic=merchant_order` se acknowledgean con `200` y se ignoran
    (el pago llega igual por el webhook v1).
  - Códigos de respuesta: `200` procesado, `400` falta/ es inválido `data.id`,
    `401` firma faltante o inválida, `502` no se pudo recuperar el pago desde Mercado Pago
    (Mercado Pago reintenta ante respuestas no exitosas).
- `GET /api/tesoreria/mercadopago/payment/update/{dataId}`: Forzar actualización de un pago
  (`200` si se publicó el evento, `502` si no se pudo recuperar desde Mercado Pago).

#### Validación de firma del webhook

`app.webhook.signature-validation` (env `APP_WEBHOOK_SIGNATURE_VALIDATION`, default `true`):

- `true`: se exige `x-signature`/`x-request-id` y se valida el HMAC con `app.secret-key`.
- `false`: **modo permisivo, solo para entornos de prueba**. Procesa la notificación aunque falten
  las cabeceras o la firma no coincida. Igual deja una traza informativa por request:

  ```
  WARN  Validación de firma de webhooks DESHABILITADA (app.webhook.signature-validation=false) ...
  WARN  [firma informativa] data.id=185328617014: la firma NO valida (ts=..., v1=..., huella secret=69dcc06cec14);
        se procesa igual por modo permisivo
  ```

  Cuando Mercado Pago empiece a firmar con el secret correcto, esa traza pasa a
  `[firma informativa] ...: la firma HABRÍA validado`, así se detecta sin tocar código.
  La huella del secret configurado se compara con
  `printf '%s' "<secret>" | sha256sum | cut -c1-12`.

  > No usar `false` en producción: es la única barrera que garantiza que la notificación la envió
  > Mercado Pago.

#### Fallback IPN legacy

`app.webhook.legacy-ipn-enabled` (env `APP_WEBHOOK_LEGACY_IPN_ENABLED`, default `false`):

- `false`: las notificaciones IPN legacy (`?id=<paymentId>&topic=payment`, sin `data.id`) se
  acknowledgean con **200** ("Notificación legacy ignorada") y se loguean, sin publicar nada. El 200
  evita que Mercado Pago entre en reintentos.
- `true`: se procesa el pago y se publica el evento con `source=IPN_LEGACY`. Habilitarlo **sólo** en
  entornos controlados y con los consumidores idempotentes ya desplegados: mientras el webhook v1 y
  el legacy procesen el mismo pago, el duplicado lo absorben core (índice único
  `uk_chequera_pago_id_mp`) y umhub (claim de notificación en `reserva_vacante`).

El evento `payment-processed` incluye además `source` (`WEBHOOK_V1` | `IPN_LEGACY` | `MANUAL`) y
`dateLastUpdated` (el `date_last_updated` del pago en Mercado Pago), que core usa para descartar
eventos entregados fuera de orden.

### Chequeras
- `GET /api/tesoreria/mercadopago/chequera/create/context/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}`: Crear preferencias para cuotas pendientes (una por una)
- `GET /api/tesoreria/mercadopago/chequera/create/context/bulk/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}`: Crear preferencias para cuotas pendientes en modo batch (obtiene todos los contextos del core en una sola llamada)

### Verificación (legado, `@Deprecated`)
- `GET /api/tesoreria/mercadopago/checking/cuota/{chequeraCuotaId}`: Verificar/renovar cuota individual
- `GET /api/tesoreria/mercadopago/checking/all/active`: Verificar todas las preferencias activas
- El scheduler de ese flujo está deshabilitado por defecto (`app.checking.legacy-scheduler.enabled=false`) y el barrido de 90 días (`/checking/all/active`) está marcado `@Deprecated`: lo reemplaza `/renovacion/run`.
- `GET /checking/cuota/{id}` se mantiene como herramienta manual de soporte.

### Renovación de vencimientos (proceso vigente)
- `POST /api/tesoreria/mercadopago/renovacion/run`: corrida manual del proceso. Body opcional:
  `{"lookbackDias": 3, "dryRun": true}`.
  - Corre todos los días a las **00:30** (`America/Argentina/Mendoza`).
  - Toma los contextos activos cuyo vencimiento publicado cayó en las últimas **72 h** (3 días) y, si la
    cuota está pendiente y tiene un vencimiento posterior vigente, actualiza el enlace en Mercado Pago.
  - `dryRun=true` recorre y loguea lo que haría, sin tocar Mercado Pago ni el core.
  - Devuelve un resumen: `paginas`, `escaneados`, `actualizados`, `creados`, `sinCambios`, `fallidos`,
    `duracionMs` y los ids fallidos.

Configuración (`bootstrap.yml`):

```properties
APP_RENOVACION_ENABLED=true
APP_RENOVACION_CRON=0 30 0 * * *
APP_RENOVACION_ZONE=America/Argentina/Mendoza
APP_RENOVACION_LOOKBACK_DIAS=3
APP_RENOVACION_PAGE_SIZE=200
APP_CHECKING_LEGACY_ENABLED=false
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

[![UM.tesoreria.mercadopago-service Build JVM Image](https://github.com/UM-services/UM.tesoreria.mercadopago-service/actions/workflows/maven.yml/badge.svg)](https://github.com/UM-services/UM.tesoreria.mercadopago-service/actions/workflows/maven.yml)
[![Generate Documentation](https://github.com/UM-services/UM.tesoreria.mercadopago-service/actions/workflows/generate-docs.yml/badge.svg)](https://github.com/UM-services/UM.tesoreria.mercadopago-service/actions/workflows/generate-docs.yml)

[![Java](https://img.shields.io/badge/Java-25-red?logo=java)](https://www.java.com)
[![Version](https://img.shields.io/badge/Version-0.12.0-orange)](https://github.com/UM-services/UM.tesoreria.mercadopago-service)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.3-blue?logo=spring)](https://spring.io/projects/spring-cloud)
[![MercadoPago](https://img.shields.io/badge/MercadoPago%20SDK-3.7.0-lightblue?logo=mercadopago)](https://www.mercadopago.com.ar/developers/es)
[![OpenAPI](https://img.shields.io/badge/OpenAPI-3.1.1-green?logo=openapi-initiative)](https://www.openapis.org/)
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
- [Persistencia de contextos de Mercado Pago](docs/persistencia-contexto-mercadopago.md)
- [Idempotencia end-to-end de `payment-processed`](docs/idempotencia-payment-processed.md)
- [Documentación de MercadoPago](https://www.mercadopago.com.ar/developers/es/reference)

## Estado del Proyecto

El estado actual del proyecto, incluyendo issues activos y milestones, se puede consultar en la [documentación del servicio](https://um-services.github.io/UM.tesoreria.mercadopago-service).

## Tecnologías

- Java 25 _(fuente: pom.xml)_
- Spring Boot 4.1.1 _(fuente: pom.xml)_
- Spring Cloud 2025.1.3 _(fuente: pom.xml)_
- MercadoPago SDK Java 3.7.0 _(fuente: pom.xml)_
- Caffeine (para caché)
- Springdoc OpenAPI 3.1.1 _(fuente: pom.xml)_

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