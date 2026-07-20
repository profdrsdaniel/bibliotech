package br.ulbra.bibliotech.api.dto;

/**
 * Corpo padrao de resposta de erro da API.
 */
public record ErroResposta(String erro, int status) {
}
