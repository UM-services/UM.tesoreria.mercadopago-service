package um.tesoreria.mercadopago.contract;

import com.mercadopago.resources.preference.Preference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.chequera.application.service.ChequeraService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.chequera.infrastructure.web.controller.ChequeraController;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.application.service.CheckingService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.infrastructure.web.controller.CheckingController;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.application.service.PagoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.EstadoWebhookPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model.ResultadoWebhookPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.infrastructure.web.controller.PaymentController;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.application.service.PreferenciaCuotaService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.infrastructure.web.controller.PreferenceCuotaController;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaVacante.application.service.PreferenciaVacanteService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaVacante.infrastructure.web.controller.PreferenceVacanteController;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.renovacion.domain.ports.in.RenovarVencimientosUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.renovacion.infrastructure.web.controller.RenovacionController;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ChequeraCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoConCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PreferenciaMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.ChequeraCuotaDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.MercadoPagoContextDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.UMPreferenceMPDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.mapper.ChequeraCuotaDtoMapper;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.mapper.ContextoMercadoPagoDtoMapper;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.mapper.PreferenciaDtoMapper;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.mapper.ReservaVacanteDtoMapper;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HttpContractTest {

    private PreferenciaCuotaService preferenciaCuotaService;
    private PreferenciaVacanteService preferenciaVacanteService;
    private PagoService pagoService;
    private ChequeraService chequeraService;
    private CheckingService checkingService;
    private RenovarVencimientosUseCase renovarVencimientosUseCase;
    private PreferenciaDtoMapper preferenciaDtoMapper;
    private ContextoMercadoPagoDtoMapper contextoMapper;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        preferenciaCuotaService = mock(PreferenciaCuotaService.class);
        preferenciaVacanteService = mock(PreferenciaVacanteService.class);
        pagoService = mock(PagoService.class);
        chequeraService = mock(ChequeraService.class);
        checkingService = mock(CheckingService.class);
        renovarVencimientosUseCase = mock(RenovarVencimientosUseCase.class);
        contextoMapper = new ContextoMercadoPagoDtoMapper();
        preferenciaDtoMapper = new PreferenciaDtoMapper(contextoMapper,
                new ChequeraCuotaDtoMapper(), new ReservaVacanteDtoMapper());

        mockMvc = MockMvcBuilders.standaloneSetup(
                new PreferenceCuotaController(preferenciaCuotaService, preferenciaDtoMapper, contextoMapper),
                new PreferenceVacanteController(preferenciaVacanteService, preferenciaDtoMapper, contextoMapper),
                new PaymentController(pagoService),
                new ChequeraController(chequeraService, preferenciaDtoMapper),
                new CheckingController(checkingService),
                new RenovacionController(renovarVencimientosUseCase)
        ).build();
    }

    @Test
    void declaraLosDiezEndpointsConLasMismasRutasYVerbos() {
        assertThat(PreferenceCuotaController.class.getAnnotation(RequestMapping.class).value())
                .containsExactly("/api/tesoreria/mercadopago/preference");
        assertThat(PreferenceVacanteController.class.getAnnotation(RequestMapping.class).value())
                .containsExactly("/api/tesoreria/mercadopago/vacante/preference");
        assertThat(PaymentController.class.getAnnotation(RequestMapping.class).value())
                .containsExactly("/api/tesoreria/mercadopago/payment");
        assertThat(ChequeraController.class.getAnnotation(RequestMapping.class).value())
                .containsExactly("/api/tesoreria/mercadopago/chequera");
        assertThat(CheckingController.class.getAnnotation(RequestMapping.class).value())
                .containsExactly("/api/tesoreria/mercadopago/checking");
        assertThat(RenovacionController.class.getAnnotation(RequestMapping.class).value())
                .containsExactly("/api/tesoreria/mercadopago/renovacion");

        Map<String, String> rutas = Stream.of(
                        rutas(PreferenceCuotaController.class),
                        rutas(PreferenceVacanteController.class),
                        rutas(PaymentController.class),
                        rutas(ChequeraController.class),
                        rutas(CheckingController.class),
                        rutas(RenovacionController.class))
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertThat(rutas).containsEntry("PreferenceCuotaController.createPreference", "POST /create")
                .containsEntry("PreferenceCuotaController.retrievePreference", "GET /retrieve/{preferenceId}")
                .containsEntry("PreferenceVacanteController.createPreference", "POST /create")
                .containsEntry("PaymentController.listener", "POST /listener")
                .containsEntry("PaymentController.update", "GET /update/{dataId}")
                .containsEntry("ChequeraController.createChequeraContext", "GET /create/context/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}")
                .containsEntry("ChequeraController.createChequeraContextBulk", "GET /create/context/bulk/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}")
                .containsEntry("CheckingController.checkingCuota", "GET /cuota/{chequeraCuotaId}")
                .containsEntry("CheckingController.checkingAllActive", "GET /all/active")
                .containsEntry("RenovacionController.ejecutar", "POST /run")
                .hasSize(10);
    }

    @Test
    void crearPreferenciaDeCuotaDevuelveElContexto() throws Exception {
        when(preferenciaCuotaService.crearYGuardar(any(), any())).thenReturn(contexto(1L));

        mockMvc.perform(post("/api/tesoreria/mercadopago/preference/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mercadoPagoContext\":{\"mercadoPagoContextId\":1,\"importe\":100.00}}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mercadoPagoContextId").value(1))
                .andExpect(jsonPath("$.importe").value(100.00));
    }

    @Test
    void recuperarPreferenciaDevuelveElJsonDelSdk() throws Exception {
        Preference preference = mock(Preference.class);
        when(preference.getId()).thenReturn("pref-123");
        when(preference.getInitPoint()).thenReturn("https://mp/init");
        when(preferenciaCuotaService.recuperar("pref-123"))
                .thenReturn(PreferenciaMercadoPago.builder().recursoOriginal(preference).build());

        mockMvc.perform(get("/api/tesoreria/mercadopago/preference/retrieve/pref-123"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("pref-123"))
                .andExpect(jsonPath("$.initPoint").value("https://mp/init"));

        verify(preferenciaCuotaService).recuperar("pref-123");
    }

    @Test
    void crearPreferenciaDeVacanteDevuelveElContexto() throws Exception {
        when(preferenciaVacanteService.crear(any(), any())).thenReturn(contexto(2L));

        mockMvc.perform(post("/api/tesoreria/mercadopago/vacante/preference/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mercadoPagoContext\":{\"mercadoPagoContextId\":2,\"importe\":200.00}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mercadoPagoContextId").value(2));
    }

    @Test
    void listenerUsaElQueryParamYDevuelveElMensajeExacto() throws Exception {
        when(pagoService.processPaymentWebhook(any(), eq("12345"))).thenReturn(procesado());

        mockMvc.perform(post("/api/tesoreria/mercadopago/payment/listener").param("data.id", "12345"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment processed"));

        verify(pagoService).processPaymentWebhook(any(), eq("12345"));
    }

    @Test
    void listenerUsaElDataIdDelBodyCuandoNoVieneEnLaUrl() throws Exception {
        when(pagoService.processPaymentWebhook(any(), eq("67890"))).thenReturn(procesado());

        mockMvc.perform(post("/api/tesoreria/mercadopago/payment/listener")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"data\":{\"id\":\"67890\"}}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment processed"));

        verify(pagoService).processPaymentWebhook(any(), eq("67890"));
    }

    @Test
    void listenerUsaElIdLegacyCuandoElTopicEsPayment() throws Exception {
        when(pagoService.processLegacyPaymentNotification("184276812795")).thenReturn(procesado());

        mockMvc.perform(post("/api/tesoreria/mercadopago/payment/listener")
                        .param("id", "184276812795")
                        .param("topic", "payment"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment processed"));

        verify(pagoService).processLegacyPaymentNotification("184276812795");
    }

    @Test
    void listenerIgnoraElTopicMerchantOrder() throws Exception {
        mockMvc.perform(post("/api/tesoreria/mercadopago/payment/listener")
                        .param("id", "45431143883")
                        .param("topic", "merchant_order"))
                .andExpect(status().isOk())
                .andExpect(content().string("Notificación ignorada: topic merchant_order"));
    }

    @Test
    void listenerDevuelve400SiNoHayDataIdNiIdLegacy() throws Exception {
        when(pagoService.processPaymentWebhook(any(), eq(null)))
                .thenReturn(new ResultadoWebhookPago(EstadoWebhookPago.DATA_ID_FALTANTE,
                        "Falta el parámetro data.id en la URL"));

        mockMvc.perform(post("/api/tesoreria/mercadopago/payment/listener"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Falta el parámetro data.id en la URL"));
    }

    @Test
    void listenerDevuelve401CuandoLaFirmaEsInvalida() throws Exception {
        when(pagoService.processPaymentWebhook(any(), eq("12345")))
                .thenReturn(new ResultadoWebhookPago(EstadoWebhookPago.FIRMA_INVALIDA, "Firma inválida"));

        mockMvc.perform(post("/api/tesoreria/mercadopago/payment/listener").param("data.id", "12345"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Firma inválida"));
    }

    @Test
    void updateDePagoDevuelve200SinBody() throws Exception {
        when(pagoService.retrieveAndPublishPayment("12345")).thenReturn(true);

        mockMvc.perform(get("/api/tesoreria/mercadopago/payment/update/12345"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(pagoService).retrieveAndPublishPayment("12345");
    }

    @Test
    void updateDePagoDevuelve502SiNoSePuedeRecuperar() throws Exception {
        when(pagoService.retrieveAndPublishPayment("12345")).thenReturn(false);

        mockMvc.perform(get("/api/tesoreria/mercadopago/payment/update/12345"))
                .andExpect(status().isBadGateway());
    }

    @Test
    void crearContextosDeChequeraDevuelveLaLista() throws Exception {
        when(chequeraService.createChequeraContext(1, 2, 3L, 4))
                .thenReturn(List.of(contextoConCuota(10L)));

        mockMvc.perform(get("/api/tesoreria/mercadopago/chequera/create/context/1/2/3/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mercadoPagoContext.mercadoPagoContextId").value(10));
    }

    @Test
    void crearContextosDeChequeraBulkDevuelveLaLista() throws Exception {
        when(chequeraService.createChequeraContextBulk(1, 2, 3L, 4))
                .thenReturn(List.of(contextoConCuota(11L)));

        mockMvc.perform(get("/api/tesoreria/mercadopago/chequera/create/context/bulk/1/2/3/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mercadoPagoContext.mercadoPagoContextId").value(11));
    }

    @Test
    void checkingCuotaDevuelveChecked() throws Exception {
        when(checkingService.checkingCuota(7L)).thenReturn("Checked");

        mockMvc.perform(get("/api/tesoreria/mercadopago/checking/cuota/7"))
                .andExpect(status().isOk())
                .andExpect(content().string("Checked"));
    }

    @Test
    void checkingAllActiveDevuelveChecked() throws Exception {
        when(checkingService.checkingAllActive()).thenReturn("Checked");

        mockMvc.perform(get("/api/tesoreria/mercadopago/checking/all/active"))
                .andExpect(status().isOk())
                .andExpect(content().string("Checked"));
    }

    @Test
    void losEndpointsGetNoAceptanPost() throws Exception {
        mockMvc.perform(post("/api/tesoreria/mercadopago/checking/all/active"))
                .andExpect(status().isMethodNotAllowed());
    }

    private ResultadoWebhookPago procesado() {
        return new ResultadoWebhookPago(EstadoWebhookPago.PROCESADO, "Payment processed");
    }

    private ContextoMercadoPago contexto(Long id) {
        return ContextoMercadoPago.builder()
                .mercadoPagoContextId(id)
                .importe(new BigDecimal("100.00"))
                .build();
    }

    private ContextoConCuota contextoConCuota(Long contextId) {
        var cuotaDto = ChequeraCuotaDto.builder()
                .chequeraCuotaId(contextId)
                .build();
        return ContextoConCuota.builder()
                .contexto(contexto(contextId))
                .cuota(ChequeraCuota.builder()
                        .chequeraCuotaId(contextId)
                        .original(cuotaDto)
                        .build())
                .build();
    }

    private Map<String, String> rutas(Class<?> controller) {
        return Arrays.stream(controller.getDeclaredMethods())
                .flatMap(method -> {
                    String key = controller.getSimpleName() + "." + method.getName();
                    if (method.isAnnotationPresent(GetMapping.class)) {
                        return Arrays.stream(method.getAnnotation(GetMapping.class).value())
                                .map(path -> Map.entry(key, "GET " + path));
                    }
                    if (method.isAnnotationPresent(PostMapping.class)) {
                        return Arrays.stream(method.getAnnotation(PostMapping.class).value())
                                .map(path -> Map.entry(key, "POST " + path));
                    }
                    return Stream.empty();
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
