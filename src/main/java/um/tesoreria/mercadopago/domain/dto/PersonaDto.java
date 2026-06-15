package um.tesoreria.mercadopago.domain.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonaDto {
    private Long uniqueId;
    private BigDecimal personaId;
    private Integer documentoId;
    private String apellido;
    private String nombre;
    private String sexo;
    @Builder.Default
    private Byte primero = 0;
    @Builder.Default
    private String cuit = "";
    @Builder.Default
    private String cbu = "";
    private String password;
    
    // Método equivalente al getter personalizado de Kotlin
    public String getApellidoNombre() {
        return String.format("%s, %s", apellido, nombre);
    }
}
