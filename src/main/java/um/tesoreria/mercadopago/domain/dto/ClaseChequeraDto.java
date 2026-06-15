package um.tesoreria.mercadopago.domain.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaseChequeraDto {
    private Integer claseChequeraId;
    private String nombre;
    @Builder.Default
    private Byte preuniversitario = 0;
    @Builder.Default
    private Byte grado = 0;
    @Builder.Default
    private Byte posgrado = 0;
    @Builder.Default
    private Byte curso = 0;
    @Builder.Default
    private Byte secundario = 0;
    @Builder.Default
    private Byte titulo = 0;
}
