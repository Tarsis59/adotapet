package br.com.adotapet.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import br.com.adotapet.domain.Perfil;
import br.com.adotapet.domain.Usuario;
import br.com.adotapet.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private void criarUsuario(String email, String senha) {
        usuarioRepository.save(new Usuario("Usuária de Teste", email, passwordEncoder.encode(senha), Perfil.VOLUNTARIO));
    }

    @Test
    void deveLogarComSucessoENaoExporSenha() throws Exception {
        criarUsuario("maria@teste.com", "Senha@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"maria@teste.com","senha":"Senha@123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tipo", is("Bearer")))
                .andExpect(jsonPath("$.usuario.email", is("maria@teste.com")))
                .andExpect(jsonPath("$.usuario.senha").doesNotExist());
    }

    @Test
    void deveRecusarSenhaIncorreta() throws Exception {
        criarUsuario("joao@teste.com", "Senha@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"joao@teste.com","senha":"SenhaErrada"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem", is("E-mail ou senha inválidos.")));
    }

    @Test
    void deveValidarCamposDoLogin() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"","senha":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos").isArray());
    }

    @Test
    void deveRegistrarNovoUsuarioComoVoluntario() throws Exception {
        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Carla Teste","email":"carla@teste.com","senha":"Senha@123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("carla@teste.com")))
                .andExpect(jsonPath("$.perfil", is("VOLUNTARIO")))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void deveRecusarRegistroComEmailDuplicado() throws Exception {
        criarUsuario("duplicado@teste.com", "Senha@123");

        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Outro Nome","email":"duplicado@teste.com","senha":"Senha@123"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", is("Este e-mail já está cadastrado.")));
    }

    @Test
    void deveExigirAutenticacaoParaRotaMe() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem", is("Autenticação necessária.")));
    }

    @Test
    void deveRecusarTokenAdulterado() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer token.invalido.adulterado"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem", is("Token inválido.")));
    }
}
