package um.tesoreria.mercadopago.service.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomicilioDto {
    private Long domicilioId;
    private BigDecimal personaId;
    private Integer documentoId;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime fecha = OffsetDateTime.now();
    
    private String calle = "";
    private String puerta = "";
    private String piso = "";
    private String dpto = "";
    private String telefono = "";
    private String movil = "";
    private String observaciones = "";
    private String codigoPostal = "";
    private Integer facultadId;
    private Integer provinciaId;
    private Integer localidadId;
    private String emailPersonal = "";
    private String emailInstitucional = "";
    private String laboral = "";
}
