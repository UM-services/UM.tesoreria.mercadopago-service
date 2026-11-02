package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PreferenciaMercadoPago {

    private String id;
    private String initPoint;
    private String json;
    private List<ItemPreferencia> items;
    private PagadorPreferencia pagador;
    private String referenciaExterna;
    private String urlNotificacion;
    private Boolean modoBinario;
    private String descripcionExtracto;
    private Object recursoOriginal;

}
