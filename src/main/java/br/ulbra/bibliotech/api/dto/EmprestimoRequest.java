package br.ulbra.bibliotech.api.dto;

/**
 * Corpo da requisicao de criacao de um emprestimo.
 */
public record EmprestimoRequest(Long socioId, Long livroId) {
}
