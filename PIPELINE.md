# PIPELINE.md — Solução do laboratório de integração contínua

> ## ⚠️ Este documento é a resolução do laboratório
>
> Ele explica o workflow de integração contínua do projeto e por que a pipeline dele **fica vermelha de propósito**. Se você ainda não tentou o laboratório, **pare e tente antes**.

---

## 1. O que é integração contínua e que problema ela resolve

Integração contínua (CI) é a prática de rodar a suíte de testes **automaticamente, a cada alteração enviada** ao repositório, numa máquina limpa e neutra — não na sua.

O ganho não é a automação em si (você já podia rodar `mvn test` na sua máquina). O ganho é **o momento em que o defeito aparece**:

- **Sem CI:** alguém envia uma alteração que quebra um teste. Ninguém roda a suíte completa naquele dia. Dias depois, outra pessoa percebe que algo está errado. Agora existem dezenas de commits desde a quebra, e descobrir **qual** deles causou o problema vira uma caça demorada.
- **Com CI:** a mesma alteração é enviada, a pipeline roda em minutos e falha. O e-mail chega com o nome de quem enviou, o commit exato e o teste que quebrou. O defeito é pego **na hora**, preso à alteração que o causou.

CI encurta a distância entre "o defeito entrou" e "o defeito foi notado" de dias para minutos. É isso que ela compra.

---

## 2. O workflow, linha a linha

O arquivo fica em `.github/workflows/testes.yml`. O GitHub lê qualquer arquivo `.yml` dentro de `.github/workflows/` e o trata como uma pipeline.

```yaml
name: Testes

on:
  push:
    branches:
      - main
      - 'task/**'
      - 'feat/**'
  pull_request:

jobs:
  testes:
    runs-on: ubuntu-latest

    steps:
      - name: Baixar o codigo
        uses: actions/checkout@v4

      - name: Configurar o JDK 25
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '25'
          cache: maven

      - name: Rodar os testes
        run: mvn test

      - name: Publicar os relatorios de teste
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: surefire-reports
          path: target/surefire-reports/
```

Chave por chave:

- **`name`** — o nome da pipeline, como aparece na aba Actions. Aqui, `Testes`.
- **`on`** — o **gatilho**: o que faz a pipeline rodar. Aqui, todo `push` nas branches `main`, em qualquer `task/**` e em qualquer `feat/**`, e todo `pull_request`. O `**` é um curinga que casa com qualquer nome depois da barra.
- **`jobs`** — a lista de **trabalhos**. Uma pipeline tem um ou mais jobs, que rodam em paralelo por padrão. Aqui há um só, chamado `testes`.
- **`runs-on`** — em que **máquina** o job roda. `ubuntu-latest` é uma máquina Linux limpa que o GitHub cria do zero para esta execução e destrói no fim. Nada da sua máquina vai junto — é essa neutralidade que dá valor ao resultado.
- **`steps`** — a **sequência de passos** do job, executados de cima para baixo. Cada passo é uma ação ou um comando.
- **`uses`** — usa uma **ação pronta**, publicada por outra pessoa. `actions/checkout@v4` baixa o seu código para a máquina; `actions/setup-java@v4` instala o Java; `actions/upload-artifact@v4` guarda arquivos. O `@v4` fixa a versão da ação.
- **`with`** — os **parâmetros** que aquela ação recebe (a distribuição do Java, a versão, etc.).
- **`run`** — executa um **comando de terminal** direto na máquina. Aqui, `mvn test`.

> **YAML é sensível a indentação.** O que está aninhado dentro do quê é definido **só pelos espaços** no começo da linha. Errar a indentação muda o significado do arquivo ou o quebra. E há uma regra rígida: **YAML não aceita tabulação para indentar** — só espaços. Se o seu editor insere `Tab`, configure-o para usar espaços, ou a pipeline nem começa.

---

## 3. Por que o cache e o `always()` importam

Dois detalhes pequenos no arquivo carregam muito peso.

### `cache: maven`

Na ação `setup-java`, a linha `cache: maven` guarda as dependências baixadas pelo Maven (JUnit, Mockito, os plugins) entre uma execução e outra.

**Sem ela:** cada execução começa numa máquina limpa, sem nada em `~/.m2`, e baixa **tudo de novo** da internet. São minutos por execução, gastos toda vez, sem motivo. Com o cache, a primeira execução baixa e guarda; as seguintes reaproveitam, e a pipeline fica bem mais rápida.

### `if: always()`

Por padrão, quando um passo **falha**, o GitHub **pula todos os passos seguintes** — a lógica é "se a compilação quebrou, não adianta continuar".

Mas aqui o passo `mvn test` **vai falhar de propósito** (os defeitos plantados fazem a suíte terminar com erro). Sem nenhuma proteção, o passo seguinte — o que publica os relatórios de teste — seria pulado. E é justamente quando os testes falham que você **mais** quer o relatório para investigar.

O `if: always()` força aquele passo a rodar **sempre**, mesmo depois de uma falha anterior. Assim o artefato com os relatórios sobe de qualquer jeito, dando a você o detalhe de cada teste que quebrou.

> Cuidado para não confundir: `if: always()` faz o **relatório** subir apesar da falha. Ele **não** faz a pipeline passar. A execução continua vermelha — e deve continuar.

---

## 4. O resultado real

Saída real do passo `Rodar os testes` na execução do GitHub Actions:

```text
<!-- SAIDA_REAL_ACTIONS -->
```

Repare na última linha: o processo terminou com **`exit code 1`**.

É esse número que decide tudo. Todo comando de terminal, ao terminar, devolve um **código de saída**: `0` significa sucesso, qualquer outro número significa falha. Quando a suíte tem testes quebrados, o `mvn test` devolve `1`. O GitHub Actions **não lê a mensagem de erro** nem interpreta o texto do log — ele olha apenas esse número. Viu `0`, marca verde; viu qualquer outra coisa, marca vermelho. A pipeline inteira gira em torno desse único inteiro que o processo devolve.

---

## 5. Por que a pipeline vermelha está certa

O sistema tem defeitos plantados. A suíte de testes os denuncia, falhando nos valores de fronteira. O `mvn test` termina com código 1. A pipeline vê o 1 e marca a execução como falha.

**Cada elo dessa corrente está funcionando exatamente como deveria.** Num projeto de verdade, esse vermelho seria o sinal inequívoco de "pare tudo e conserte antes de seguir". Aqui, como os defeitos são parte do exercício, o vermelho é o **resultado esperado** — a prova de que a pipeline enxerga o que a suíte enxerga.

Uma pipeline verde neste projeto seria a notícia ruim: significaria que ela **não** está detectando os defeitos que sabemos existir.

---

## 6. O erro de tentar deixar a pipeline verde

Existe uma flag do Maven que faz a pipeline passar mesmo com teste falhando:

```bash
mvn test -Dmaven.test.failure.ignore=true
```

Com ela, o Maven roda todos os testes, registra as falhas no relatório, mas **devolve código de saída 0** mesmo assim. A pipeline veria `0` e ficaria verde — com os testes quebrados por baixo.

**Nunca use isso para "resolver" uma pipeline vermelha.** O raciocínio é o mesmo do teste sem asserção: você não consertou nada, apenas mandou o sistema de alarme mentir. E é pior do que não ter pipeline nenhuma, porque agora existe um selo verde afirmando que está tudo bem.

O dano de verdade aparece **no futuro**. Hoje você sabe que os testes falham por causa dos defeitos plantados. Mas com a flag ligada, no dia em que alguém introduzir um defeito **novo**, a pipeline também vai ficar verde — porque ela foi ensinada a engolir qualquer falha. O alarme que você silenciou para os defeitos conhecidos fica silenciado para os desconhecidos também. A função inteira da pipeline — avisar quando algo quebra — é destruída.

Por isso o passo de teste deste workflow é `mvn test`, puro, sem nenhuma flag que engula falha.

---

## 7. Uma limitação desta pipeline

Este workflow roda **apenas testes unitários** — os da branch de onde ele parte, que rodam sozinhos, sem depender de nada externo.

Os testes de interface, como os de Selenium, **não** rodariam aqui como está. Eles precisam de duas coisas que esta máquina não tem prontas: um **navegador** e a **aplicação no ar**. Para rodá-los na pipeline seria preciso acrescentar passos: subir a aplicação em segundo plano, esperar até ela responder na porta, e rodar o navegador em **modo sem interface gráfica** (headless), já que a máquina do GitHub não tem tela.

Isso é perfeitamente possível — muitos projetos fazem —, mas exige montar essa infraestrutura toda antes do teste. E não é uma limitação do GitHub Actions: é uma característica dos próprios testes de interface. **Quanto mais alto na pirâmide de testes, mais infraestrutura o teste exige para rodar.** Um teste unitário precisa só da JVM; um teste de interface precisa de servidor, navegador e da orquestração entre eles. É por isso que a base da pirâmide (muitos testes unitários, rápidos e baratos) roda em toda pipeline, e o topo (poucos testes de interface, lentos e caros) costuma rodar em pipelines separadas ou menos frequentes.

---

## 8. Como acompanhar a execução

1. No repositório, no GitHub, clique na aba **Actions** (no topo, entre "Pull requests" e "Projects").
2. A lista mostra todas as execuções, a mais recente no topo. Um ✓ verde indica sucesso; um ✗ vermelho, falha. Clique numa execução para abri-la.
3. Dentro da execução, clique no job **testes** para ver os passos. Cada passo pode ser expandido para mostrar o log completo daquele passo — é onde você lê a saída do `mvn test`.
4. Para baixar os relatórios: na página da execução, role até o fim, até a seção **Artifacts**, e clique em **surefire-reports** para baixar o `.zip` com os relatórios detalhados de cada teste. Graças ao `if: always()`, esse artefato está lá **mesmo com a execução vermelha**.

---

## 9. Como rodar localmente

Antes de enviar, você pode rodar a mesma coisa que a pipeline roda:

```bash
mvn test
```

O resultado (as mesmas falhas) aparece no seu terminal. A diferença é que a pipeline roda isso numa máquina limpa, a cada push, para todo mundo — sem depender de você lembrar de rodar.
