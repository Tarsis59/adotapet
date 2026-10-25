package br.com.adotapet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.adotapet.domain.Animal;
import br.com.adotapet.domain.StatusAdocao;

public interface AnimalRepository extends JpaRepository<Animal, Long>, JpaSpecificationExecutor<Animal> {

    long countByStatus(StatusAdocao status);
}
