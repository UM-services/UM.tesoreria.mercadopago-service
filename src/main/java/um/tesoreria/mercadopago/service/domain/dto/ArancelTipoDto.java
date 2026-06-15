package um.tesoreria.mercadopago.service.domain.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArancelTipoDto {
    private Integer arancelTipoId;
    private String descripcion = "";
    private Byte medioArancel = 0;
    private Integer arancelTipoIdCompleto;
}
