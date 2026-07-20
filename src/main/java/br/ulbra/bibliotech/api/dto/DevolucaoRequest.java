package br.ulbra.bibliotech.api.dto;

import java.time.LocalDate;

/**
 * Corpo da requisicao de registro de devolucao.
 */
public record DevolucaoRequest(LocalDate dataDevolucao) {
}
