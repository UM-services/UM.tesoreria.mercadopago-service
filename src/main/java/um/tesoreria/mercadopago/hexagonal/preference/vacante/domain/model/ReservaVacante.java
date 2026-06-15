package um.tesoreria.mercadopago.hexagonal.preference.vacante.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import um.tesoreria.mercadopago.domain.dto.DomicilioDto;
import um.tesoreria.mercadopago.domain.dto.PersonaDto;
import um.tesoreria.mercadopago.util.Jsonifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaVacante {

    private UUID reservaVacanteId;
    private Long personaUniqueId;
    private UUID campanhaId;
    private String estado;
    private BigDecimal importe;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime vencimiento;
    private Campanha campanha;
    private PersonaDto persona;
    private DomicilioDto domicilio;
    private LocalDateTime created;
    private LocalDateTime updated;

    public String jsonify() {
        return Jsonifier.builder(this).build();
    }
}
