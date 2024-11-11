package um.tesoreria.mercadopago.service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UMPreferenceMPDto {

    private MercadoPagoContextDto mercadoPagoContext;
    private ChequeraCuotaDto chequeraCuota;

}
