# API REST do BiblioTech

Este documento descreve a API REST do BiblioTech, usada na aula de testes de API com Postman.

> ⚠️ **Lembre-se:** o BiblioTech tem defeitos plantados de propósito. Este documento descreve como a API **deveria** se comportar segundo a especificação. O seu trabalho é testar cada endpoint e verificar se o comportamento real bate com o que está descrito aqui. Onde não bater, você encontrou algo.

---

## 1. Como subir a aplicação

Na raiz do projeto, rode:

```bash
mvn spring-boot:run
```

Na primeira vez o Maven baixa as dependências do Spring Boot (precisa de internet e demora um pouco). Da segunda vez em diante sobe em poucos segundos.

**Como saber que está no ar:** o console mostra uma linha parecida com esta:

```
Tomcat started on port 8080 (http)
Started BibliotechApplication in 0.5 seconds
```

A partir daí a API responde em `http://localhost:8080`. Para um teste rápido, abra o navegador em <http://localhost:8080/api/socios> — deve aparecer a lista de sócios em JSON.

Para **parar** a aplicação, volte ao terminal onde ela está rodando e tecle `Ctrl + C`.

---

## 2. Endpoints

| Método | Rota | O que faz |
|---|---|---|
| GET | `/api/socios` | Lista todos os sócios |
| GET | `/api/socios/{id}` | Busca um sócio pelo id |
| GET | `/api/livros` | Lista todos os livros |
| GET | `/api/livros/{id}` | Busca um livro pelo id |
| GET | `/api/emprestimos` | Lista todos os empréstimos |
| GET | `/api/emprestimos/{id}` | Busca um empréstimo pelo id |
| POST | `/api/emprestimos` | Cria um empréstimo |
| POST | `/api/emprestimos/{id}/devolucao` | Registra a devolução de um empréstimo |

---

## 3. Códigos de status esperados (a especificação)

Esta é a tabela contra a qual você testa. É o resultado esperado de cada situação.

| Situação | Status esperado |
|---|---|
| Consulta bem-sucedida | `200 OK` |
| Empréstimo criado | `201 Created` |
| Devolução registrada | `200 OK` |
| Recurso não encontrado | `404 Not Found` |
| Corpo da requisição inválido ou campo obrigatório faltando | `400 Bad Request` |
| Regra de negócio impede a operação | `409 Conflict` |

### Formato de erro

Toda resposta de erro deve ter este formato, com a mensagem já tratada para leitura humana:

```json
{
  "erro": "Socio bloqueado por multa acumulada.",
  "status": 409
}
```

Uma resposta de erro **nunca** deve conter stack trace, nome de classe Java (como `java.lang....`) nem caminho de arquivo. Se você vir algo assim, anote — é informação interna vazando para fora.

---

## 4. Exemplos de requisição e resposta

Os exemplos usam os dados iniciais descritos na seção 5.

### GET /api/socios

Lista todos os sócios.

**Resposta — 200 OK**

```json
[
  { "id": 1, "nome": "Ana Prado", "email": "ana.prado@ulbra.br", "multaAcumulada": 0.0, "emprestimosAtivos": 0 },
  { "id": 2, "nome": "Bruno Lima", "email": "bruno.lima@ulbra.br", "multaAcumulada": 35.0, "emprestimosAtivos": 0 }
]
```

### GET /api/socios/{id}

Busca um sócio pelo id.

**Requisição:** `GET /api/socios/1`

**Resposta — 200 OK**

```json
{ "id": 1, "nome": "Ana Prado", "email": "ana.prado@ulbra.br", "multaAcumulada": 0.0, "emprestimosAtivos": 0 }
```

Se o id **não existir**, o esperado é `404 Not Found` com o corpo de erro padrão.

### GET /api/livros

Lista todos os livros.

**Resposta — 200 OK**

```json
[
  { "id": 10, "titulo": "Dom Casmurro", "autor": "Machado de Assis", "isbn": "978-85-359-0277-5", "disponivel": true, "reservadoPorSocioId": null },
  { "id": 13, "titulo": "Macunaima", "autor": "Mario de Andrade", "isbn": "978-85-260-1234-7", "disponivel": true, "reservadoPorSocioId": 4 }
]
```

### GET /api/livros/{id}

Busca um livro pelo id.

**Requisição:** `GET /api/livros/14`

**Resposta — 200 OK**

```json
{ "id": 14, "titulo": "A Hora da Estrela", "autor": "Clarice Lispector", "isbn": "978-85-209-1234-0", "disponivel": false, "reservadoPorSocioId": null }
```

Se o id **não existir**, o esperado é `404 Not Found`.

### GET /api/emprestimos

Lista todos os empréstimos já registrados. Logo que a aplicação sobe, a lista está vazia (`[]`).

### GET /api/emprestimos/{id}

Busca um empréstimo pelo id.

**Requisição:** `GET /api/emprestimos/1`

**Resposta — 200 OK**

```json
{
  "id": 1,
  "socioId": 1,
  "socioNome": "Ana Prado",
  "livroId": 10,
  "livroTitulo": "Dom Casmurro",
  "dataEmprestimo": "2026-07-20",
  "dataPrevistaDevolucao": "2026-08-03",
  "dataDevolucao": null,
  "multaCobrada": 0.0
}
```

Se o id **não existir**, o esperado é `404 Not Found`.

### POST /api/emprestimos

Cria um empréstimo para um sócio e um livro.

**Corpo da requisição**

```json
{
  "socioId": 1,
  "livroId": 10
}
```

**Resposta esperada — 201 Created**

```json
{
  "id": 1,
  "socioId": 1,
  "socioNome": "Ana Prado",
  "livroId": 10,
  "livroTitulo": "Dom Casmurro",
  "dataEmprestimo": "2026-07-20",
  "dataPrevistaDevolucao": "2026-08-03",
  "dataDevolucao": null,
  "multaCobrada": 0.0
}
```

Situações que impedem o empréstimo (livro indisponível, livro reservado para outro sócio, sócio bloqueado por multa, sócio no limite de empréstimos) devem devolver `409 Conflict` com a mensagem do motivo. Uma requisição sem `socioId` ou sem `livroId` deve devolver `400 Bad Request`.

### POST /api/emprestimos/{id}/devolucao

Registra a devolução de um empréstimo e calcula a multa.

**Requisição:** `POST /api/emprestimos/1/devolucao`

**Corpo da requisição**

```json
{
  "dataDevolucao": "2026-08-10"
}
```

**Resposta esperada — 200 OK**

```json
{
  "id": 1,
  "socioId": 1,
  "socioNome": "Ana Prado",
  "livroId": 10,
  "livroTitulo": "Dom Casmurro",
  "dataEmprestimo": "2026-07-20",
  "dataPrevistaDevolucao": "2026-08-03",
  "dataDevolucao": "2026-08-10",
  "multaCobrada": 7.0
}
```

Se o empréstimo **não existir**, o esperado é `404 Not Found`.

---

## 5. Dados iniciais

Ao subir, a aplicação já vem populada. Os ids são fixos e redondos para facilitar os testes.

### Sócios

| id | Nome | Multa acumulada | Empréstimos ativos | Observação |
|---|---|---|---|---|
| 1 | Ana Prado | R$ 0,00 | 0 | livre para pegar livros |
| 2 | Bruno Lima | R$ 35,00 | 0 | multa alta — deve estar bloqueado |
| 3 | Carla Nunes | R$ 0,00 | 3 | está no limite de empréstimos |
| 4 | Diego Alves | R$ 5,00 | 1 | tem uma reserva a seu favor (livro 13) |
| 5 | Elisa Rocha | R$ 0,00 | 0 | livre para pegar livros |

### Livros

| id | Título | Autor | Disponível | Reservado para |
|---|---|---|---|---|
| 10 | Dom Casmurro | Machado de Assis | sim | — |
| 11 | Grande Sertao: Veredas | Guimaraes Rosa | sim | — |
| 12 | Vidas Secas | Graciliano Ramos | sim | — |
| 13 | Macunaima | Mario de Andrade | sim | sócio 4 |
| 14 | A Hora da Estrela | Clarice Lispector | **não** | — |
| 15 | O Cortico | Aluisio Azevedo | sim | — |
| 16 | Iracema | Jose de Alencar | sim | — |
| 17 | Memorias Postumas de Bras Cubas | Machado de Assis | sim | — |

Esses dados são recriados toda vez que a aplicação sobe. Se você bagunçar o estado testando, basta parar (`Ctrl + C`) e subir de novo.

---

## 6. Como testar com Postman

O [Postman](https://www.postman.com/downloads/) é uma ferramenta gráfica para enviar requisições HTTP e ver as respostas. Passo a passo:

### Criando uma requisição GET

1. Abra o Postman e clique em **New → HTTP Request** (ou no botão **+** para abrir uma aba nova).
2. No campo de URL, digite `http://localhost:8080/api/socios`.
3. O método à esquerda já vem como **GET** — deixe assim.
4. Clique em **Send**.
5. A resposta aparece no painel de baixo. O corpo (JSON) fica na aba **Body**.

### Onde ver o código de status

Logo acima do corpo da resposta, à direita, o Postman mostra **Status** (por exemplo, `200 OK`, `201 Created`, `404 Not Found`). **É esse número que você compara com a tabela da seção 3.** Ao lado dele aparecem também o tempo da requisição e o tamanho da resposta.

### Criando uma requisição POST com corpo JSON

1. Abra uma aba nova e troque o método de **GET** para **POST**.
2. Na URL, digite `http://localhost:8080/api/emprestimos`.
3. Vá na aba **Body**, marque a opção **raw** e, no seletor à direita, escolha **JSON**.
4. No campo de texto, cole o corpo:

   ```json
   {
     "socioId": 1,
     "livroId": 10
   }
   ```

5. Clique em **Send** e confira o **Status** e o corpo da resposta.

### Roteiro sugerido de testes

Para exercitar a especificação da seção 3, vale a pena testar pelo menos:

- Listar sócios e livros (GET) — confira o status e os dados.
- Buscar um sócio que existe e um que não existe — confira os dois status.
- Buscar um empréstimo que não existe — confira o status.
- Criar um empréstimo válido — confira o status e o corpo.
- Tentar criar um empréstimo que uma regra de negócio impede (por exemplo, para um sócio bloqueado ou um livro indisponível) — confira o status e a mensagem de erro.
- Criar um empréstimo com o corpo faltando um campo — confira o status.
- Registrar uma devolução — confira o status e a multa calculada.

Compare **cada** status obtido com a tabela da seção 3. É na comparação sistemática que os problemas aparecem.
