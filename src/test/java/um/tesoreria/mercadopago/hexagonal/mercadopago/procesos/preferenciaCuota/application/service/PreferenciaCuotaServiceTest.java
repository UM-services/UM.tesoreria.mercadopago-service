package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.application.service.ContextoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.domain.ports.in.ActualizarPreferenciaCuotaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.domain.ports.in.CrearPreferenciaCuotaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.domain.ports.in.RecuperarPreferenciaCuotaUseCase;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ChequeraCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PreferenciaCuotaServiceTest {

    @Mock
    private CrearPreferenciaCuotaUseCase crearPreferenciaCuotaUseCase;
    @Mock
    private ActualizarPreferenciaCuotaUseCase actualizarPreferenciaCuotaUseCase;
    @Mock
    private RecuperarPreferenciaCuotaUseCase recuperarPreferenciaCuotaUseCase;
    @Mock
    private ContextoService contextoService;

    private PreferenciaCuotaService service;

    @BeforeEach
    void setUp() {
        service = new PreferenciaCuotaService(crearPreferenciaCuotaUseCase, actualizarPreferenciaCuotaUseCase,
                recuperarPreferenciaCuotaUseCase, contextoService);
    }

    @Test
    void crearNoPersisteElContexto() {
        var contexto = contexto();
        var cuota = cuota();
        when(crearPreferenciaCuotaUseCase.crear(contexto, cuota)).thenReturn(contexto);

        assertThat(service.crear(contexto, cuota)).isSameAs(contexto);
        verifyNoInteractions(contextoService);
    }

    @Test
    void crearYGuardarPersisteUnaSolaVezCuandoSeCreoLaPreferencia() {
        var contexto = contexto();
        var cuota = cuota();
        when(crearPreferenciaCuotaUseCase.crear(contexto, cuota)).thenAnswer(invocation -> {
            contexto.setInitPoint("https://mp/init");
            return contexto;
        });

        var result = service.crearYGuardar(contexto, cuota);

        assertThat(result).isSameAs(contexto);
        verify(contextoService).actualizar(contexto);
    }

    @Test
    void crearYGuardarPersisteCuandoHayCambiosPendientes() {
        var contexto = contexto();
        contexto.setInitPoint("https://mp/init");
        contexto.setChanged((byte) 1);
        var cuota = cuota();
        when(crearPreferenciaCuotaUseCase.crear(contexto, cuota)).thenReturn(contexto);

        service.crearYGuardar(contexto, cuota);

        verify(contextoService).actualizar(contexto);
    }

    @Test
    void crearYGuardarNoPersisteSiElContextoNoCambio() {
        var contexto = contexto();
        contexto.setInitPoint("https://mp/init");
        contexto.setChanged((byte) 0);
        var cuota = cuota();
        when(crearPreferenciaCuotaUseCase.crear(contexto, cuota)).thenReturn(contexto);

        service.crearYGuardar(contexto, cuota);

        verify(contextoService, never()).actualizar(any());
    }

    @Test
    void crearYGuardarNoPersisteSiFalloLaCreacion() {
        var contexto = contexto();
        var cuota = cuota();
        when(crearPreferenciaCuotaUseCase.crear(contexto, cuota)).thenReturn(contexto);

        service.crearYGuardar(contexto, cuota);

        verify(contextoService, never()).actualizar(any());
    }

    private ContextoMercadoPago contexto() {
        return ContextoMercadoPago.builder()
                .mercadoPagoContextId(5L)
                .chequeraCuotaId(100L)
                .build();
    }

    private ChequeraCuota cuota() {
        return ChequeraCuota.builder().chequeraCuotaId(100L).build();
    }

}
