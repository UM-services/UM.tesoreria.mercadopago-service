package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.renovacion.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.application.service.ContextoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.application.service.PreferenciaCuotaService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.renovacion.domain.model.ResultadoRenovacion;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.renovacion.domain.ports.in.RenovarVencimientosUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.CandidatoRenovacion;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RenovarVencimientosUseCaseImpl implements RenovarVencimientosUseCase {

    private final ContextoService contextoService;
    private final PreferenciaCuotaService preferenciaCuotaService;

    @Value("${app.renovacion.page-size:200}")
    private int pageSize;

    @Value("${app.renovacion.max-fallidos-detalle:50}")
    private int maxFallidosDetalle;

    @Override
    public ResultadoRenovacion ejecutar(int lookbackDias, boolean dryRun) {
        long inicio = System.nanoTime();
        var resumen = new Resumen(dryRun);

        // Pasada 1: vencimientos publicados dentro de la ventana (72 h).
        procesar(lookbackDias, false, resumen);
        // Pasada 2: contextos que quedaron marcados para reintento fuera de la ventana.
        procesar(lookbackDias, true, resumen);

        long duracionMs = Duration.ofNanos(System.nanoTime() - inicio).toMillis();
        var resultado = new ResultadoRenovacion(dryRun, resumen.paginas, resumen.escaneados,
                resumen.actualizados, resumen.creados, resumen.sinCambios, resumen.fallidos,
                duracionMs, resumen.fallidosIds);
        log.info("Renovacion de vencimientos finalizada -> {}", resultado);
        return resultado;
    }

    private void procesar(int lookbackDias, boolean reintentos, Resumen resumen) {
        OffsetDateTime afterFecha = null;
        Long afterId = null;
        List<CandidatoRenovacion> pagina;
        do {
            pagina = contextoService.findRenovaciones(lookbackDias, reintentos, afterFecha, afterId, pageSize);
            if (pagina.isEmpty()) {
                break;
            }
            resumen.paginas++;
            // El cursor se toma antes de mutar los contextos (la fecha cambia al renovarlos).
            var ultimo = pagina.get(pagina.size() - 1).contexto();
            afterFecha = ultimo.getFechaVencimiento();
            afterId = ultimo.getMercadoPagoContextId();

            for (var candidato : pagina) {
                resumen.escaneados++;
                if (!candidato.requiereActualizacion()) {
                    resumen.sinCambios++;
                    continue;
                }

                if (resumen.dryRun) {
                    resumen.actualizados++;
                    log.info("[dryRun] renovaria contexto {} (cuota {}) a {} / {}",
                            candidato.contexto().getMercadoPagoContextId(),
                            candidato.cuota().getChequeraCuotaId(),
                            candidato.fechaVencimientoDestino(), candidato.importeDestino());
                    continue;
                }

                var resultado = procesar(candidato);
                if (resultado == null) {
                    resumen.fallidos++;
                    if (resumen.fallidosIds.size() < maxFallidosDetalle) {
                        resumen.fallidosIds.add(candidato.contexto().getMercadoPagoContextId());
                    }
                } else if (resultado.creado()) {
                    resumen.creados++;
                } else {
                    resumen.actualizados++;
                }
            }
        } while (pagina.size() == pageSize);
    }

    private ResultadoProcesado procesar(CandidatoRenovacion candidato) {
        var contexto = candidato.contexto();
        Long contextoId = contexto.getMercadoPagoContextId();
        var cuota = candidato.cuota();

        boolean teniaPreferencia = contexto.getInitPoint() != null && contexto.getPreferenceId() != null;
        contexto.setFechaVencimiento(candidato.fechaVencimientoDestino());
        contexto.setImporte(candidato.importeDestino());
        contexto.setChanged((byte) 1);

        var resultado = preferenciaCuotaService.crear(contexto, cuota);

        if (resultado == null && teniaPreferencia) {
            log.warn("Preferencia {} no disponible en Mercado Pago; se recrea para cuota {}",
                    contexto.getPreferenceId(), cuota.getChequeraCuotaId());
            contexto.setInitPoint(null);
            contexto.setPreferenceId(null);
            contexto.setPreference(null);
            resultado = preferenciaCuotaService.crear(contexto, cuota);
            teniaPreferencia = false;
        }

        if (resultado == null || resultado.getInitPoint() == null) {
            log.warn("No se pudo renovar el contexto {} (cuota {})", contextoId, cuota.getChequeraCuotaId());
            marcarParaReintento(contextoId);
            return null;
        }

        if (teniaPreferencia) {
            // La actualizacion exitosa es la unica que setea lastVencimientoUpdated.
            if (resultado.getLastVencimientoUpdated() == null) {
                log.warn("Mercado Pago no actualizo la preferencia del contexto {} (cuota {})",
                        contextoId, cuota.getChequeraCuotaId());
                marcarParaReintento(contextoId);
                return null;
            }
            contextoService.actualizarPreferencia(resultado);
            return new ResultadoProcesado(false);
        }

        resultado.setChanged((byte) 0);
        resultado.setLastVencimientoUpdated(OffsetDateTime.now(ZoneOffset.UTC));
        contextoService.actualizarPreferencia(resultado);
        return new ResultadoProcesado(true);
    }

    private void marcarParaReintento(Long mercadoPagoContextId) {
        try {
            contextoService.marcarChanged(mercadoPagoContextId, (byte) 1);
        } catch (Exception e) {
            log.error("No se pudo marcar el contexto {} para reintento: {}", mercadoPagoContextId, e.getMessage());
        }
    }

    private record ResultadoProcesado(boolean creado) {
    }

    private static final class Resumen {
        private final boolean dryRun;
        private final List<Long> fallidosIds = new ArrayList<>();
        private int paginas;
        private int escaneados;
        private int actualizados;
        private int creados;
        private int sinCambios;
        private int fallidos;

        private Resumen(boolean dryRun) {
            this.dryRun = dryRun;
        }
    }
}
