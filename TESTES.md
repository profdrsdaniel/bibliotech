# TESTES.md — Solução da suíte de `CalculadoraMulta`

> ## ⚠️ Este documento é a resolução do laboratório
>
> Ele explica o raciocínio da suíte de testes e **revela os defeitos plantados no sistema**.
>
> Se você ainda não tentou fazer os testes por conta própria, **pare e tente antes**. O valor deste material está em conferir o seu raciocínio contra o nosso — e isso só funciona se você já tiver um raciocínio próprio para comparar. Ler a solução antes de tentar é trocar o aprendizado por uma sensação de aprendizado.

---

## 1. O que esta suíte cobre

A suíte testa a classe `CalculadoraMulta`, responsável pela regra REQ-003 (multa por atraso na devolução). São 15 execuções ao todo: quatro testes de partição de equivalência (um por faixa de comportamento), nove execuções de um teste parametrizado que varre as fronteiras entre as faixas, e dois testes de tratamento de erro para datas nulas. Todos os valores esperados foram derivados da especificação escrita, e não do que o sistema devolve — essa distinção é o coração de tudo o que vem a seguir.

---

## 2. Como os casos foram escolhidos

Esta é a parte que importa. Os valores não saíram de intuição nem de "vou testar alguns números e ver no que dá". Cada caso foi produzido por uma de duas técnicas de projeto de teste caixa-preta.

### 2.1 Partição de equivalência

A ideia: em vez de testar infinitos valores de atraso, dividimos a entrada em **faixas dentro das quais o comportamento esperado é o mesmo**. Se o sistema trata todos os valores de uma faixa igual, basta um representante por faixa para exercitar aquele comportamento.

Lendo a REQ-003, o atraso se divide em quatro faixas:

| Partição | Faixa de atraso | Comportamento esperado | Representante no teste |
|---|---|---|---|
| **P1** | ≤ 0 dias (em dia ou adiantado) | multa R$ 0,00 | 3 dias adiantado |
| **P2** | 1 a 2 dias (carência) | multa R$ 0,00 | 1 dia de atraso |
| **P3** | 3 a 50 dias | R$ 1,00 por dia | 10 dias de atraso |
| **P4** | 51 dias ou mais | trava no teto de R$ 50,00 | 365 dias de atraso |

**Por que P1 e P2 são classes diferentes se as duas dão R$ 0,00?**

Porque o *resultado* ser igual não significa que o *caminho* seja igual. Em P1 não houve atraso nenhum — o número de dias de atraso é zero ou negativo. Em P2 houve atraso, mas ele foi perdoado pela carência. São dois motivos distintos para chegar ao mesmo R$ 0,00, e no código são dois trechos de lógica diferentes. Se um dia alguém quebrar a carência sem mexer no tratamento de devolução adiantada (ou vice-versa), só uma das duas classes vai acusar. Juntar as duas num único teste esconderia metade dos defeitos possíveis. Partição de equivalência é sobre equivalência de **comportamento interno esperado**, não de valor de saída.

### 2.2 Análise de valor-limite

Partição de equivalência escolhe um valor *no meio* de cada faixa. O problema é que defeitos quase nunca moram no meio — moram nas **bordas**, no exato ponto em que o comportamento deveria mudar de uma faixa para outra. Um `<=` escrito onde deveria ser `<`, um `+ 1` a mais, um teto que não é aplicado: nada disso aparece no meio da faixa. Só aparece na fronteira.

A regra da análise de valor-limite: para cada fronteira entre duas faixas, teste **três** valores — o último valor da faixa de baixo, o valor exato da fronteira, e o primeiro valor da faixa de cima.

Temos duas fronteiras críticas:

**Fronteira carência → cobrança (entre 2 e 3 dias):**

| Dias | Faixa | Esperado |
|---|---|---|
| 2 | último da carência | R$ 0,00 |
| 3 | primeiro que cobra | R$ 3,00 |
| 4 | já dentro da cobrança | R$ 4,00 |

**Fronteira cobrança → teto (em torno de 50 dias):**

| Dias | Faixa | Esperado |
|---|---|---|
| 49 | ainda abaixo do teto | R$ 49,00 |
| 50 | exatamente no teto | R$ 50,00 |
| 51 | acima do teto | R$ 50,00 |

Mais os limites do começo da escala (−1, 0, 1) para cercar a transição "adiantado / em dia / carência". Todos esses valores foram reunidos no teste parametrizado.

### 2.3 De onde veio cada teste

| Teste | Técnica |
|---|---|
| `devolucaoAdiantadaNaoGeraMulta` (P1) | Partição de equivalência |
| `atrasoDentroDaCarenciaNaoGeraMulta` (P2) | Partição de equivalência |
| `atrasoAcimaDaCarenciaCobraPorDia` (P3) | Partição de equivalência |
| `atrasoMuitoLongoRespeitaOTeto` (P4) | Partição de equivalência |
| `fronteirasDaMultaGeramValorEsperado` (−1, 0, 1, 2, 3, 4, 49, 50, 51) | Análise de valor-limite |
| `dataPrevistaNulaLancaExcecao` | Teste de robustez (entrada inválida) |
| `dataDevolucaoNulaLancaExcecao` | Teste de robustez (entrada inválida) |

---

## 3. Por que testes parametrizados aqui

As nove fronteiras poderiam virar nove métodos `@Test` quase idênticos, mudando só dois números em cada. Em vez disso usamos um `@ParameterizedTest` com `@CsvSource`, onde cada linha é um par `(dias de atraso, multa esperada)`:

```
"-1, 0.00",
"0,  0.00",
"1,  0.00",
"2,  0.00",
"3,  3.00",
"4,  4.00",
"49, 49.00",
"50, 50.00",
"51, 50.00"
```

Essa tabela **é** a planilha de casos de teste, escrita como código. Cada linha corresponde a uma linha da especificação de valores-limite da seção 2.2. Vantagens sobre nove métodos separados:

- **Menos ruído, mais sinal.** A lógica de execução aparece uma vez só; o que varia — os dados — fica isolado numa tabela legível. Adicionar uma fronteira nova é acrescentar uma linha, não copiar um método inteiro.
- **Relatório item a item.** Com o atributo `name` do `@ParameterizedTest`, cada linha vira uma execução nomeada no relatório ("atraso de 3 dias deve gerar multa de R$ 3.00"). Uma falhar não impede as outras de rodar — vemos exatamente quais valores passam e quais não.
- **A tabela documenta a regra.** Quem abrir o teste lê a especificação numérica inteira num bloco, do jeito que ela aparece no requisito.

---

## 4. O resultado da execução

Saída real de `mvn test`, recortada na parte dos resultados dos testes:

```
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running br.ulbra.bibliotech.regras.CalculadoraMultaTest
[ERROR] Tests run: 15, Failures: 3, Errors: 0, Skipped: 0, Time elapsed: 0.092 s <<< FAILURE! -- in br.ulbra.bibliotech.regras.CalculadoraMultaTest
[ERROR] br.ulbra.bibliotech.regras.CalculadoraMultaTest.fronteirasDaMultaGeramValorEsperado(long, double)[5] -- Time elapsed: 0.004 s <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: <3.0> but was: <0.0>
	at br.ulbra.bibliotech.regras.CalculadoraMultaTest.fronteirasDaMultaGeramValorEsperado(CalculadoraMultaTest.java:111)

[ERROR] br.ulbra.bibliotech.regras.CalculadoraMultaTest.fronteirasDaMultaGeramValorEsperado(long, double)[9] -- Time elapsed: 0 s <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: <50.0> but was: <51.0>
	at br.ulbra.bibliotech.regras.CalculadoraMultaTest.fronteirasDaMultaGeramValorEsperado(CalculadoraMultaTest.java:111)

[ERROR] br.ulbra.bibliotech.regras.CalculadoraMultaTest.atrasoMuitoLongoRespeitaOTeto -- Time elapsed: 0.001 s <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: <50.0> but was: <365.0>
	at br.ulbra.bibliotech.regras.CalculadoraMultaTest.atrasoMuitoLongoRespeitaOTeto(CalculadoraMultaTest.java:86)

[INFO] Results:
[INFO]
[ERROR] Failures:
[ERROR]   CalculadoraMultaTest.atrasoMuitoLongoRespeitaOTeto:86 expected: <50.0> but was: <365.0>
[ERROR]   CalculadoraMultaTest.fronteirasDaMultaGeramValorEsperado:111 expected: <3.0> but was: <0.0>
[ERROR]   CalculadoraMultaTest.fronteirasDaMultaGeramValorEsperado:111 expected: <50.0> but was: <51.0>
[INFO]
[ERROR] Tests run: 15, Failures: 3, Errors: 0, Skipped: 0
[INFO]
[INFO] BUILD FAILURE
```

**15 testes executados, 3 falharam.** O `BUILD FAILURE` no fim é esperado: o Maven considera o build falho quando há teste vermelho, e é exatamente isso que queremos ver aqui.

> **Nota sobre o relatório de cobertura (JaCoCo):** ao rodar, você pode ver um bloco grande de `IllegalClassFormatException ... Unsupported class file major version 69` antes dos resultados. **Isso não é falha de teste** — é o JaCoCo tentando (e não conseguindo) medir cobertura sobre bytecode de Java 25. Os testes rodam normalmente mesmo assim; o que se perde é só o relatório de cobertura. Se esse ruído estiver aparecendo na sua máquina, avise o professor.

---

## 5. Interpretando as falhas

Três testes vermelhos. Vamos ler cada um voltando à especificação — nunca ao que o sistema fez.

### Falha 1 — atraso de 3 dias

- **Valor testado:** devolução com 3 dias de atraso (linha `[5]` do `@CsvSource`).
- **O que a especificação determina:** a carência é de 2 dias. A partir de 3 dias cobra-se R$ 1,00 por cada dia de atraso. Logo, 3 dias → **R$ 3,00**.
- **O que o sistema devolveu:** `R$ 0,00` (`expected: <3.0> but was: <0.0>`).
- **Onde está o defeito:** no **sistema**. A especificação diz preto no branco que 3 dias já geram cobrança. O teste espera R$ 3,00 porque o requisito manda R$ 3,00. O sistema está estendendo a carência para um dia a mais do que deveria.

### Falha 2 — atraso de 51 dias

- **Valor testado:** devolução com 51 dias de atraso (linha `[9]` do `@CsvSource`).
- **O que a especificação determina:** a multa nunca ultrapassa R$ 50,00. 51 × R$ 1,00 daria R$ 51,00, mas o teto corta em **R$ 50,00**.
- **O que o sistema devolveu:** `R$ 51,00` (`expected: <50.0> but was: <51.0>`).
- **Onde está o defeito:** no **sistema**. O requisito define um teto explícito de R$ 50,00. O sistema multiplicou dias por valor e entregou o resultado cru, sem aplicar o teto.

### Falha 3 — atraso de 365 dias (P4)

- **Valor testado:** devolução com 365 dias de atraso.
- **O que a especificação determina:** teto de R$ 50,00, valha o atraso que valer.
- **O que o sistema devolveu:** `R$ 365,00` (`expected: <50.0> but was: <365.0>`).
- **Onde está o defeito:** no **sistema**, e é o mesmo defeito da Falha 2 — o teto simplesmente não é aplicado. Com um atraso enorme, o problema fica escancarado: a multa dispara sem limite.

### O detalhe que dá a lição: os vizinhos passam

Repare no que **não** falhou:

- **Atraso de 2 dias passou** (esperava R$ 0,00, recebeu R$ 0,00). **Atraso de 4 dias passou** (R$ 4,00). O defeito da carência só se revela **exatamente em 3 dias**. Se você tivesse testado só 2 e 4 — dois valores perfeitamente razoáveis —, veria tudo verde e concluiria que a carência está certa.
- **Atraso de 50 dias passou** (R$ 50,00, porque 50 × 1 = 50 coincide com o teto). O defeito do teto só aparece **acima de 50**. Em 50 exatos, o valor sem teto e o valor com teto dão o mesmo número, e o defeito se esconde.

É por isso que a análise de valor-limite não é preciosismo. Um defeito de fronteira é invisível em qualquer valor que não seja a própria fronteira. Quem testa "uns números aí" quase sempre erra a mira por um dia — e um dia é tudo.

---

## 6. A lição principal

> **O valor esperado sai do requisito, nunca do comportamento observado.**

Diante de um teste vermelho, existe uma tentação forte: "o sistema devolveu R$ 51,00, então deixa eu botar 51 no esperado e pronto, ficou verde". **Não faça isso. Nunca.**

Quando você ajusta o valor esperado para bater com o que o sistema faz, você não corrigiu nada — você apenas escreveu, em código, uma afirmação permanente de que o comportamento errado é o comportamento certo. E aí o teste passa a **trabalhar contra você**:

- Um sistema sem teste nenhum está apenas sem verificação — é um vazio honesto.
- Um sistema com um teste que valida o comportamento errado é **pior**: ele carrega um selo verde afirmando que está tudo certo. Se um dia alguém corrigir o defeito de verdade, esse teste vai ficar **vermelho** e acusar a correção de ser o problema. O teste virou guardião do defeito.

Teste verde não é o objetivo. Teste **correto** é o objetivo. Um teste correto pode e deve ficar vermelho quando o sistema está errado — é assim que ele cumpre a função dele. Os três vermelhos desta suíte são três testes fazendo exatamente o trabalho para o qual foram escritos.

---

## 7. Como rodar

Na raiz do projeto:

```bash
mvn test
```

- O resumo (quantos rodaram, quantos falharam) aparece no fim da saída do terminal.
- Os relatórios detalhados de cada teste ficam em `target/surefire-reports/`.
- O relatório de cobertura do JaCoCo, quando gerado, fica em `target/site/jacoco/index.html` (abra no navegador).

Lembre: neste projeto, ver os três vermelhos **é** o resultado esperado. Não corrija o código de produção, não ajuste os valores esperados. As falhas são a entrega.
