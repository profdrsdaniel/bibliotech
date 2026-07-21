# TESTES-INTERFACE.md — Solução do laboratório de automação de interface

> ## ⚠️ Este documento é a resolução do laboratório
>
> Ele explica a suíte Selenium do fluxo de empréstimo e **revela um defeito plantado** na interface. Se você ainda não tentou o laboratório, **pare e tente antes**.
>
> **Aviso operacional:** estes testes dirigem um navegador de verdade contra a aplicação. **A aplicação precisa estar no ar** em `http://localhost:8080` enquanto os testes rodam. Se ela estiver desligada, todos falham na primeira linha.

---

## 1. O que esta suíte testa

Três cenários do fluxo de empréstimo pela tela:

1. Um empréstimo autorizado, que deve exibir mensagem de sucesso.
2. A tabela de empréstimos, que carrega com atraso e exige espera.
3. Um empréstimo recusado por regra de negócio, que **deveria** exibir mensagem de erro.

O terceiro é o que denuncia o defeito. Os valores esperados vêm da especificação das telas (seção 5 do [INTERFACE.md](INTERFACE.md)), nunca do que a tela devolve.

---

## 2. Como rodar

Você precisa de **dois terminais**.

**Terminal 1 — sobe a aplicação e deixa rodando:**

```bash
mvn spring-boot:run
```

Espere aparecer `Started BibliotechApplication`. Deixe esse terminal aberto.

**Terminal 2 — roda os testes:**

```bash
mvn test -Dtest=EmprestimoWebTest -Dmaven.test.failure.ignore=true
```

O Chrome vai abrir sozinho, executar os cliques e fechar. Se um teste borra o estado (por exemplo, empresta o livro 10), reinicie a aplicação no Terminal 1 (`Ctrl + C` e `mvn spring-boot:run` de novo) para recriar os dados iniciais.

---

## 3. Os localizadores escolhidos

A estratégia de localização é metade de um teste de interface. Escolhas frágeis geram suítes que quebram a cada mudança de layout.

| Elemento | Localizador usado | Por quê |
|---|---|---|
| Tabela de livros | `By.id("tabela-livros")` | Tem `id` estável; localizar por id é o mais robusto |
| Botão **Emprestar** da linha do livro 10 | `By.cssSelector("tr[data-id='10'] a.botao")` | O botão **não tem `id`**. Ancoramos na linha pelo `data-id` e descemos até o link de ação |
| Seleção de sócio | `By.id("select-socio")` | Id estável; embrulhado num `Select` do Selenium |
| Botão confirmar empréstimo | `By.id("btn-confirmar-emprestimo")` | Id estável |
| Área de sucesso / erro | `By.id("mensagem-sucesso")` / `By.id("mensagem-erro")` | Ids estáveis |
| Tabela de empréstimos | `By.id("tabela-emprestimos")` | Id estável |

### O botão sem id: `data-id` ancorado versus XPath absoluto

O botão Emprestar não tem identificador próprio. Duas formas de chegar até ele:

**XPath absoluto** (frágil):

```
/html/body/main/table/tbody/tr[1]/td[5]/a
```

Isso lê "o link na quinta célula da primeira linha do corpo da tabela". Quebra se: alguém adicionar uma coluna (muda o `td[5]`), reordenar as linhas (muda o `tr[1]`), ou embrulhar a tabela numa `div` (muda o caminho inteiro). Ele descreve a **posição no desenho da página**, que muda o tempo todo.

**Seletor CSS ancorado no `data-id`** (robusto):

```
tr[data-id='10'] a.botao
```

Isso lê "o link de ação na linha cujo registro é o livro 10". Não importa em que posição a linha 10 esteja, quantas colunas existam, ou como a página esteja embrulhada: enquanto a linha do livro 10 carregar `data-id="10"` e o botão tiver a classe `botao`, o localizador acha. Ele descreve o **dado**, não o desenho.

A regra prática: ancore no que identifica o registro (`data-id`), não na posição visual. Por isso a suíte não usa XPath absoluto em lugar nenhum.

---

## 4. O experimento da espera

Esta é a parte central do laboratório. A tabela de `/emprestimos` sobe no HTML **já pronta, mas com `display:none`**, e só é revelada por JavaScript depois de alguns segundos. Isso simula uma consulta lenta.

### Sem espera — o código que quebra

```java
navegador.get(URL_BASE + "/emprestimos");
WebElement tabela = navegador.findElement(By.id("tabela-emprestimos"));
String conteudo = tabela.getText();   // vem vazio!
```

O `findElement` **encontra** a tabela — ela está no HTML desde o começo — mas ela está oculta, sem conteúdo renderizado. O `getText()` de um elemento com `display:none` devolve **string vazia**. O teste lê nada e ou falha numa asserção confusa, ou pior, passa por engano lendo vazio.

### Por que o erro acontece mesmo com a tabela existindo

A intuição errada é "deu erro porque o elemento não existe". Não é isso. **O elemento existe desde o primeiro byte do HTML.** O que ainda não aconteceu é ele ficar **visível**. Presença no DOM e visibilidade na tela são duas coisas diferentes, e é justamente essa diferença que a página explora.

### Por que `presenceOfElementLocated` não resolveria

A tentação é adicionar uma espera assim:

```java
wait.until(ExpectedConditions.presenceOfElementLocated(By.id("tabela-emprestimos")));
```

Isso **não** resolve. `presenceOfElementLocated` só exige que o elemento esteja no DOM — e ele já está, desde o início. A espera passaria **imediatamente**, com a tabela ainda oculta. O teste seguiria para a linha seguinte, leria o texto vazio, e o erro apareceria ali, **longe da causa real**. Você perderia tempo investigando a linha errada.

### Com espera — a condição certa

```java
// A tabela ja existe no HTML desde o inicio, porem com display:none, e so
// e revelada depois de alguns segundos. Por isso esperamos VISIBILIDADE, e
// nao presenca: presenca passaria de imediato, com a tabela ainda oculta.
WebElement tabela = wait.until(
        ExpectedConditions.visibilityOfElementLocated(By.id("tabela-emprestimos")));
String conteudo = tabela.getText();   // agora vem preenchido
```

`visibilityOfElementLocated` exige duas coisas: o elemento no DOM **e** com altura e largura maiores que zero. Enquanto a tabela estiver `display:none`, a condição continua falsa e a espera segura o teste. No instante em que o JavaScript revela a tabela, a condição vira verdadeira e o teste avança — nem antes, nem muito depois.

### A alternativa: esperar o "Carregando..." sumir

Outra condição igualmente válida é esperar o indicador de carregamento desaparecer:

```java
wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("carregando")));
```

`invisibilityOfElementLocated` passa quando o elemento fica oculto (ou é removido). Como a página esconde o `carregando` no mesmo momento em que revela a tabela, esperar por um ou por outro dá no mesmo. Escolha o que deixar a intenção do teste mais clara.

---

## 5. Por que nunca usar `Thread.sleep`

A saída preguiçosa para o problema da tabela lenta seria:

```java
Thread.sleep(3000);   // "espera uns segundos e torce"
```

Isso é a origem das suítes **flaky** (que falham de forma intermitente), por três motivos:

- **Desperdiça tempo quando é rápido.** Se a tabela aparece em 200 ms, você ainda espera os 3 segundos inteiros. Multiplique por centenas de testes e a suíte fica lenta sem necessidade.
- **Quebra quando é lento.** Se um dia a máquina estiver carregada e a tabela levar 3,5 segundos, o `sleep(3000)` acaba antes, o teste lê vazio e falha — sem que nada esteja errado no sistema.
- **Fixa um número arbitrário.** 3000 não tem justificativa nenhuma; é um chute que ora sobra, ora falta.

A espera explícita (`WebDriverWait` + `ExpectedConditions`) resolve os três: ela retorna **assim que** a condição fica verdadeira (rápida quando dá, paciente quando precisa), e só desiste depois do timeout máximo. Por isso esta suíte não tem **nenhum** `Thread.sleep`.

---

## 6. O resultado da execução

Saída real de `mvn test`:

```
[INFO] Running br.ulbra.bibliotech.interfaceweb.EmprestimoWebTest
[ERROR] Tests run: 3, Failures: 1, Errors: 0, Skipped: 0 <<< FAILURE! -- in br.ulbra.bibliotech.interfaceweb.EmprestimoWebTest
[ERROR] EmprestimoWebTest.emprestimoRecusadoExibeMensagemDeErro <<< FAILURE!
org.opentest4j.AssertionFailedError: A area de mensagem de erro deveria conter texto
    quando o emprestimo e recusado por regra de negocio ==> expected: <false> but was: <true>

[INFO] Running br.ulbra.bibliotech.interfaceweb.AmbienteSeleniumTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] Results:
[ERROR] Tests run: 4, Failures: 1, Errors: 0, Skipped: 0
```

**Testes 1 e 2 passam. O teste 3 falha** — e é o resultado esperado. (`AmbienteSeleniumTest` é o teste de fumaça do ambiente, do laboratório anterior; ele passa.)

A asserção `expected: <false> but was: <true>` lê assim: esperávamos que "o texto do erro estar em branco" fosse `false` (ou seja, que houvesse texto), mas foi `true` — o texto estava em branco. A área de erro ficou vazia.

---

## 7. O defeito encontrado: a recusa silenciosa

O teste 3 tentou emprestar um livro para o sócio 2, que tem R$ 35,00 de multa e está bloqueado (REQ-004). O que aconteceu:

- **A regra de negócio funcionou.** O empréstimo **não** foi criado. Se você olhar em `/api/emprestimos`, não há registro novo. O sistema recusou corretamente, por dentro.
- **A API também recusaria certo.** O endpoint devolveria um erro para essa mesma tentativa. A camada de decisão está intacta.
- **O que quebrou foi a comunicação com o usuário.** A tela voltou para a lista de livros **sem exibir mensagem nenhuma**. A área `mensagem-erro` existe no HTML, mas ficou vazia. Para quem está na frente da tela, é como se o clique não tivesse feito nada: nenhum aviso, nenhuma explicação.

E aqui está a lição do laboratório: **existem defeitos que só moram nesta camada.** A lógica está certa, a API está certa, os dados estão certos. O que está errado é exclusivamente a apresentação — e nenhum teste de unidade, de serviço ou de API pegaria isso, porque para todos eles o sistema se comportou perfeitamente. Só um teste que **olha a tela** percebe que o usuário foi deixado no escuro.

É por isso que a automação de interface existe, apesar de ser mais cara, mais lenta e mais frágil que as outras camadas de teste: ela é a única que enxerga a fatia de comportamento que só existe na tela. Você não escreve muitos testes assim — escreve poucos, nos fluxos que importam —, mas para esses, não há substituto.

---

## 8. Os outros defeitos da interface

A recusa silenciosa não é o único problema plantado nas telas. Há outros comportamentos observáveis que valem uma olhada com o mesmo espírito — comparando o que a tela faz com o que a especificação (seção 5 do [INTERFACE.md](INTERFACE.md)) determina:

- a **formatação da multa** na tabela de empréstimos;
- o comportamento do **botão de confirmar** diante de um clique duplo;
- o tratamento de uma **data inválida** no formulário de devolução.

Ficam como exercício. Para cada um, a pergunta é a mesma: o que a tela faz, e o que a especificação manda?
