package br.com.adotapet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.adotapet.domain.Animal;
import br.com.adotapet.domain.Especie;
import br.com.adotapet.domain.Porte;
import br.com.adotapet.domain.Sexo;
import br.com.adotapet.domain.StatusAdocao;
import br.com.adotapet.dto.AnimalMapper;
import br.com.adotapet.dto.AnimalRequest;
import br.com.adotapet.dto.AnimalResponse;
import br.com.adotapet.exception.RecursoNaoEncontradoException;
import br.com.adotapet.repository.AnimalRepository;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @Mock
    private AnimalRepository animalRepository;

    private AnimalService animalService;

    @BeforeEach
    void configurar() {
        animalService = new AnimalService(animalRepository, new AnimalMapper());
    }

    private AnimalRequest requestValido() {
        return new AnimalRequest(
                "Bidu", Especie.CACHORRO, "Labrador", Sexo.MACHO, Porte.GRANDE, 3,
                new BigDecimal("20.0"), true, true, StatusAdocao.DISPONIVEL,
                "Animal dócil.", null);
    }

    @Test
    void deveCriarAnimal() {
        when(animalRepository.save(any(Animal.class))).thenAnswer(invocacao -> {
            Animal animal = invocacao.getArgument(0);
            return animal;
        });

        AnimalResponse resposta = animalService.criar(requestValido());

        assertThat(resposta.nome()).isEqualTo("Bidu");
        assertThat(resposta.especie()).isEqualTo(Especie.CACHORRO);
        verify(animalRepository).save(any(Animal.class));
    }

    @Test
    void deveAtualizarAnimalExistente() {
        Animal existente = new Animal();
        existente.setNome("Nome Antigo");
        when(animalRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(animalRepository.save(any(Animal.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        AnimalResponse resposta = animalService.atualizar(1L, requestValido());

        assertThat(resposta.nome()).isEqualTo("Bidu");
    }

    @Test
    void deveLancarExcecaoAoAtualizarAnimalInexistente() {
        when(animalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> animalService.atualizar(99L, requestValido()));
    }

    @Test
    void deveLancarExcecaoAoBuscarAnimalInexistente() {
        when(animalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> animalService.buscarPorId(99L));
    }

    @Test
    void deveExcluirAnimalExistente() {
        Animal existente = new Animal();
        when(animalRepository.findById(1L)).thenReturn(Optional.of(existente));

        animalService.excluir(1L);

        verify(animalRepository).delete(existente);
    }

    @Test
    void deveLancarExcecaoAoExcluirAnimalInexistente() {
        when(animalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> animalService.excluir(99L));
    }
}
