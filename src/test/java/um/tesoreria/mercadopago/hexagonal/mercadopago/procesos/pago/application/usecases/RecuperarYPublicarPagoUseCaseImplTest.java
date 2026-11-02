package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.application.service.MercadoPagoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.application.service.MensajeriaService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mensajeria.domain.model.PagoProcesadoEvento;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.OrigenNotificacion;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PagoMercadoPago;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecuperarYPublicarPagoUseCaseImplTest {

    @Mock
    private MercadoPagoService mercadoPagoService;
    @Mock
    private MensajeriaService mensajeriaService;

    private RecuperarYPublicarPagoUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new RecuperarYPublicarPagoUseCaseImpl(mercadoPagoService, mensajeriaService);
    }

    @Test
    void siNoEncuentraElPagoNoPublicaYDevuelveFalse() {
        when(mercadoPagoService.recuperarPago(12345L)).thenReturn(Optional.empty());

        assertThat(useCase.recuperarYPublicar("12345", OrigenNotificacion.WEBHOOK_V1)).isFalse();
        verifyNoInteractions(mensajeriaService);
    }

    @Test
    void parseaLaReferenciaCortaYPublicaElEvento() {
        assertThat(publicarConReferencia("100-50")).isTrue();

        var event = capturarEvento();
        assertThat(event.getChequeraCuotaId()).isEqualTo(100L);
        assertThat(event.getMercadoPagoContextId()).isEqualTo(50L);
        assertThat(event.getReservaVacanteId()).isNull();
        assertThat(event.getPaymentId()).isEqualTo("12345");
        assertThat(event.getStatus()).isEqualTo("approved");
        assertThat(event.getPaymentJson()).isEqualTo("{\"id\":12345}");
    }

    @Test
    void parseaLaReferenciaLargaDeTresPartes() {
        assertThat(publicarConReferencia("prefijo-100-50")).isTrue();

        assertThat(capturarEvento().getChequeraCuotaId()).isEqualTo(100L);
    }

    @Test
    void parseaLaReferenciaDeVacanteDeSeisPartes() {
        UUID reserva = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        assertThat(publicarConReferencia(reserva + "-50")).isTrue();

        var event = capturarEvento();
        assertThat(event.getReservaVacanteId()).isEqualTo(reserva);
        assertThat(event.getMercadoPagoContextId()).isEqualTo(50L);
        assertThat(event.getChequeraCuotaId()).isNull();
    }

    @Test
    void conReferenciaInvalidaNoPublicaYDevuelveFalse() {
        assertThat(publicarConReferencia("abc-def")).isFalse();

        verify(mensajeriaService, never()).publicar(any());
    }

    @Test
    void conReferenciaNulaNoPublicaYDevuelveFalse() {
        assertThat(publicarConReferencia(null)).isFalse();

        verify(mensajeriaService, never()).publicar(any());
    }

    @Test
    void conReferenciaDeCuatroPartesNoPublica() {
        assertThat(publicarConReferencia("a-b-c-d")).isFalse();

        verify(mensajeriaService, never()).publicar(any());
    }

    @Test
    void conDataIdNoNumericoPropagaLaExcepcion() {
        assertThatThrownBy(() -> useCase.recuperarYPublicar("abc", OrigenNotificacion.WEBHOOK_V1))
                .isInstanceOf(NumberFormatException.class);
        verifyNoInteractions(mensajeriaService);
    }

    @Test
    void elEventoIndicaElOrigenYLaUltimaActualizacionDelPago() {
        assertThat(publicarConReferencia("100-50", OrigenNotificacion.IPN_LEGACY)).isTrue();

        var event = capturarEvento();
        assertThat(event.getSource()).isEqualTo("IPN_LEGACY");
        assertThat(event.getDateLastUpdated())
                .isEqualTo(OffsetDateTime.of(2026, 10, 20, 12, 30, 0, 0, ZoneOffset.UTC));
    }

    @Test
    void elOrigenManualSePropagaEnElEvento() {
        assertThat(publicarConReferencia("100-50", OrigenNotificacion.MANUAL)).isTrue();

        assertThat(capturarEvento().getSource()).isEqualTo("MANUAL");
    }

    private boolean publicarConReferencia(String externalReference) {
        return publicarConReferencia(externalReference, OrigenNotificacion.WEBHOOK_V1);
    }

    private boolean publicarConReferencia(String externalReference, OrigenNotificacion origen) {
        when(mercadoPagoService.recuperarPago(12345L)).thenReturn(Optional.of(pago(externalReference)));
        return useCase.recuperarYPublicar("12345", origen);
    }

    private PagoMercadoPago pago(String externalReference) {
        return PagoMercadoPago.builder()
                .paymentId(12345L)
                .externalReference(externalReference)
                .status("approved")
                .statusDetail("accredited")
                .dateApproved(OffsetDateTime.of(2026, 10, 20, 12, 0, 0, 0, ZoneOffset.UTC))
                .dateCreated(OffsetDateTime.of(2026, 10, 20, 11, 0, 0, 0, ZoneOffset.UTC))
                .dateLastUpdated(OffsetDateTime.of(2026, 10, 20, 12, 30, 0, 0, ZoneOffset.UTC))
                .transactionAmount(new BigDecimal("1234.50"))
                .json("{\"id\":12345}")
                .build();
    }

    private PagoProcesadoEvento capturarEvento() {
        ArgumentCaptor<PagoProcesadoEvento> captor = ArgumentCaptor.forClass(PagoProcesadoEvento.class);
        verify(mensajeriaService).publicar(captor.capture());
        return captor.getValue();
    }

}
