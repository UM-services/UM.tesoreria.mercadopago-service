package um.tesoreria.mercadopago.service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArancelTipoDto {
    private Integer arancelTipoId;
    private String descripcion = "";
    private Byte medioArancel = 0;
    private Integer arancelTipoIdCompleto;
}
