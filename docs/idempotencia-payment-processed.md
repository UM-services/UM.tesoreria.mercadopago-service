# Implementación completa de idempotencia end-to-end (`payment-processed`)

**Documento operativo para un agente.** Describe, fase por fase, todos los cambios necesarios para que el
procesamiento de pagos sea idempotente en los tres repositorios involucrados, de modo que se puedan habilitar
sin riesgo el webhook v1 en modo permisivo y el fallback IPN legacy del productor.

---

## 0. Cómo usar este documento

### 0.1 Alcance

| repo | ruta (workspace actual) | rol |
|---|---|---|
| `um.tesoreria.mercadopago-service` | `~/workspaces/java/um/um.tesoreria.mercadopago-service` | **productor** del evento `payment-processed` |
| `um.tesoreria.core-service` | `~/workspaces/java/um/um.tesoreria.core-service` | **consumidor** principal (cuotas y reservas de vacante) |
| `um.tesoreria.umhub-service` | `~/workspaces/java/um/um.tesoreria.umhub-service` | **consumidor** de notificaciones de reservas (n8n) |

### 0.2 Orden de ejecución obligatorio

```
Fase 1 (core: robustez + dedup)  →  Fase 2 (umhub: claim de notificación)  →  Fase 3 (productor: contrato + flags)
```

**Nunca al revés**: si se habilita primero el doble camino del productor (v1 + legacy) sin consumidores
idempotentes, se duplican pagos reales.

### 0.3 Reglas duras para el agente

1. **No** ejecutar `git commit`, `git branch`, `git push` ni ninguna operación destructiva. Dejar los cambios en
   el working tree salvo autorización explícita del usuario.
2. **No** usar `@Query` en Spring Data (regla global del usuario). Para consultas usar:
   - *derived query methods* (`findByXAndY`, `existsByX`, `countByX`, …),
   - `@Lock` sobre un derived method cuando haga falta bloqueo pesimista,
   - Criteria API / `JpaSpecificationExecutor` si el derived no alcanza.
   El DDL (índices, columnas) va en un script SQL manual, **no** como `@Query(nativeQuery = true)`.
3. Respetar la arquitectura hexagonal de cada repo:
   - `domain/` sólo `java.*` + Lombok (en core y umhub);
   - `application/` sólo `domain/` + anotaciones Spring permitidas (`@Component`, `@Service`, `@Transactional`, `@Value`);
   - nada de `infrastructure` en `application/`;
   - nombres y mensajes en español, siguiendo el estilo existente.
4. En `um.tesoreria.core-service`, si aparece una **necesidad de importar otra slice** que no exista ya, **detenerse
   y preguntar** (regla del `AGENTS.md` del repo). Las slices `mercadoPagoContext` → `reservaVacante` ya se importan
   hoy en `ProcessPaymentEventUseCaseImpl`, así que ese patrón está permitido.
5. No agregar Flyway/Liquibase: `ddl-auto: none` y el esquema se gestiona fuera de la app.
6. Cada fase termina con **sus tests verdes** y un párrafo de verificación manual. No avanzar con tests rojos.

### 0.4 Comandos por repo

| repo | build | tests |
|---|---|---|
| mercadopago | `mvn -o -DskipTests package` | `mvn -o test` |
| core | `mvn -q -DskipTests compile` | `mvn test` (H2 en memoria, sin Kafka/DB) |
| umhub | `./mvnw -q -DskipTests package` | `./mvnw test` |

### 0.5 Definición de terminado (global)

- [ ] El mismo `paymentId` entregado N veces produce **un** `chequera_pago`, **una** cuota pagada y **una** notificación.
- [ ] Un fallo a mitad de proceso no deja estados parciales y, al reintentar, no duplica.
- [ ] Dos entregas concurrentes no crean dos registros.
- [ ] Un mensaje inválido termina en `payment-processed.DLT` (no se pierde y no bloquea el tópico).
- [ ] `GET /api/tesoreria/mercadopago/payment/update/{id}` se puede repetir sin efectos extra.
- [ ] Todas las suites de los 3 repos en verde.

---

## 1. Contexto: por qué hace falta

Un webhook y un tópico Kafka son **at-least-once**. Los duplicados son normales (reintentos de Mercado Pago,
IPN legacy + webhook v1, retry del productor, rebalanceo de consumers). La idempotencia se implementa **donde
cambia el estado**, no en el productor ni con cachés en memoria.

Estado actual (verificado en el código):

| repo | archivo | problema |
|---|---|---|
| core | `listener/PaymentEventListener.java` | `catch (Exception) { log.error }` → el offset se commitea y el evento se pierde |
| core | `hexagonal/mercadoPagoContext/application/usecases/ProcessPaymentEventUseCaseImpl.java` | sin `@Transactional`: contexto + pago + cuota no son atómicos |
| core | `service/facade/PagoService.java#registraPagoMP` | dedup manual con `findByIdMercadoPago` pero **sin índice único** en DB; `nextOrden` con carrera |
| core | `configuration/KafkaConsumerConfig.java` | `FixedBackOff(1000, 2)` sin `Recoverer` → el record se descarta |
| umhub | `.../reservavacante/infrastructure/kafka/PaymentProcessedConsumer.java` | sin marca de "ya notificado"; `catch (Exception)` |
| umhub | `.../infrastructure/client/ReservaVacanteNotificationFeignAdapter.java` | **también** silencia el error del POST a n8n |
| umhub | `configuration/KafkaConsumerConfig.java` | sin DLT |
| core | DB MySQL | `chequera_pago` sin unique sobre `idMercadoPago` |

---

## 2. Claves de idempotencia (diseño)

| efecto | clave | mecanismo |
|---|---|---|
| actualizar campos del contexto | `paymentId` (key de Kafka ⇒ orden por pago) | escritura de los mismos valores |
| crear `chequera_pago` | `paymentId` | unique index + pre-check |
| marcar cuota pagada | `chequeraCuotaId` | update condicional / no-op si ya está |
| marcar reserva pagada | `reservaVacanteId` | update condicional |
| notificar pago de reserva | `paymentId` | claim atómico en core (fase 2) |
| refunds / chargebacks | `paymentId + status` | **no bloquear**: son transiciones legítimas |

> **No** bloquear el evento completo por `paymentId`: se perderían `approved → refunded` y chargebacks.
> El bloqueo por `paymentId` aplica sólo al efecto que no debe repetirse.

---

## 3. Fase 1 — `um.tesoreria.core-service`

### F1.1 DDL: índice único en `chequera_pago` (manual, fuera del repo)

Guardar el script en `docs/sql/2026-10-21-uk-chequera-pago-id-mp.sql`.

**Paso 1 — confirmar el nombre real de la columna** (la entidad no declara `@Column`, aplica el naming por defecto):

```sql
SHOW COLUMNS FROM chequera_pago LIKE '%mp%';
-- esperado: id_mercado_pago
```

**Paso 2 — detectar duplicados existentes:**

```sql
SELECT id_mercado_pago, COUNT(*) AS repeticiones, MIN(clave) AS conservar
FROM chequera_pago
WHERE id_mercado_pago IS NOT NULL AND id_mercado_pago <> ''
GROUP BY id_mercado_pago
HAVING COUNT(*) > 1;
```

Si hay filas: **no borrar**. Son registros contables; se corrigen por el circuito administrativo (reversa/anulación)
antes de crear el índice. Documentar cada caso en el ticket.

**Paso 3 — crear el índice:**

```sql
ALTER TABLE chequera_pago
    ADD UNIQUE KEY uk_chequera_pago_id_mp (id_mercado_pago);
```

> MySQL permite múltiples `NULL` en un índice `UNIQUE`, así que los pagos que no son de Mercado Pago no se ven afectados.

**Rollback:**

```sql
ALTER TABLE chequera_pago DROP INDEX uk_chequera_pago_id_mp;
```

**Verificación:** insertar dos veces el mismo `id_mercado_pago` en una transacción de prueba debe fallar con
`Duplicate entry`.

---

### F1.2 `PagoService.registraPagoMP` idempotente

Archivo: `src/main/java/um/tesoreria/core/service/facade/PagoService.java` (método en la línea ~151).

Estado actual (resumen):

```java
// Evita registrar pagos repetidos para el caso de una nueva notificacion de MP
try {
    return chequeraPagoService.findByIdMercadoPago(mercadoPagoContext.getIdMercadoPago());
} catch (ChequeraPagoException e) {
    log.debug("No Existe Pago de MP previo");
}
// ... calcula nextOrden, crea chequeraPago, actualiza contexto, marcarPago(...)
```

Cambios:

1. Mantener el pre-check (es el camino rápido y evita excepciones en el caso normal).
2. Envolver la creación en `try/catch (DataIntegrityViolationException)`:
   - si al capturar existe un pago con ese `idMercadoPago` → devolverlo (otra entrega ganó la carrera);
   - si el conflicto fue por `chp_orden` (unique `fac,tch,chs,pro,alt,cuo,orden`) → recalcular `nextOrden`
     y reintentar **una sola vez**;
   - si no se puede resolver → relanzar (que Kafka reintente / derive a DLT).
3. Loguear el duplicado en `info` con `paymentId` y `chequeraPagoId`, sin stacktrace.

Esqueleto:

```java
public ChequeraPago registraPagoMP(Long mercadoPagoContextId) {
    var context = mercadoPagoContextService.findByMercadoPagoContextId(mercadoPagoContextId);

    var existente = buscarPagoMp(context.getIdMercadoPago()); // devuelve null si no hay
    if (existente != null) {
        log.info("Pago MP {} ya registrado (chequeraPagoId={}): no-op",
                context.getIdMercadoPago(), existente.getChequeraPagoId());
        return existente;
    }
    try {
        return crearPagoMp(context);
    } catch (DataIntegrityViolationException e) {
        var concurrente = buscarPagoMp(context.getIdMercadoPago());
        if (concurrente != null) {
            log.info("Pago MP {} registrado por otra entrega (chequeraPagoId={}): no-op",
                    context.getIdMercadoPago(), concurrente.getChequeraPagoId());
            return concurrente;
        }
        throw e;
    }
}
```

`buscarPagoMp` debe usar el `ChequeraPagoService.findByIdMercadoPago` existente y devolver `null` cuando lanza
`ChequeraPagoException` (no cambiar la firma pública del service).

**Tests** (`src/test/java/um/tesoreria/core/service/facade/PagoServiceTest.java`, crear si no existe):

- `registraPagoMpExistenteNoCreaOtro`
- `registraPagoMpConConflictoDeUnicidadDevuelveElExistente`
- `registraPagoMpConConflictoDeOrdenReintentaUnaVez`

---

### F1.3 `ProcessPaymentEventUseCaseImpl` transaccional y con guardas

Archivo: `src/main/java/um/tesoreria/core/hexagonal/mercadoPagoContext/application/usecases/ProcessPaymentEventUseCaseImpl.java`.

Cambios:

1. Agregar `@Transactional` al método `processPaymentEvent` (import `org.springframework.transaction.annotation.Transactional`).
2. Antes de aplicar el estado, si el contexto ya tiene **el mismo** `paymentId` y `chequeraPagoId != null`,
   cortar el efecto secundario (el estado ya está aplicado):

```java
if (context.getChequeraPagoId() != null
        && Objects.equals(context.getIdMercadoPago(), event.getPaymentId())) {
    log.info("Contexto {} ya procesado para el pago {}: no-op",
            context.getMercadoPagoContextId(), event.getPaymentId());
    return;
}
```

3. Mantener los chequeos de mismatch (`chequeraCuotaId` / `reservaVacanteId`) que ya existen.
4. **No** capturar excepciones: si `registraPagoMP` o `updateReservaVacante` fallan, la transacción revierte y el
   errorHandler de Kafka reintenta.

**Tests** (`src/test/java/.../mercadoPagoContext/application/usecases/ProcessPaymentEventUseCaseImplTest.java`):

- `eventoDuplicadoNoVuelveARegistrarElPago`
- `eventoDuplicadoConContextoYaProcesadoEsNoOp`
- `fallaElRegistroDePagoYPropagaLaExcepcion` (verifica que **no** se silencia)
- `pagoDeCuotaMarcaLaCuota` / `pagoDeReservaMarcaLaReserva` (si no existen)

---

### F1.4 Listener: dejar de silenciar errores

Archivo: `src/main/java/um/tesoreria/core/listener/PaymentEventListener.java`.

```java
@KafkaListener(topics = "payment-processed", groupId = "tesoreria-core-group",
               containerFactory = "kafkaListenerContainerFactory")
public void handlePaymentProcessed(PaymentProcessedEvent event) {
    log.debug("Received payment event: {}", event);
    mercadoPagoContextService.processPaymentEvent(event); // sin try/catch
}
```

> Si en el futuro se quiere atrapar algo, debe ser **solo** un duplicado ya resuelto, y la transacción debe cerrar OK.

**Test:** `PaymentEventListenerTest` — cuando el service lanza, el listener **propaga** la excepción
(`assertThatThrownBy`).

---

### F1.5 Reintentos con backoff + DLT

Archivo: `src/main/java/um/tesoreria/core/configuration/KafkaConsumerConfig.java`.

`KafkaProducerConfig` ya expone `KafkaTemplate<String, Object>`, así que se puede inyectar en la factory:

```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, PaymentProcessedEvent> kafkaListenerContainerFactory(
        KafkaTemplate<String, Object> kafkaTemplate) {

    var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
            (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition()));

    var errorHandler = new DefaultErrorHandler(recoverer, new ExponentialBackOffWithMaxRetries(5));
    errorHandler.setCommitRecovered(false);

    var factory = new ConcurrentKafkaListenerContainerFactory<String, PaymentProcessedEvent>();
    factory.setConsumerFactory(consumerFactory());
    factory.setCommonErrorHandler(errorHandler);
    return factory;
}
```

Imports: `org.springframework.kafka.listener.DeadLetterPublishingRecoverer`, `org.springframework.util.backoff.ExponentialBackOffWithMaxRetries`, `org.apache.kafka.common.TopicPartition`.

**Test a actualizar:** `src/test/java/um/tesoreria/core/configuration/KafkaConsumerConfigTest.java`
(`mantieneElErrorHandlerConDosReintentos` hoy invoca `kafkaListenerContainerFactory()` sin argumentos; hay que pasarle
un mock de `KafkaTemplate` y renombrar el test a `usaDeadLetterYBackoffExponencial`).

**Test nuevo:** `unErrorDelListenerDerivaAlTopicDlt` (con `DeadLetterPublishingRecoverer` mockeado o verificando que el
handler configurado es `DefaultErrorHandler` y el topic destino es `payment-processed.DLT`).

---

### F1.6 Verificación de la fase 1

```bash
cd ~/workspaces/java/um/um.tesoreria.core-service
mvn test
```

Manual (en develop, con el productor ya publicando):

1. Publicar el mismo evento dos veces (o golpear dos veces `GET /payment/update/{id}` del productor).
2. En logs del core: la segunda vez aparece `ya registrado ... no-op`.
3. En DB: `SELECT COUNT(*) FROM chequera_pago WHERE id_mercado_pago = '<paymentId>';` → **1**.

---

## 4. Fase 2 — idempotencia de la notificación (`umhub-service`)

`umhub-service` **no tiene base de datos** (ver su `AGENTS.md`): no se puede crear una tabla ahí. La notificación
sale por Feign a un webhook de **n8n**, y tanto el consumer como el adapter silencian errores.

### Opción recomendada (A): claim atómico en `core-service`

`core-service` es dueño de `reserva_vacante` y tiene DB + transacciones.

#### A.1 DDL

```sql
ALTER TABLE reserva_vacante
    ADD COLUMN notificacion_pago TINYINT(1) NOT NULL DEFAULT 0;
```

Rollback: `ALTER TABLE reserva_vacante DROP COLUMN notificacion_pago;`

#### A.2 core — caso de uso `ReclamarNotificacionPagoUseCase`

Slice: `hexagonal/umhub/reservaVacante/`.

- `domain/ports/in/ReclamarNotificacionPagoUseCase.java`
  ```java
  boolean reclamar(UUID reservaVacanteId);   // true = esta instancia debe notificar
  void liberar(UUID reservaVacanteId);       // el envío falló: vuelve a habilitar
  ```
- `application/usecases/ReclamarNotificacionPagoUseCaseImpl.java` con `@Transactional`:
  1. cargar la reserva con **bloqueo pesimista** (derived method, sin `@Query`):
     ```java
     // infrastructure/persistence/repository/JpaReservaVacanteRepository.java
     @Lock(LockModeType.PESSIMISTIC_WRITE)
     Optional<ReservaVacanteEntity> findWithLockByReservaVacanteId(UUID reservaVacanteId);
     ```
  2. si `notificacionPago == 1` → `return false`;
  3. setear `notificacionPago = 1`, guardar, `return true`.
- `liberar`: `UPDATE` lógico (cargar con lock, poner `0`, guardar).

> Alternativa sin `@Lock`: `@Version` en la entidad + reintento optimista. Preferir `@Lock` por simplicidad.

#### A.3 core — endpoints internos

Base path existente del controller: `/api/tesoreria/core/umhub/reservaVacante`
(en `infrastructure/web/controller/ReservaVacanteController.java`; ya hay `POST /` y `GET /{reservaVacanteId}`).

```java
@PostMapping("/{reservaVacanteId}/notificacion/claim")
public ResponseEntity<Boolean> claim(@PathVariable UUID reservaVacanteId) {
    return ResponseEntity.ok(reclamarNotificacionPagoUseCase.reclamar(reservaVacanteId));
}

@PostMapping("/{reservaVacanteId}/notificacion/release")
public ResponseEntity<Void> release(@PathVariable UUID reservaVacanteId) {
    reclamarNotificacionPagoUseCase.liberar(reservaVacanteId);
    return ResponseEntity.ok().build();
}
```

Rutas finales: `POST /api/tesoreria/core/umhub/reservaVacante/{id}/notificacion/claim` y `.../release`.

#### A.4 umhub — consumir el claim

1. Nuevo puerto de salida: `domain/ports/out/ReservaVacanteNotificacionClaimPort.java`
   ```java
   boolean claim(UUID reservaVacanteId);
   void release(UUID reservaVacanteId);
   ```
2. Adapter Feign en `infrastructure/client/` (sigue el patrón de `ReservaVacanteNotificationFeignAdapter`).
3. `PaymentProcessedConsumer`:
   ```java
   if (!claimPort.claim(event.getReservaVacanteId())) {
       log.info("Notificación de pago ya enviada para reserva {}: no-op", event.getReservaVacanteId());
       return;
   }
   try {
       notificationService.notifyPayment(reserva, event);
   } catch (Exception e) {
       claimPort.release(event.getReservaVacanteId());
       throw e; // que Kafka reintente
   }
   ```
4. **Quitar** el `try/catch` interno de `ReservaVacanteNotificationFeignAdapter.notifyPayment` (hoy se come el error
   y por eso el consumer nunca se entera).
5. DLT en `umhub/configuration/KafkaConsumerConfig.java` (mismo patrón que F1.5; umhub tiene su propio
   `KafkaConsumerConfig` y un `KafkaTemplate` sólo si existe — si no hay producer config, crear uno mínimo
   `KafkaTemplate<String, Object>` con `JsonSerializer` para el recoverer).

> Si el usuario prefiere **no tocar core**: opción B = enviar `paymentId` como clave de idempotencia en el payload a
> n8n y aceptar el riesgo de reintentos (menos robusto). Documentar la decisión antes de implementar.

#### A.5 Tests

- core: `ReclamarNotificacionPagoUseCaseImplTest` — primer claim `true`, segundo `false`, tras `release` vuelve a `true`.
- umhub: `PaymentProcessedConsumerTest` — claim `false` ⇒ no llama a `notificationService`; excepción del notificador ⇒
  `release` + excepción propagada.

---

## 5. Fase 3 — `um.tesoreria.mercadopago-service` (productor)

### F3.1 Campos aditivos en el evento

Agregar a `PagoProcesadoEvento`, `PaymentProcessedEvent` (wire), `PagoProcesadoMapper` **y** a los dos
`PaymentProcessedEvent` de los consumidores (core y umhub):

```java
private OffsetDateTime dateLastUpdated; // date_last_updated del pago en MP
private String source;                  // "webhook-v1" | "ipn-legacy" | "manual"
```

Compatibilidad: los tres consumidores deserializan con `FAIL_ON_UNKNOWN_PROPERTIES` deshabilitado, así que el orden
de despliegue entre repos no importa.

### F3.2 Origen de la notificación

- Nuevo enum en `procesos/pago/domain/model/OrigenNotificacion.java` (`WEBHOOK_V1`, `IPN_LEGACY`, `MANUAL`).
- Cambiar el puerto `RecuperarYPublicarPagoUseCase` a:
  ```java
  boolean recuperarYPublicar(String dataId, OrigenNotificacion origen);
  ```
- `RecuperarYPublicarPagoUseCaseImpl`: setear `source = origen.name()` y `dateLastUpdated` del pago.
- `PagoService.processPaymentWebhook` → `WEBHOOK_V1`; `processLegacyPaymentNotification` → `IPN_LEGACY`;
  `retrieveAndPublishPayment` → `MANUAL`.
- `PagoMapper`: `.dateLastUpdated(payment.getDateLastUpdated())` (existe en el SDK; verificar la versión del `pom.xml`).
- `PagoMercadoPago`: nuevo campo `dateLastUpdated`.

### F3.3 Flags de operación (ya implementado, dejar documentado)

| propiedad | default | uso |
|---|---|---|
| `app.webhook.signature-validation` (`APP_WEBHOOK_SIGNATURE_VALIDATION`) | `true` | `false` = modo permisivo sólo en develop |
| `legacy-ipn` (pendiente de implementar) | `false` | habilitar el fallback IPN en entornos controlados |

> Mientras exista el fallback legacy **y** el webhook v1 procesando el mismo pago, los consumidores idempotentes
> absorben el duplicado. No habilitar ambos caminos en producción hasta completar fases 1 y 2.

### F3.4 Tests del productor

- `ProcesarWebhookPagoUseCaseImplTest`: sigue en verde.
- `KafkaContractTest` / `WireFormatContractTest`: actualizar el JSON esperado con `dateLastUpdated` y `source`.
- `RecuperarYPublicarPagoUseCaseImplTest`: verificar `source` por cada origen.

---

## 6. Observabilidad (fase transversal)

En core y umhub, con Micrometer (core ya expone Prometheus):

| métrica | tipo | uso |
|---|---|---|
| `payment_event_procesado_total{resultado="procesado\|duplicado\|error"}` | counter | tasa de duplicados y fallos |
| `payment_event_reintento_total` | counter | salud de la integración |
| `payment_event_dlt_total` | counter | alerta si > 0 |

Logs:

- duplicado: `INFO "Pago MP <paymentId> ya registrado (chequeraPagoId=<id>): no-op"`.
- DLT: `ERROR` con `paymentId`, topic y offset.
- No loguear el payload completo del pago en nivel INFO (hoy el productor lo hace en `log.info` del evento: bajar a
  DEBUG si el volumen molesta).

Alertas sugeridas: `payment_event_dlt_total > 0` y tasa de duplicados > 10 %.

---

## 7. Testing end-to-end

Con el stack local (`docker compose` del ecosistema):

1. **Duplicado secuencial**: mismo pago por v1 y por legacy ⇒ 1 `chequera_pago`, 1 notificación.
2. **Duplicado concurrente**: publicar el mismo evento desde dos hilos ⇒ 1 fila (valida el unique index).
3. **Fallo intermedio**: matar la DB entre el insert y el update ⇒ al reintentar, 1 fila.
4. **Mensaje inválido**: publicar JSON roto en `payment-processed` ⇒ aparece en `payment-processed.DLT`.
5. **Replay manual**: `GET /api/tesoreria/mercadopago/payment/update/{paymentId}` 3 veces ⇒ sin duplicados.
6. **Sin firma**: con `APP_WEBHOOK_SIGNATURE_VALIDATION=false`, el webhook v1 debe procesar y publicar (una vez).

Automatizar 1–5 como tests de integración con Testcontainers (MySQL + Kafka) en core; si el repo no tiene
Testcontainers, dejarlo documentado como deuda y cubrir con H2 + mocks lo máximo posible.

---

## 8. Plan de despliegue y rollback

| fase | repo | cambio | riesgo | rollback |
|---|---|---|---|---|
| 0 | core | limpiar duplicados + índice único | bajo (previo análisis) | `DROP INDEX` |
| 1 | core | transacción, no silenciar, DLT, dedup | bajo | revertir commits |
| 2 | core + umhub | claim/release de notificación | bajo | `DROP COLUMN notificacion_pago` + revertir |
| 3 | mercadopago | campos nuevos del evento | nulo (aditivo) | revertir |
| 4 | mercadopago (dev) | `SIGNATURE_VALIDATION=false`, `legacy-ipn=true` | sólo dev | flags |
| 5 | mercadopago (prod) | opcional `legacy-ipn=true` | requiere 1–3 | flags |

Checklist de salida a producción:

- [ ] Fase 1 desplegada y métricas de duplicado en 0 durante 48 h.
- [ ] Índice único creado en producción sin duplicados previos.
- [ ] Fase 2 desplegada (o notificaciones aceptadas como at-least-once por decisión explícita).
- [ ] Runbook de `payment-processed.DLT` escrito (cómo inspeccionar y reprocesar).

---

## 9. Trampas conocidas

1. **`@Transactional` + `DataIntegrityViolationException`**: al capturar dentro de la misma transacción queda
   `rollback-only`. La recuperación del pago existente debe hacerse **fuera** de la transacción que falló (o en una
   transacción nueva). Alternativa: dejar que reviente y que el reintento de Kafka entre por el pre-check.
2. **Unique index con datos sucios**: si ya hay duplicados, `ALTER TABLE` falla. Limpiar primero.
3. **`nextOrden`**: no asumir que es thread-safe; el unique de `chp_orden` es la última defensa.
4. **Nombre de columna**: `idMercadoPago` no tiene `@Column`; verificar con `SHOW COLUMNS` antes de escribir el SQL.
5. **No usar `@Query`** (regla del usuario): ni para el claim, ni para contar duplicados. Sí se puede usar SQL manual
   en scripts de migración.
6. **umhub no tiene DB**: no intentar agregar JPA ahí; el estado va en core o en n8n con idempotency key.
7. **`PaymentEventListener` con `@PostConstruct`**: no romper el arranque al cambiar el listener.
8. **Doble publicación legítima**: un `payment.updated` con otro `status` **debe** publicarse; la idempotencia no debe
   filtrarlo.
9. **Kafka key**: el productor ya usa `paymentId` como key; mantenerlo para preservar el orden por pago.

---

## 10. Anexo: matriz de archivos a tocar

### core-service

| archivo | cambio |
|---|---|
| `listener/PaymentEventListener.java` | quitar `try/catch` |
| `configuration/KafkaConsumerConfig.java` | backoff exponencial + DLT |
| `configuration/KafkaConsumerConfigTest.java` | adaptar firma del bean + tests DLT |
| `event/PaymentProcessedEvent.java` | `dateLastUpdated`, `source` |
| `hexagonal/mercadoPagoContext/application/usecases/ProcessPaymentEventUseCaseImpl.java` | `@Transactional`, guardas, no silenciar |
| `service/facade/PagoService.java` | `registraPagoMP` idempotente |
| `hexagonal/umhub/reservaVacante/**` | claim/release (fase 2) |
| `docs/sql/2026-10-21-uk-chequera-pago-id-mp.sql` | índice único |
| `docs/sql/2026-10-21-reserva-vacante-notificacion-pago.sql` | columna nueva |

### umhub-service

| archivo | cambio |
|---|---|
| `.../reservavacante/infrastructure/kafka/PaymentProcessedConsumer.java` | claim, release, propagar excepción |
| `.../infrastructure/client/ReservaVacanteNotificationFeignAdapter.java` | no silenciar el error del POST |
| `.../domain/ports/out/ReservaVacanteNotificacionClaimPort.java` | nuevo puerto |
| `.../infrastructure/client/...ClaimFeignAdapter.java` + `...ClaimFeignClient.java` | adapter |
| `configuration/KafkaConsumerConfig.java` | DLT |

### mercadopago-service

| archivo | cambio |
|---|---|
| `.../shared/domain/model/PagoMercadoPago.java` | `dateLastUpdated` |
| `.../gateways/mercadoPago/infrastructure/client/mapper/PagoMapper.java` | mapear `dateLastUpdated` |
| `.../procesos/pago/domain/model/OrigenNotificacion.java` | nuevo enum |
| `.../procesos/pago/domain/ports/in/RecuperarYPublicarPagoUseCase.java` | parámetro `origen` |
| `.../procesos/pago/application/usecases/RecuperarYPublicarPagoUseCaseImpl.java` | `source` + `dateLastUpdated` |
| `.../procesos/pago/application/service/PagoService.java` | pasar origen |
| `.../gateways/mensajeria/domain/model/PagoProcesadoEvento.java` | campos nuevos |
| `.../gateways/mensajeria/infrastructure/kafka/event/PaymentProcessedEvent.java` | campos nuevos |
| `.../gateways/mensajeria/infrastructure/kafka/mapper/PagoProcesadoMapper.java` | mapear campos nuevos |
| `KafkaContractTest`, `WireFormatContractTest`, `RecuperarYPublicarPagoUseCaseImplTest` | actualizar |
