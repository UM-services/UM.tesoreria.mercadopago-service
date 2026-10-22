package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.pago.domain.model;

public record ComponentesFirma(String ts, String v1) {

    public static ComponentesFirma parse(String xSignature) {
        String ts = null;
        String v1 = null;
        for (String part : xSignature.split(",")) {
            String[] keyValue = part.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                if ("ts".equals(key)) {
                    ts = value;
                } else if ("v1".equals(key)) {
                    v1 = value;
                }
            }
        }
        return ts != null && v1 != null ? new ComponentesFirma(ts, v1) : null;
    }

}
