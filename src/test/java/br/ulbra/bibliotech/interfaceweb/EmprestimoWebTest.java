package br.ulbra.bibliotech.interfaceweb;

import br.ulbra.bibliotech.interfaceweb.paginas.ListaDeEmprestimosPage;
import br.ulbra.bibliotech.interfaceweb.paginas.ListaDeLivrosPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Suite de automacao de interface do fluxo de emprestimo.
 *
 * <p>Requer a aplicacao no ar em {@code http://localhost:8080} (suba com
 * {@code mvn spring-boot:run} em outro terminal). Os localizadores e as esperas
 * vivem nas page objects do pacote {@code paginas}; aqui ficam apenas a intencao
 * de cada cenario e o julgamento do resultado.</p>
 */
@DisplayName("Interface web - fluxo de emprestimo automatizado com Selenium")
class EmprestimoWebTest {

    private WebDriver navegador;

    @BeforeEach
    void abrirNavegador() {
        navegador = new ChromeDriver();
    }

    @AfterEach
    void fecharNavegador() {
        if (navegador != null) {
            navegador.quit();
        }
    }

    @Test
    @DisplayName("Emprestimo autorizado exibe mensagem de sucesso")
    void emprestimoAutorizadoExibeMensagemDeSucesso() {
        String mensagem = new ListaDeLivrosPage(navegador)
                .abrir()
                .emprestarLivro(10)
                .selecionarSocio(1)
                .confirmar()
                .mensagemDeSucesso();

        assertFalse(mensagem.isBlank(),
                "A area de mensagem de sucesso deveria conter texto apos um emprestimo autorizado");
    }

    @Test
    @DisplayName("Tabela de emprestimos so e lida depois de ficar visivel")
    void tabelaDeEmprestimosCarregaComAtraso() {
        String conteudo = new ListaDeEmprestimosPage(navegador)
                .abrir()
                .conteudoDaTabela();

        assertFalse(conteudo.isBlank(),
                "A tabela de emprestimos deveria ter conteudo depois de carregada");
    }

    @Test
    @DisplayName("Emprestimo recusado por regra de negocio exibe mensagem de erro")
    void emprestimoRecusadoExibeMensagemDeErro() {
        String mensagem = new ListaDeLivrosPage(navegador)
                .abrir()
                .emprestarLivro(11)
                .selecionarSocio(2)
                .confirmar()
                .mensagemDeErro();

        assertFalse(mensagem.isBlank(),
                "A area de mensagem de erro deveria conter texto quando o emprestimo e recusado por regra de negocio");
    }
}
