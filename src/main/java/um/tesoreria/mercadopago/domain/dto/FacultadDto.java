package um.tesoreria.mercadopago.domain.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultadDto {

    private Integer facultadId;
    @Builder.Default
    private String nombre = "";
    @Builder.Default
    private String codigoempresa = "";
    @Builder.Default
    private String server = "";
    @Builder.Default
    private String dbadm = "";
    @Builder.Default
    private String dsn = "";
    @Builder.Default
    private BigDecimal cuentacontable = BigDecimal.ZERO;
    @Builder.Default
    private String apiserver = "";
    @Builder.Default
    private Long apiport = 0L;
    
}
