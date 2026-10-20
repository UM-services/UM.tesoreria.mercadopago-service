package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ItemPreferencia {

    private String id;
    private String titulo;
    private String descripcion;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private String categoria;
    private String moneda;

}
