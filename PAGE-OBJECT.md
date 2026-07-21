# PAGE-OBJECT.md — Solução do laboratório de Page Object

> ## ⚠️ Este documento é a resolução do laboratório
>
> Ele mostra como refatorar a suíte Selenium usando o padrão Page Object. Se você ainda não tentou o laboratório, **pare e tente antes**.
>
> Este laboratório é uma **refatoração**: o comportamento dos testes não muda (continuam dois passando e um falhando), só a organização do código.

---

## 1. O problema que o padrão resolve

Na suíte original, os localizadores da tela ficavam espalhados dentro dos testes. Veja o teste de empréstimo autorizado **antes**:

```java
navegador.get(URL_BASE + "/");
espera.until(ExpectedConditions.visibilityOfElementLocated(By.id("tabela-livros")));
navegador.findElement(By.cssSelector("tr[data-id='10'] a.botao")).click();
Select seletorSocio = new Select(
        espera.until(ExpectedConditions.visibilityOfElementLocated(By.id("select-socio"))));
seletorSocio.selectByValue("1");
navegador.findElement(By.id("btn-confirmar-emprestimo")).click();
espera.until(ExpectedConditions.visibilityOfElementLocated(By.id("tabela-livros")));
WebElement mensagemSucesso = navegador.findElement(By.id("mensagem-sucesso"));
assertFalse(mensagemSucesso.getText().isBlank(), "...");
```

E o mesmo teste **depois**:

```java
String mensagem = new ListaDeLivrosPage(navegador)
        .abrir()
        .emprestarLivro(10)
        .selecionarSocio(1)
        .confirmar()
        .mensagemDeSucesso();

assertFalse(mensagem.isBlank(), "...");
```

A diferença fica evidente quando um `id` muda. Suponha que o botão de confirmar deixe de ser `btn-confirmar-emprestimo` e vire outro id.

- **Antes:** esse localizador estava escrito **em dois lugares** — no teste de empréstimo autorizado e no de empréstimo recusado. Os dois quebram, e você precisa achar e corrigir os dois. O seletor do botão Emprestar (`tr[data-id='...'] a.botao`) também aparecia em dois testes; e o `tabela-livros`, em **três**. Cada mudança de tela obriga uma caçada por todos os pontos duplicados.
- **Depois:** cada localizador existe em **um único lugar**, dentro da page object da sua tela. Muda o id do botão? Você corrige a linha `BOTAO_CONFIRMAR` na `FormularioEmprestimoPage`, **uma vez**, e todos os testes que passam por ali voltam a funcionar. Zero caçada.

Essa é a promessa central do Page Object: **um localizador, um lugar.** A tela mudou? O conserto é local e único.

---

## 2. A anatomia de uma page object

Cada uma das três classes tem as mesmas cinco partes. Veja a `ListaDeLivrosPage`:

**1. Localizadores** — no topo, como `private static final By`:

```java
private static final By TABELA = By.id("tabela-livros");
private static final By MENSAGEM_SUCESSO = By.id("mensagem-sucesso");
private static final By MENSAGEM_ERRO = By.id("mensagem-erro");
```

Regra: todo localizador da tela mora aqui, e só aqui. É a "lista de endereços" da página, num lugar só.

**2. O driver, recebido no construtor:**

```java
private final WebDriver navegador;

public ListaDeLivrosPage(WebDriver navegador) {
    this.navegador = navegador;
    this.espera = new WebDriverWait(navegador, Duration.ofSeconds(10));
}
```

Regra: a page object **recebe** o driver, nunca o cria. Quem controla o ciclo de vida do navegador (abrir, fechar) é o teste; a page object só o usa. Assim a mesma instância de navegador atravessa todas as telas do fluxo.

**3. Ações** — o que o usuário faz na tela, cada uma devolvendo a próxima page object:

```java
public FormularioEmprestimoPage emprestarLivro(long livroId) {
    navegador.findElement(botaoEmprestar(livroId)).click();
    return new FormularioEmprestimoPage(navegador);
}
```

Regra: uma ação representa um gesto do usuário e leva à tela seguinte.

**4. Consultas** — o que a tela mostra, devolvido como dado simples:

```java
public String mensagemDeSucesso() {
    return navegador.findElement(MENSAGEM_SUCESSO).getText();
}
```

Regra: consultas devolvem `String` (ou outro dado), **sem julgar** se o valor está certo.

**5. Localizadores dinâmicos** — quando o localizador depende de um parâmetro, um método privado o monta:

```java
private By botaoEmprestar(long livroId) {
    return By.cssSelector("tr[data-id='" + livroId + "'] a.botao");
}
```

Regra: a lógica de montar o seletor (aqui, ancorar no `data-id` da linha) fica escondida dentro da page object. O teste diz `emprestarLivro(10)` e não sabe — nem precisa saber — como a linha 10 é localizada.

---

## 3. Nomear a ação, não o clique

O nome de um método de page object deve descrever **o que o usuário quer**, não **o que o Selenium faz**.

| Nome mecânico (evite) | Nome por intenção (prefira) |
|---|---|
| `clicarBotaoEmprestar()` | `emprestarLivro(10)` |
| `preencherSelectSocio()` | `selecionarSocio(1)` |
| `clicarBotaoConfirmar()` | `confirmar()` |

Por que a segunda coluna sobrevive a mudanças de tela: suponha que o botão Emprestar vire um menu suspenso, ou um link, ou um gesto de arrastar. A **ação do usuário continua a mesma** — ele quer emprestar o livro 10. O método `emprestarLivro(10)` continua com o nome certo; muda só o que está dentro dele. Já `clicarBotaoEmprestar()` passaria a mentir: não há mais botão, não há mais clique. O nome mecânico está preso à implementação de hoje; o nome por intenção descreve o propósito, que é estável.

---

## 4. O encadeamento

Repare que cada ação devolve a page object da **tela seguinte**:

- `abrir()` devolve a própria `ListaDeLivrosPage`;
- `emprestarLivro(10)` devolve uma `FormularioEmprestimoPage`;
- `selecionarSocio(1)` devolve a própria `FormularioEmprestimoPage`;
- `confirmar()` devolve uma `ListaDeLivrosPage`, porque é para lá que o sistema volta.

Isso permite escrever o teste como uma frase:

```java
new ListaDeLivrosPage(navegador)
        .abrir()
        .emprestarLivro(10)
        .selecionarSocio(1)
        .confirmar()
        .mensagemDeSucesso();
```

O efeito colateral mais valioso é este: **o compilador passa a conhecer a navegação do sistema.** Como `emprestarLivro` devolve um `FormularioEmprestimoPage`, você só consegue chamar, na sequência, os métodos que existem naquela tela. Se você tentar `emprestarLivro(10).conteudoDaTabela()`, o código **nem compila** — `conteudoDaTabela` não existe no formulário. O fluxo válido de telas vira uma regra verificada em tempo de compilação, e caminhos impossíveis pela interface ficam impossíveis também no teste.

---

## 5. Por que asserção não entra na page object

As page objects **não têm nenhuma asserção**. Nenhum `assertFalse`, nenhum `assertEquals`. Elas informam; o teste julga. A divisão é: a page object diz "a mensagem de sucesso é este texto"; o teste decide "esse texto deveria estar preenchido".

O argumento é prático. Imagine que a page object tivesse um método `verificarMensagemDeSucessoPreenchida()`, que já faz a asserção por dentro. Ele serviria para **um cenário só** — o que espera a mensagem preenchida. No dia em que você precisar de um teste que verifica a mensagem **vazia**, ou que ela contém um texto específico, esse método não serve, e você teria que criar outro, e outro.

Um método que devolve `String` — `mensagemDeSucesso()` — serve para **qualquer** verificação:

```java
assertFalse(pagina.mensagemDeSucesso().isBlank());       // um teste
assertEquals("Emprestimo realizado", pagina.mensagemDeSucesso());  // outro
assertTrue(pagina.mensagemDeSucesso().contains("Dom Casmurro"));   // outro
```

A page object devolve o dado bruto e não decide nada; cada teste faz o julgamento que precisa. É por isso que a asserção fica do lado do teste: é lá que mora a expectativa, e a expectativa muda de um teste para outro.

---

## 6. Onde a espera foi parar

Na suíte original, a espera pela tabela lenta de `/emprestimos` estava escrita **dentro do teste**. Na versão refatorada, ela foi para dentro do `abrir()` da `ListaDeEmprestimosPage`:

```java
public ListaDeEmprestimosPage abrir() {
    navegador.get(URL);
    // A tabela ja existe no HTML desde o inicio, porem com display:none, e so
    // e revelada depois de alguns segundos... Por isso a espera e por VISIBILIDADE.
    espera.until(ExpectedConditions.visibilityOfElementLocated(TABELA));
    return this;
}
```

E o teste ficou assim:

```java
String conteudo = new ListaDeEmprestimosPage(navegador)
        .abrir()
        .conteudoDaTabela();
```

O teste **não sabe** que aquela tela demora. Ele pede para abrir e para ler o conteúdo, como faria com qualquer outra tela. O detalhe de que a tabela sobe oculta e precisa de espera por visibilidade é um problema **daquela tela** — e por isso vive na page object daquela tela. Se um dia a tela deixar de ter atraso, você remove a espera de um lugar só, e nenhum teste precisa ser tocado. A page object encapsula não só *onde* estão os elementos, mas também *como* lidar com o comportamento peculiar da tela.

---

## 7. O resultado da execução

A refatoração não pode mudar o comportamento. Saída real de `mvn test` **depois** da refatoração:

```text
[INFO] Running br.ulbra.bibliotech.interfaceweb.EmprestimoWebTest
[ERROR] Tests run: 3, Failures: 1, Errors: 0, Skipped: 0 <<< FAILURE! -- in EmprestimoWebTest
[ERROR] EmprestimoWebTest.emprestimoRecusadoExibeMensagemDeErro <<< FAILURE!
org.opentest4j.AssertionFailedError: A area de mensagem de erro deveria conter texto
    quando o emprestimo e recusado por regra de negocio ==> expected: <false> but was: <true>

[INFO] Running br.ulbra.bibliotech.interfaceweb.AmbienteSeleniumTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] Results:
[ERROR] Tests run: 4, Failures: 1, Errors: 0, Skipped: 0
```

**Dois testes de fluxo passam, um falha** — exatamente como antes da refatoração. (`AmbienteSeleniumTest` é o teste de fumaça do ambiente, que também continua passando.) O teste que falha continua sendo o do empréstimo recusado, pelo mesmo motivo: a área de erro fica vazia (defeito da recusa silenciosa). Nada no comportamento mudou — só a organização do código.

Essa é a prova de que a refatoração foi feita certo: o mesmo número de testes, as mesmas falhas, pelas mesmas razões. Uma refatoração que muda o resultado dos testes não é refatoração; é alteração de comportamento disfarçada.

---

## 8. Como rodar

Você precisa de **dois terminais**.

**Terminal 1 — sobe a aplicação:**

```bash
mvn spring-boot:run
```

Espere aparecer `Started BibliotechApplication`. Deixe rodando.

**Terminal 2 — roda os testes:**

```bash
mvn test -Dtest=EmprestimoWebTest -Dmaven.test.failure.ignore=true
```

Se um teste borrar o estado (o de sucesso empresta o livro 10), reinicie a aplicação no Terminal 1 para recriar os dados iniciais antes de rodar de novo.
