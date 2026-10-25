package br.com.adotapet.dto;

public record EstatisticasResponse(
        long total,
        long disponiveis,
        long emProcesso,
        long adotados) {
}
