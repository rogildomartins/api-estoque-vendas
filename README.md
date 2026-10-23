# API de Controle de Estoque e Vendas

![CI](https://github.com/manoelrogildo/api-estoque-vendas/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-brightgreen)

API REST para cadastro de produtos, movimentação de estoque com histórico (kardex) e registro de vendas com baixa automática de estoque.

O projeto nasceu da minha experiência de mais de 10 anos dando suporte a ERPs de varejo. Ele modela regras que vejo no dia a dia: o saldo nunca fica negativo, toda alteração de estoque precisa ser rastreável e cancelar uma venda devolve os itens ao estoque.

## Funcionalidades

- **Produtos:** cadastro com SKU único, NCM (exigido na NF-e/NFC-e), preço e estoque mínimo; inativação lógica, que preserva o histórico.
- **Estoque:** entradas, saídas e ajustes de inventário. Cada operação grava saldo anterior e posterior (kardex).
- **Vendas:** baixa automática de estoque de todos os itens em uma única transação. Se um item não tiver saldo, nada é gravado.
- **Cancelamento:** devolve os itens ao estoque e registra o motivo.
- **Relatório de estoque baixo:** produtos com saldo menor ou igual ao estoque mínimo.

## Decisões técnicas

| Decisão | Motivo |
|---|---|
| Regras de negócio nas entidades (`Produto.darSaida`, `Venda.cancelar`) | Evita estoque negativo independentemente de quem chama o código |
| `@Transactional` na venda | Garante tudo ou nada: sem baixa parcial de estoque |
| `@Version` (lock otimista) no produto | Impede que duas vendas simultâneas corrompam o saldo (retorna 409) |
| Preço congelado no item da venda | Alterar o preço do produto não muda vendas antigas |
| Flyway + `ddl-auto=validate` | Schema versionado; o Hibernate só confere se as entidades batem com o banco |
| `@EntityGraph` no detalhe da venda | Carrega itens e produtos em uma consulta, evitando N+1 |
| `open-in-view=false` | Nada de consultas escondidas na camada web |
| Erros no padrão RFC 9457 (Problem Details) | Respostas de erro consistentes, com detalhes por campo na validação |
| DTOs com `record` | Entidades JPA nunca são expostas diretamente na API |

## Tecnologias

Java 21 · Spring Boot 4 · Spring Web MVC · Spring Data JPA / Hibernate · Bean Validation · Flyway · MySQL · H2 · springdoc-openapi (Swagger) · JUnit 5 · Mockito · MockMvc · Docker · GitHub Actions

## Como rodar

### Opção 1: sem instalar nada além do Java 21 (banco H2 em memória com dados de exemplo)

```bash
./mvnw spring-boot:run        # Linux/Mac
mvnw.cmd spring-boot:run      # Windows
```

- Swagger: http://localhost:8080/swagger-ui.html
- Console H2: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:estoque`, usuário `sa`, sem senha)

### Opção 2: com MySQL via Docker

```bash
docker compose up --build
```

### Testes

```bash
./mvnw test
```

São 17 testes: unitários das regras de domínio, do serviço de vendas (com Mockito) e testes de integração de ponta a ponta (MockMvc + H2 + Flyway).

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/produtos` | Cadastra produto |
| GET | `/api/produtos?nome=&page=&size=` | Lista produtos ativos (paginado) |
| GET | `/api/produtos/{id}` | Detalha produto |
| PUT | `/api/produtos/{id}` | Atualiza dados cadastrais |
| DELETE | `/api/produtos/{id}` | Inativa produto |
| GET | `/api/produtos/estoque-baixo` | Produtos abaixo do estoque mínimo |
| POST | `/api/produtos/{id}/movimentacoes` | Entrada, saída ou ajuste de inventário |
| GET | `/api/produtos/{id}/movimentacoes` | Histórico (kardex) do produto |
| POST | `/api/vendas` | Registra venda e baixa estoque |
| GET | `/api/vendas` | Lista vendas (paginado) |
| GET | `/api/vendas/{id}` | Detalha venda com itens |
| POST | `/api/vendas/{id}/cancelamento` | Cancela venda e devolve estoque |

### Exemplo: registrar uma venda

```http
POST /api/vendas
Content-Type: application/json

{
  "itens": [
    { "produtoId": 1, "quantidade": 2 },
    { "produtoId": 4, "quantidade": 3 }
  ]
}
```

```json
{
  "id": 1,
  "status": "CONCLUIDA",
  "valorTotal": 70.77,
  "itens": [
    { "produtoId": 1, "sku": "ARZ-5KG", "nome": "Arroz Branco Tipo 1 5kg", "quantidade": 2, "precoUnitario": 27.90, "subtotal": 55.80 },
    { "produtoId": 4, "sku": "ACU-1KG", "nome": "Açúcar Cristal 1kg", "quantidade": 3, "precoUnitario": 4.99, "subtotal": 14.97 }
  ]
}
```

### Exemplo: erro de estoque insuficiente (HTTP 422)

```json
{
  "title": "Regra de negócio violada",
  "status": 422,
  "detail": "Estoque insuficiente para o produto \"Óleo de Soja 900ml\". Disponível: 5, solicitado: 10"
}
```

## Estrutura

Organizado por funcionalidade (package by feature):

```
src/main/java/com/manoelrogildo/estoque
├── produto/        # entidade, repositório, serviço, controller e DTOs
├── estoque/        # movimentações e kardex
├── venda/          # vendas e itens
└── compartilhado/  # tratamento de erros e configuração
```

## Próximos passos

- [ ] Autenticação com Spring Security + JWT
- [ ] Testes de integração com Testcontainers (MySQL real)
- [ ] Cálculo de tributos (ICMS/PIS/COFINS) por NCM
- [ ] Geração do XML da NFC-e a partir da venda

## Autor

**Manoel Rogildo Martins**: [LinkedIn](https://www.linkedin.com/in/manoelrogildo)
