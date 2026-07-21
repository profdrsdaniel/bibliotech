package br.ulbra.bibliotech.interfaceweb.paginas;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page object do formulario de emprestimo.
 *
 * <p>Concentra os localizadores e as acoes dessa tela. Nao contem nenhuma
 * assercao.</p>
 */
public class FormularioEmprestimoPage {

    private static final By SELECT_SOCIO = By.id("select-socio");
    private static final By BOTAO_CONFIRMAR = By.id("btn-confirmar-emprestimo");

    private final WebDriver navegador;
    private final WebDriverWait espera;

    public FormularioEmprestimoPage(WebDriver navegador) {
        this.navegador = navegador;
        this.espera = new WebDriverWait(navegador, Duration.ofSeconds(10));
    }

    public FormularioEmprestimoPage selecionarSocio(long socioId) {
        WebElement campo = espera.until(ExpectedConditions.visibilityOfElementLocated(SELECT_SOCIO));
        new Select(campo).selectByValue(String.valueOf(socioId));
        return this;
    }

    public ListaDeLivrosPage confirmar() {
        navegador.findElement(BOTAO_CONFIRMAR).click();
        // Confirmar o emprestimo devolve o usuario para a lista de livros.
        return new ListaDeLivrosPage(navegador).aguardarCarregar();
    }
}
