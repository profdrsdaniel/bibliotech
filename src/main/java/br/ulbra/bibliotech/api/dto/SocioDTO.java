package br.ulbra.bibliotech.api.dto;

import br.ulbra.bibliotech.dominio.Socio;

/**
 * Representacao de saida de um socio na API.
 */
public record SocioDTO(
        Long id,
        String nome,
        String email,
        double multaAcumulada,
        int emprestimosAtivos) {

    public static SocioDTO de(Socio socio) {
        return new SocioDTO(
                socio.getId(),
                socio.getNome(),
                socio.getEmail(),
                socio.getMultaAcumulada(),
                socio.getEmprestimosAtivos());
    }
}
