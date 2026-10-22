package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.renovacion.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.application.service.ContextoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.application.service.PreferenciaCuotaService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.CandidatoRenovacion;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ChequeraCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RenovarVencimientosUseCaseImplTest {

    private static final OffsetDateTime VENCIMIENTO_VIEJO = OffsetDateTime.of(2026, 10, 10, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime VENCIMIENTO_NUEVO = OffsetDateTime.of(2026, 10, 20, 0, 0, 0, 0, ZoneOffset.UTC);

    @Mock
    private ContextoService contextoService;
    @Mock
    private PreferenciaCuotaService preferenciaCuotaService;

    private RenovarVencimientosUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new RenovarVencimientosUseCaseImpl(contextoService, preferenciaCuotaService);
        ReflectionTestUtils.setField(useCase, "pageSize", 10);
        ReflectionTestUtils.setField(useCase, "maxFallidosDetalle", 50);
    }

    @Test
    void noHaceNadaSiNoHayCandidatos() {
        when(contextoService.findRenovaciones(anyInt(), eq(false), any(), any(), anyInt())).thenReturn(List.of());

        var resultado = useCase.ejecutar(3, false);

        assertThat(resultado.escaneados()).isZero();
        assertThat(resultado.paginas()).isZero();
        verify(preferenciaCuotaService, never()).crear(any(), any());
    }

    @Test
    void enDryRunNoLlamaAMercadoPagoNiPersiste() {
        when(contextoService.findRenovaciones(anyInt(), eq(false), any(), any(), anyInt())).thenReturn(List.of(candidato(false)));

        var resultado = useCase.ejecutar(3, true);

        assertThat(resultado.dryRun()).isTrue();
        assertThat(resultado.actualizados()).isEqualTo(1);
        verify(preferenciaCuotaService, never()).crear(any(), any());
        verify(contextoService, never()).actualizarPreferencia(any());
    }

    @Test
    void actualizaLaPreferenciaCuandoElCambioEsReal() {
        var candidato = candidato(false);
        when(contextoService.findRenovaciones(anyInt(), eq(false), any(), any(), anyInt())).thenReturn(List.of(candidato));
        when(preferenciaCuotaService.crear(candidato.contexto(), candidato.cuota()))
                .thenAnswer(invocation -> {
                    var contexto = invocation.getArgument(0, ContextoMercadoPago.class);
                    contexto.setChanged((byte) 0);
                    contexto.setLastVencimientoUpdated(OffsetDateTime.now(ZoneOffset.UTC));
                    return contexto;
                });

        var resultado = useCase.ejecutar(3, false);

        assertThat(resultado.actualizados()).isEqualTo(1);
        assertThat(resultado.creados()).isZero();
        assertThat(resultado.fallidos()).isZero();
        verify(contextoService).actualizarPreferencia(candidato.contexto());
        // el contexto queda en el vencimiento nuevo
        assertThat(candidato.contexto().getFechaVencimiento()).isEqualTo(VENCIMIENTO_NUEVO);
        assertThat(candidato.contexto().getImporte()).isEqualByComparingTo(new BigDecimal("49500"));
    }

    @Test
    void recreaLaPreferenciaSiYaNoExisteEnMercadoPago() {
        var candidato = candidato(false);
        var recreado = candidato(false);
        when(contextoService.findRenovaciones(anyInt(), eq(false), any(), any(), anyInt())).thenReturn(List.of(candidato));
        when(preferenciaCuotaService.crear(any(ContextoMercadoPago.class), eq(candidato.cuota())))
                .thenReturn(null)
                .thenAnswer(invocation -> {
                    var contexto = invocation.getArgument(0, ContextoMercadoPago.class);
                    contexto.setInitPoint("https://mp/nuevo");
                    contexto.setPreferenceId("nueva");
                    return contexto;
                });

        var resultado = useCase.ejecutar(3, false);

        assertThat(resultado.creados()).isEqualTo(1);
        assertThat(resultado.fallidos()).isZero();
        verify(contextoService).actualizarPreferencia(candidato.contexto());
        assertThat(candidato.contexto().getPreferenceId()).isEqualTo("nueva");
    }

    @Test
    void siMercadoPagoFallaMarcaParaReintento() {
        var candidato = candidato(false);
        when(contextoService.findRenovaciones(anyInt(), eq(false), any(), any(), anyInt())).thenReturn(List.of(candidato));
        when(preferenciaCuotaService.crear(any(ContextoMercadoPago.class), eq(candidato.cuota())))
                .thenAnswer(invocation -> {
                    var contexto = invocation.getArgument(0, ContextoMercadoPago.class);
                    // la actualizacion fallida deja lastVencimientoUpdated en null
                    return contexto;
                });

        var resultado = useCase.ejecutar(3, false);

        assertThat(resultado.fallidos()).isEqualTo(1);
        assertThat(resultado.fallidosIds()).containsExactly(candidato.contexto().getMercadoPagoContextId());
        verify(contextoService, never()).actualizarPreferencia(any());
        verify(contextoService).marcarChanged(candidato.contexto().getMercadoPagoContextId(), (byte) 1);
    }

    @Test
    void ignoraCandidatosSinCambioReal() {
        var candidato = candidato(true);
        when(contextoService.findRenovaciones(anyInt(), eq(false), any(), any(), anyInt())).thenReturn(List.of(candidato));

        var resultado = useCase.ejecutar(3, false);

        assertThat(resultado.sinCambios()).isEqualTo(1);
        verify(preferenciaCuotaService, never()).crear(any(), any());
    }

    @Test
    void recorreTodasLasPaginas() {
        var pagina1 = List.of(candidato(false));
        when(contextoService.findRenovaciones(anyInt(), eq(false), any(), any(), anyInt())).thenReturn(pagina1, List.of());
        ReflectionTestUtils.setField(useCase, "pageSize", 1);
        when(preferenciaCuotaService.crear(any(ContextoMercadoPago.class), any()))
                .thenAnswer(invocation -> {
                    var contexto = invocation.getArgument(0, ContextoMercadoPago.class);
                    contexto.setLastVencimientoUpdated(OffsetDateTime.now(ZoneOffset.UTC));
                    return contexto;
                });

        var resultado = useCase.ejecutar(3, false);

        assertThat(resultado.paginas()).isEqualTo(1);
        assertThat(resultado.escaneados()).isEqualTo(1);
    }

    private CandidatoRenovacion candidato(boolean sinCambio) {
        var contexto = ContextoMercadoPago.builder()
                .mercadoPagoContextId(50L)
                .chequeraCuotaId(7L)
                .initPoint("https://mp/init")
                .preferenceId("pref-1")
                .fechaVencimiento(sinCambio ? VENCIMIENTO_NUEVO : VENCIMIENTO_VIEJO)
                .importe(sinCambio ? new BigDecimal("49500") : new BigDecimal("45000"))
                .changed((byte) 0)
                .activo((byte) 1)
                .build();
        var cuota = ChequeraCuota.builder()
                .chequeraCuotaId(7L)
                .productoNombre("Matrícula")
                .build();
        return new CandidatoRenovacion(contexto, cuota, VENCIMIENTO_NUEVO, new BigDecimal("49500"));
    }

}
