package um.tesoreria.mercadopago.hexagonal.preference.vacante.infrastructure.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.mercadopago.domain.dto.MercadoPagoContextDto;
import um.tesoreria.mercadopago.domain.dto.UMPreferenceMPDto;
import um.tesoreria.mercadopago.hexagonal.preference.vacante.application.service.PreferenceVacanteService;

@RestController
@RequestMapping("/api/tesoreria/mercadopago/vacante/preference")
@RequiredArgsConstructor
public class PreferenceVacanteController {

    private final PreferenceVacanteService service;

    @PostMapping("/create")
    public ResponseEntity<MercadoPagoContextDto> createPreference(
            @RequestBody UMPreferenceMPDto umPreferenceMPDto) {
        return ResponseEntity.ok(service.createPreference(umPreferenceMPDto));
    }

}
