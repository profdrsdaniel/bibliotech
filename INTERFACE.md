# Interface web do BiblioTech

Este documento descreve a interface web do BiblioTech, usada na aula de automação de testes com Selenium.

> ⚠️ **Lembre-se:** o BiblioTech tem defeitos plantados de propósito. Este documento descreve como a interface **deveria** se comportar segundo a especificação. Seu trabalho é automatizar as telas, comparar o comportamento real com o que está descrito aqui e registrar onde eles divergem.

---

## 1. Como subir a aplicação

Na raiz do projeto:

```bash
mvn spring-boot:run
```

Na primeira vez o Maven baixa as dependências (precisa de internet). Quando aparecer no console `Started BibliotechApplication`, a aplicação está no ar em `http://localhost:8080`.

Para parar, tecle `Ctrl + C` no terminal.

### Endereço de cada página

| Página | URL |
|---|---|
| Lista de livros (inicial) | `http://localhost:8080/` |
| Formulário de empréstimo | `http://localhost:8080/emprestar?livroId={id}` |
| Lista de empréstimos | `http://localhost:8080/emprestimos` |
| Formulário de devolução | `http://localhost:8080/devolver?emprestimoId={id}` |

Todas as páginas têm, no topo, um menu com links para **Livros** e **Emprestimos**.

---

## 2. As telas

### 2.1 Livros (`/`)

Mostra uma tabela com todos os livros do acervo: id, título, autor, disponibilidade e uma coluna de ação.

- Livro **disponível**: exibe um botão **Emprestar**, que leva ao formulário de empréstimo daquele livro.
- Livro **indisponível**: exibe o texto "Emprestado", sem botão.

### 2.2 Formulário de empréstimo (`/emprestar?livroId={id}`)

- Mostra em destaque o livro selecionado (somente leitura).
- Tem um campo de seleção com os sócios disponíveis (nome e id).
- O botão **Confirmar empréstimo** registra o empréstimo.
- Há um link para voltar à lista de livros.

Ao confirmar um empréstimo válido, a aplicação volta para a lista de livros e exibe uma mensagem de sucesso.

### 2.3 Empréstimos (`/emprestimos`)

Mostra uma tabela com todos os empréstimos: id, sócio, livro, data do empréstimo, data prevista, data de devolução e multa.

- Empréstimo **ativo**: exibe um botão **Devolver**, que leva ao formulário de devolução.
- Empréstimo **encerrado**: exibe a data de devolução e a multa; não há botão.

Esta página consulta os dados e só mostra a tabela **depois de um pequeno intervalo de carregamento**. Enquanto carrega, aparece um indicador de "Carregando..."; quando termina, o indicador some e a tabela aparece. Um teste automatizado precisa **esperar** a tabela ficar disponível antes de ler seu conteúdo.

### 2.4 Formulário de devolução (`/devolver?emprestimoId={id}`)

- Mostra em destaque o empréstimo que está sendo encerrado.
- Tem um campo de data para informar a data da devolução.
- O botão **Confirmar devolução** registra a devolução e calcula a multa.

---

## 3. Identificadores estáveis para automação

Os elementos abaixo têm `id` fixo e previsível. Use-os para escrever seus localizadores.

| Elemento | id |
|---|---|
| Tabela de livros | `tabela-livros` |
| Tabela de empréstimos | `tabela-emprestimos` |
| Seleção de sócio | `select-socio` |
| Botão de confirmar empréstimo | `btn-confirmar-emprestimo` |
| Campo de data de devolução | `input-data-devolucao` |
| Botão de confirmar devolução | `btn-confirmar-devolucao` |
| Área de mensagem de sucesso | `mensagem-sucesso` |
| Área de mensagem de erro | `mensagem-erro` |
| Indicador de carregamento | `carregando` |

Além disso, **cada linha das tabelas** tem um atributo `data-id` com o id do registro daquela linha. Use-o para localizar uma linha específica — por exemplo, a linha do livro 10 é a que tem `data-id="10"`.

---

## 4. Elementos sem identificador

Nem todo elemento que você precisa acionar tem um `id`. Alguns botões de ação dentro das tabelas **não têm identificador próprio** e precisarão de outras estratégias de localização: por texto do elemento, por posição dentro da linha (`data-id`), por seletor CSS relativo, ou por XPath.

Isso é de propósito. A maioria das telas reais não vem com um `id` pronto em cada elemento, e saber localizar sem esse apoio é parte do que a disciplina quer treinar. Descobrir **quais** elementos são esses faz parte do exercício.

---

## 5. Comportamento esperado de cada fluxo

Esta é a especificação contra a qual você compara o comportamento real.

### Empréstimo bem-sucedido

Selecionar um sócio sem impedimentos e um livro disponível e confirmar deve:

1. registrar o empréstimo;
2. levar de volta à lista de livros;
3. exibir uma **mensagem de sucesso** na área `mensagem-sucesso`;
4. fazer o livro passar a aparecer como indisponível ("Emprestado") na lista.

### Empréstimo impedido por regra de negócio

Se uma regra de negócio impede o empréstimo — sócio bloqueado por multa, livro reservado para outro sócio, ou sócio no limite de empréstimos ativos — a operação **não deve** ser realizada, e o usuário deve receber uma **mensagem de erro clara**, na área `mensagem-erro`, explicando o motivo. O usuário nunca deve ficar sem saber o que aconteceu.

### Proteção contra envio duplicado

Ao confirmar um empréstimo, a ação de confirmação deve ser protegida contra envio duplicado, de modo que um clique repetido, ou dois cliques rápidos, **não** gerem mais de um empréstimo para a mesma solicitação.

### Devolução bem-sucedida

Informar uma data válida e confirmar a devolução deve registrar a devolução, calcular a multa e levar à lista de empréstimos, mostrando o empréstimo como encerrado.

### Devolução com data inválida ou em branco

Se a data de devolução estiver em branco ou em formato inválido, o sistema deve tratar o problema de forma amigável, exibindo uma **mensagem de validação no próprio formulário** e permitindo que o usuário corrija. Um dado inválido informado pelo usuário nunca deve derrubar a aplicação.

### Exibição de valores monetários

As multas exibidas nas tabelas devem estar formatadas como valor em reais, no padrão `R$ 0,00` (por exemplo, `R$ 7,00` ou `R$ 3,50`).

---

## 6. Dados iniciais

A aplicação sobe já populada (os mesmos dados descritos em [API.md](API.md)): sócios de id 1 a 5 e livros de id 10 a 17, com um sócio bloqueado por multa, um sócio no limite de empréstimos, um livro reservado e um livro indisponível. Esse conjunto é recriado toda vez que a aplicação sobe; se você bagunçar o estado testando, basta reiniciar.
