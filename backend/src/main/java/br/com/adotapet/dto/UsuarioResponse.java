package br.com.adotapet.dto;

import br.com.adotapet.domain.Perfil;

/** Representação pública do usuário — nunca contém a senha. */
public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        Perfil perfil) {
}
