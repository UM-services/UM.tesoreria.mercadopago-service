package um.tesoreria.mercadopago.hexagonal.mercadopago.datos.tipoChequeraTarjeta.application.usecases;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import um.tesoreria.mercadopago.hexagonal.mercadopago.datos.tipoChequeraTarjeta.domain.ports.out.TipoChequeraTarjetaRepository;
import um.tesoreria.mercadopago.hexagonal.mercadopago.shared.domain.model.TipoChequeraTarjeta;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTipoChequeraTarjetaUseCaseImplTest {

    @Mock
    private TipoChequeraTarjetaRepository repository;

    @InjectMocks
    private GetTipoChequeraTarjetaUseCaseImpl useCase;

    @Test
    void devuelveLaConfiguracionActivaDeLaMismaAlternativa() {
        when(repository.findByUnique(2, 5)).thenReturn(Optional.of(tipo((byte) 1, 5)));

        assertThat(useCase.getByUnique(2, 5)).isPresent();
    }

    @Test
    void descartaLaConfiguracionInactiva() {
        when(repository.findByUnique(2, 5)).thenReturn(Optional.of(tipo((byte) 0, 5)));

        assertThat(useCase.getByUnique(2, 5)).isEmpty();
    }

    @Test
    void descartaLaConfiguracionDeOtraAlternativa() {
        when(repository.findByUnique(2, 5)).thenReturn(Optional.of(tipo((byte) 1, 9)));

        assertThat(useCase.getByUnique(2, 5)).isEmpty();
    }

    @Test
    void anteUnErrorDevuelveVacio() {
        when(repository.findByUnique(2, 5)).thenThrow(new RuntimeException("404"));

        assertThat(useCase.getByUnique(2, 5)).isEmpty();
    }

    private TipoChequeraTarjeta tipo(Byte active, Integer alternativaId) {
        return TipoChequeraTarjeta.builder()
                .tipoChequeraId(2)
                .alternativaId(alternativaId)
                .installments(6)
                .defaultInstallments(1)
                .active(active)
                .build();
    }

}
