package br.com.adotapet.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.com.adotapet.domain.Animal;
import br.com.adotapet.domain.Especie;
import br.com.adotapet.domain.Perfil;
import br.com.adotapet.domain.Porte;
import br.com.adotapet.domain.Sexo;
import br.com.adotapet.domain.StatusAdocao;
import br.com.adotapet.domain.Usuario;
import br.com.adotapet.repository.AnimalRepository;
import br.com.adotapet.repository.UsuarioRepository;

/**
 * Popula o banco com um usuário administrador e animais de exemplo na primeira execução.
 * Não roda no perfil de teste para manter os testes de integração previsíveis e isolados.
 */
@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final AnimalRepository animalRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UsuarioRepository usuarioRepository,
            AnimalRepository animalRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.animalRepository = animalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        criarAdminSeNecessario();
        criarAnimaisSeNecessario();
    }

    private void criarAdminSeNecessario() {
        if (usuarioRepository.count() > 0) {
            return;
        }
        Usuario admin = new Usuario(
                "Administrador",
                "admin@adotapet.com",
                passwordEncoder.encode("Admin@123"),
                Perfil.ADMIN);
        usuarioRepository.save(admin);
    }

    private void criarAnimaisSeNecessario() {
        if (animalRepository.count() > 0) {
            return;
        }

        animalRepository.save(novoAnimal(
                "Bidu", Especie.CACHORRO, "Labrador", Sexo.MACHO, Porte.GRANDE, 3,
                new BigDecimal("28.5"), true, true, StatusAdocao.DISPONIVEL,
                "Bidu é um labrador muito dócil, adora brincar com crianças e passear no parque.",
                foto("1518717758536-85ae29035b6d")));

        animalRepository.save(novoAnimal(
                "Mimi", Especie.GATO, "Tigrada", Sexo.FEMEA, Porte.PEQUENO, 2,
                new BigDecimal("3.8"), true, true, StatusAdocao.DISPONIVEL,
                "Mimi é uma gata calma, prefere ambientes tranquilos e carinho na hora certa.",
                foto("1574158622682-e40e69881006")));

        animalRepository.save(novoAnimal(
                "Rex", Especie.CACHORRO, "Vira-lata", Sexo.MACHO, Porte.MEDIO, 5,
                new BigDecimal("18.0"), true, true, StatusAdocao.EM_PROCESSO,
                "Rex foi resgatado das ruas e já está totalmente recuperado, muito brincalhão.",
                foto("1587300003388-59208cc962cb")));

        animalRepository.save(novoAnimal(
                "Luna", Especie.GATO, "Vira-lata", Sexo.FEMEA, Porte.PEQUENO, 1,
                new BigDecimal("3.2"), false, true, StatusAdocao.DISPONIVEL,
                "Luna é filhote, está em fase de socialização e adora brincar com bolinhas.",
                foto("1592194996308-7b43878e84a6")));

        animalRepository.save(novoAnimal(
                "Thor", Especie.CACHORRO, "Pastor Alemão", Sexo.MACHO, Porte.GRANDE, 4,
                new BigDecimal("32.0"), true, true, StatusAdocao.ADOTADO,
                "Thor foi adotado após seis meses no abrigo, era muito protetor e leal.",
                foto("1589941013453-ec89f33b5e95")));

        animalRepository.save(novoAnimal(
                "Nina", Especie.CACHORRO, "Poodle", Sexo.FEMEA, Porte.PEQUENO, 6,
                new BigDecimal("6.5"), true, true, StatusAdocao.DISPONIVEL,
                "Nina é uma cadela idosa, tranquila, ideal para quem busca um companheiro calmo.",
                foto("1561037404-61cd46aa615b")));

        animalRepository.save(novoAnimal(
                "Felix", Especie.GATO, "Vira-lata", Sexo.MACHO, Porte.MEDIO, 3,
                new BigDecimal("4.5"), true, false, StatusAdocao.EM_PROCESSO,
                "Felix é bem independente, mas gosta de companhia à noite.",
                foto("1596854407944-bf87f6fdd49e")));

        animalRepository.save(novoAnimal(
                "Pingo", Especie.OUTRO, "Coelho", Sexo.MACHO, Porte.PEQUENO, 1,
                new BigDecimal("1.8"), false, false, StatusAdocao.DISPONIVEL,
                "Pingo é um coelho de estimação, dócil e acostumado a viver dentro de casa.",
                foto("1585110396000-c9ffd4e4b308")));
    }

    /** URL de uma foto pública (Unsplash) redimensionada para a interface. */
    private static String foto(String idFoto) {
        return "https://images.unsplash.com/photo-" + idFoto + "?w=900&q=80&auto=format&fit=crop";
    }

    private Animal novoAnimal(
            String nome, Especie especie, String raca, Sexo sexo, Porte porte, int idadeAnos,
            BigDecimal pesoKg, boolean castrado, boolean vacinado, StatusAdocao status,
            String descricao, String fotoUrl) {
        Animal animal = new Animal();
        animal.setNome(nome);
        animal.setEspecie(especie);
        animal.setRaca(raca);
        animal.setSexo(sexo);
        animal.setPorte(porte);
        animal.setIdadeAnos(idadeAnos);
        animal.setPesoKg(pesoKg);
        animal.setCastrado(castrado);
        animal.setVacinado(vacinado);
        animal.setStatus(status);
        animal.setDescricao(descricao);
        animal.setFotoUrl(fotoUrl);
        return animal;
    }
}
