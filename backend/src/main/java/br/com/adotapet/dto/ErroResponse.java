package br.com.adotapet.dto;

import java.time.Instant;
import java.util.List;

/** Corpo padrão de erro retornado pela API. */
public record ErroResponse(
        Instant timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        List<CampoErro> campos) {

    public static ErroResponse de(int status, String erro, String mensagem, String caminho) {
        return new ErroResponse(Instant.now(), status, erro, mensagem, caminho, List.of());
    }

    public static ErroResponse de(int status, String erro, String mensagem, String caminho, List<CampoErro> campos) {
        return new ErroResponse(Instant.now(), status, erro, mensagem, caminho, campos);
    }
}
