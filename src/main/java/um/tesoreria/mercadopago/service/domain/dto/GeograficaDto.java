package um.tesoreria.mercadopago.service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeograficaDto {
    private Integer geograficaId;
    private String nombre = "";
    private Byte sinChequera = 0;
}
