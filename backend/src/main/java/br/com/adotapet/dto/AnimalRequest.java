package br.com.adotapet.dto;

import java.math.BigDecimal;

import br.com.adotapet.domain.Especie;
import br.com.adotapet.domain.Porte;
import br.com.adotapet.domain.Sexo;
import br.com.adotapet.domain.StatusAdocao;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AnimalRequest(

        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 2, max = 60, message = "O nome deve ter entre 2 e 60 caracteres.")
        String nome,

        @NotNull(message = "A espécie é obrigatória.")
        Especie especie,

        @Size(max = 60, message = "A raça deve ter no máximo 60 caracteres.")
        String raca,

        @NotNull(message = "O sexo é obrigatório.")
        Sexo sexo,

        @NotNull(message = "O porte é obrigatório.")
        Porte porte,

        @NotNull(message = "A idade é obrigatória.")
        @Min(value = 0, message = "A idade deve ser entre 0 e 30 anos.")
        @Max(value = 30, message = "A idade deve ser entre 0 e 30 anos.")
        Integer idadeAnos,

        @DecimalMin(value = "0.1", message = "O peso deve ser entre 0,1 e 120 kg.")
        @DecimalMax(value = "120", message = "O peso deve ser entre 0,1 e 120 kg.")
        BigDecimal pesoKg,

        @NotNull(message = "Informe se o animal é castrado.")
        Boolean castrado,

        @NotNull(message = "Informe se o animal é vacinado.")
        Boolean vacinado,

        @NotNull(message = "O status é obrigatório.")
        StatusAdocao status,

        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres.")
        String descricao,

        @Size(max = 500, message = "A URL da foto deve ter no máximo 500 caracteres.")
        @Pattern(regexp = "^$|^https?://\\S+$", message = "Informe uma URL válida iniciando com http:// ou https://.")
        String fotoUrl) {
}
