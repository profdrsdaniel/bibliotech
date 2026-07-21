# COBERTURA.md — Solução do laboratório de cobertura

> ## ⚠️ Este documento é a resolução do laboratório
>
> Ele explica o que a cobertura de código mede e o que ela **não** mede, usando números reais medidos neste projeto. Se você ainda não tentou o laboratório por conta própria, **pare e tente antes** — o valor está em confrontar a sua conclusão com a nossa.

---

## 1. A pergunta do laboratório

Cobertura de código é uma métrica que diz quanto do código foi executado pelos testes. A pergunta é: **cobertura alta significa testes bons?**

Para responder, medimos a cobertura da `CalculadoraMulta` em dois cenários e comparamos.

---

## 2. Os números medidos

Medições reais, lidas do relatório do JaCoCo (`target/site/jacoco/index.html`) sobre a classe `br.ulbra.bibliotech.regras.CalculadoraMulta`.

| Cenário | Cobertura de instrução | Cobertura de ramo |
|---|---|---|
| **Suíte real** (`CalculadoraMultaTest`, 15 testes com asserções) | **100%** (33/33) | **100%** (6/6) |
| **Classe fabricada sozinha** (`CoberturaFabricadaTest`, 1 teste, **zero asserções**) | **100%** (33/33) | **100%** (6/6) |

Leia essa tabela duas vezes. Os dois cenários dão **exatamente o mesmo número**: 100% de instrução e 100% de ramo.

Um deles é uma suíte de 15 testes que **encontra 3 defeitos** do sistema. O outro é uma única classe que **não verifica nada** e não encontra defeito nenhum. Para a métrica de cobertura, os dois são indistinguíveis.

---

## 3. O que as cores do relatório significam

No relatório do JaCoCo, cada linha de código aparece pintada:

- **Verde** — a linha foi executada pelos testes.
- **Vermelho** — a linha **não** foi executada por nenhum teste.
- **Amarelo** — a linha tem uma decisão (um `if`, um `&&`, um `? :`) que foi executada **por um lado só**. Um dos ramos rodou, o outro não. É o aviso mais sutil e mais importante do relatório: a linha *parece* coberta, mas metade da lógica dela nunca foi exercitada.

### Onde está o amarelo na CalculadoraMulta?

Aqui vem a parte honesta: **nesta classe, nos dois cenários acima, não há nenhum amarelo.** A `CalculadoraMulta` fica inteira **verde**, com 6 de 6 ramos cobertos. Isso acontece porque tanto a suíte real quanto a classe fabricada exercitam **os dois lados de cada decisão**:

- `if (dataPrevistaDevolucao == null)` — testada com data nula e com data preenchida;
- `if (dataDevolucao == null)` — idem;
- `if (diasAtraso <= CARENCIA_DIAS + 1)` — testada com atraso dentro da carência (ramo verdadeiro) e com atraso cobrável (ramo falso).

Com os dois lados de cada `if` executados, o JaCoCo pinta tudo de verde e o ramo fecha em 100%.

### O que faria essa linha ficar amarela

Para ver o amarelo, imagine uma suíte que só testasse devoluções **dentro** da carência (atrasos de 0, 1 e 2 dias) e nunca um atraso cobrável. A linha

```java
if (diasAtraso <= PoliticaEmprestimo.CARENCIA_DIAS + 1) {
```

seria executada em toda chamada, então ficaria **coberta** na contagem de instrução — mas o ramo "falso" (o caminho que cobra a multa) nunca rodaria. O JaCoCo marcaria essa linha com o **losango amarelo** de "1 de 2 ramos", e a linha `return diasAtraso * MULTA_POR_DIA;` logo abaixo ficaria **vermelha**. É assim que o amarelo denuncia um teste que passou perto do defeito mas não chegou a exercitá-lo.

### E o resto do sistema?

Se você rolar o relatório, vai ver que **todas as outras classes** do sistema (`Emprestimo`, `Livro`, `Socio`, `EmprestimoService`, o repositório em memória) aparecem **vermelhas**, com 0% de cobertura. Elas não foram executadas por nenhum destes testes. Isso é esperado — até aqui só existe suíte para a `CalculadoraMulta`. O vermelho aí está dizendo uma verdade útil: essas partes ainda não têm teste nenhum.

---

## 4. Por que a classe fabricada atinge cobertura alta

A `CoberturaFabricadaTest` tem um único método que chama a `CalculadoraMulta` de todas as formas possíveis: devolução adiantada, atraso dentro da carência, atraso cobrável, data prevista nula e data de devolução nula (as duas últimas dentro de `try/catch` que engole a exceção).

Ao fazer isso, ela **executa** cada linha e cada ramo da classe. E cobertura é exatamente isso: uma medida de **execução**. O JaCoCo instrumenta o código e conta o que passou pela CPU. Ele não tem como saber se, depois de executar a linha, o teste **conferiu** se o resultado estava certo.

Executar não é verificar. A classe fabricada executa tudo e verifica nada.

---

## 5. Quantos defeitos a classe fabricada detecta

**Nenhum.**

O sistema tem defeitos plantados na `CalculadoraMulta` — a carência conta um dia a mais do que deveria, e o teto de R$ 50,00 nunca é aplicado. A suíte real, com suas asserções, **falha em 3 testes** por causa desses defeitos: é assim que ela os denuncia.

A classe fabricada roda os mesmos caminhos, com os mesmos valores de entrada passando pelos mesmos defeitos — e **passa tranquila, verde, 100%**. Por quê? Porque um teste só pode falhar se houver uma asserção que possa ser violada. Sem `assertEquals`, sem `assertThrows`, sem nada que compare o resultado obtido com o esperado, **não existe nada que possa dar errado**. O `7.0` que o sistema devolve errado é calculado, é executado, é contabilizado na cobertura — e é simplesmente descartado, porque ninguém olhou para ele.

Um teste sem asserção é um teste que **nunca falha**. E um teste que nunca falha não protege contra nada.

---

## 6. A conclusão: a assimetria

Junte as duas medições e você chega no ponto do laboratório. A cobertura é uma métrica **assimétrica**:

> **Cobertura baixa prova que os testes são insuficientes.**
> **Cobertura alta não prova que os testes são bons.**

As duas metades são verdadeiras ao mesmo tempo, e é a combinação delas que importa:

- Se a cobertura está **baixa**, você tem uma certeza: existe código que nenhum teste toca. Ali podem morar defeitos inteiros sem ninguém perceber. A métrica apontou um buraco real, e vale a pena tapá-lo. Nesse sentido, cobertura é um ótimo **detector de buraco**.
- Se a cobertura está **alta**, você não tem certeza nenhuma sobre qualidade. Alta cobertura é **necessária, mas não suficiente**. Ela diz que o código foi executado, não que foi verificado. A `CoberturaFabricadaTest` é a prova viva: 100% de cobertura, zero defeitos encontrados.

Por isso a cobertura serve como **detector de buraco, não como certificado de qualidade**. Use-a para descobrir o que ainda não foi testado — e nunca como carimbo de que o que foi testado está correto. O carimbo de qualidade vem das **asserções**: são elas que transformam execução em verificação, e é a asserção, não a cobertura, que pega o defeito.

---

## 7. Como rodar

Suíte real (mede a cobertura da suíte de verdade):

```bash
mvn clean test -Dtest=CalculadoraMultaTest -Dmaven.test.failure.ignore=true
```

Classe fabricada sozinha (mede a cobertura sem nenhuma asserção):

```bash
mvn clean test -Dtest=CoberturaFabricadaTest
```

Suíte completa:

```bash
mvn clean test -Dmaven.test.failure.ignore=true
```

Em todos os casos, o relatório é gerado em:

```
target/site/jacoco/index.html
```

Abra esse arquivo no navegador e navegue até `br.ulbra.bibliotech.regras` → `CalculadoraMulta` para ver o código pintado linha a linha.

> O `-Dmaven.test.failure.ignore=true` faz o Maven gerar o relatório mesmo com testes vermelhos. Lembre: as 3 falhas da `CalculadoraMultaTest` são esperadas — são os defeitos plantados sendo denunciados.
