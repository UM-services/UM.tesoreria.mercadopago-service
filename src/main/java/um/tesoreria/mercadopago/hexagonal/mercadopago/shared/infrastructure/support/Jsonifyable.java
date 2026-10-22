package um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.support;

public interface Jsonifyable {

    default String jsonify() {
        return Jsonifier.builder(this).build();
    }

}
