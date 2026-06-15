package um.tesoreria.mercadopago.hexagonal.preference.cuota.infrastructure.web.controller;

import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import um.tesoreria.mercadopago.domain.dto.MercadoPagoContextDto;
import um.tesoreria.mercadopago.domain.dto.UMPreferenceMPDto;
import um.tesoreria.mercadopago.hexagonal.preference.cuota.application.service.PreferenceCuotaService;
import um.tesoreria.mercadopago.hexagonal.preference.service.PreferenceService;

@RestController
@RequestMapping("/api/tesoreria/mercadopago/preference")
@RequiredArgsConstructor
public class PreferenceCuotaController {

    private final PreferenceCuotaService service;
    private final PreferenceService preferenceService;

    @PostMapping("/create")
    public ResponseEntity<MercadoPagoContextDto> createPreference(
            @RequestBody UMPreferenceMPDto umPreferenceMPDto) {
        return ResponseEntity.ok(service.createPreference(umPreferenceMPDto));
    }

    @GetMapping("/retrieve/{preferenceId}")
    public ResponseEntity<Preference> retrievePreference(@PathVariable String preferenceId) {
        return ResponseEntity.ok(preferenceService.retrievePreference(preferenceId));
    }

}
