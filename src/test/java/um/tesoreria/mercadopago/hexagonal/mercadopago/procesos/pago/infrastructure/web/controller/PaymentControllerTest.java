package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.infrastructure.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.application.service.PagoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.CabecerasWebhook;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.EstadoWebhookPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.ResultadoWebhookPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.PaymentNotificationDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private static final ResultadoWebhookPago PROCESADO =
            new ResultadoWebhookPago(EstadoWebhookPago.PROCESADO, "Payment processed");

    @Mock
    private PagoService pagoService;

    @Mock
    private HttpServletRequest request;

    private PaymentController paymentController;

    @BeforeEach
    void setUp() {
        paymentController = new PaymentController(pagoService);
    }

    @Test
    void listenerProcesaElDataIdDelQueryParam() {
        when(pagoService.processPaymentWebhook(any(), eq("12345"))).thenReturn(PROCESADO);

        ResponseEntity<String> response = paymentController.listener(request, "12345", null, null, null);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Payment processed");
    }

    @Test
    void listenerProcesaElDataIdDelBody() {
        PaymentNotificationDto notification = PaymentNotificationDto.builder()
                .data(PaymentNotificationDto.DataDto.builder().id("67890").build())
                .build();
        when(pagoService.processPaymentWebhook(any(), eq("67890"))).thenReturn(PROCESADO);

        ResponseEntity<String> response = paymentController.listener(request, null, null, null, notification);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Payment processed");
    }

    @Test
    void listenerUsaElIdDeLaNotificacionLegadaCuandoElTopicEsPayment() {
        when(pagoService.processLegacyPaymentNotification("184276812795")).thenReturn(PROCESADO);

        ResponseEntity<String> response = paymentController.listener(request, null, "184276812795", "payment", null);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(pagoService).processLegacyPaymentNotification("184276812795");
        verify(pagoService, never()).processPaymentWebhook(any(), any());
    }

    @Test
    void listenerResponde200CuandoElLegacyEstaApagado() {
        when(pagoService.processLegacyPaymentNotification("184276812795")).thenReturn(
                new ResultadoWebhookPago(EstadoWebhookPago.LEGACY_IGNORADA, "Notificación legacy ignorada"));

        ResponseEntity<String> response = paymentController.listener(request, null, "184276812795", "payment", null);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Notificación legacy ignorada");
    }

    @Test
    void listenerIgnoraLaNotificacionLegadaDeMerchantOrder() {
        ResponseEntity<String> response = paymentController.listener(request, null, "45431143883", "merchant_order", null);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Notificación ignorada: topic merchant_order");
        verify(pagoService, never()).processPaymentWebhook(any(), any());
        verify(pagoService, never()).processLegacyPaymentNotification(any());
    }

    @Test
    void listenerLlamaAlServicioConNullSiFaltaElDataId() {
        when(pagoService.processPaymentWebhook(any(), eq(null)))
                .thenReturn(new ResultadoWebhookPago(EstadoWebhookPago.DATA_ID_FALTANTE,
                        "Falta el parámetro data.id en la URL"));

        ResponseEntity<String> response = paymentController.listener(request, null, null, null, null);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("Falta el parámetro data.id en la URL");
        verify(pagoService).processPaymentWebhook(any(), eq(null));
    }

    @Test
    void listenerDevuelve401CuandoLaFirmaEsInvalida() {
        when(pagoService.processPaymentWebhook(any(), eq("12345")))
                .thenReturn(new ResultadoWebhookPago(EstadoWebhookPago.FIRMA_INVALIDA, "Firma inválida"));

        ResponseEntity<String> response = paymentController.listener(request, "12345", null, null, null);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody()).isEqualTo("Firma inválida");
    }

    @Test
    void listenerDevuelve502CuandoNoSePudoRecuperarElPago() {
        when(pagoService.processPaymentWebhook(any(), eq("12345")))
                .thenReturn(new ResultadoWebhookPago(EstadoWebhookPago.PAGO_NO_ENCONTRADO,
                        "No se pudo recuperar el pago desde Mercado Pago"));

        ResponseEntity<String> response = paymentController.listener(request, "12345", null, null, null);

        assertThat(response.getStatusCode().value()).isEqualTo(502);
    }

    @Test
    void listenerPropagaLasCabecerasDeMercadoPago() {
        when(request.getHeader("x-signature")).thenReturn("ts=1,v1=abc");
        when(request.getHeader("x-request-id")).thenReturn("req-1");
        when(pagoService.processPaymentWebhook(any(), eq("1"))).thenReturn(PROCESADO);

        paymentController.listener(request, "1", null, null, null);

        ArgumentCaptor<CabecerasWebhook> captor = ArgumentCaptor.forClass(CabecerasWebhook.class);
        verify(pagoService).processPaymentWebhook(captor.capture(), eq("1"));
        assertThat(captor.getValue().xSignature()).isEqualTo("ts=1,v1=abc");
        assertThat(captor.getValue().xRequestId()).isEqualTo("req-1");
    }

    @Test
    void updateDevuelve200CuandoElPagoSePublica() {
        when(pagoService.retrieveAndPublishPayment("12345")).thenReturn(true);

        ResponseEntity<Void> response = paymentController.update("12345");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void updateDevuelve502CuandoElPagoNoSePuedeRecuperar() {
        when(pagoService.retrieveAndPublishPayment("12345")).thenReturn(false);

        ResponseEntity<Void> response = paymentController.update("12345");

        assertThat(response.getStatusCode().value()).isEqualTo(502);
    }

}
