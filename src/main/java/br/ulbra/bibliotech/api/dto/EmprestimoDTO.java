package br.ulbra.bibliotech.api.dto;

import br.ulbra.bibliotech.dominio.Emprestimo;

import java.time.LocalDate;

/**
 * Representacao de saida de um emprestimo na API.
 */
public record EmprestimoDTO(
        Long id,
        Long socioId,
        String socioNome,
        Long livroId,
        String livroTitulo,
        LocalDate dataEmprestimo,
        LocalDate dataPrevistaDevolucao,
        LocalDate dataDevolucao,
        double multaCobrada) {

    public static EmprestimoDTO de(Emprestimo emprestimo) {
        return new EmprestimoDTO(
                emprestimo.getId(),
                emprestimo.getSocio().getId(),
                emprestimo.getSocio().getNome(),
                emprestimo.getLivro().getId(),
                emprestimo.getLivro().getTitulo(),
                emprestimo.getDataEmprestimo(),
                emprestimo.getDataPrevistaDevolucao(),
                emprestimo.getDataDevolucao(),
                emprestimo.getMultaCobrada());
    }
}
