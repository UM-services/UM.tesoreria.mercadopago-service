package um.tesoreria.mercadopago.domain.dto;

import lombok.*;
import um.tesoreria.mercadopago.hexagonal.preference.vacante.domain.model.ReservaVacante;
import um.tesoreria.mercadopago.util.Jsonifier;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UMPreferenceMPDto {

    private MercadoPagoContextDto mercadoPagoContext;
    private ChequeraCuotaDto chequeraCuota;
    private ReservaVacante reservaVacante;

    public String jsonify() {
        return Jsonifier.builder(this).build();
    }

}
