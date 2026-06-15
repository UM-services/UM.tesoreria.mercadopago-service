package um.tesoreria.mercadopago.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import um.tesoreria.mercadopago.util.Jsonifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChequeraCuotaDto {

    private Long chequeraCuotaId;
    private Long chequeraId;
    private Integer facultadId;
    private Integer tipoChequeraId;
    private Long chequeraSerieId;
    private Integer productoId;
    private Integer alternativaId;
    private Integer cuotaId;
    @Builder.Default
    private Integer mes = 0;
    @Builder.Default
    private Integer anho = 0;
    private Integer arancelTipoId;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime vencimiento1;
    @Builder.Default
    private BigDecimal importe1 = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal importe1Original = BigDecimal.ZERO;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime vencimiento2;
    @Builder.Default
    private BigDecimal importe2 = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal importe2Original = BigDecimal.ZERO;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime vencimiento3;
    @Builder.Default
    private BigDecimal importe3 = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal importe3Original = BigDecimal.ZERO;

    @Builder.Default
    private String codigoBarras = "";
    @Builder.Default
    private String i2Of5 = "";
    @Builder.Default
    private Byte pagado = 0;
    @Builder.Default
    private Byte baja = 0;
    @Builder.Default
    private Byte manual = 0;
    @Builder.Default
    private Byte compensada = 0;
    @Builder.Default
    private Integer tramoId = 0;
    
    private FacultadDto facultad;
    private TipoChequeraDto tipoChequera;
    private ProductoDto producto;
    private ChequeraSerieDto chequeraSerie;

    public String jsonify() {
        return Jsonifier.builder(this).build();
    }

}
