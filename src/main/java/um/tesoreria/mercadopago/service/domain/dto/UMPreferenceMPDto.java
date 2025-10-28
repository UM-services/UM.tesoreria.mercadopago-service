package um.tesoreria.mercadopago.service.domain.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import um.tesoreria.mercadopago.service.util.Jsonifier;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UMPreferenceMPDto {

    private MercadoPagoContextDto mercadoPagoContext;
    private ChequeraCuotaDto chequeraCuota;

    public String jsonify() {
        return Jsonifier.builder(this).build();
    }

}
