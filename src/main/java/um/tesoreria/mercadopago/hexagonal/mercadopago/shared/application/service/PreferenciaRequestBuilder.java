package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ChequeraCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ItemPreferencia;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.MediosPagoPreferencia;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PagadorPreferencia;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ReservaVacante;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.SolicitudPreferencia;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.TipoChequeraTarjeta;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.UrlsRetornoPreferencia;

import java.util.ArrayList;
import java.util.List;

@Service
public class PreferenciaRequestBuilder {

    private static final String URL_RETORNO = "https://www.um.edu.ar";
    private static final String DESCRIPCION_EXTRACTO = "UNIVMENDOZA";

    @Value("${app.notification-url:}")
    private String notificationUrl;

    public SolicitudPreferencia construirParaCuota(ContextoMercadoPago contexto,
                                                   ChequeraCuota cuota,
                                                   TipoChequeraTarjeta tipoChequera) {
        ItemPreferencia item = ItemPreferencia.builder()
                .id("item-key-" + cuota.getFacultadId() + "-" + cuota.getTipoChequeraId() + "-"
                        + cuota.getChequeraSerieId() + "-" + cuota.getProductoId() + "-"
                        + cuota.getAlternativaId() + "-" + cuota.getCuotaId() + "-id-"
                        + cuota.getChequeraCuotaId())
                .titulo(cuota.getProductoNombre())
                .cantidad(1)
                .precioUnitario(contexto.getImporte())
                .categoria("others")
                .moneda("ARS")
                .descripcion(cuota.getProductoNombre() + "-Periodo-" + cuota.getMes() + "-" + cuota.getAnho())
                .build();

        PagadorPreferencia pagador = PagadorPreferencia.builder()
                .nombre(cuota.getPersonaNombre())
                .apellido(cuota.getPersonaApellido())
                .email(cuota.getEmailPersonal())
                .build();

        return construir(contexto, List.of(item), pagador,
                cuota.getChequeraCuotaId() + "-" + contexto.getMercadoPagoContextId(),
                mediosPago(tipoChequera, false));
    }

    public SolicitudPreferencia construirParaReserva(ContextoMercadoPago contexto, ReservaVacante reserva) {
        ItemPreferencia item = ItemPreferencia.builder()
                .id("item-vacante-" + reserva.getReservaVacanteId())
                .titulo(reserva.getCampanhaNombre())
                .cantidad(1)
                .precioUnitario(contexto.getImporte())
                .categoria("others")
                .moneda("ARS")
                .descripcion(reserva.getCampanhaNombre())
                .build();

        PagadorPreferencia pagador = PagadorPreferencia.builder()
                .nombre(reserva.getPersonaNombre())
                .apellido(reserva.getPersonaApellido())
                .email(reserva.getEmailPersonal())
                .build();

        return construir(contexto, List.of(item), pagador,
                reserva.getReservaVacanteId() + "-" + contexto.getMercadoPagoContextId(),
                mediosPago(null, true));
    }

    public SolicitudPreferencia construirParaActualizacion(ContextoMercadoPago contexto,
                                                           um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PreferenciaMercadoPago preferencia,
                                                           TipoChequeraTarjeta tipoChequera) {
        var item = preferencia.getItems().getFirst();
        List<ItemPreferencia> items = List.of(ItemPreferencia.builder()
                .id(item.getId())
                .titulo(item.getTitulo())
                .cantidad(1)
                .precioUnitario(contexto.getImporte())
                .categoria("others")
                .moneda("ARS")
                .descripcion(item.getDescripcion())
                .build());

        return SolicitudPreferencia.builder()
                .items(items)
                .pagador(preferencia.getPagador())
                .urlsRetorno(urlsRetorno())
                .referenciaExterna(preferencia.getReferenciaExterna())
                .urlNotificacion(preferencia.getUrlNotificacion())
                .expira(true)
                .fechaVencimiento(FechasMercadoPago.sumarHorasDeGracia(contexto.getFechaVencimiento()))
                .mediosPago(mediosPago(tipoChequera, false))
                .modoBinario(preferencia.getModoBinario())
                .descripcionExtracto(preferencia.getDescripcionExtracto())
                .build();
    }

    private SolicitudPreferencia construir(ContextoMercadoPago contexto,
                                           List<ItemPreferencia> items,
                                           PagadorPreferencia pagador,
                                           String referenciaExterna,
                                           MediosPagoPreferencia mediosPago) {
        return SolicitudPreferencia.builder()
                .items(items)
                .pagador(pagador)
                .urlsRetorno(urlsRetorno())
                .referenciaExterna(referenciaExterna)
                .urlNotificacion(notificationUrl)
                .expira(true)
                .fechaVencimiento(FechasMercadoPago.sumarHorasDeGracia(contexto.getFechaVencimiento()))
                .mediosPago(mediosPago)
                .modoBinario(true)
                .descripcionExtracto(DESCRIPCION_EXTRACTO)
                .build();
    }

    private UrlsRetornoPreferencia urlsRetorno() {
        return UrlsRetornoPreferencia.builder()
                .exito(URL_RETORNO)
                .pendiente(URL_RETORNO)
                .fallo(URL_RETORNO)
                .build();
    }

    private MediosPagoPreferencia mediosPago(TipoChequeraTarjeta tipoChequera, boolean sinRestricciones) {
        if (sinRestricciones) {
            return MediosPagoPreferencia.builder()
                    .tiposExcluidos(List.of())
                    .build();
        }
        List<String> excluidos = new ArrayList<>(List.of("ticket", "prepaid_card"));
        if (tipoChequera == null) {
            excluidos.add("credit_card");
            return MediosPagoPreferencia.builder()
                    .tiposExcluidos(excluidos)
                    .build();
        }
        return MediosPagoPreferencia.builder()
                .tiposExcluidos(excluidos)
                .cuotas(tipoChequera.getInstallments())
                .cuotasPorDefecto(tipoChequera.getDefaultInstallments())
                .build();
    }

}
