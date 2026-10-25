package br.com.adotapet.security;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import br.com.adotapet.dto.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * Responde 401 em JSON ({@link ErroResponse}), distinguindo token ausente, inválido e expirado
 * conforme o atributo deixado pelo {@link JwtAuthenticationFilter}.
 */
@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        String mensagem = mensagemParaRequisicao(request);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErroResponse corpo = ErroResponse.de(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Não autorizado",
                mensagem,
                request.getRequestURI());

        objectMapper.writeValue(response.getOutputStream(), corpo);
    }

    private String mensagemParaRequisicao(HttpServletRequest request) {
        Object erro = request.getAttribute(JwtAuthenticationFilter.ATRIBUTO_ERRO);
        if (JwtAuthenticationFilter.ERRO_TOKEN_EXPIRADO.equals(erro)) {
            return "Token expirado. Faça login novamente.";
        }
        if (JwtAuthenticationFilter.ERRO_TOKEN_INVALIDO.equals(erro)) {
            return "Token inválido.";
        }
        return "Autenticação necessária.";
    }
}
