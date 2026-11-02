package um.tesoreria.mercadopago.contract;

import com.mercadopago.client.preference.PreferencePaymentTypeRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.infrastructure.client.mapper.SolicitudPreferenciaMapper;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.application.service.PreferenciaRequestBuilder;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ChequeraCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ItemPreferencia;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PagadorPreferencia;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PreferenciaMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ReservaVacante;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.TipoChequeraTarjeta;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PreferenceRequestContractTest {

    private static final OffsetDateTime VENCIMIENTO = OffsetDateTime.of(2026, 10, 31, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final UUID VACANTE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private PreferenciaRequestBuilder builder;
    private SolicitudPreferenciaMapper mapper;

    @BeforeEach
    void setUp() {
        builder = new PreferenciaRequestBuilder();
        ReflectionTestUtils.setField(builder, "notificationUrl", "https://notify.um.edu.ar");
        mapper = new SolicitudPreferenciaMapper();
    }

    @Test
    void armaLaSolicitudDeCuotaConLasMismasReglas() {
        PreferenceRequest request = mapper.toSdk(builder.construirParaCuota(contexto(50L, "1234.50"), cuota(), tipoChequera()));

        assertThat(request.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getId()).isEqualTo("item-key-1-2-3-4-5-6-id-100");
            assertThat(item.getTitle()).isEqualTo("Matrícula");
            assertThat(item.getDescription()).isEqualTo("Matrícula-Periodo-8-2026");
            assertThat(item.getQuantity()).isEqualTo(1);
            assertThat(item.getUnitPrice()).isEqualByComparingTo("1234.50");
            assertThat(item.getCurrencyId()).isEqualTo("ARS");
            assertThat(item.getCategoryId()).isEqualTo("others");
        });
        assertThat(request.getPayer().getName()).isEqualTo("Ana");
        assertThat(request.getPayer().getSurname()).isEqualTo("Perez");
        assertThat(request.getPayer().getEmail()).isEqualTo("ana@um.edu.ar");
        assertThat(request.getExternalReference()).isEqualTo("100-50");
        assertThat(request.getNotificationUrl()).isEqualTo("https://notify.um.edu.ar");
        assertThat(request.getExpirationDateTo()).isEqualTo(VENCIMIENTO.plusHours(30));
        assertThat(request.getExpires()).isTrue();
        assertThat(request.getBinaryMode()).isTrue();
        assertThat(request.getStatementDescriptor()).isEqualTo("UNIVMENDOZA");
        assertThat(request.getBackUrls().getSuccess()).isEqualTo("https://www.um.edu.ar");
        assertThat(request.getBackUrls().getPending()).isEqualTo("https://www.um.edu.ar");
        assertThat(request.getBackUrls().getFailure()).isEqualTo("https://www.um.edu.ar");
        assertThat(request.getPaymentMethods().getExcludedPaymentTypes().stream()
                .map(PreferencePaymentTypeRequest::getId))
                .containsExactly("ticket", "prepaid_card");
        assertThat(request.getPaymentMethods().getInstallments()).isEqualTo(6);
        assertThat(request.getPaymentMethods().getDefaultInstallments()).isEqualTo(1);
    }

    @Test
    void excluyeTarjetaDeCreditoCuandoNoHayConfiguracionDeTipoDeChequera() {
        PreferenceRequest request = mapper.toSdk(builder.construirParaCuota(contexto(50L, "1234.50"), cuota(), null));

        assertThat(request.getPaymentMethods().getExcludedPaymentTypes().stream()
                .map(PreferencePaymentTypeRequest::getId))
                .containsExactly("ticket", "prepaid_card", "credit_card");
        assertThat(request.getPaymentMethods().getInstallments()).isNull();
        assertThat(request.getPaymentMethods().getDefaultInstallments()).isNull();
    }

    @Test
    void armaLaSolicitudDeVacanteSinRestricciones() {
        PreferenceRequest request = mapper.toSdk(builder.construirParaReserva(contexto(60L, "500.00"), reserva()));

        assertThat(request.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getId()).isEqualTo("item-vacante-" + VACANTE_ID);
            assertThat(item.getTitle()).isEqualTo("Curso de verano");
            assertThat(item.getDescription()).isEqualTo("Curso de verano");
            assertThat(item.getUnitPrice()).isEqualByComparingTo("500.00");
        });
        assertThat(request.getPayer().getName()).isEqualTo("Ana");
        assertThat(request.getPayer().getSurname()).isEqualTo("Perez");
        assertThat(request.getPayer().getEmail()).isEqualTo("ana@um.edu.ar");
        assertThat(request.getExternalReference()).isEqualTo(VACANTE_ID + "-60");
        assertThat(request.getExpirationDateTo()).isEqualTo(VENCIMIENTO.plusHours(30));
        assertThat(request.getPaymentMethods().getExcludedPaymentTypes()).isEmpty();
        assertThat(request.getPaymentMethods().getInstallments()).isNull();
        assertThat(request.getPaymentMethods().getDefaultInstallments()).isNull();
    }

    @Test
    void armaLaActualizacionConLosDatosDeLaPreferenciaExistente() {
        var preferencia = PreferenciaMercadoPago.builder()
                .items(List.of(ItemPreferencia.builder()
                        .id("item-1").titulo("Matrícula").descripcion("Matrícula-Periodo-8-2026").build()))
                .pagador(PagadorPreferencia.builder().nombre("Ana").apellido("Perez").email("ana@um.edu.ar").build())
                .referenciaExterna("100-50")
                .urlNotificacion("https://notify")
                .modoBinario(true)
                .descripcionExtracto("UNIVMENDOZA")
                .build();

        PreferenceRequest request = mapper.toSdk(builder.construirParaActualizacion(
                contexto(50L, "2000.00"), preferencia, tipoChequera()));

        assertThat(request.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getId()).isEqualTo("item-1");
            assertThat(item.getTitle()).isEqualTo("Matrícula");
            assertThat(item.getUnitPrice()).isEqualByComparingTo("2000.00");
            assertThat(item.getDescription()).isEqualTo("Matrícula-Periodo-8-2026");
        });
        assertThat(request.getPayer().getName()).isEqualTo("Ana");
        assertThat(request.getExternalReference()).isEqualTo("100-50");
        assertThat(request.getNotificationUrl()).isEqualTo("https://notify");
        assertThat(request.getExpirationDateTo()).isEqualTo(VENCIMIENTO.plusHours(30));
        assertThat(request.getPaymentMethods().getInstallments()).isEqualTo(6);
        assertThat(request.getBinaryMode()).isTrue();
        assertThat(request.getStatementDescriptor()).isEqualTo("UNIVMENDOZA");
    }

    private ChequeraCuota cuota() {
        return ChequeraCuota.builder()
                .chequeraCuotaId(100L)
                .facultadId(1)
                .tipoChequeraId(2)
                .chequeraSerieId(3L)
                .productoId(4)
                .alternativaId(5)
                .cuotaId(6)
                .mes(8)
                .anho(2026)
                .productoNombre("Matrícula")
                .personaNombre("Ana")
                .personaApellido("Perez")
                .emailPersonal("ana@um.edu.ar")
                .build();
    }

    private ReservaVacante reserva() {
        return ReservaVacante.builder()
                .reservaVacanteId(VACANTE_ID)
                .campanhaNombre("Curso de verano")
                .campanhaValorReserva(new BigDecimal("500.00"))
                .personaNombre("Ana")
                .personaApellido("Perez")
                .emailPersonal("ana@um.edu.ar")
                .build();
    }

    private ContextoMercadoPago contexto(Long id, String importe) {
        return ContextoMercadoPago.builder()
                .mercadoPagoContextId(id)
                .importe(new BigDecimal(importe))
                .fechaVencimiento(VENCIMIENTO)
                .build();
    }

    private TipoChequeraTarjeta tipoChequera() {
        return TipoChequeraTarjeta.builder()
                .tipoChequeraId(2)
                .alternativaId(5)
                .installments(6)
                .defaultInstallments(1)
                .active((byte) 1)
                .build();
    }

}
