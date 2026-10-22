package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SolicitudPreferencia {

    private List<ItemPreferencia> items;
    private PagadorPreferencia pagador;
    private UrlsRetornoPreferencia urlsRetorno;
    private String referenciaExterna;
    private String urlNotificacion;
    private Boolean expira;
    private OffsetDateTime fechaVencimiento;
    private MediosPagoPreferencia mediosPago;
    private Boolean modoBinario;
    private String descripcionExtracto;

}
