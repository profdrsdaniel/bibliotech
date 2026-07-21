package br.ulbra.bibliotech.interfaceweb.paginas;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page object da tela de lista de emprestimos.
 *
 * <p>Esta tela carrega com atraso: a espera fica encapsulada aqui, dentro do
 * {@link #abrir()}. O teste nao precisa saber que a tela demora.</p>
 */
public class ListaDeEmprestimosPage {

    private static final String URL = "http://localhost:8080/emprestimos";
    private static final By TABELA = By.id("tabela-emprestimos");
    private static final By CARREGANDO = By.id("carregando");

    private final WebDriver navegador;
    private final WebDriverWait espera;

    public ListaDeEmprestimosPage(WebDriver navegador) {
        this.navegador = navegador;
        this.espera = new WebDriverWait(navegador, Duration.ofSeconds(10));
    }

    public ListaDeEmprestimosPage abrir() {
        navegador.get(URL);
        // A tabela ja existe no HTML desde o inicio, porem com display:none, e so
        // e revelada depois de alguns segundos, quando o indicador CARREGANDO some.
        // Por isso a espera e por VISIBILIDADE, e nao por presenca: presenca
        // passaria de imediato, com a tabela ainda oculta.
        espera.until(ExpectedConditions.visibilityOfElementLocated(TABELA));
        return this;
    }

    public String conteudoDaTabela() {
        return navegador.findElement(TABELA).getText();
    }
}
