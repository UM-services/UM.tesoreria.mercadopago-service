package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.renovacion.infrastructure.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.renovacion.domain.model.ResultadoRenovacion;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.renovacion.domain.model.SolicitudRenovacion;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.renovacion.domain.ports.in.RenovarVencimientosUseCase;

@RestController
@RequestMapping("/api/tesoreria/mercadopago/renovacion")
@RequiredArgsConstructor
public class RenovacionController {

    private final RenovarVencimientosUseCase renovarVencimientosUseCase;

    /**
     * Corrida manual del proceso. Con {@code dryRun=true} solo informa que haria.
     */
    @PostMapping("/run")
    public ResponseEntity<ResultadoRenovacion> ejecutar(
            @RequestBody(required = false) SolicitudRenovacion solicitud) {
        var pedido = solicitud != null ? solicitud : new SolicitudRenovacion(3, false);
        return ResponseEntity.ok(renovarVencimientosUseCase.ejecutar(pedido.lookbackEfectivo(), pedido.esDryRun()));
    }

}
