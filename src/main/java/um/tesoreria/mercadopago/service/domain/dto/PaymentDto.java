package um.tesoreria.mercadopago.service.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class PaymentDto {

    private Long id;

    @JsonProperty("live_mode")
    private Boolean liveMode;

    private String type;

    @JsonProperty("date_created")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
    private OffsetDateTime dateCreated;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("api_version")
    private String apiVersion;

    private String action;

    private DataPaymentDto data;

}
