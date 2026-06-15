package um.tesoreria.mercadopago.domain.dto;

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

    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime fecha = OffsetDateTime.now();

    @Builder.Default
    private String calle = "";
    @Builder.Default
    private String puerta = "";
    @Builder.Default
    private String piso = "";
    @Builder.Default
    private String dpto = "";
    @Builder.Default
    private String telefono = "";
    @Builder.Default
    private String movil = "";
    @Builder.Default
    private String observaciones = "";
    @Builder.Default
    private String codigoPostal = "";
    private Integer facultadId;
    private Integer provinciaId;
    private Integer localidadId;
    @Builder.Default
    private String emailPersonal = "";
    @Builder.Default
    private String emailInstitucional = "";
    @Builder.Default
    private String laboral = "";

}
