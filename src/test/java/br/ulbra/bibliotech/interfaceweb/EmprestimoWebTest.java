package br.ulbra.bibliotech.interfaceweb;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Suite de automacao de interface do fluxo de emprestimo.
 *
 * <p>Requer a aplicacao no ar em {@code http://localhost:8080} (suba com
 * {@code mvn spring-boot:run} em outro terminal). Os valores esperados vem da
 * especificacao das telas (secao 5 do INTERFACE.md), nunca do que a tela
 * devolve.</p>
 */
@DisplayName("Interface web - fluxo de emprestimo automatizado com Selenium")
class EmprestimoWebTest {

    private static final String URL_BASE = "http://localhost:8080";

    private WebDriver navegador;
    private WebDriverWait espera;

    @BeforeEach
    void abrirNavegador() {
        navegador = new ChromeDriver();
        espera = new WebDriverWait(navegador, Duration.ofSeconds(10));
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
        navegador.get(URL_BASE + "/");
        espera.until(ExpectedConditions.visibilityOfElementLocated(By.id("tabela-livros")));

        // O botao Emprestar nao tem id proprio; localizamos pela linha do livro
        // (data-id) e descemos ate o link de acao com um seletor CSS relativo.
        navegador.findElement(By.cssSelector("tr[data-id='10'] a.botao")).click();

        Select seletorSocio = new Select(
                espera.until(ExpectedConditions.visibilityOfElementLocated(By.id("select-socio"))));
        seletorSocio.selectByValue("1");

        navegador.findElement(By.id("btn-confirmar-emprestimo")).click();

        espera.until(ExpectedConditions.visibilityOfElementLocated(By.id("tabela-livros")));
        WebElement mensagemSucesso = navegador.findElement(By.id("mensagem-sucesso"));
        assertFalse(mensagemSucesso.getText().isBlank(),
                "A area de mensagem de sucesso deveria conter texto apos um emprestimo autorizado");
    }

    @Test
    @DisplayName("Tabela de emprestimos so e lida depois de ficar visivel")
    void tabelaDeEmprestimosCarregaComAtraso() {
        navegador.get(URL_BASE + "/emprestimos");

        // A tabela ja existe no HTML desde o inicio, porem com display:none, e so
        // e revelada depois de alguns segundos. Por isso esperamos VISIBILIDADE, e
        // nao presenca: presenca passaria de imediato, com a tabela ainda oculta.
        WebElement tabela = espera.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("tabela-emprestimos")));

        assertFalse(tabela.getText().isBlank(),
                "A tabela de emprestimos deveria ter conteudo depois de carregada");
    }

    @Test
    @DisplayName("Emprestimo recusado por regra de negocio exibe mensagem de erro")
    void emprestimoRecusadoExibeMensagemDeErro() {
        navegador.get(URL_BASE + "/");
        espera.until(ExpectedConditions.visibilityOfElementLocated(By.id("tabela-livros")));

        navegador.findElement(By.cssSelector("tr[data-id='11'] a.botao")).click();

        Select seletorSocio = new Select(
                espera.until(ExpectedConditions.visibilityOfElementLocated(By.id("select-socio"))));
        seletorSocio.selectByValue("2");

        navegador.findElement(By.id("btn-confirmar-emprestimo")).click();

        espera.until(ExpectedConditions.visibilityOfElementLocated(By.id("tabela-livros")));
        WebElement mensagemErro = navegador.findElement(By.id("mensagem-erro"));
        assertFalse(mensagemErro.getText().isBlank(),
                "A area de mensagem de erro deveria conter texto quando o emprestimo e recusado por regra de negocio");
    }
}
