package um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.application.usecases;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.contexto.application.service.ContextoService;
import um.tesoreria.mercadopago.hexagonal.mercadopago.procesos.checking.domain.ports.in.ChequearCuotaUseCase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChequearTodasUseCaseImplTest {

    @Mock
    private ContextoService contextoService;
    @Mock
    private ChequearCuotaUseCase chequearCuotaUseCase;

    @InjectMocks
    private ChequearTodasUseCaseImpl useCase;

    @Test
    void chequeaTodosLosContextosActivos() {
        when(contextoService.getActivosParaCambiar()).thenReturn(List.of(1L, 2L));
        when(chequearCuotaUseCase.chequear(1L)).thenReturn("Checked");
        when(chequearCuotaUseCase.chequear(2L)).thenReturn("Checked");

        assertThat(useCase.chequearTodas()).isEqualTo("Checked");

        verify(chequearCuotaUseCase).chequear(1L);
        verify(chequearCuotaUseCase).chequear(2L);
    }

}
