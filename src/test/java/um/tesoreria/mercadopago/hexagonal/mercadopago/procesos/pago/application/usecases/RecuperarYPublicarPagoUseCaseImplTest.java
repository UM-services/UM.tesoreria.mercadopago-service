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
    void siNoEncuentraElPagoNoPublica() {
        when(mercadoPagoService.recuperarPago(12345L)).thenReturn(Optional.empty());

        useCase.recuperarYPublicar("12345");

        verifyNoInteractions(mensajeriaService);
    }

    @Test
    void parseaLaReferenciaCortaYPublicaElEvento() {
        publicarConReferencia("100-50");

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
        publicarConReferencia("prefijo-100-50");

        assertThat(capturarEvento().getChequeraCuotaId()).isEqualTo(100L);
    }

    @Test
    void parseaLaReferenciaDeVacanteDeSeisPartes() {
        UUID reserva = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        publicarConReferencia(reserva + "-50");

        var event = capturarEvento();
        assertThat(event.getReservaVacanteId()).isEqualTo(reserva);
        assertThat(event.getMercadoPagoContextId()).isEqualTo(50L);
        assertThat(event.getChequeraCuotaId()).isNull();
    }

    @Test
    void conReferenciaInvalidaNoPublica() {
        publicarConReferencia("abc-def");

        verify(mensajeriaService, never()).publicar(any());
    }

    @Test
    void conReferenciaDeCuatroPartesNoPublica() {
        publicarConReferencia("a-b-c-d");

        verify(mensajeriaService, never()).publicar(any());
    }

    @Test
    void conDataIdNoNumericoPropagaLaExcepcion() {
        assertThatThrownBy(() -> useCase.recuperarYPublicar("abc"))
                .isInstanceOf(NumberFormatException.class);
        verifyNoInteractions(mensajeriaService);
    }

    private void publicarConReferencia(String externalReference) {
        when(mercadoPagoService.recuperarPago(12345L)).thenReturn(Optional.of(pago(externalReference)));
        useCase.recuperarYPublicar("12345");
    }

    private PagoMercadoPago pago(String externalReference) {
        return PagoMercadoPago.builder()
                .paymentId(12345L)
                .externalReference(externalReference)
                .status("approved")
                .statusDetail("accredited")
                .dateApproved(OffsetDateTime.of(2026, 10, 20, 12, 0, 0, 0, ZoneOffset.UTC))
                .dateCreated(OffsetDateTime.of(2026, 10, 20, 11, 0, 0, 0, ZoneOffset.UTC))
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
