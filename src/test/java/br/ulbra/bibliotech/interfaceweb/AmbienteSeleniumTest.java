package br.ulbra.bibliotech.interfaceweb;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Teste de fumaca do ambiente de automacao.
 *
 * <p>Nao valida regra de negocio nenhuma. Serve apenas para confirmar que o
 * Selenium consegue abrir o Chrome, navegar ate a aplicacao e fecha-lo. Se este
 * teste passar, o ambiente esta pronto para os testes de interface.</p>
 *
 * <p>Requer a aplicacao no ar em {@code http://localhost:8080/} (suba com
 * {@code mvn spring-boot:run} em outro terminal).</p>
 */
@DisplayName("Teste de fumaca - confirma que o ambiente Selenium + Chrome funciona")
class AmbienteSeleniumTest {

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
    @DisplayName("Abre a pagina inicial e confirma que o titulo nao esta vazio")
    void paginaInicialTemTitulo() {
        navegador.get("http://localhost:8080/");

        String titulo = navegador.getTitle();

        assertNotNull(titulo);
        assertFalse(titulo.isBlank());
    }
}
