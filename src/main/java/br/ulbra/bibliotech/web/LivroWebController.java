package br.ulbra.bibliotech.web;

import br.ulbra.bibliotech.dominio.Livro;
import br.ulbra.bibliotech.dominio.Socio;
import br.ulbra.bibliotech.repositorio.LivroRepository;
import br.ulbra.bibliotech.repositorio.SocioRepository;
import br.ulbra.bibliotech.servico.EmprestimoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * Telas de livros e do fluxo de emprestimo.
 */
@Controller
public class LivroWebController {

    private final LivroRepository livroRepository;
    private final SocioRepository socioRepository;
    private final EmprestimoService emprestimoService;

    public LivroWebController(LivroRepository livroRepository,
                              SocioRepository socioRepository,
                              EmprestimoService emprestimoService) {
        this.livroRepository = livroRepository;
        this.socioRepository = socioRepository;
        this.emprestimoService = emprestimoService;
    }

    @GetMapping("/")
    public String listarLivros(Model model) {
        model.addAttribute("livros", livroRepository.listarTodos());
        return "livros";
    }

    @GetMapping("/emprestar")
    public String formularioEmprestimo(@RequestParam("livroId") Long livroId, Model model) {
        Livro livro = livroRepository.buscarPorId(livroId).orElse(null);
        model.addAttribute("livro", livro);
        model.addAttribute("socios", socioRepository.listarTodos());
        return "emprestar";
    }

    @PostMapping("/emprestar")
    public String confirmarEmprestimo(@RequestParam("socioId") Long socioId,
                                      @RequestParam("livroId") Long livroId,
                                      RedirectAttributes redirectAttributes) {
        Socio socio = socioRepository.buscarPorId(socioId).orElse(null);
        Livro livro = livroRepository.buscarPorId(livroId).orElse(null);

        try {
            emprestimoService.emprestar(socio, livro, LocalDate.now());
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Emprestimo do livro '" + livro.getTitulo() + "' realizado com sucesso.");
        } catch (IllegalStateException excecao) {
            return "redirect:/";
        }

        return "redirect:/";
    }
}
