package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.application.service.ChequeraCuotaService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.tipoChequeraTarjeta.application.service.TipoChequeraTarjetaService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.application.exception.MercadoPagoException;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.application.service.MercadoPagoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.domain.ports.in.ActualizarPreferenciaCuotaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.application.service.PreferenciaRequestBuilder;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ChequeraCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PreferenciaMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.SolicitudPreferencia;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PreferenciaCuotaUseCasesTest {

    private static final Long CONTEXTO_ID = 5L;
    private static final Long CUOTA_ID = 100L;

    @Mock
    private MercadoPagoService mercadoPagoService;
    @Mock
    private TipoChequeraTarjetaService tipoChequeraTarjetaService;
    @Mock
    private ChequeraCuotaService chequeraCuotaService;
    @Mock
    private PreferenciaRequestBuilder preferenciaRequestBuilder;
    @Mock
    private ActualizarPreferenciaCuotaUseCase actualizarPreferenciaCuotaUseCase;

    private CrearPreferenciaCuotaUseCaseImpl crearUseCase;
    private ActualizarPreferenciaCuotaUseCaseImpl actualizarUseCase;
    private RecuperarPreferenciaCuotaUseCaseImpl recuperarUseCase;

    @BeforeEach
    void setUp() {
        crearUseCase = new CrearPreferenciaCuotaUseCaseImpl(mercadoPagoService,
                tipoChequeraTarjetaService, preferenciaRequestBuilder, actualizarPreferenciaCuotaUseCase);
        actualizarUseCase = new ActualizarPreferenciaCuotaUseCaseImpl(mercadoPagoService, chequeraCuotaService,
                tipoChequeraTarjetaService, preferenciaRequestBuilder);
        recuperarUseCase = new RecuperarPreferenciaCuotaUseCaseImpl(mercadoPagoService);
    }

    @Test
    void conInitPointSinCambiosDevuelveElMismoContexto() {
        var contexto = contexto();
        contexto.setInitPoint("https://mp/init");

        assertThat(crearUseCase.crear(contexto, cuota())).isSameAs(contexto);
        verifyNoInteractions(mercadoPagoService, preferenciaRequestBuilder);
    }

    @Test
    void conInitPointYChangedDelegaEnLaActualizacion() {
        var contexto = contexto();
        contexto.setInitPoint("https://mp/init");
        contexto.setChanged((byte) 1);
        var cuota = cuota();
        when(actualizarPreferenciaCuotaUseCase.actualizar(contexto, cuota)).thenReturn(contexto);

        assertThat(crearUseCase.crear(contexto, cuota)).isSameAs(contexto);
        verify(actualizarPreferenciaCuotaUseCase).actualizar(contexto, cuota);
    }

    @Test
    void sinInitPointCreaLaPreferenciaSinPersistirElContexto() {
        var contexto = contexto();
        var cuota = cuota();
        when(tipoChequeraTarjetaService.getByUnique(2, 5)).thenReturn(Optional.empty());
        var solicitud = SolicitudPreferencia.builder().build();
        when(preferenciaRequestBuilder.construirParaCuota(contexto, cuota, null)).thenReturn(solicitud);
        when(mercadoPagoService.crear(solicitud)).thenReturn(PreferenciaMercadoPago.builder()
                .id("pref-1").initPoint("https://mp/init").json("{\"id\":\"pref-1\"}").build());

        var result = crearUseCase.crear(contexto, cuota);

        assertThat(result.getInitPoint()).isEqualTo("https://mp/init");
        assertThat(result.getPreferenceId()).isEqualTo("pref-1");
        assertThat(result.getPreference()).isEqualTo("{\"id\":\"pref-1\"}");
    }

    @Test
    void siElSdkFallaDevuelveElContextoSinCambios() {
        var contexto = contexto();
        var cuota = cuota();
        when(tipoChequeraTarjetaService.getByUnique(2, 5)).thenReturn(Optional.empty());
        var solicitud = SolicitudPreferencia.builder().build();
        when(preferenciaRequestBuilder.construirParaCuota(contexto, cuota, null)).thenReturn(solicitud);
        when(mercadoPagoService.crear(solicitud))
                .thenThrow(new MercadoPagoException("boom", 500, "error", null));

        var result = crearUseCase.crear(contexto, cuota);

        assertThat(result.getInitPoint()).isNull();
        assertThat(result.getPreferenceId()).isNull();
    }

    @Test
    void rechazaUnContextoNulo() {
        assertThatThrownBy(() -> crearUseCase.crear(null, cuota()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void actualizarDevuelveNullSiNoEncuentraLaPreferencia() {
        var contexto = contexto();
        contexto.setPreferenceId("pref-1");
        when(mercadoPagoService.recuperar("pref-1")).thenReturn(Optional.empty());

        assertThat(actualizarUseCase.actualizar(contexto)).isNull();
        verifyNoInteractions(chequeraCuotaService);
    }

    @Test
    void actualizarAplicaLaRespuestaYNoActualizaCore() {
        var contexto = contexto();
        contexto.setPreferenceId("pref-1");
        contexto.setChanged((byte) 1);
        var preferencia = PreferenciaMercadoPago.builder().id("pref-1").build();
        when(mercadoPagoService.recuperar("pref-1")).thenReturn(Optional.of(preferencia));
        when(chequeraCuotaService.getById(CUOTA_ID)).thenReturn(cuota());
        when(tipoChequeraTarjetaService.getByUnique(2, 5)).thenReturn(Optional.empty());
        var solicitud = SolicitudPreferencia.builder().build();
        when(preferenciaRequestBuilder.construirParaActualizacion(contexto, preferencia, null)).thenReturn(solicitud);
        when(mercadoPagoService.actualizar(eq("pref-1"), any())).thenReturn(
                PreferenciaMercadoPago.builder().json("{\"id\":\"pref-1\"}").build());

        var result = actualizarUseCase.actualizar(contexto);

        assertThat(result.getChanged()).isZero();
        assertThat(result.getPreference()).isEqualTo("{\"id\":\"pref-1\"}");
        assertThat(result.getLastVencimientoUpdated()).isNotNull();
    }

    @Test
    void actualizarConErrorMantieneElContexto() {
        var contexto = contexto();
        contexto.setPreferenceId("pref-1");
        contexto.setChanged((byte) 1);
        var preferencia = PreferenciaMercadoPago.builder().id("pref-1").build();
        when(mercadoPagoService.recuperar("pref-1")).thenReturn(Optional.of(preferencia));
        when(chequeraCuotaService.getById(CUOTA_ID)).thenReturn(cuota());
        when(tipoChequeraTarjetaService.getByUnique(2, 5)).thenReturn(Optional.empty());
        when(preferenciaRequestBuilder.construirParaActualizacion(contexto, preferencia, null))
                .thenReturn(SolicitudPreferencia.builder().build());
        when(mercadoPagoService.actualizar(eq("pref-1"), any()))
                .thenThrow(new MercadoPagoException("boom", 500, "error", null));

        var result = actualizarUseCase.actualizar(contexto);

        assertThat(result.getChanged()).isEqualTo((byte) 1);
        assertThat(result.getLastVencimientoUpdated()).isNull();
    }

    @Test
    void recuperarDevuelveLaPreferenciaONull() {
        var preferencia = PreferenciaMercadoPago.builder().id("pref-1").build();
        when(mercadoPagoService.recuperar("pref-1")).thenReturn(Optional.of(preferencia));
        when(mercadoPagoService.recuperar("pref-2")).thenReturn(Optional.empty());

        assertThat(recuperarUseCase.recuperar("pref-1")).isSameAs(preferencia);
        assertThat(recuperarUseCase.recuperar("pref-2")).isNull();
    }

    private ContextoMercadoPago contexto() {
        return ContextoMercadoPago.builder()
                .mercadoPagoContextId(CONTEXTO_ID)
                .chequeraCuotaId(CUOTA_ID)
                .importe(new BigDecimal("1234.50"))
                .fechaVencimiento(OffsetDateTime.of(2026, 10, 31, 0, 0, 0, 0, ZoneOffset.UTC))
                .build();
    }

    private ChequeraCuota cuota() {
        return ChequeraCuota.builder()
                .chequeraCuotaId(CUOTA_ID)
                .tipoChequeraId(2)
                .alternativaId(5)
                .build();
    }

}
