package um.tesoreria.mercadopago.service.domain.dto;

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
    private Byte primero = 0;
    private String cuit = "";
    private String cbu = "";
    private String password;
    
    // Método equivalente al getter personalizado de Kotlin
    public String getApellidoNombre() {
        return String.format("%s, %s", apellido, nombre);
    }
}
