package br.com.adotapet.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import br.com.adotapet.dto.CampoErro;
import br.com.adotapet.dto.ErroResponse;

/** Centraliza o tratamento de exceções, garantindo que toda resposta de erro siga {@link ErroResponse}. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException ex, WebRequest request) {
        List<CampoErro> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(this::paraCampoErro)
                .toList();

        ErroResponse corpo = ErroResponse.de(
                HttpStatus.BAD_REQUEST.value(),
                "Dados inválidos",
                "Corrija os campos indicados e tente novamente.",
                caminho(request),
                campos);
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarJsonMalformado(HttpMessageNotReadableException ex, WebRequest request) {
        ErroResponse corpo = ErroResponse.de(
                HttpStatus.BAD_REQUEST.value(),
                "Requisição inválida",
                "O corpo da requisição é inválido ou contém um valor não reconhecido.",
                caminho(request));
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tratarTipoInvalido(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String mensagem = "O parâmetro '" + ex.getName() + "' recebeu um valor inválido.";
        ErroResponse corpo = ErroResponse.de(
                HttpStatus.BAD_REQUEST.value(),
                "Requisição inválida",
                mensagem,
                caminho(request));
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(RecursoNaoEncontradoException ex, WebRequest request) {
        ErroResponse corpo = ErroResponse.de(
                HttpStatus.NOT_FOUND.value(),
                "Não encontrado",
                ex.getMessage(),
                caminho(request));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corpo);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> tratarRotaInexistente(NoResourceFoundException ex, WebRequest request) {
        ErroResponse corpo = ErroResponse.de(
                HttpStatus.NOT_FOUND.value(),
                "Não encontrado",
                "O recurso solicitado não existe.",
                caminho(request));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corpo);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> tratarMetodoNaoSuportado(HttpRequestMethodNotSupportedException ex,
            WebRequest request) {
        ErroResponse corpo = ErroResponse.de(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Método não permitido",
                "O método " + ex.getMethod() + " não é suportado para este recurso.",
                caminho(request));
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(corpo);
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErroResponse> tratarEmailDuplicado(EmailJaCadastradoException ex, WebRequest request) {
        ErroResponse corpo = ErroResponse.de(
                HttpStatus.CONFLICT.value(),
                "Conflito",
                ex.getMessage(),
                caminho(request));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(corpo);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErroResponse> tratarCredenciaisInvalidas(BadCredentialsException ex, WebRequest request) {
        ErroResponse corpo = ErroResponse.de(
                HttpStatus.UNAUTHORIZED.value(),
                "Não autorizado",
                "E-mail ou senha inválidos.",
                caminho(request));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(corpo);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarGenerico(Exception ex, WebRequest request) {
        ErroResponse corpo = ErroResponse.de(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.",
                caminho(request));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(corpo);
    }

    private CampoErro paraCampoErro(FieldError erro) {
        return new CampoErro(erro.getField(), erro.getDefaultMessage());
    }

    private String caminho(WebRequest request) {
        if (request instanceof org.springframework.web.context.request.ServletWebRequest servletRequest) {
            return servletRequest.getRequest().getRequestURI();
        }
        return request.getDescription(false).replace("uri=", "");
    }
}
