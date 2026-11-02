package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.application.service.ContextoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.preferenciaCuota.application.service.PreferenciaCuotaService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ChequeraCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoConCuota;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.ContextoMercadoPago;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChequearCuotaUseCaseImplTest {

    @Mock
    private ContextoService contextoService;
    @Mock
    private PreferenciaCuotaService preferenciaCuotaService;

    private ChequearCuotaUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ChequearCuotaUseCaseImpl(contextoService, preferenciaCuotaService);
    }

    @Test
    void siNoHayContextoNoCreaNiActualiza() {
        when(contextoService.crearContexto(7L)).thenReturn(null);

        assertThat(useCase.chequear(7L)).isEqualTo("Checked");

        verifyNoInteractions(preferenciaCuotaService);
        verify(contextoService, never()).actualizar(any());
    }

    @Test
    void siNoSePudoCrearLaPreferenciaNoActualiza() {
        var par = par(5L);
        when(contextoService.crearContexto(7L)).thenReturn(par);
        when(preferenciaCuotaService.crear(par.getContexto(), par.getCuota())).thenReturn(null);

        assertThat(useCase.chequear(7L)).isEqualTo("Checked");

        verify(contextoService, never()).actualizar(any());
    }

    @Test
    void actualizaElContextoCreado() {
        var par = par(5L);
        par.getContexto().setInitPoint("https://mp/init");
        par.getContexto().setChanged((byte) 1);
        when(contextoService.crearContexto(7L)).thenReturn(par);
        when(preferenciaCuotaService.crear(par.getContexto(), par.getCuota())).thenReturn(par.getContexto());

        assertThat(useCase.chequear(7L)).isEqualTo("Checked");

        verify(contextoService).actualizar(par.getContexto());
    }

    @Test
    void siMercadoPagoNoDevuelveLinkNoPersiste() {
        var par = par(5L);
        when(contextoService.crearContexto(7L)).thenReturn(par);
        when(preferenciaCuotaService.crear(par.getContexto(), par.getCuota())).thenReturn(par.getContexto());

        assertThat(useCase.chequear(7L)).isEqualTo("Checked");

        verify(contextoService, never()).actualizar(any());
    }

    @Test
    void siNoCambioNadaNoPersiste() {
        var par = par(5L);
        par.getContexto().setInitPoint("https://mp/init");
        par.getContexto().setChanged((byte) 0);
        when(contextoService.crearContexto(7L)).thenReturn(par);
        when(preferenciaCuotaService.crear(par.getContexto(), par.getCuota())).thenReturn(par.getContexto());

        assertThat(useCase.chequear(7L)).isEqualTo("Checked");

        verify(contextoService, never()).actualizar(any());
    }

    @Test
    void siFallaLaActualizacionIgualDevuelveChecked() {
        var par = par(5L);
        par.getContexto().setInitPoint("https://mp/init");
        when(contextoService.crearContexto(7L)).thenReturn(par);
        when(preferenciaCuotaService.crear(par.getContexto(), par.getCuota())).thenReturn(par.getContexto());
        when(contextoService.actualizar(par.getContexto())).thenThrow(new RuntimeException("core caido"));

        assertThat(useCase.chequear(7L)).isEqualTo("Checked");
    }

    private ContextoConCuota par(Long contextId) {
        return ContextoConCuota.builder()
                .contexto(ContextoMercadoPago.builder().mercadoPagoContextId(contextId).build())
                .cuota(ChequeraCuota.builder().chequeraCuotaId(7L).build())
                .build();
    }

}
