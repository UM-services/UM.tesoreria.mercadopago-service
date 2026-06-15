package um.tesoreria.mercadopago.service.domain.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.*;
import um.tesoreria.mercadopago.service.util.Jsonifier;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UMPreferenceMPDto {

    private MercadoPagoContextDto mercadoPagoContext;
    private ChequeraCuotaDto chequeraCuota;

    public String jsonify() {
        return Jsonifier.builder(this).build();
    }

}
