package br.com.adotapet.dto;

import java.math.BigDecimal;
import java.time.Instant;

import br.com.adotapet.domain.Especie;
import br.com.adotapet.domain.Porte;
import br.com.adotapet.domain.Sexo;
import br.com.adotapet.domain.StatusAdocao;

public record AnimalResponse(
        Long id,
        String nome,
        Especie especie,
        String raca,
        Sexo sexo,
        Porte porte,
        Integer idadeAnos,
        BigDecimal pesoKg,
        Boolean castrado,
        Boolean vacinado,
        StatusAdocao status,
        String descricao,
        String fotoUrl,
        Instant criadoEm,
        Instant atualizadoEm) {
}
