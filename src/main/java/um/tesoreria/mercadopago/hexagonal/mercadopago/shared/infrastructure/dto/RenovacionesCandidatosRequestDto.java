package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.support.Jsonifyable;

import java.time.OffsetDateTime;

/**
 * Pedido de candidatos a renovar enviado al core. Se manda por body (POST), sin query params.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenovacionesCandidatosRequestDto implements Jsonifyable {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime hoy;

    private Integer lookbackDias;
    private Boolean reintentos;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime afterFecha;

    private Long afterId;
    private Integer size;

}
