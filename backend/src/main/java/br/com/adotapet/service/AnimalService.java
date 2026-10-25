package br.com.adotapet.service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import br.com.adotapet.domain.Animal;
import br.com.adotapet.domain.Especie;
import br.com.adotapet.domain.StatusAdocao;
import br.com.adotapet.dto.AnimalMapper;
import br.com.adotapet.dto.AnimalRequest;
import br.com.adotapet.dto.AnimalResponse;
import br.com.adotapet.dto.EstatisticasResponse;
import br.com.adotapet.exception.RecursoNaoEncontradoException;
import br.com.adotapet.repository.AnimalRepository;
import br.com.adotapet.repository.AnimalSpecifications;

/** Regras de negócio do CRUD de animais. */
@Service
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final AnimalMapper animalMapper;

    public AnimalService(AnimalRepository animalRepository, AnimalMapper animalMapper) {
        this.animalRepository = animalRepository;
        this.animalMapper = animalMapper;
    }

    public Page<AnimalResponse> listar(String busca, Especie especie, StatusAdocao status, Pageable pageable) {
        Specification<Animal> especificacao = Specification.allOf(
                Arrays.asList(
                        AnimalSpecifications.comBusca(busca),
                        AnimalSpecifications.comEspecie(especie),
                        AnimalSpecifications.comStatus(status))
                        .stream()
                        .filter(Objects::nonNull)
                        .toList());
        return animalRepository.findAll(especificacao, pageable).map(animalMapper::paraResponse);
    }

    public AnimalResponse buscarPorId(Long id) {
        return animalMapper.paraResponse(buscarEntidadePorId(id));
    }

    public AnimalResponse criar(AnimalRequest request) {
        Animal animal = animalMapper.paraEntidade(request);
        animal = animalRepository.save(animal);
        return animalMapper.paraResponse(animal);
    }

    public AnimalResponse atualizar(Long id, AnimalRequest request) {
        Animal animal = buscarEntidadePorId(id);
        animalMapper.atualizarEntidade(request, animal);
        animal = animalRepository.save(animal);
        return animalMapper.paraResponse(animal);
    }

    public void excluir(Long id) {
        Animal animal = buscarEntidadePorId(id);
        animalRepository.delete(animal);
    }

    public EstatisticasResponse estatisticas() {
        long disponiveis = animalRepository.countByStatus(StatusAdocao.DISPONIVEL);
        long emProcesso = animalRepository.countByStatus(StatusAdocao.EM_PROCESSO);
        long adotados = animalRepository.countByStatus(StatusAdocao.ADOTADO);
        long total = disponiveis + emProcesso + adotados;
        return new EstatisticasResponse(total, disponiveis, emProcesso, adotados);
    }

    private Animal buscarEntidadePorId(Long id) {
        return animalRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Animal não encontrado com id " + id + "."));
    }
}
