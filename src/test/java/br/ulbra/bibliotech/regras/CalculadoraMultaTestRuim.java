package br.ulbra.bibliotech.regras;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

/**
 * Suíte propositalmente ruim.
 * Passa 100% verde sobre um sistema que tem TRÊS defeitos.
 * Seu trabalho: descobrir por que ela engana e refatorá-la.
 */
class CalculadoraMultaTestRuim {

    // ---------- Anti-padrão 1: Métodos copiados ----------
    // Oito @Test quase idênticos, um por valor de atraso.
    // Calculadora e data criadas dentro de cada método (sem @BeforeEach).

    @Test
    void testAtraso0Dias() {
        CalculadoraMulta calc = new CalculadoraMulta();
        LocalDate prevista = LocalDate.of(2026, 3, 10);
        LocalDate devolucao = prevista.plusDays(0);
        double multa = calc.calcular(prevista, devolucao);
        assertEquals(0.0, multa);
    }

    @Test
    void testAtraso1Dia() {
        CalculadoraMulta calc = new CalculadoraMulta();
        LocalDate prevista = LocalDate.of(2026, 3, 10);
        LocalDate devolucao = prevista.plusDays(1);
        double multa = calc.calcular(prevista, devolucao);
        assertEquals(0.0, multa);
    }

    @Test
    void testAtraso2Dias() {
        CalculadoraMulta calc = new CalculadoraMulta();
        LocalDate prevista = LocalDate.of(2026, 3, 10);
        LocalDate devolucao = prevista.plusDays(2);
        double multa = calc.calcular(prevista, devolucao);
        assertEquals(0.0, multa);
    }

    @Test
    void testAtraso4Dias() {
        CalculadoraMulta calc = new CalculadoraMulta();
        LocalDate prevista = LocalDate.of(2026, 3, 10);
        LocalDate devolucao = prevista.plusDays(4);
        double multa = calc.calcular(prevista, devolucao);
        assertEquals(4.0, multa);
    }

    @Test
    void testAtraso5Dias() {
        CalculadoraMulta calc = new CalculadoraMulta();
        LocalDate prevista = LocalDate.of(2026, 3, 10);
        LocalDate devolucao = prevista.plusDays(5);
        double multa = calc.calcular(prevista, devolucao);
        assertEquals(5.0, multa);
    }

    @Test
    void testAtraso7Dias() {
        CalculadoraMulta calc = new CalculadoraMulta();
        LocalDate prevista = LocalDate.of(2026, 3, 10);
        LocalDate devolucao = prevista.plusDays(7);
        double multa = calc.calcular(prevista, devolucao);
        assertEquals(7.0, multa);
    }

    @Test
    void testAtraso10Dias() {
        CalculadoraMulta calc = new CalculadoraMulta();
        LocalDate prevista = LocalDate.of(2026, 3, 10);
        LocalDate devolucao = prevista.plusDays(10);
        double multa = calc.calcular(prevista, devolucao);
        assertEquals(10.0, multa);
    }

    @Test
    void testAtraso30Dias() {
        CalculadoraMulta calc = new CalculadoraMulta();
        LocalDate prevista = LocalDate.of(2026, 3, 10);
        LocalDate devolucao = prevista.plusDays(30);
        double multa = calc.calcular(prevista, devolucao);
        assertEquals(30.0, multa);
    }

    // ---------- Anti-padrão 2 e 3: Lógica no teste + Esperado recalculado ----------
    // O for percorre vários dias e o if decide o esperado copiando a regra do sistema.
    // Se o sistema tem bug, o teste copia o mesmo bug e concorda.

    @Test
    void testVariosValoresComLoop() {
        CalculadoraMulta calc = new CalculadoraMulta();
        LocalDate prevista = LocalDate.of(2026, 3, 10);
        double valorDiario = 1.00;

        for (int dias = 0; dias <= 60; dias++) {
            LocalDate devolucao = prevista.plusDays(dias);
            double multa = calc.calcular(prevista, devolucao);

            double esperado;
            if (dias <= 3) {
                esperado = 0.0;
            } else {
                esperado = dias * valorDiario;
            }
            assertEquals(esperado, multa);
        }
    }

    // ---------- Anti-padrão 4: Teste sem asserção ----------
    // Chama o método em vários cenários mas não verifica nada.
    // Existe só para inflar a cobertura de código.

    @Test
    void testCobertura() {
        CalculadoraMulta calc = new CalculadoraMulta();
        LocalDate prevista = LocalDate.of(2026, 3, 10);

        calc.calcular(prevista, prevista.plusDays(0));
        calc.calcular(prevista, prevista.plusDays(1));
        calc.calcular(prevista, prevista.plusDays(3));
        calc.calcular(prevista, prevista.plusDays(10));
        calc.calcular(prevista, prevista.plusDays(50));
        calc.calcular(prevista, prevista.plusDays(100));
        // nenhum assertEquals — o teste "passa" sem verificar nada
    }

    // ---------- Anti-padrão 5: Assertion roulette ----------
    // Cinco verificações num método só, sem @DisplayName.
    // Quando falha, você não sabe qual das cinco quebrou.

    @Test
    void testDatasEspeciais() {
        CalculadoraMulta calc = new CalculadoraMulta();
        LocalDate prevista = LocalDate.of(2026, 3, 10);

        assertEquals(0.0, calc.calcular(prevista, prevista));
        assertEquals(0.0, calc.calcular(prevista, prevista.plusDays(1)));
        assertEquals(0.0, calc.calcular(prevista, prevista.plusDays(2)));
        assertThrows(IllegalArgumentException.class, () -> calc.calcular(null, prevista));
        assertThrows(IllegalArgumentException.class, () -> calc.calcular(prevista, null));
    }
}
