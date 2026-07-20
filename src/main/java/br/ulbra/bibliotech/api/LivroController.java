package br.ulbra.bibliotech.api;

import br.ulbra.bibliotech.api.dto.LivroDTO;
import br.ulbra.bibliotech.repositorio.LivroRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de consulta de livros do acervo.
 */
@RestController
@RequestMapping("/api/livros")
public class LivroController {

    private final LivroRepository livroRepository;

    public LivroController(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    @GetMapping
    public List<LivroDTO> listar() {
        return livroRepository.listarTodos().stream()
                .map(LivroDTO::de)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LivroDTO> buscar(@PathVariable("id") Long id) {
        LivroDTO livro = livroRepository.buscarPorId(id)
                .map(LivroDTO::de)
                .orElse(null);
        return ResponseEntity.ok(livro);
    }
}
