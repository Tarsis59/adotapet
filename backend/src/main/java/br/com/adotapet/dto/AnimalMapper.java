package br.com.adotapet.dto;

import org.springframework.stereotype.Component;

import br.com.adotapet.domain.Animal;

/** Converte entre a entidade {@link Animal} e os DTOs expostos pela API. */
@Component
public class AnimalMapper {

    public Animal paraEntidade(AnimalRequest request) {
        Animal animal = new Animal();
        copiarCampos(request, animal);
        return animal;
    }

    public void atualizarEntidade(AnimalRequest request, Animal animal) {
        copiarCampos(request, animal);
    }

    private void copiarCampos(AnimalRequest request, Animal animal) {
        animal.setNome(request.nome().trim());
        animal.setEspecie(request.especie());
        animal.setRaca(textoOpcional(request.raca()));
        animal.setSexo(request.sexo());
        animal.setPorte(request.porte());
        animal.setIdadeAnos(request.idadeAnos());
        animal.setPesoKg(request.pesoKg());
        animal.setCastrado(request.castrado());
        animal.setVacinado(request.vacinado());
        animal.setStatus(request.status());
        animal.setDescricao(textoOpcional(request.descricao()));
        animal.setFotoUrl(textoOpcional(request.fotoUrl()));
    }

    /** Campos opcionais em branco são gravados como nulos, evitando strings vazias no banco. */
    private static String textoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public AnimalResponse paraResponse(Animal animal) {
        return new AnimalResponse(
                animal.getId(),
                animal.getNome(),
                animal.getEspecie(),
                animal.getRaca(),
                animal.getSexo(),
                animal.getPorte(),
                animal.getIdadeAnos(),
                animal.getPesoKg(),
                animal.getCastrado(),
                animal.getVacinado(),
                animal.getStatus(),
                animal.getDescricao(),
                animal.getFotoUrl(),
                animal.getCriadoEm(),
                animal.getAtualizadoEm());
    }
}
