package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaVacante.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import um.tesoreria.mercadopago.audit.AuditEventPublisher;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.application.service.ContextoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.application.exception.MercadoPagoException;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.application.service.MercadoPagoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.application.service.PreferenciaRequestBuilder;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PreferenciaMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ReservaVacante;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.SolicitudPreferencia;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CrearPreferenciaVacanteUseCaseImplTest {

    @Mock
    private MercadoPagoService mercadoPagoService;
    @Mock
    private ContextoService contextoService;
    @Mock
    private PreferenciaRequestBuilder preferenciaRequestBuilder;
    @Mock
    private AuditEventPublisher audit;

    private CrearPreferenciaVacanteUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CrearPreferenciaVacanteUseCaseImpl(mercadoPagoService, contextoService,
                preferenciaRequestBuilder, audit);
    }

    @Test
    void siYaTieneInitPointLoDevuelveSinLlamarAlSdk() {
        var contexto = ContextoMercadoPago.builder().mercadoPagoContextId(5L).initPoint("https://mp/init").build();

        assertThat(useCase.crear(contexto, reserva())).isSameAs(contexto);
        verifyNoInteractions(mercadoPagoService, preferenciaRequestBuilder);
    }

    @Test
    void creaLaPreferenciaSinRestriccionesYPersisteElContexto() {
        var contexto = ContextoMercadoPago.builder().mercadoPagoContextId(5L).build();
        var reserva = reserva();
        var solicitud = SolicitudPreferencia.builder().build();
        when(preferenciaRequestBuilder.construirParaReserva(contexto, reserva)).thenReturn(solicitud);
        when(mercadoPagoService.crear(solicitud)).thenReturn(PreferenciaMercadoPago.builder()
                .id("pref-1").initPoint("https://mp/init").json("{}").build());

        var result = useCase.crear(contexto, reserva);

        assertThat(result.getInitPoint()).isEqualTo("https://mp/init");
        assertThat(result.getPreferenceId()).isEqualTo("pref-1");
        verify(contextoService).actualizar(contexto);
        verify(audit).publishPreferenceCreated(eq("pref-1"), any());
    }

    @Test
    void siElSdkFallaNoActualizaCore() {
        var contexto = ContextoMercadoPago.builder().mercadoPagoContextId(5L).build();
        var reserva = reserva();
        var solicitud = SolicitudPreferencia.builder().build();
        when(preferenciaRequestBuilder.construirParaReserva(contexto, reserva)).thenReturn(solicitud);
        when(mercadoPagoService.crear(solicitud))
                .thenThrow(new MercadoPagoException("boom", 500, "error", null));

        assertThat(useCase.crear(contexto, reserva).getInitPoint()).isNull();
        verify(contextoService, never()).actualizar(any());
        verify(audit).publishPreferenceFailed("INSERT", null, "boom");
    }

    @Test
    void rechazaUnContextoNulo() {
        assertThatThrownBy(() -> useCase.crear(null, reserva()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private ReservaVacante reserva() {
        return ReservaVacante.builder()
                .reservaVacanteId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .campanhaNombre("Curso de verano")
                .build();
    }

}
