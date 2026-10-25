package br.com.adotapet.dto;

/** Erro de validação de um campo específico. */
public record CampoErro(String campo, String mensagem) {
}
