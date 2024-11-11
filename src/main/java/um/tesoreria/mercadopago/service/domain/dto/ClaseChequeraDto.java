package um.tesoreria.mercadopago.service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaseChequeraDto {
    private Integer claseChequeraId;
    private String nombre;
    private Byte preuniversitario = 0;
    private Byte grado = 0;
    private Byte posgrado = 0;
    private Byte curso = 0;
    private Byte secundario = 0;
    private Byte titulo = 0;
}
