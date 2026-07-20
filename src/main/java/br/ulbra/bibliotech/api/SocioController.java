package br.ulbra.bibliotech.api;

import br.ulbra.bibliotech.api.dto.SocioDTO;
import br.ulbra.bibliotech.repositorio.SocioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de consulta de socios.
 */
@RestController
@RequestMapping("/api/socios")
public class SocioController {

    private final SocioRepository socioRepository;

    public SocioController(SocioRepository socioRepository) {
        this.socioRepository = socioRepository;
    }

    @GetMapping
    public List<SocioDTO> listar() {
        return socioRepository.listarTodos().stream()
                .map(SocioDTO::de)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SocioDTO> buscar(@PathVariable("id") Long id) {
        SocioDTO socio = socioRepository.buscarPorId(id)
                .map(SocioDTO::de)
                .orElse(null);
        return ResponseEntity.ok(socio);
    }
}
