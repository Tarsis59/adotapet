package br.com.adotapet.repository;

import org.springframework.data.jpa.domain.Specification;

import br.com.adotapet.domain.Animal;
import br.com.adotapet.domain.Especie;
import br.com.adotapet.domain.StatusAdocao;

/** Specifications de filtro para a busca paginada de animais. */
public final class AnimalSpecifications {

    private AnimalSpecifications() {
    }

    public static Specification<Animal> comBusca(String busca) {
        if (busca == null || busca.isBlank()) {
            return null;
        }
        String termo = "%" + busca.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nome")), termo),
                cb.like(cb.lower(root.get("raca")), termo));
    }

    public static Specification<Animal> comEspecie(Especie especie) {
        if (especie == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("especie"), especie);
    }

    public static Specification<Animal> comStatus(StatusAdocao status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
