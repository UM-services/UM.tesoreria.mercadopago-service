package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampanhaDto {

    private UUID campanhaId;
    private String nombre;
    private Byte activa;
    private BigDecimal valorReserva;
    private LocalDateTime created;

}
