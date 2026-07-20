package br.ulbra.bibliotech.api;

import br.ulbra.bibliotech.api.dto.DevolucaoRequest;
import br.ulbra.bibliotech.api.dto.EmprestimoDTO;
import br.ulbra.bibliotech.api.dto.EmprestimoRequest;
import br.ulbra.bibliotech.dominio.Emprestimo;
import br.ulbra.bibliotech.dominio.Livro;
import br.ulbra.bibliotech.dominio.Socio;
import br.ulbra.bibliotech.repositorio.EmprestimoRepository;
import br.ulbra.bibliotech.repositorio.LivroRepository;
import br.ulbra.bibliotech.repositorio.SocioRepository;
import br.ulbra.bibliotech.servico.EmprestimoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Endpoints de emprestimos: consulta, criacao e devolucao.
 */
@RestController
@RequestMapping("/api/emprestimos")
public class EmprestimoController {

    private final EmprestimoService emprestimoService;
    private final EmprestimoRepository emprestimoRepository;
    private final SocioRepository socioRepository;
    private final LivroRepository livroRepository;

    public EmprestimoController(EmprestimoService emprestimoService,
                                EmprestimoRepository emprestimoRepository,
                                SocioRepository socioRepository,
                                LivroRepository livroRepository) {
        this.emprestimoService = emprestimoService;
        this.emprestimoRepository = emprestimoRepository;
        this.socioRepository = socioRepository;
        this.livroRepository = livroRepository;
    }

    @GetMapping
    public List<EmprestimoDTO> listar() {
        return emprestimoRepository.listarTodos().stream()
                .map(EmprestimoDTO::de)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmprestimoDTO> buscar(@PathVariable("id") Long id) {
        Emprestimo emprestimo = emprestimoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Emprestimo nao encontrado"));
        return ResponseEntity.ok(EmprestimoDTO.de(emprestimo));
    }

    @PostMapping
    public ResponseEntity<EmprestimoDTO> criar(@RequestBody EmprestimoRequest request) {
        Socio socio = socioRepository.buscarPorId(request.socioId()).orElse(null);
        Livro livro = livroRepository.buscarPorId(request.livroId()).orElse(null);

        Emprestimo emprestimo = emprestimoService.emprestar(socio, livro, LocalDate.now());

        return ResponseEntity.ok(EmprestimoDTO.de(emprestimo));
    }

    @PostMapping("/{id}/devolucao")
    public ResponseEntity<EmprestimoDTO> devolver(@PathVariable("id") Long id,
                                                  @RequestBody DevolucaoRequest request) {
        Emprestimo emprestimo = emprestimoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Emprestimo nao encontrado"));

        emprestimoService.devolver(emprestimo, request.dataDevolucao());

        return ResponseEntity.ok(EmprestimoDTO.de(emprestimo));
    }
}
