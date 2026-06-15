package um.tesoreria.mercadopago.service.domain.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDto {
    private Integer productoId;
    private String nombre = "";
}
