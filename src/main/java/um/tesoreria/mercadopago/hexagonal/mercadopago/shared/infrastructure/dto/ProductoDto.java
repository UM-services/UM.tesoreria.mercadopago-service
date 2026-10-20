package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto;

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
