package br.com.adotapet.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.adotapet.domain.Especie;
import br.com.adotapet.domain.StatusAdocao;
import br.com.adotapet.dto.AnimalRequest;
import br.com.adotapet.dto.AnimalResponse;
import br.com.adotapet.dto.EstatisticasResponse;
import br.com.adotapet.dto.PaginaResponse;
import br.com.adotapet.service.AnimalService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/animais")
public class AnimalController {

    private static final int TAMANHO_MAXIMO_PAGINA = 50;

    private final AnimalService animalService;

    public AnimalController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @GetMapping
    public ResponseEntity<PaginaResponse<AnimalResponse>> listar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Especie especie,
            @RequestParam(required = false) StatusAdocao status,
            @PageableDefault(size = 10) @SortDefault(sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {

        Pageable paginavel = limitarTamanho(pageable);
        Page<AnimalResponse> pagina = animalService.listar(busca, especie, status, paginavel);
        return ResponseEntity.ok(PaginaResponse.de(pagina));
    }

    @GetMapping("/estatisticas")
    public ResponseEntity<EstatisticasResponse> estatisticas() {
        return ResponseEntity.ok(animalService.estatisticas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimalResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(animalService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<AnimalResponse> criar(@Valid @RequestBody AnimalRequest request) {
        AnimalResponse criado = animalService.criar(request);
        URI localizacao = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(localizacao).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimalResponse> atualizar(@PathVariable Long id, @Valid @RequestBody AnimalRequest request) {
        return ResponseEntity.ok(animalService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        animalService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /** Garante que o tamanho de página nunca exceda o limite, mesmo se o cliente pedir mais. */
    private Pageable limitarTamanho(Pageable pageable) {
        if (pageable.getPageSize() <= TAMANHO_MAXIMO_PAGINA) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), TAMANHO_MAXIMO_PAGINA, pageable.getSort());
    }
}
