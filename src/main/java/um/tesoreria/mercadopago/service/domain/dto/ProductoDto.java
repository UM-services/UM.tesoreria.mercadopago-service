package um.tesoreria.mercadopago.service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDto {
    private Integer productoId;
    private String nombre = "";
}
