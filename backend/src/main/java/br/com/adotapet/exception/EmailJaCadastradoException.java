package br.com.adotapet.exception;

/** Lançada ao tentar registrar um usuário com e-mail já existente. */
public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException(String mensagem) {
        super(mensagem);
    }
}
