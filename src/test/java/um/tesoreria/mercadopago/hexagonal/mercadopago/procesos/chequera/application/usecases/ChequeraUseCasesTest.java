package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.chequera.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.chequeraCuota.application.service.ChequeraCuotaService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.application.service.ContextoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.application.service.PreferenciaCuotaService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ChequeraCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoConCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChequeraUseCasesTest {

    @Mock
    private ChequeraCuotaService chequeraCuotaService;
    @Mock
    private ContextoService contextoService;
    @Mock
    private PreferenciaCuotaService preferenciaCuotaService;

    private CrearContextosChequeraUseCaseImpl crearUseCase;
    private CrearContextosChequeraBulkUseCaseImpl bulkUseCase;

    @BeforeEach
    void setUp() {
        crearUseCase = new CrearContextosChequeraUseCaseImpl(chequeraCuotaService, contextoService, preferenciaCuotaService);
        bulkUseCase = new CrearContextosChequeraBulkUseCaseImpl(contextoService, preferenciaCuotaService);
    }

    @Test
    void creaUnContextoPorCuotaPendienteYActualizaCadaUno() {
        var cuota1 = cuota(100L);
        var cuota2 = cuota(101L);
        var par = par(10L, cuota1);
        when(chequeraCuotaService.getPendientes(1, 2, 3L, 4)).thenReturn(List.of(cuota1, cuota2));
        when(contextoService.crearContexto(100L)).thenReturn(par);
        when(contextoService.crearContexto(101L)).thenReturn(null);
        when(preferenciaCuotaService.crear(par.getContexto(), par.getCuota())).thenReturn(par.getContexto());

        var result = crearUseCase.crear(1, 2, 3L, 4);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCuota()).isSameAs(cuota1);
        verify(contextoService).actualizar(par.getContexto());
    }

    @Test
    void noAgregaLaPreferenciaSiElSdkNoLaCreo() {
        var cuota = cuota(100L);
        var par = par(10L, cuota);
        when(chequeraCuotaService.getPendientes(1, 2, 3L, 4)).thenReturn(List.of(cuota));
        when(contextoService.crearContexto(100L)).thenReturn(par);
        when(preferenciaCuotaService.crear(par.getContexto(), par.getCuota())).thenReturn(null);

        assertThat(crearUseCase.crear(1, 2, 3L, 4)).isEmpty();
        verify(contextoService, never()).actualizar(any());
    }

    @Test
    void siFallaLaActualizacionIgualAgregaLaPreferencia() {
        var cuota = cuota(100L);
        var par = par(10L, cuota);
        when(chequeraCuotaService.getPendientes(1, 2, 3L, 4)).thenReturn(List.of(cuota));
        when(contextoService.crearContexto(100L)).thenReturn(par);
        when(preferenciaCuotaService.crear(par.getContexto(), par.getCuota())).thenReturn(par.getContexto());
        when(contextoService.actualizar(any())).thenThrow(new RuntimeException("core caido"));

        assertThat(crearUseCase.crear(1, 2, 3L, 4)).hasSize(1);
    }

    @Test
    void bulkActualizaTodosLosContextosEnUnaSolaLlamada() {
        var par1 = par(10L, cuota(100L));
        var par2 = par(20L, cuota(101L));
        when(contextoService.crearContextosChequera(1, 2, 3L, 4)).thenReturn(List.of(par1, par2));
        when(preferenciaCuotaService.crear(par1.getContexto(), par1.getCuota())).thenReturn(par1.getContexto());
        when(preferenciaCuotaService.crear(par2.getContexto(), par2.getCuota())).thenReturn(null);

        var result = bulkUseCase.crear(1, 2, 3L, 4);

        assertThat(result).hasSize(1);
        verify(contextoService).actualizarTodos(List.of(par1.getContexto()));
    }

    @Test
    void bulkNoActualizaSiNoCreoNingunaPreferencia() {
        var par = par(10L, cuota(100L));
        when(contextoService.crearContextosChequera(1, 2, 3L, 4)).thenReturn(List.of(par));
        when(preferenciaCuotaService.crear(par.getContexto(), par.getCuota())).thenReturn(null);

        assertThat(bulkUseCase.crear(1, 2, 3L, 4)).isEmpty();
        verify(contextoService, never()).actualizarTodos(any());
    }

    @Test
    void bulkSiFallaLaActualizacionIgualDevuelveLasPreferencias() {
        var par = par(10L, cuota(100L));
        when(contextoService.crearContextosChequera(1, 2, 3L, 4)).thenReturn(List.of(par));
        when(preferenciaCuotaService.crear(par.getContexto(), par.getCuota())).thenReturn(par.getContexto());
        when(contextoService.actualizarTodos(any())).thenThrow(new RuntimeException("core caido"));

        assertThat(bulkUseCase.crear(1, 2, 3L, 4)).hasSize(1);
    }

    private ChequeraCuota cuota(Long id) {
        return ChequeraCuota.builder().chequeraCuotaId(id).build();
    }

    private ContextoConCuota par(Long contextId, ChequeraCuota cuota) {
        return ContextoConCuota.builder()
                .contexto(ContextoMercadoPago.builder().mercadoPagoContextId(contextId).build())
                .cuota(cuota)
                .build();
    }

}
