package br.com.adotapet.exception;

/** Lançada quando um recurso solicitado (por id) não existe. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
