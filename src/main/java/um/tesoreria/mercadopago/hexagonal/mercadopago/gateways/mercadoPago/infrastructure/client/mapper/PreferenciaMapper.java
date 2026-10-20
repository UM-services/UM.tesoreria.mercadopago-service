package um.tesoreria.mercadopago.hexagonal.mercadopago.gateways.mercadoPago.infrastructure.client.mapper;

import com.mercadopago.resources.preference.Preference;
import org.springframework.stereotype.Component;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ItemPreferencia;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PagadorPreferencia;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PreferenciaMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.support.Jsonifier;

import java.util.List;

@Component
public class PreferenciaMapper {

    public PreferenciaMercadoPago toDomain(Preference preference) {
        if (preference == null) return null;

        List<ItemPreferencia> items = preference.getItems() == null ? null : preference.getItems().stream()
                .map(item -> ItemPreferencia.builder()
                        .id(item.getId())
                        .titulo(item.getTitle())
                        .descripcion(item.getDescription())
                        .cantidad(item.getQuantity())
                        .precioUnitario(item.getUnitPrice())
                        .categoria(item.getCategoryId())
                        .moneda(item.getCurrencyId())
                        .build())
                .toList();

        PagadorPreferencia pagador = preference.getPayer() == null ? null : PagadorPreferencia.builder()
                .nombre(preference.getPayer().getName())
                .apellido(preference.getPayer().getSurname())
                .email(preference.getPayer().getEmail())
                .build();

        return PreferenciaMercadoPago.builder()
                .id(preference.getId())
                .initPoint(preference.getInitPoint())
                .json(Jsonifier.builder(preference).build())
                .items(items)
                .pagador(pagador)
                .referenciaExterna(preference.getExternalReference())
                .urlNotificacion(preference.getNotificationUrl())
                .modoBinario(preference.getBinaryMode())
                .descripcionExtracto(preference.getStatementDescriptor())
                .recursoOriginal(preference)
                .build();
    }

}
