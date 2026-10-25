package br.com.adotapet.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import br.com.adotapet.domain.Perfil;
import br.com.adotapet.domain.Usuario;
import br.com.adotapet.repository.UsuarioRepository;
import br.com.adotapet.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnimalControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Value("${adotapet.jwt.secret}")
    private String segredoJwt;

    private String token;

    @BeforeEach
    void autenticar() {
        String email = "voluntaria" + System.nanoTime() + "@teste.com";
        usuarioRepository.save(new Usuario("Voluntária", email, passwordEncoder.encode("Senha@123"), Perfil.VOLUNTARIO));
        token = jwtService.gerarToken(email, "Voluntária", Perfil.VOLUNTARIO);
    }

    private String corpoAnimalValido(String nome, String status) {
        return """
                {
                  "nome": "%s",
                  "especie": "CACHORRO",
                  "raca": "Vira-lata",
                  "sexo": "MACHO",
                  "porte": "MEDIO",
                  "idadeAnos": 2,
                  "pesoKg": 12.5,
                  "castrado": true,
                  "vacinado": true,
                  "status": "%s",
                  "descricao": "Animal de teste.",
                  "fotoUrl": null
                }
                """.formatted(nome, status);
    }

    @Test
    void deveRecusarRequisicaoSemToken() throws Exception {
        mockMvc.perform(get("/api/animais"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem", is("Autenticação necessária.")));
    }

    @Test
    void deveRecusarTokenMalformado() throws Exception {
        mockMvc.perform(get("/api/animais").header("Authorization", "Bearer isso-nao-e-um-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem", is("Token inválido.")));
    }

    @Test
    void deveRecusarTokenExpirado() throws Exception {
        JwtService servicoExpirado = new JwtService(segredoJwt, "PT-5S");
        String tokenExpirado = servicoExpirado.gerarToken("expirado@teste.com", "Expirada", Perfil.VOLUNTARIO);

        mockMvc.perform(get("/api/animais").header("Authorization", "Bearer " + tokenExpirado))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem", is("Token expirado. Faça login novamente.")));
    }

    @Test
    void deveExecutarFluxoCrudCompleto() throws Exception {
        String respostaCriacao = mockMvc.perform(post("/api/animais")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAnimalValido("Bidu", "DISPONIVEL")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nome", is("Bidu")))
                .andReturn().getResponse().getContentAsString();

        Number idLido = com.jayway.jsonpath.JsonPath.read(respostaCriacao, "$.id");
        long id = idLido.longValue();

        mockMvc.perform(get("/api/animais/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Bidu")));

        mockMvc.perform(get("/api/animais")
                        .header("Authorization", "Bearer " + token)
                        .param("busca", "Bidu")
                        .param("especie", "CACHORRO")
                        .param("status", "DISPONIVEL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo").isArray())
                .andExpect(jsonPath("$.totalElementos", is(1)));

        mockMvc.perform(put("/api/animais/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAnimalValido("Bidu Atualizado", "ADOTADO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Bidu Atualizado")))
                .andExpect(jsonPath("$.status", is("ADOTADO")));

        mockMvc.perform(delete("/api/animais/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/animais/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarEstatisticas() throws Exception {
        mockMvc.perform(post("/api/animais")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAnimalValido("Rex", "DISPONIVEL")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/animais/estatisticas").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").exists())
                .andExpect(jsonPath("$.disponiveis").exists());
    }

    @Test
    void deveRetornar404AoBuscarAnimalInexistente() throws Exception {
        mockMvc.perform(get("/api/animais/999999").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void deveRetornar400AoCriarAnimalComCorpoInvalido() throws Exception {
        mockMvc.perform(post("/api/animais")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"","idadeAnos": 50}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos").isArray());
    }

    @Test
    void deveRetornar400AoCriarAnimalComEnumInvalido() throws Exception {
        mockMvc.perform(post("/api/animais")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Animal",
                                  "especie": "DINOSSAURO",
                                  "sexo": "MACHO",
                                  "porte": "MEDIO",
                                  "idadeAnos": 2,
                                  "castrado": true,
                                  "vacinado": true,
                                  "status": "DISPONIVEL"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400AoCriarAnimalComFotoUrlInvalida() throws Exception {
        String corpo = corpoAnimalValido("Fotogênico", "DISPONIVEL")
                .replace("\"fotoUrl\": null", "\"fotoUrl\": \"nao-e-uma-url\"");

        mockMvc.perform(post("/api/animais")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo", is("fotoUrl")));
    }

    @Test
    void deveGravarCamposOpcionaisEmBrancoComoNulos() throws Exception {
        String corpo = corpoAnimalValido("Sem Detalhes", "DISPONIVEL")
                .replace("\"raca\": \"Vira-lata\"", "\"raca\": \"  \"")
                .replace("\"descricao\": \"Animal de teste.\"", "\"descricao\": \"\"")
                .replace("\"fotoUrl\": null", "\"fotoUrl\": \"\"");

        mockMvc.perform(post("/api/animais")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("http://localhost/api/animais/")))
                .andExpect(jsonPath("$.raca").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.descricao").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.fotoUrl").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void deveRetornar404EmJsonParaRotaInexistente() throws Exception {
        mockMvc.perform(get("/api/rota-inexistente")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem", is("O recurso solicitado não existe.")));
    }
}
