package br.ulbra.bibliotech.web;

import br.ulbra.bibliotech.dominio.Emprestimo;
import br.ulbra.bibliotech.repositorio.EmprestimoRepository;
import br.ulbra.bibliotech.servico.EmprestimoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * Telas de emprestimos e do fluxo de devolucao.
 */
@Controller
public class EmprestimoWebController {

    private final EmprestimoRepository emprestimoRepository;
    private final EmprestimoService emprestimoService;

    public EmprestimoWebController(EmprestimoRepository emprestimoRepository,
                                   EmprestimoService emprestimoService) {
        this.emprestimoRepository = emprestimoRepository;
        this.emprestimoService = emprestimoService;
    }

    @GetMapping("/emprestimos")
    public String listarEmprestimos(Model model) {
        model.addAttribute("emprestimos", emprestimoRepository.listarTodos());
        return "emprestimos";
    }

    @GetMapping("/devolver")
    public String formularioDevolucao(@RequestParam("emprestimoId") Long emprestimoId, Model model) {
        Emprestimo emprestimo = emprestimoRepository.buscarPorId(emprestimoId).orElse(null);
        model.addAttribute("emprestimo", emprestimo);
        return "devolver";
    }

    @PostMapping("/devolver")
    public String confirmarDevolucao(@RequestParam("emprestimoId") Long emprestimoId,
                                     @RequestParam("dataDevolucao") String dataDevolucao,
                                     RedirectAttributes redirectAttributes) {
        Emprestimo emprestimo = emprestimoRepository.buscarPorId(emprestimoId).orElse(null);

        LocalDate data = LocalDate.parse(dataDevolucao);
        double multa = emprestimoService.devolver(emprestimo, data);

        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Devolucao registrada. Multa apurada: " + multa);
        return "redirect:/emprestimos";
    }
}
