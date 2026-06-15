package um.tesoreria.mercadopago.service.domain.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeograficaDto {
    private Integer geograficaId;
    private String nombre = "";
    private Byte sinChequera = 0;
}
