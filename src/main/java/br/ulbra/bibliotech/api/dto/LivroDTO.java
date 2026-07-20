package br.ulbra.bibliotech.api.dto;

import br.ulbra.bibliotech.dominio.Livro;

/**
 * Representacao de saida de um livro na API.
 */
public record LivroDTO(
        Long id,
        String titulo,
        String autor,
        String isbn,
        boolean disponivel,
        Long reservadoPorSocioId) {

    public static LivroDTO de(Livro livro) {
        return new LivroDTO(
                livro.getId(),
                livro.getTitulo(),
                livro.getAutor(),
                livro.getIsbn(),
                livro.isDisponivel(),
                livro.getReservadoPorSocioId());
    }
}
