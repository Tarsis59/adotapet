package br.com.adotapet.dto;

import org.springframework.stereotype.Component;

import br.com.adotapet.domain.Usuario;

/** Converte entre a entidade {@link Usuario} e os DTOs expostos pela API. */
@Component
public class UsuarioMapper {

    public UsuarioResponse paraResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil());
    }
}
