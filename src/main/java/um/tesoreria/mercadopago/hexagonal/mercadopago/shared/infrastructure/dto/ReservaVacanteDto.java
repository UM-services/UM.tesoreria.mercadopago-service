package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaVacanteDto {

    private UUID reservaVacanteId;
    private Long personaUniqueId;
    private UUID campanhaId;
    private String estado;
    private BigDecimal importe;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime vencimiento;

    private CampanhaDto campanha;
    private PersonaDto persona;
    private DomicilioDto domicilio;
    private LocalDateTime created;
    private LocalDateTime updated;

}
