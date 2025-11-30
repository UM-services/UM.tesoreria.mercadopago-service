package um.tesoreria.mercadopago.service.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import um.tesoreria.mercadopago.service.domain.dto.PaymentNotificationDto;
import um.tesoreria.mercadopago.service.service.PaymentService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private HttpServletRequest request;

    private PaymentController paymentController;

    @BeforeEach
    void setUp() {
        paymentController = new PaymentController(paymentService);
    }

    @Test
    void listener_shouldProcessPayment_whenDataIdIsQueryParam() {
        String dataId = "12345";
        when(paymentService.processPaymentWebhook(request, dataId)).thenReturn("Payment processed");

        ResponseEntity<String> response = paymentController.listener(request, dataId, null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Payment processed", response.getBody());
        verify(paymentService).processPaymentWebhook(request, dataId);
    }

    @Test
    void listener_shouldProcessPayment_whenDataIdIsRequestBody() {
        String dataId = "67890";
        PaymentNotificationDto notification = PaymentNotificationDto.builder()
                .data(PaymentNotificationDto.DataDto.builder().id(dataId).build())
                .build();

        when(paymentService.processPaymentWebhook(request, dataId)).thenReturn("Payment processed");

        ResponseEntity<String> response = paymentController.listener(request, null, notification);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Payment processed", response.getBody());
        verify(paymentService).processPaymentWebhook(request, dataId);
    }

    @Test
    void listener_shouldCallService_whenDataIdIsMissing() {
        when(paymentService.processPaymentWebhook(request, null)).thenReturn("Error");

        ResponseEntity<String> response = paymentController.listener(request, null, null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Error", response.getBody());
        verify(paymentService).processPaymentWebhook(request, null);
    }
}
