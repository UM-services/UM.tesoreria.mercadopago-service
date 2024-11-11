package um.tesoreria.mercadopago.service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacultadDto {

    private Integer facultadId;
    private String nombre = "";
    private String codigoempresa = "";
    private String server = "";
    private String dbadm = "";
    private String dsn = "";
    private BigDecimal cuentacontable = BigDecimal.ZERO;
    private String apiserver = "";
    private Long apiport = 0L;
    
}
