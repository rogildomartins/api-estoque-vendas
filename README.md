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
- **Autenticação e perfis:** login com JWT. O **OPERADOR** (caixa) consulta e vende; o **ADMIN** também cadastra produtos, movimenta estoque, cancela vendas e gerencia usuários.

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
| JWT com o resource server do Spring Security (HS256) | Sem biblioteca de terceiros; API stateless, sem sessão no servidor |
| Senhas com BCrypt e mesma mensagem para qualquer falha de login | Não revela quais logins existem nem quais contas estão inativas |
| Segredo do JWT obrigatório no perfil `mysql` | A aplicação não sobe em produção com o segredo de desenvolvimento |

## Tecnologias

Java 21 · Spring Boot 4 · Spring Web MVC · Spring Security (JWT) · Spring Data JPA / Hibernate · Bean Validation · Flyway · MySQL · H2 · springdoc-openapi (Swagger) · JUnit 5 · Mockito · MockMvc · Docker · GitHub Actions

## Como rodar

### Opção 1: sem instalar nada além do Java 21 (banco H2 em memória com dados de exemplo)

```bash
./mvnw spring-boot:run        # Linux/Mac
mvnw.cmd spring-boot:run      # Windows
```

- Swagger: http://localhost:8080/swagger-ui.html
- Console H2: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:estoque`, usuário `sa`, sem senha)
- Usuário inicial: `admin` / `admin123`

### Opção 2: com MySQL via Docker

```bash
docker compose up --build
```

No perfil `mysql`, a variável `JWT_SECRET` (mínimo de 32 bytes) é obrigatória. O usuário `admin` é criado no primeiro start com a senha de `ADMIN_PASSWORD`. O `docker-compose.yml` traz valores de exemplo para uso local.

### Autenticação

1. Faça login e copie o `accessToken`:

```http
POST /api/auth/login
Content-Type: application/json

{ "login": "admin", "senha": "admin123" }
```

```json
{ "accessToken": "eyJhbGciOiJIUzI1NiJ9...", "tokenType": "Bearer", "expiraEm": "2026-10-23T23:00:00Z", "papel": "ADMIN" }
```

2. Envie o token nas demais requisições: `Authorization: Bearer <token>`. No Swagger, use o botão **Authorize**.

O token vale 2 horas (`app.seguranca.jwt.expiracao`). Sem token ou com token inválido, a API responde 401; sem permissão para a operação, responde 403.

### Testes

```bash
./mvnw test
```

São 24 testes: unitários das regras de domínio, do serviço de vendas (com Mockito) e testes de integração de ponta a ponta (MockMvc + H2 + Flyway), incluindo login, token e permissões por perfil.

## Endpoints

| Método | Rota | Descrição | Perfil |
|---|---|---|---|
| POST | `/api/auth/login` | Login; devolve o token JWT | público |
| POST | `/api/produtos` | Cadastra produto | ADMIN |
| GET | `/api/produtos?nome=&page=&size=` | Lista produtos ativos (paginado) | qualquer |
| GET | `/api/produtos/{id}` | Detalha produto | qualquer |
| PUT | `/api/produtos/{id}` | Atualiza dados cadastrais | ADMIN |
| DELETE | `/api/produtos/{id}` | Inativa produto | ADMIN |
| GET | `/api/produtos/estoque-baixo` | Produtos abaixo do estoque mínimo | qualquer |
| POST | `/api/produtos/{id}/movimentacoes` | Entrada, saída ou ajuste de inventário | ADMIN |
| GET | `/api/produtos/{id}/movimentacoes` | Histórico (kardex) do produto | qualquer |
| POST | `/api/vendas` | Registra venda e baixa estoque | qualquer |
| GET | `/api/vendas` | Lista vendas (paginado) | qualquer |
| GET | `/api/vendas/{id}` | Detalha venda com itens | qualquer |
| POST | `/api/vendas/{id}/cancelamento` | Cancela venda e devolve estoque | ADMIN |
| POST | `/api/usuarios` | Cadastra usuário (ADMIN ou OPERADOR) | ADMIN |
| GET | `/api/usuarios` | Lista usuários | ADMIN |
| DELETE | `/api/usuarios/{id}` | Inativa usuário | ADMIN |

"qualquer" = qualquer usuário autenticado.

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
├── usuario/        # usuários, perfis e criação do admin inicial
├── seguranca/      # configuração do Spring Security, login e emissão do JWT
└── compartilhado/  # tratamento de erros e configuração
```

## Próximos passos

- [x] Autenticação com Spring Security + JWT
- [ ] Testes de integração com Testcontainers (MySQL real)
- [ ] Cálculo de tributos (ICMS/PIS/COFINS) por NCM
- [ ] Geração do XML da NFC-e a partir da venda

## Autor

**Manoel Rogildo Martins**: [LinkedIn](https://www.linkedin.com/in/manoelrogildo)
