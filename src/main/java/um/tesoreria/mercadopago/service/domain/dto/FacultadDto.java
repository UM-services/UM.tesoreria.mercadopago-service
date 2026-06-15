package um.tesoreria.mercadopago.service.domain.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
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
