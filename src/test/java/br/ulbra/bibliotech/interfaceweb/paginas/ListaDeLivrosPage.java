package br.ulbra.bibliotech.interfaceweb.paginas;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page object da tela de lista de livros (a pagina inicial).
 *
 * <p>Concentra os localizadores e as acoes dessa tela. Nao contem nenhuma
 * assercao: informa o que a tela mostra e deixa o julgamento para o teste.</p>
 */
public class ListaDeLivrosPage {

    private static final String URL = "http://localhost:8080/";
    private static final By TABELA = By.id("tabela-livros");
    private static final By MENSAGEM_SUCESSO = By.id("mensagem-sucesso");
    private static final By MENSAGEM_ERRO = By.id("mensagem-erro");

    private final WebDriver navegador;
    private final WebDriverWait espera;

    public ListaDeLivrosPage(WebDriver navegador) {
        this.navegador = navegador;
        this.espera = new WebDriverWait(navegador, Duration.ofSeconds(10));
    }

    public ListaDeLivrosPage abrir() {
        navegador.get(URL);
        return aguardarCarregar();
    }

    public FormularioEmprestimoPage emprestarLivro(long livroId) {
        navegador.findElement(botaoEmprestar(livroId)).click();
        return new FormularioEmprestimoPage(navegador);
    }

    public String mensagemDeSucesso() {
        return navegador.findElement(MENSAGEM_SUCESSO).getText();
    }

    public String mensagemDeErro() {
        return navegador.findElement(MENSAGEM_ERRO).getText();
    }

    ListaDeLivrosPage aguardarCarregar() {
        espera.until(ExpectedConditions.visibilityOfElementLocated(TABELA));
        return this;
    }

    private By botaoEmprestar(long livroId) {
        // O botao Emprestar nao tem id proprio; ancoramos na linha do livro
        // (data-id) e descemos ate o link de acao com um seletor CSS relativo.
        return By.cssSelector("tr[data-id='" + livroId + "'] a.botao");
    }
}
