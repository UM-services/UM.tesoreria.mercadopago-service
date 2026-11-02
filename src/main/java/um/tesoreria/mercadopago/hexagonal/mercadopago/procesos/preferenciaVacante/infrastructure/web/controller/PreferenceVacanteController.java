package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaVacante.infrastructure.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaVacante.application.service.PreferenciaVacanteService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.MercadoPagoContextDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.UMPreferenceMPDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.mapper.ContextoMercadoPagoDtoMapper;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.mapper.PreferenciaDtoMapper;

@RestController
@RequestMapping("/api/tesoreria/mercadopago/vacante/preference")
@RequiredArgsConstructor
public class PreferenceVacanteController {

    private final PreferenciaVacanteService preferenciaVacanteService;
    private final PreferenciaDtoMapper preferenciaDtoMapper;
    private final ContextoMercadoPagoDtoMapper contextoMapper;

    @PostMapping("/create")
    public ResponseEntity<MercadoPagoContextDto> createPreference(@RequestBody UMPreferenceMPDto dto) {
        var contexto = contextoMapper.toDomain(dto == null ? null : dto.getMercadoPagoContext());
        var reserva = preferenciaDtoMapper.toReserva(dto);
        return ResponseEntity.ok(contextoMapper.toDto(preferenciaVacanteService.crear(contexto, reserva)));
    }

}
