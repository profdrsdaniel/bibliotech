package br.ulbra.bibliotech.regras;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

/**
 * Classe de demonstracao para a aula de cobertura de codigo.
 *
 * <p>Ela executa todos os caminhos da {@link CalculadoraMulta} sem verificar
 * nenhum resultado. Existe apenas para provar um ponto: cobertura mede o que foi
 * executado, nao o que foi verificado. Rodando esta classe sozinha, a cobertura
 * da {@code CalculadoraMulta} fica alta, e mesmo assim ela nao detecta nenhum dos
 * defeitos plantados no sistema.</p>
 *
 * <p><strong>Nao use esta classe como modelo.</strong> Um teste sem assercoes e
 * um teste que nunca falha, e um teste que nunca falha nao protege contra nada.
 * Ela esta aqui como contraexemplo, nao como referencia.</p>
 */
@DisplayName("Demonstracao - cobertura alta sem nenhuma verificacao (NAO e modelo)")
class CoberturaFabricadaTest {

    @Test
    @DisplayName("Executa todos os caminhos da CalculadoraMulta sem verificar nada")
    void executaTodosOsCaminhosSemVerificar() {
        CalculadoraMulta calculadora = new CalculadoraMulta();
        LocalDate prevista = LocalDate.of(2026, 3, 10);

        calculadora.calcular(prevista, prevista.minusDays(3));
        calculadora.calcular(prevista, prevista.plusDays(1));
        calculadora.calcular(prevista, prevista.plusDays(10));

        try {
            calculadora.calcular(null, prevista);
        } catch (IllegalArgumentException excecao) {
            // caminho de data prevista nula
        }

        try {
            calculadora.calcular(prevista, null);
        } catch (IllegalArgumentException excecao) {
            // caminho de data de devolucao nula
        }
    }
}
