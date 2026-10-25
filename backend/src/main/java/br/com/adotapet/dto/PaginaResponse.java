package br.com.adotapet.dto;

import java.util.List;

import org.springframework.data.domain.Page;

/** Envelope de paginação exposto pela API (não expõe o tipo Page do Spring diretamente). */
public record PaginaResponse<T>(
        List<T> conteudo,
        int pagina,
        int tamanho,
        long totalElementos,
        int totalPaginas) {

    public static <T> PaginaResponse<T> de(Page<T> page) {
        return new PaginaResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
