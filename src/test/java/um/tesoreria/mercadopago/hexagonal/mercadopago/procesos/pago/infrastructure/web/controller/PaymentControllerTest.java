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
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.PaymentNotificationDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

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
        String dataId = "12345";
        when(pagoService.processPaymentWebhook(any(), eq(dataId))).thenReturn("Payment processed");

        ResponseEntity<String> response = paymentController.listener(request, dataId, null);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Payment processed");
    }

    @Test
    void listenerProcesaElDataIdDelBody() {
        String dataId = "67890";
        PaymentNotificationDto notification = PaymentNotificationDto.builder()
                .data(PaymentNotificationDto.DataDto.builder().id(dataId).build())
                .build();
        when(pagoService.processPaymentWebhook(any(), eq(dataId))).thenReturn("Payment processed");

        ResponseEntity<String> response = paymentController.listener(request, null, notification);

        assertThat(response.getBody()).isEqualTo("Payment processed");
    }

    @Test
    void listenerLlamaAlServicioConNullSiFaltaElDataId() {
        when(pagoService.processPaymentWebhook(any(), eq(null))).thenReturn("Error");

        ResponseEntity<String> response = paymentController.listener(request, null, null);

        assertThat(response.getBody()).isEqualTo("Error");
        verify(pagoService).processPaymentWebhook(any(), eq(null));
    }

    @Test
    void listenerPropagaLasCabecerasDeMercadoPago() {
        when(request.getHeader("x-signature")).thenReturn("ts=1,v1=abc");
        when(request.getHeader("x-request-id")).thenReturn("req-1");
        when(pagoService.processPaymentWebhook(any(), eq("1"))).thenReturn("ok");

        paymentController.listener(request, "1", null);

        ArgumentCaptor<CabecerasWebhook> captor = ArgumentCaptor.forClass(CabecerasWebhook.class);
        verify(pagoService).processPaymentWebhook(captor.capture(), eq("1"));
        assertThat(captor.getValue().xSignature()).isEqualTo("ts=1,v1=abc");
        assertThat(captor.getValue().xRequestId()).isEqualTo("req-1");
    }

}
