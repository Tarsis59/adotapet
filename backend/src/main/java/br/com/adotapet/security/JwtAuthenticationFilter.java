package br.com.adotapet.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Lê o header {@code Authorization: Bearer <token>}, valida o JWT e, se válido, autentica a
 * requisição no {@link SecurityContextHolder}. Falhas são sinalizadas via atributos da requisição
 * para que o {@link JsonAuthenticationEntryPoint} produza a mensagem correta.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String ATRIBUTO_ERRO = "erroAutenticacao";
    public static final String ERRO_TOKEN_EXPIRADO = "TOKEN_EXPIRADO";
    public static final String ERRO_TOKEN_INVALIDO = "TOKEN_INVALIDO";

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioDetailsService usuarioDetailsService) {
        this.jwtService = jwtService;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String cabecalho = request.getHeader("Authorization");
        if (cabecalho == null || !cabecalho.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = cabecalho.substring(7);
        try {
            String email = jwtService.extrairEmail(token);
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails usuario = usuarioDetailsService.loadUserByUsername(email);
                var autenticacao = new UsernamePasswordAuthenticationToken(
                        usuario, null, usuario.getAuthorities());
                autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(autenticacao);
            }
        } catch (ExpiredJwtException ex) {
            request.setAttribute(ATRIBUTO_ERRO, ERRO_TOKEN_EXPIRADO);
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
            request.setAttribute(ATRIBUTO_ERRO, ERRO_TOKEN_INVALIDO);
        }

        filterChain.doFilter(request, response);
    }
}
