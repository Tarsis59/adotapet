package br.com.adotapet.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.adotapet.domain.Perfil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Geração e validação de tokens JWT (HS256). O subject é o e-mail do usuário e as claims
 * adicionais trazem nome e perfil para evitar consultas extras no frontend.
 */
@Service
public class JwtService {

    private final SecretKey chave;
    private final Duration expiracao;

    public JwtService(
            @Value("${adotapet.jwt.secret}") String segredo,
            @Value("${adotapet.jwt.expiracao:PT2H}") String expiracao) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracao = Duration.parse(expiracao);
    }

    public String gerarToken(String email, String nome, Perfil perfil) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(expiracao);
        return Jwts.builder()
                .subject(email)
                .claim("nome", nome)
                .claim("perfil", perfil.name())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiraEm))
                .signWith(chave)
                .compact();
    }

    public Instant expiracaoDoToken(String token) {
        return extrairClaims(token).getExpiration().toInstant();
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    /** Valida assinatura e expiração; lança {@link io.jsonwebtoken.JwtException} em caso de falha. */
    public Claims extrairClaims(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token);
        return jws.getPayload();
    }
}
