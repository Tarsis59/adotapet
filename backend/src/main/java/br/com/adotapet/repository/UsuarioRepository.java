package br.com.adotapet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.adotapet.domain.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
