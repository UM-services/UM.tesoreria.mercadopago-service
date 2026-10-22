package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.infrastructure.web.controller;

import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.application.service.PreferenciaCuotaService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.MercadoPagoContextDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.dto.UMPreferenceMPDto;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.mapper.ContextoMercadoPagoDtoMapper;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.infrastructure.mapper.PreferenciaDtoMapper;

@RestController
@RequestMapping("/api/tesoreria/mercadopago/preference")
@RequiredArgsConstructor
public class PreferenceCuotaController {

    private final PreferenciaCuotaService preferenciaCuotaService;
    private final PreferenciaDtoMapper preferenciaDtoMapper;
    private final ContextoMercadoPagoDtoMapper contextoMapper;

    @PostMapping("/create")
    public ResponseEntity<MercadoPagoContextDto> createPreference(@RequestBody UMPreferenceMPDto dto) {
        var contextoConCuota = preferenciaDtoMapper.toDomain(dto);
        var contexto = preferenciaCuotaService.crearYGuardar(
                contextoConCuota == null ? null : contextoConCuota.getContexto(),
                contextoConCuota == null ? null : contextoConCuota.getCuota());
        return ResponseEntity.ok(contextoMapper.toDto(contexto));
    }

    @GetMapping("/retrieve/{preferenceId}")
    public ResponseEntity<Preference> retrievePreference(@PathVariable String preferenceId) {
        var preferencia = preferenciaCuotaService.recuperar(preferenceId);
        return ResponseEntity.ok(preferencia == null ? null : (Preference) preferencia.getRecursoOriginal());
    }

}
