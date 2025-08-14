package um.tesoreria.mercadopago.service.domain.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoChequeraMercadoPagoCreditCardDto {

    private UUID id;
    private Integer tipoChequeraId;
    private Integer alternativaId;
    private Integer installments;
    private Integer defaultInstallments;
    private Byte active;
    private TipoChequeraDto tipoChequera;

    public String jsonify() {
        try {
            return JsonMapper.builder()
                    .findAndAddModules()
                    .build()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "jsonify error " + e.getMessage();
        }
    }

}
