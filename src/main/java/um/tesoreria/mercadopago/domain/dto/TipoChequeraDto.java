package um.tesoreria.mercadopago.domain.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoChequeraDto {

    private Integer tipoChequeraId;
    @Builder.Default
    private String nombre = "";
    @Builder.Default
    private String prefijo = "";
    @Builder.Default
    private Integer geograficaId = 1;
    @Builder.Default
    private Integer claseChequeraId = 2;
    @Builder.Default
    private Byte imprimir = 0;
    @Builder.Default
    private Byte contado = 0;
    @Builder.Default
    private Byte multiple = 0;
    private String emailCopia;
    private GeograficaDto geografica;
    private ClaseChequeraDto claseChequera;

}
