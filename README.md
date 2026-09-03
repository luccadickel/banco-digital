# Banco Digital - API de Transferências

API REST de um banco digital simplificado, com foco em consistência dos dados, resiliência e performance em um cenário simulando alta concorrência. A API permite transferências de valores entre contas e consultas de movimentações financeiras.

# Contexto

O desafio central do projeto estava em garantir que transferências concorrestes na mesma conta, não corrompam o saldo (consistência), que a mesma operação não seja processada duas vezes (idempotência/resiliência), e que tudo isso não reduza a performance.

## Dependências

- Java: 21
- Spring Boot 4.1.1 (Spring MVC + Spring Data JPA)
- PostgreSQL
- Maven
- Swagger/OpenAPI (springdoc) - documentação interativa
- JUnit 5 + Mockito - testes
- Lombok

## Como rodar o projeto

### Pré requisitos
- Java 21
- PostgreSQL rodando localmente
- Maven (ou o wrapper ./mvnw incluído)

## 1. Banco de dados

Crie o banco de dados no PostgreSQL

```sql
    CREATE DATABASE banco_digital
```

O schema está versionado em data/schema.sql e a base de dados inicial de clientes está no insert.sql

## 2. Variáveis de ambiente

As credenciais do banco vêm de um arquivo .env(não versionado devido as secrets). Crie o seu a partir do .env.example

```
DB_URL=jdbc:postgresql://localhost:5432/banco_digital
DB_USERNAME=postgres
DB_PASSWORD=sua_senha
```

## 3. Executar

```bash
./mvnw spring-boot:run
```
A aplicação sobe em http://localhost:8080.

## 4. Documentação da API (Swagger)

Com a aplicação rodando, acesse:
```
http://localhost:8080/swagger-ui.html
```

## Endpoints principais
| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/transfers` | Realiza uma transferência entre contas |
| `GET`  | `/accounts/{accountId}/transfers` | Consulta as movimentações de uma conta (paginado) |


### Exemplo de transferência 

```
POST /transfers
{
  "sourceAccountId": 1,
  "destinationAccountId": 2,
  "amount": 100.00,
  "idempotencyKey": "a3f2c9e1-..."
}
```

## Decisão de arquitetura

### Arquitetura em camadas
O projeto segue uma separação clara de responsabilidades: **Controller** (camada web, DTOs e validação de entrada), **Service** (regras de negócio, orquestração transacional), **Repository** (acesso a dados via Spring Data JPA), **Mapper** (conversão entre DTOs e entidades) e um **Exception Handler** global que traduz exceções de domínio em respostas HTTP. Os serviços não conhecem tipos da camada web em suas regras de negócio; a tradução DTO ↔ domínio acontece via mappers.


## Testes

- **Testes unitários de serviço** (`TransferServiceTest`, `AccountServiceTest`): validam a lógica de negócio (validações, cálculo de saldo, idempotência, tratamento de conta inexistente) com colaboradores mockados.
- **Testes de concorrência** (`TransferConcurrencyTest`, `@SpringBootTest` com banco real):
  - Lost update: 100 transferências concorrentes na mesma conta; verifica que o saldo final é exato.
  - Deadlock: transferências simultâneas em direções opostas (A→B e B→A); verifica que todas completam sem travar.

Rodar todos os testes:
```bash
./mvnw test
```

## Evoluções possíveis (fora do escopo desta versão)

Como possibilidade de evoluções futuras, avliei alguns pontos que seriam relevantes em um ambiente de maior escala e contexto real com autenticação:

- **Update atômico** (`UPDATE ... SET balance = balance - :amount WHERE balance >= :amount`) como alternativa ao lock pessimista utilizado, encurtando a janela de lock e aumentando o throughput. Trade-off: menos legível/orientado a domínio.
- **Outbox pattern** para a notificação, caso ela passe a ser uma chamada externa que pode falhar (e-mail/SMS): gravaria o evento na mesma transação do débito/crédito e um processo à parte o publicaria, com consumidor idempotente, garantindo que transferência e notificação nunca divirjam (resolve o *dual-write problem*).
- **Réplica de leitura** para separar a carga das consultas de extrato das escritas de transferência.
- **Autenticação/autorização:** a identidade do operador viria do contexto de segurança (JWT/sessão) e o sistema validaria a titularidade da conta antes de cada operação. Nesta versão, o identificador da conta é recebido diretamente na requisição.


## Estrutura do projeto

```
src/main/java/br/com/bancodigital/
├── controller/       # endpoints REST
│   ├── request/      # DTOs de entrada
│   └── response/     # DTOs de saída
├── service/          # regras de negócio
├── repository/       # acesso a dados (Spring Data JPA)
├── domain/           # entidades JPA
├── mapper/           # conversão DTO ↔ entidade
├── event/            # eventos de domínio
├── listener/         # listeners de eventos
└── exception/        # exceções de domínio + handler global
```