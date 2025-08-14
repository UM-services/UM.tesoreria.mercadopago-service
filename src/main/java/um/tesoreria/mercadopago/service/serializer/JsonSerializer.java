package um.tesoreria.mercadopago.service.serializer;

public interface JsonSerializer {
    <T> String jsonify(T value);
}
