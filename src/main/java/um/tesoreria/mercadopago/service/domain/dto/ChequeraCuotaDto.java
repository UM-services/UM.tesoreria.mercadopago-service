package um.tesoreria.mercadopago.service.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import um.tesoreria.mercadopago.service.util.Jsonifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
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
    private Integer mes = 0;
    private Integer anho = 0;
    private Integer arancelTipoId;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    private OffsetDateTime vencimiento1;
    private BigDecimal importe1 = BigDecimal.ZERO;
    private BigDecimal importe1Original = BigDecimal.ZERO;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    private OffsetDateTime vencimiento2;
    private BigDecimal importe2 = BigDecimal.ZERO;
    private BigDecimal importe2Original = BigDecimal.ZERO;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    private OffsetDateTime vencimiento3;
    private BigDecimal importe3 = BigDecimal.ZERO;
    private BigDecimal importe3Original = BigDecimal.ZERO;
    
    private String codigoBarras = "";
    private String i2Of5 = "";
    private Byte pagado = 0;
    private Byte baja = 0;
    private Byte manual = 0;
    private Byte compensada = 0;
    private Integer tramoId = 0;
    
    private FacultadDto facultad;
    private TipoChequeraDto tipoChequera;
    private ProductoDto producto;
    private ChequeraSerieDto chequeraSerie;

    public String jsonify() {
        return Jsonifier.builder(this).build();
    }

}
