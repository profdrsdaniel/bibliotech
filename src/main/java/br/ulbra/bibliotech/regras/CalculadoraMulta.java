package br.ulbra.bibliotech.regras;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Calcula a multa por atraso na devolucao de um exemplar.
 *
 * <p>Implementa a REQ-003: a multa e de {@value PoliticaEmprestimo#MULTA_POR_DIA}
 * real por dia de atraso, existe uma carencia de
 * {@value PoliticaEmprestimo#CARENCIA_DIAS} dias e o valor final nunca ultrapassa
 * o teto de {@value PoliticaEmprestimo#TETO_MULTA} reais.</p>
 */
public class CalculadoraMulta {

    /**
     * Calcula o valor da multa devida por um emprestimo.
     *
     * <p>Devolucao adiantada ou na data prevista nao gera multa. Atraso dentro da
     * carencia tambem nao gera multa. Ultrapassada a carencia, cobra-se
     * {@link PoliticaEmprestimo#MULTA_POR_DIA} por cada dia de atraso, contando
     * desde o primeiro dia, limitado ao teto de
     * {@link PoliticaEmprestimo#TETO_MULTA}.</p>
     *
     * @param dataPrevistaDevolucao data em que o exemplar deveria ter sido devolvido
     * @param dataDevolucao         data em que o exemplar foi efetivamente devolvido
     * @return o valor da multa em reais, nunca negativo
     * @throws IllegalArgumentException se alguma das datas for nula
     */
    public double calcular(LocalDate dataPrevistaDevolucao, LocalDate dataDevolucao) {
        if (dataPrevistaDevolucao == null) {
            throw new IllegalArgumentException("Data prevista de devolucao nao pode ser nula");
        }
        if (dataDevolucao == null) {
            throw new IllegalArgumentException("Data de devolucao nao pode ser nula");
        }

        long diasAtraso = ChronoUnit.DAYS.between(dataPrevistaDevolucao, dataDevolucao);

        if (diasAtraso <= PoliticaEmprestimo.CARENCIA_DIAS + 1) {
            return 0.0;
        }

        return diasAtraso * PoliticaEmprestimo.MULTA_POR_DIA;
    }
}
