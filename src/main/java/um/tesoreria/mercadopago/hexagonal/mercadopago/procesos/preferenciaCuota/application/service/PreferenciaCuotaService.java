package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.application.service.ContextoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.domain.ports.in.ActualizarPreferenciaCuotaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.domain.ports.in.CrearPreferenciaCuotaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.domain.ports.in.RecuperarPreferenciaCuotaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ChequeraCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.PreferenciaMercadoPago;

@Service
@RequiredArgsConstructor
public class PreferenciaCuotaService {

    private final CrearPreferenciaCuotaUseCase crearPreferenciaCuotaUseCase;
    private final ActualizarPreferenciaCuotaUseCase actualizarPreferenciaCuotaUseCase;
    private final RecuperarPreferenciaCuotaUseCase recuperarPreferenciaCuotaUseCase;
    private final ContextoService contextoService;

    public ContextoMercadoPago crear(ContextoMercadoPago contexto, ChequeraCuota cuota) {
        return crearPreferenciaCuotaUseCase.crear(contexto, cuota);
    }

    /**
     * Crea o actualiza la preferencia en Mercado Pago y persiste el contexto una única vez
     * mediante el caso de uso de actualización. Solo persiste si el contexto fue modificado.
     */
    public ContextoMercadoPago crearYGuardar(ContextoMercadoPago contexto, ChequeraCuota cuota) {
        boolean requierePersistencia = contexto != null
                && (contexto.getInitPoint() == null || Byte.valueOf((byte) 1).equals(contexto.getChanged()));
        var resultado = crear(contexto, cuota);
        if (requierePersistencia && resultado != null && resultado.getInitPoint() != null) {
            contextoService.actualizar(resultado);
        }
        return resultado;
    }

    public ContextoMercadoPago actualizar(ContextoMercadoPago contexto) {
        return actualizarPreferenciaCuotaUseCase.actualizar(contexto);
    }

    public PreferenciaMercadoPago recuperar(String preferenceId) {
        return recuperarPreferenciaCuotaUseCase.recuperar(preferenceId);
    }

}
