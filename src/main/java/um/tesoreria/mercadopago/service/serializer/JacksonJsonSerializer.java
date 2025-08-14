package um.tesoreria.mercadopago.service.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

public final class JacksonJsonSerializer implements JsonSerializer {
    @Override
    public <T> String jsonify(T value) {
        try {
            return JsonMapper
                    .builder()
                    .findAndAddModules()
                    .build()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "jsonify error " + e.getMessage();
        }
    }
}
