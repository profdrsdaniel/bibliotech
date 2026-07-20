package br.ulbra.bibliotech.regras;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Suite de testes unitarios da {@link CalculadoraMulta}.
 *
 * <p>Os valores esperados de cada teste vem da especificacao da REQ-003, nunca do
 * que o sistema devolve. Os casos foram derivados por particao de equivalencia e
 * analise de valor-limite.</p>
 */
@DisplayName("CalculadoraMulta - multa por atraso na devolucao (REQ-003)")
class CalculadoraMultaTest {

    private CalculadoraMulta calculadora;
    private LocalDate prevista;

    @BeforeEach
    void preparar() {
        calculadora = new CalculadoraMulta();
        prevista = LocalDate.of(2026, 3, 10);
    }

    // -----------------------------------------------------------------------
    // Particao de equivalencia: um representante de cada faixa de comportamento
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("P1 - devolucao adiantada nao gera multa")
    void devolucaoAdiantadaNaoGeraMulta() {
        // preparar
        LocalDate devolucao = prevista.minusDays(3);

        // executar
        double multa = calculadora.calcular(prevista, devolucao);

        // verificar
        assertEquals(0.00, multa, 0.001);
    }

    @Test
    @DisplayName("P2 - atraso dentro da carencia nao gera multa")
    void atrasoDentroDaCarenciaNaoGeraMulta() {
        // preparar
        LocalDate devolucao = prevista.plusDays(1);

        // executar
        double multa = calculadora.calcular(prevista, devolucao);

        // verificar
        assertEquals(0.00, multa, 0.001);
    }

    @Test
    @DisplayName("P3 - atraso acima da carencia cobra um real por dia")
    void atrasoAcimaDaCarenciaCobraPorDia() {
        // preparar
        LocalDate devolucao = prevista.plusDays(10);

        // executar
        double multa = calculadora.calcular(prevista, devolucao);

        // verificar
        assertEquals(10.00, multa, 0.001);
    }

    @Test
    @DisplayName("P4 - atraso muito longo respeita o teto de R$ 50,00")
    void atrasoMuitoLongoRespeitaOTeto() {
        // preparar
        LocalDate devolucao = prevista.plusDays(365);

        // executar
        double multa = calculadora.calcular(prevista, devolucao);

        // verificar
        assertEquals(50.00, multa, 0.001);
    }

    // -----------------------------------------------------------------------
    // Analise de valor-limite: fronteiras entre as faixas
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "atraso de {0} dias deve gerar multa de R$ {1}")
    @DisplayName("Fronteiras entre as faixas de multa")
    @CsvSource({
            "-1, 0.00",
            "0,  0.00",
            "1,  0.00",
            "2,  0.00",
            "3,  3.00",
            "4,  4.00",
            "49, 49.00",
            "50, 50.00",
            "51, 50.00"
    })
    void fronteirasDaMultaGeramValorEsperado(long diasAtraso, double multaEsperada) {
        LocalDate devolucao = prevista.plusDays(diasAtraso);

        double multa = calculadora.calcular(prevista, devolucao);

        assertEquals(multaEsperada, multa, 0.001);
    }

    // -----------------------------------------------------------------------
    // Tratamento de entradas invalidas
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Data prevista nula lanca IllegalArgumentException")
    void dataPrevistaNulaLancaExcecao() {
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcular(null, prevista));
    }

    @Test
    @DisplayName("Data de devolucao nula lanca IllegalArgumentException")
    void dataDevolucaoNulaLancaExcecao() {
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcular(prevista, null));
    }
}
