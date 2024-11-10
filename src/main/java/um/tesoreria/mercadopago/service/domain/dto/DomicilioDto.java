package um.tesoreria.mercadopago.service.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DomicilioDto {
    private Long domicilioId;
    private BigDecimal personaId;
    private Integer documentoId;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
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
