package br.ulbra.bibliotech.servico;

import br.ulbra.bibliotech.dominio.Emprestimo;
import br.ulbra.bibliotech.dominio.Livro;
import br.ulbra.bibliotech.dominio.Socio;
import br.ulbra.bibliotech.regras.CalculadoraMulta;
import br.ulbra.bibliotech.repositorio.EmprestimoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Suite de testes do {@link EmprestimoService}.
 *
 * <p>Os valores esperados vem da especificacao das regras REQ-001 e REQ-004,
 * nunca do que o sistema devolve. O repositorio e substituido por um duble de
 * teste criado com Mockito: nos testes de regra ele e apenas um dummy, e nos
 * testes de gravacao ele e usado para verificar a interacao com {@code verify}.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmprestimoService - regras de impedimento e gravacao (REQ-001 e REQ-004)")
class EmprestimoServiceTest {

    @Mock
    private EmprestimoRepository repositorio;

    private EmprestimoService servico;

    @BeforeEach
    void preparar() {
        servico = new EmprestimoService(repositorio, new CalculadoraMulta());
    }

    // -----------------------------------------------------------------------
    // REQ-001: limite de emprestimos ativos
    // Aqui o repositorio e um dummy: existe so porque o construtor exige.
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "com {0} emprestimos ativos, atingiuLimite deve ser {1}")
    @DisplayName("Limite de emprestimos ativos (REQ-001)")
    @CsvSource({
            "0, false",
            "2, false",
            "3, true",
            "4, true"
    })
    void limiteDeEmprestimosRetornaEsperado(int emprestimosAtivos, boolean esperado) {
        Socio socio = new Socio(1L, "Socio Teste", "teste@ulbra.br", 0.0, emprestimosAtivos);

        boolean atingiu = servico.atingiuLimiteDeEmprestimos(socio);

        assertEquals(esperado, atingiu);
    }

    // -----------------------------------------------------------------------
    // REQ-004: bloqueio por multa acumulada
    // Aqui o repositorio tambem e um dummy.
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "com multa de R$ {0}, estaBloqueado deve ser {1}")
    @DisplayName("Bloqueio por multa acumulada (REQ-004)")
    @CsvSource({
            "0.00,  false",
            "19.99, false",
            "20.00, false",
            "20.01, true",
            "35.00, true"
    })
    void bloqueioPorMultaRetornaEsperado(double multaAcumulada, boolean esperado) {
        Socio socio = new Socio(1L, "Socio Teste", "teste@ulbra.br", multaAcumulada, 0);

        boolean bloqueado = servico.estaBloqueadoPorMulta(socio);

        assertEquals(esperado, bloqueado);
    }

    // -----------------------------------------------------------------------
    // Interacao com o repositorio (mock de verdade)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Emprestimo autorizado deve ser salvo no repositorio")
    void emprestimoAutorizadoDeveSerSalvo() {
        when(repositorio.proximoId()).thenReturn(1L);

        Socio socio = new Socio(1L, "Ana Prado", "ana.prado@ulbra.br", 0.0, 0);
        Livro livro = new Livro(10L, "Dom Casmurro", "Machado de Assis", "978-85-359-0277-5");

        servico.emprestar(socio, livro, LocalDate.of(2026, 3, 1));

        verify(repositorio).salvar(any(Emprestimo.class));
    }

    @Test
    @DisplayName("Emprestimo recusado por multa nunca deve ser salvo")
    void emprestimoRecusadoNuncaDeveSerSalvo() {
        Socio socio = new Socio(2L, "Bruno Lima", "bruno.lima@ulbra.br", 35.0, 0);
        Livro livro = new Livro(11L, "Vidas Secas", "Graciliano Ramos", "978-85-03-01204-8");

        assertThrows(IllegalStateException.class,
                () -> servico.emprestar(socio, livro, LocalDate.of(2026, 3, 1)));

        verify(repositorio, never()).salvar(any());
    }
}
