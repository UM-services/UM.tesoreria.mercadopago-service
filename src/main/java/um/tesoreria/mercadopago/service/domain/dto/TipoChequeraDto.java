package um.tesoreria.mercadopago.service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoChequeraDto {
    private Integer tipoChequeraId;
    private String nombre = "";
    private String prefijo = "";
    private Integer geograficaId = 1;
    private Integer claseChequeraId = 2;
    private Byte imprimir = 0;
    private Byte contado = 0;
    private Byte multiple = 0;
    private GeograficaDto geografica;
    private ClaseChequeraDto claseChequera;
}
