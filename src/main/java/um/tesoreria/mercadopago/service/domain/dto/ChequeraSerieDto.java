package um.tesoreria.mercadopago.service.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChequeraSerieDto {
    private Long chequeraId;
    private Integer facultadId;
    private Integer tipoChequeraId;
    private Long chequeraSerieId;
    private BigDecimal personaId;
    private Integer documentoId;
    private Integer lectivoId;
    private Integer arancelTipoId;
    private Integer cursoId;
    private Byte asentado;
    private Integer geograficaId;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime fecha;
    
    private Integer cuotasPagadas;
    private String observaciones;
    private Integer alternativaId;
    private Byte algoPagado;
    private Integer tipoImpresionId;
    private Byte flagPayperTic = 0;
    private String usuarioId;
    private Byte enviado = 0;
    private Byte retenida = 0;
    private Long version;

    private Byte hpum = 0;
    private BigDecimal becaPorcentaje = BigDecimal.ZERO;
    private String becaResolucion;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
    private OffsetDateTime becaFecha;
    private Long becaUserId;

    private Integer cuotasDeuda = 0;
    private BigDecimal importeDeuda = BigDecimal.ZERO;
    
    private FacultadDto facultad;
    private TipoChequeraDto tipoChequera;
    private PersonaDto persona;
    private DomicilioDto domicilio;
    private LectivoDto lectivo;
    private ArancelTipoDto arancelTipo;
    private GeograficaDto geografica;
}
