package br.com.adotapet.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.adotapet.dto.LoginRequest;
import br.com.adotapet.dto.LoginResponse;
import br.com.adotapet.dto.RegistroRequest;
import br.com.adotapet.dto.UsuarioResponse;
import br.com.adotapet.security.UsuarioAutenticado;
import br.com.adotapet.service.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/registrar")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        UsuarioResponse usuario = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(@AuthenticationPrincipal UsuarioAutenticado principal) {
        return ResponseEntity.ok(authService.me(principal.getUsername()));
    }
}
