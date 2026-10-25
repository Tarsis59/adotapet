package br.com.adotapet.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import br.com.adotapet.domain.Perfil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;

class JwtServiceTest {

    private static final String SEGREDO = "Chave-Secreta-De-Teste-Com-No-Minimo-32-Bytes-Para-HS256";

    private final JwtService jwtService = new JwtService(SEGREDO, "PT2H");

    @Test
    void deveGerarTokenEValidarClaims() {
        String token = jwtService.gerarToken("usuario@teste.com", "Usuário Teste", Perfil.VOLUNTARIO);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extrairEmail(token)).isEqualTo("usuario@teste.com");

        var claims = jwtService.extrairClaims(token);
        assertThat(claims.getSubject()).isEqualTo("usuario@teste.com");
        assertThat(claims.get("nome")).isEqualTo("Usuário Teste");
        assertThat(claims.get("perfil")).isEqualTo("VOLUNTARIO");
    }

    @Test
    void deveDetectarTokenExpirado() {
        JwtService servicoComExpiracaoNoPassado = new JwtService(SEGREDO, "PT-5S");
        String tokenExpirado = servicoComExpiracaoNoPassado.gerarToken(
                "usuario@teste.com", "Usuário Teste", Perfil.VOLUNTARIO);

        assertThrows(ExpiredJwtException.class, () -> jwtService.extrairClaims(tokenExpirado));
    }

    @Test
    void deveRecusarTokenComAssinaturaInvalida() {
        JwtService outroServico = new JwtService("Outra-Chave-Secreta-Com-No-Minimo-32-Bytes-1234567890", "PT2H");
        String token = outroServico.gerarToken("usuario@teste.com", "Usuário Teste", Perfil.VOLUNTARIO);

        assertThrows(SignatureException.class, () -> jwtService.extrairClaims(token));
    }
}
