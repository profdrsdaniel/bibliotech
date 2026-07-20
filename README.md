# BiblioTech

Sistema de gestão de biblioteca usado como **sistema sob teste** na disciplina de Controle de Qualidade e Teste de Software.

---

## ⚠️ Leia isto antes de qualquer coisa

**Este sistema contém defeitos plantados de propósito.**

Ele *parece* funcionar. Ele compila, roda e produz respostas plausíveis na maioria dos casos. Mas em algumas situações específicas ele desobedece as próprias regras de negócio documentadas mais abaixo.

**Seu trabalho é encontrar esses defeitos escrevendo testes — não é corrigi-los.**

Não altere o código de `src/main/java`. Se você "consertar" o sistema, perde o objeto de estudo e não tem mais como demonstrar que achou alguma coisa. O que você entrega são os **testes** que falham, provando que o comportamento diverge da especificação.

Os defeitos não estão escondidos atrás de nada exótico. Estão em condições comuns, do tipo que qualquer pessoa escreve num dia normal de trabalho. É exatamente por isso que eles são difíceis de ver lendo o código — e por isso que testes sistemáticos são necessários.

---

## Ferramentas necessárias

| Ferramenta | Versão | Download |
|---|---|---|
| JDK (Java Development Kit) | 25 | https://adoptium.net/temurin/releases/?version=25 |
| IntelliJ IDEA **Community Edition** | qualquer recente | https://www.jetbrains.com/idea/download/ |
| Git | qualquer recente | https://git-scm.com/downloads |

### Sobre o JDK

No site do Adoptium, escolha o seu sistema operacional e arquitetura, e baixe o instalador (`.msi` no Windows, `.pkg` no macOS). Pegue a opção **JDK**, não a **JRE**.

### Sobre o IntelliJ

Na página de download da JetBrains, o botão grande e destacado no topo é o da **Ultimate**, que é paga e tem trial de 30 dias. **Não é essa que você quer.** Role a página para baixo até encontrar a seção **IntelliJ IDEA Community Edition** — é gratuita, de código aberto, e faz tudo o que a disciplina precisa. O download dela costuma ficar bem abaixo do da versão paga.

### Maven

Você **não** precisa instalar o Maven separadamente. O IntelliJ já vem com uma cópia embutida e vai usá-la automaticamente ao abrir o projeto. A instalação separada só é necessária se você quiser rodar `mvn` direto no terminal.

---

## Confirmando que o Java está instalado

Abra um terminal (Prompt de Comando ou PowerShell no Windows, Terminal no macOS/Linux) e rode **os dois** comandos:

```bash
java -version
javac -version
```

Os dois precisam responder alguma coisa com número 25. Algo assim:

```
openjdk version "25" 2025-09-16
javac 25
```

> **Atenção a este detalhe:** se o `java -version` funciona mas o `javac -version` dá erro de comando não encontrado, você instalou o **JRE** (Java Runtime Environment, que só *executa* Java) em vez do **JDK** (Java Development Kit, que também *compila*). Volte ao Adoptium e baixe o JDK. Sem o `javac` você não compila nada.
>
> No Windows, outra causa possível para o mesmo sintoma é o JDK ter sido instalado sem marcar a opção de adicionar ao `PATH`. Reinstale marcando essa opção.

---

## Passo a passo

### 1. Clonar o repositório

```bash
git clone <URL-DO-REPOSITORIO>
cd bibliotech
```

### 2. Abrir no IntelliJ

1. Abra o IntelliJ IDEA.
2. **File → Open** (ou "Open" na tela inicial).
3. Navegue até a pasta `bibliotech` e selecione **a pasta**, não um arquivo dentro dela.
4. Se aparecer uma caixa perguntando, escolha abrir como **projeto Maven** e confie no projeto ("Trust Project").
5. Espere. Sério, espere. Veja o aviso da primeira compilação logo abaixo.

### 3. ⏳ A primeira vez demora — e precisa de internet

Na primeira abertura, o Maven vai **baixar da internet** todas as bibliotecas de que o projeto depende (JUnit, Mockito, os plugins de build). Isso pode levar de **2 a 10 minutos**, dependendo da sua conexão, e a barra de progresso no rodapé do IntelliJ pode parecer travada em alguns momentos. É normal.

**Você precisa estar conectado à internet nessa primeira vez.** Não tente fazer isso pela primeira vez em uma rede bloqueada, num café sem sinal, ou cinco minutos antes da aula começar.

Depois que esse download terminar, tudo fica guardado numa pasta local no seu computador (`~/.m2` no macOS/Linux, `C:\Users\SeuUsuario\.m2` no Windows). **Da segunda vez em diante o projeto compila e roda offline**, sem problema nenhum.

### 4. Rodar a demonstração

Abra `src/main/java/br/ulbra/bibliotech/Main.java` e clique no ▶️ verde ao lado da declaração `public class Main` (ou do método `main`). Escolha "Run 'Main.main()'".

O console vai imprimir alguns cenários de uso do sistema — empréstimo, devolução, tentativa de retirada recusada — mostrando o que cada operação produz. É uma forma rápida de ver o sistema em funcionamento antes de começar a testá-lo.

### 5. Rodar os testes

Pelo terminal, na raiz do projeto:

```bash
mvn test
```

Ou, pelo IntelliJ: clique com o botão direito na pasta `src/test/java` → **Run 'All Tests'**.

Neste momento a pasta de testes está **vazia de propósito**. Ela é sua. Vai se encher ao longo do semestre.

---

## Estrutura do projeto

```
bibliotech/
├── pom.xml                         <- configuração do Maven: dependências e plugins
├── README.md                       <- este arquivo
├── .gitignore
└── src/
    ├── main/java/br/ulbra/bibliotech/
    │   ├── Main.java               <- demonstração executável; COMECE OLHANDO AQUI
    │   │
    │   ├── regras/
    │   │   ├── PoliticaEmprestimo.java   <- todas as constantes das regras de negócio
    │   │   └── CalculadoraMulta.java     <- cálculo de multa por atraso (REQ-003)
    │   │
    │   ├── servico/
    │   │   └── EmprestimoService.java    <- as decisões: pode emprestar? quanto cobrar?
    │   │                                    (REQ-001, REQ-004, REQ-005, REQ-006)
    │   │
    │   ├── dominio/
    │   │   ├── Socio.java                <- dados e estado do sócio
    │   │   ├── Livro.java                <- exemplar do acervo
    │   │   └── Emprestimo.java           <- a retirada em si (REQ-002)
    │   │
    │   └── repositorio/
    │       ├── EmprestimoRepository.java          <- interface; permite dublês com Mockito
    │       └── EmprestimoRepositoryEmMemoria.java <- guarda tudo num LinkedHashMap
    │
    └── test/java/                  <- VAZIA. É aqui que você trabalha.
```

**Sugestão de ordem de leitura:** rode o `Main` primeiro e veja a saída. Depois leia `PoliticaEmprestimo` para conhecer os números da política. Em seguida `CalculadoraMulta` e `EmprestimoService`, que são onde as decisões acontecem. As classes de `dominio` e `repositorio` são estrutura de apoio.

---

## Regras de negócio

Esta é a **especificação oficial** do sistema. É contra ela que você testa. Sempre que o código divergir do que está escrito aqui, quem está certo é esta tabela.

| ID | Regra |
|---|---|
| **REQ-001** | Um sócio pode ter no máximo **3 empréstimos** ativos simultâneos |
| **REQ-002** | O prazo de devolução é de **14 dias** corridos a partir da data do empréstimo |
| **REQ-003** | Multa de **R$ 1,00 por dia** de atraso, com **carência de 2 dias** e **teto de R$ 50,00** |
| **REQ-004** | Sócio com multa acumulada **acima de R$ 20,00** fica bloqueado (R$ 20,00 exatos **não** bloqueia) |
| **REQ-005** | Um exemplar reservado por um sócio não pode ser emprestado a outro |
| **REQ-006** | Um exemplar indisponível não pode ser emprestado |

### REQ-003 detalhada: como a multa funciona

Três coisas acontecem em sequência:

1. **Devolução em dia ou adiantada** → multa de R$ 0,00.
2. **Carência:** atraso de **1 ou 2 dias** → multa de R$ 0,00.
3. **Acima da carência:** a partir de **3 dias** de atraso, cobra-se R$ 1,00 por **cada** dia de atraso — **incluindo os dois dias da carência**. A carência não é descontada; ela apenas define a partir de quando se cobra.
4. **Teto:** o valor final **nunca** passa de R$ 50,00.

Ou seja: com 3 dias de atraso cobra-se por 3 dias (R$ 3,00), e não por 1 dia.

| Dias de atraso | Multa correta | Por quê |
|---|---|---|
| devolveu adiantado | R$ 0,00 | sem atraso |
| 0 | R$ 0,00 | devolveu na data prevista |
| 1 | R$ 0,00 | dentro da carência |
| 2 | R$ 0,00 | dentro da carência (último dia dela) |
| 3 | R$ 3,00 | passou da carência: cobra os 3 dias |
| 4 | R$ 4,00 | 4 × R$ 1,00 |
| 10 | R$ 10,00 | 10 × R$ 1,00 |
| 49 | R$ 49,00 | 49 × R$ 1,00 |
| 50 | R$ 50,00 | 50 × R$ 1,00, exatamente no teto |
| 51 | R$ 50,00 | passou do teto, trava em R$ 50,00 |
| 200 | R$ 50,00 | o teto vale sempre |

### REQ-004 detalhada: quando o sócio fica bloqueado

O bloqueio acontece quando a multa acumulada é **estritamente maior** que R$ 20,00. Quem deve exatamente R$ 20,00 **ainda pode** pegar livros.

| Multa acumulada | Situação correta |
|---|---|
| R$ 0,00 | liberado |
| R$ 19,99 | liberado |
| R$ 20,00 | liberado (o limite é *acima de* R$ 20,00) |
| R$ 20,01 | bloqueado |
| R$ 35,00 | bloqueado |

### REQ-001 detalhada: quantos empréstimos simultâneos

O sócio pode ter **até 3** empréstimos ativos. Ao chegar em 3, ele **não pode** retirar o quarto enquanto não devolver algum.

| Empréstimos ativos | Pode retirar mais um? |
|---|---|
| 0 | sim |
| 1 | sim |
| 2 | sim |
| 3 | não — já está no limite |
| 4 | não |

---

## API REST

Além de rodar como demonstração no console, o BiblioTech expõe uma API REST usada na aula de testes de API com Postman. Ela sobe com `mvn spring-boot:run` e responde em `http://localhost:8080`.

Os endpoints, os exemplos de requisição e resposta, a tabela de códigos de status esperados e o passo a passo com o Postman estão documentados em **[API.md](API.md)**.

---

## Interface web

O BiblioTech também tem uma interface web (páginas de livros, empréstimos e formulários de ação), usada na aula de automação de testes com Selenium. Sobe junto com `mvn spring-boot:run` e fica acessível em `http://localhost:8080`.

A descrição de cada tela, a tabela de identificadores estáveis para automação e o comportamento esperado de cada fluxo estão documentados em **[INTERFACE.md](INTERFACE.md)**.

---

## Problemas comuns

| Problema | Causa provável | Solução |
|---|---|---|
| `Cannot resolve symbol 'org.junit'` | O Maven ainda não baixou as dependências, ou o download falhou | Abra a aba **Maven** (lateral direita do IntelliJ) e clique no ícone de **Reload All Maven Projects** (as duas setas circulares). Confirme que você está com internet. |
| `Cannot resolve symbol 'org.mockito'` | Mesma causa acima | Mesma solução acima |
| `Project JDK is not defined` ou erro de "language level" | O IntelliJ não sabe qual JDK usar | **File → Project Structure → Project**: em *SDK* escolha o JDK 25 (se não aparecer, clique em *Add SDK → Download JDK* e baixe o 25). Em *Language level* escolha 25. |
| `mvn: command not found` (ou `'mvn' não é reconhecido...`) | O Maven não está instalado como programa separado no seu sistema | Você não precisa dele: use o botão ▶️ do IntelliJ e o painel Maven, que usam a cópia embutida. Se quiser mesmo o comando no terminal, instale o Maven e adicione-o ao `PATH`. |
| O build falha com erros de "could not resolve dependencies" ou "connection timed out" | Você está sem internet e é a **primeira** compilação | Conecte-se à internet e rode de novo. Depois desse primeiro download, o projeto funciona offline. |
| Você criou uma classe de teste, mas não aparece o ▶️ para rodá-la | A pasta `src/test/java` não está marcada como *Test Sources Root* | Clique com o botão direito na pasta `src/test/java` → **Mark Directory as → Test Sources Root** (ela deve ficar verde). Se isso não resolver, recarregue o projeto Maven, que já configura isso pelo `pom.xml`. |
| `java -version` funciona mas `javac -version` não | Você instalou o JRE em vez do JDK | Baixe o **JDK** 25 no Adoptium (veja a seção de instalação) |
| Os testes rodam mas o relatório de cobertura não aparece | O relatório do JaCoCo é gerado na fase de teste | Rode `mvn test` e abra `target/site/jacoco/index.html` no navegador |

> **Se você seguir este README direitinho e mesmo assim travar, o problema é do professor, não seu.** Avise — o roteiro está incompleto ou errado e precisa ser corrigido para a turma toda.

---

## Um aviso final sobre como você vai testar

Suponha que você decida testar o cálculo de multa. Você escolhe alguns valores para exercitar: **5 dias**, **10 dias**, **30 dias** de atraso. Escreve os três testes. Roda.

**Os três passam.** Verde. Cobertura alta. Parece resolvido.

E o sistema continua cobrando o valor errado.

Esse é o ponto central da disciplina: **escolher valores "redondos" e bem no meio de cada faixa é o jeito mais confiável de não achar defeito nenhum**. Defeitos moram nas fronteiras — no primeiro valor que entra numa faixa, no último que sai dela, no exato ponto em que uma condição muda de resultado.

Antes de sair escrevendo testes, pare e pergunte: para cada regra desta especificação, **onde exatamente ficam as fronteiras?** Qual é o último valor de um lado e o primeiro valor do outro? Teste os dois. Depois teste o valor exato da fronteira também.

Cobertura de código alta não é o mesmo que teste bom. Você pode executar 100% das linhas deste projeto e não encontrar um único defeito.
