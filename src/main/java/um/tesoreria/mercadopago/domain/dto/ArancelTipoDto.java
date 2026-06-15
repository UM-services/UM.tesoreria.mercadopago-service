package um.tesoreria.mercadopago.domain.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArancelTipoDto {
    private Integer arancelTipoId;
    @Builder.Default
    private String descripcion = "";
    @Builder.Default
    private Byte medioArancel = 0;
    private Integer arancelTipoIdCompleto;
}
