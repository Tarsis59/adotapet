package br.com.adotapet.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.adotapet.domain.Perfil;
import br.com.adotapet.domain.Usuario;
import br.com.adotapet.dto.LoginRequest;
import br.com.adotapet.dto.LoginResponse;
import br.com.adotapet.dto.RegistroRequest;
import br.com.adotapet.dto.UsuarioMapper;
import br.com.adotapet.dto.UsuarioResponse;
import br.com.adotapet.exception.EmailJaCadastradoException;
import br.com.adotapet.repository.UsuarioRepository;
import br.com.adotapet.security.JwtService;
import br.com.adotapet.security.UsuarioAutenticado;

/** Regras de negócio de autenticação e cadastro de usuários. */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioMapper usuarioMapper;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioMapper = usuarioMapper;
    }

    public LoginResponse login(LoginRequest request) {
        var autenticacao = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

        Usuario usuario = ((UsuarioAutenticado) autenticacao.getPrincipal()).getUsuario();
        String token = jwtService.gerarToken(usuario.getEmail(), usuario.getNome(), usuario.getPerfil());
        return LoginResponse.de(token, jwtService.expiracaoDoToken(token), usuarioMapper.paraResponse(usuario));
    }

    public UsuarioResponse registrar(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailJaCadastradoException("Este e-mail já está cadastrado.");
        }
        Usuario usuario = new Usuario(
                request.nome(),
                request.email(),
                passwordEncoder.encode(request.senha()),
                Perfil.VOLUNTARIO);
        usuario = usuarioRepository.save(usuario);
        return usuarioMapper.paraResponse(usuario);
    }

    public UsuarioResponse me(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado."));
        return usuarioMapper.paraResponse(usuario);
    }
}
