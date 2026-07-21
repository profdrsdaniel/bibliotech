# SERVICO.md — Solução do laboratório de dublês de teste

> ## ⚠️ Este documento é a resolução do laboratório
>
> Ele explica a suíte do `EmprestimoService`, mostra como usar dublês de teste com o Mockito e **revela dois dos defeitos plantados** no sistema. Se você ainda não tentou o laboratório por conta própria, **pare e tente antes**.

---

## 1. O que esta suíte testa

O `EmprestimoService` é a camada que decide se um empréstimo pode acontecer. Ele depende do `EmprestimoRepository` para gravar e consultar empréstimos. Testar o serviço sem depender de um banco real é o que motiva os **dublês de teste**: no lugar do repositório de verdade, entra um objeto controlado pelo teste.

Esta suíte cobre duas regras de decisão — o limite de empréstimos (REQ-001) e o bloqueio por multa (REQ-004) — e duas garantias de interação com o repositório: o empréstimo autorizado é gravado, e o recusado nunca é.

Os valores esperados saem da especificação, nunca do que o sistema devolve.

---

## 2. O mapa de dependências

Antes de escolher o dublê, é preciso saber **quais métodos usam o repositório**. Essa é a informação que decide onde é necessário um mock de verdade e onde um dummy basta.

| Método do `EmprestimoService` | Usa o repositório? | Como |
|---|---|---|
| `atingiuLimiteDeEmprestimos(Socio)` | **Não** | Lê só o estado do próprio sócio |
| `estaBloqueadoPorMulta(Socio)` | **Não** | Lê só a multa do próprio sócio |
| `verificarImpedimento(Socio, Livro)` | **Não** | Combina as verificações acima com o estado do livro |
| `emprestar(Socio, Livro, LocalDate)` | **Sim** | Chama `proximoId()` e `salvar(...)` |
| `devolver(Emprestimo, LocalDate)` | **Sim** | Chama `salvar(...)` |

A leitura desse mapa é direta:

- Para testar `atingiuLimiteDeEmprestimos` e `estaBloqueadoPorMulta`, o repositório **não é tocado**. Ele só precisa existir porque o construtor do serviço o exige. Aí basta um **dummy**.
- Para testar `emprestar`, o repositório **é** usado. Aí precisamos de um **mock**, tanto para fornecer o `proximoId()` quanto para verificar se `salvar(...)` foi chamado.

Testar com dummy o que não usa a dependência evita configuração inútil — e o Mockito, no modo estrito, reclama de `when(...)` que nunca é exercitado (*unnecessary stubbing*). Por isso, nos testes parametrizados de regra, **não há nenhum `when(...)`**.

---

## 3. Os cinco tipos de dublê, aplicados a este projeto

"Dublê de teste" (*test double*) é o termo guarda-chuva. Há cinco tipos, e a diferença entre eles é o quanto de comportamento eles têm:

| Tipo | O que é | Neste projeto |
|---|---|---|
| **Dummy** | Objeto que só preenche um parâmetro; nunca é usado de verdade | O `@Mock EmprestimoRepository` nos dois testes parametrizados de regra — está lá só porque o construtor exige, sem nenhum `when(...)` |
| **Stub** | Devolve respostas fixas configuradas para a chamada | O `when(repositorio.proximoId()).thenReturn(1L)` no teste de gravação: o repositório responde um id fixo |
| **Spy** | Objeto real embrulhado, que registra como foi chamado | Não usamos nesta suíte; seria um `spy(...)` sobre um objeto real |
| **Mock** | Dublê programável **cuja interação é verificada** com `verify(...)` | O `EmprestimoRepository` nos testes de `emprestar`, onde checamos se `salvar(...)` foi (ou não) chamado |
| **Fake** | Implementação funcional, porém simplificada, leve demais para produção | O **`EmprestimoRepositoryEmMemoria`** do próprio projeto: guarda tudo num `LinkedHashMap`, funciona de verdade, mas some quando a aplicação para |

Repare que o mesmo objeto Mockito é **dummy** num teste e **mock** noutro. O que muda não é o objeto: é o papel que ele cumpre naquele teste. Dummy é o mock que ninguém interroga.

---

## 4. Por que o teste do `never()` é o mais interessante

Quatro dos nossos testes verificam um **valor**: chamam um método e comparam o retorno com o esperado. O teste do empréstimo recusado é diferente. Ele precisa provar que **nada aconteceu** — que o `salvar(...)` nunca foi chamado.

E aí está o ponto: **não existe valor de retorno que prove uma ausência.** O método lançou uma exceção; não devolveu nada que se possa inspecionar para concluir "o repositório não foi tocado". A única forma de afirmar isso é **perguntar ao próprio dublê** o que aconteceu com ele:

```java
verify(repositorio, never()).salvar(any());
```

Essa linha é uma asserção sobre **comportamento**, não sobre estado. Sem o dublê, essa verificação seria impossível: com um banco real, você teria que consultar a base depois e torcer para que "não achar nada" signifique "nada foi gravado" — o que é bem mais frágil. O mock transforma "nada foi gravado" numa pergunta direta e determinística.

É por isso que dublês não servem só para isolar dependências lentas. Eles habilitam uma categoria inteira de verificação — a de interação — que de outro modo não teria como ser feita.

---

## 5. O resultado da execução

Saída real de `mvn test`:

```
[INFO] Running br.ulbra.bibliotech.servico.EmprestimoServiceTest
[ERROR] Tests run: 11, Failures: 2, Errors: 0, Skipped: 0 <<< FAILURE! -- in br.ulbra.bibliotech.servico.EmprestimoServiceTest
[ERROR] EmprestimoServiceTest.limiteDeEmprestimosRetornaEsperado(int, boolean)[3] <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: <true> but was: <false>
[ERROR] EmprestimoServiceTest.bloqueioPorMultaRetornaEsperado(double, boolean)[3] <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: <false> but was: <true>

[INFO] Running br.ulbra.bibliotech.regras.CalculadoraMultaTest
[ERROR] Tests run: 15, Failures: 3, Errors: 0, Skipped: 0 <<< FAILURE! -- in br.ulbra.bibliotech.regras.CalculadoraMultaTest

[INFO] Results:
[ERROR] Failures:
[ERROR]   CalculadoraMultaTest.atrasoMuitoLongoRespeitaOTeto:86 expected: <50.0> but was: <365.0>
[ERROR]   CalculadoraMultaTest.fronteirasDaMultaGeramValorEsperado:111 expected: <3.0> but was: <0.0>
[ERROR]   CalculadoraMultaTest.fronteirasDaMultaGeramValorEsperado:111 expected: <50.0> but was: <51.0>
[ERROR]   EmprestimoServiceTest.bloqueioPorMultaRetornaEsperado:88 expected: <false> but was: <true>
[ERROR]   EmprestimoServiceTest.limiteDeEmprestimosRetornaEsperado:66 expected: <true> but was: <false>
[ERROR] Tests run: 26, Failures: 5, Errors: 0, Skipped: 0
[INFO] BUILD FAILURE
```

**26 testes, 5 falhas.** Três são da `CalculadoraMultaTest` (o laboratório anterior). As **duas** novas são do `EmprestimoService`, e são o resultado esperado — os defeitos plantados sendo denunciados. Os outros 9 testes do serviço, inclusive os de `verify` e `never()`, passam.

---

## 6. Interpretando as falhas

### Falha 1 — sócio com 3 empréstimos ativos

- **Valor testado:** `atingiuLimiteDeEmprestimos` para um sócio com **3** empréstimos ativos.
- **O que a especificação determina (REQ-001):** o máximo é 3. Ao chegar em 3, o sócio **não pode** retirar mais. Logo, `atingiuLimite` deve ser `true`.
- **O que o sistema devolveu:** `false` (`expected: <true> but was: <false>`).
- **Conclusão:** o defeito está no **sistema**. Ele libera o quarto empréstimo, deixando o sócio passar de 3.

### Falha 2 — sócio com multa de exatamente R$ 20,00

- **Valor testado:** `estaBloqueadoPorMulta` para uma multa acumulada de **R$ 20,00**.
- **O que a especificação determina (REQ-004):** fica bloqueado quem deve **acima de** R$ 20,00. Exatamente R$ 20,00 **não** bloqueia. Logo, deve ser `false`.
- **O que o sistema devolveu:** `true` (`expected: <false> but was: <true>`).
- **Conclusão:** o defeito está no **sistema**. Ele bloqueia quem deve exatamente R$ 20,00, quando deveria liberar.

### O detalhe: os vizinhos passam

- Com **2** empréstimos (`false`) e com **4** empréstimos (`true`), o resultado está certo. Só em **3** exatos o defeito aparece.
- Com **R$ 19,99** (`false`) e com **R$ 20,01** (`true`), o resultado está certo. Só em **R$ 20,00** exatos o defeito aparece.

Se você tivesse testado só valores "no meio" de cada faixa, veria tudo verde. É a fronteira exata que denuncia — exatamente o que a análise de valor-limite manda testar.

---

## 7. Sobre o caso dos R$ 20,00

Esse defeito merece atenção porque nasce de uma **ambiguidade de leitura de requisito**.

A regra diz: bloqueado quem tem multa **"acima de" R$ 20,00**. "Acima de" exclui o próprio valor — quem deve exatamente R$ 20,00 está no limite, mas ainda liberado. Em código, "acima de" é o operador `>`:

```java
return socio.getMultaAcumulada() > PoliticaEmprestimo.LIMITE_MULTA_PARA_BLOQUEIO;
```

O sistema, porém, usa `>=`:

```java
return socio.getMultaAcumulada() >= PoliticaEmprestimo.LIMITE_MULTA_PARA_BLOQUEIO;
```

Com `>=`, o próprio R$ 20,00 passa a bloquear. É a diferença entre "maior que" e "maior ou igual a" — um único caractere — e ela muda o comportamento em exatamente um ponto de fronteira. Requisitos escritos em linguagem natural ("acima de", "até", "no máximo", "a partir de") são cheios dessas bordas. Cada uma vira uma decisão de `>` contra `>=`, e cada troca dessas é um defeito de fronteira esperando um teste de valor-limite para ser pego.

---

## 8. Como rodar

Suíte do serviço apenas:

```bash
mvn test -Dtest=EmprestimoServiceTest -Dmaven.test.failure.ignore=true
```

Suíte completa:

```bash
mvn test
```

Lembre: ver as **2 falhas** do `EmprestimoService` (mais as 3 da `CalculadoraMulta`) **é** o resultado esperado. Não corrija o código de produção nem ajuste os valores esperados — as falhas são a entrega.
