package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeograficaDto {
    private Integer geograficaId;
    @Builder.Default
    private String nombre = "";
    @Builder.Default
    private Byte sinChequera = 0;
}
