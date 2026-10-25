package br.com.adotapet.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** Configuração de CORS — permite o frontend Angular (origem configurável) consumir a API. */
@Configuration
public class CorsConfig {

    @Value("${adotapet.cors.origem-permitida:http://localhost:4200}")
    private String origemPermitida;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOrigins(List.of(origemPermitida));
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuracao.setExposedHeaders(List.of("Authorization", "Location"));
        configuracao.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", configuracao);
        return fonte;
    }
}
