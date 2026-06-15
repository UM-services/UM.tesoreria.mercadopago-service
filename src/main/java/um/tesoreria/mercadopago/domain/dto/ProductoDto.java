package um.tesoreria.mercadopago.domain.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDto {

    private Integer productoId;
    @Builder.Default
    private String nombre = "";

}
