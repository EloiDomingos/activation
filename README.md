# API de Solicitações de Ativação de Produtos

## Objetivo
API REST para gerenciar solicitações de ativação de produtos de clientes, garantindo a integridade dos dados e integrando-se com um provedor externo.

## Tecnologias Utilizadas
- Java 17
- Spring Boot 3.x
- Spring Data JPA & Hibernate
- PostgreSQL
- Flyway
- Docker & Docker Compose
- JUnit 5, Mockito & MockMvc

## Requisitos para Execução
- Docker e Docker Compose instalados.
- Maven (opcional, para execução local sem Docker).

## Instruções para Execução com Docker
Para iniciar o ambiente completo (API + Banco de Dados + Mock do Provedor):
```bash
docker compose up --build
```
A aplicação será compilada automaticamente dentro do container (multi-stage build).

## Instruções para Execução dos Testes
Para executar os testes automatizados:
```bash
mvn test
```

## Configurações por Variáveis de Ambiente
- `PORT`: Porta da aplicação (padrão: 8080)
- `DB_URL`: URL de conexão com o banco (padrão local: jdbc:postgresql://localhost:5432/activation_db)
- `DB_USERNAME`: Usuário do banco (padrão: postgres)
- `DB_PASSWORD`: Senha do banco (padrão: postgres)
- `PROVIDER_URL`: URL do provedor externo (padrão: http://localhost:8081)

## Endpoints Disponíveis

### POST /api/v1/requests
Cria uma nova solicitação de ativação.
**Corpo da Requisição:**
```json
{
  "externalId": "EXT-12345",
  "customerId": "CUST-10001",
  "productCode": "INTERNET-500"
}
```
**Validações:**
- `externalId`: Obrigatório, máximo 100 caracteres.
- `customerId`: Obrigatório, máximo 100 caracteres.
- `productCode`: Obrigatório, máximo 100 caracteres.

**Respostas:**
- `201 Created`: Solicitação criada com sucesso.
- `200 OK`: Solicitação repetida com mesmo conteúdo.
- `400 Bad Request`: Dados inválidos ou campos obrigatórios ausentes.
- `409 Conflict`: `externalId` já existente com conteúdo diferente ou violação de concorrência.
- `502 Bad Gateway`: Falha na integração com o provedor externo.

### GET /api/v1/requests/{id}
Busca uma solicitação pelo seu UUID.
**Respostas:**
- `200 OK`: Dados da solicitação retornados.
- `404 Not Found`: Solicitação não encontrada.

## Prevenção de Duplicidades e Concorrência
A API utiliza duas camadas de proteção:
1. **Verificação no Service**: Antes de criar, consulta se o `externalId` já existe e aplica a política de retorno (200 para mesmo conteúdo, 409 para conteúdo diferente).
2. **Restrição UNIQUE no Banco**: Garante a integridade em caso de requisições simultâneas que passem pela consulta inicial. A violação é tratada pelo `GlobalExceptionHandler` retornando 409.

## Integração com o Provedor
A integração é feita de forma síncrona via `RestTemplate`. O status da solicitação evolui de `PENDING` -> `PROCESSING` -> `COMPLETED` (ou `FAILED`).
**Decisão:** Não reexecutamos a ativação para repetições legítimas (mesmo conteúdo) para evitar chamadas redundantes ao provedor, conforme as instruções.

## Segurança (Spring Security, JWT, OAuth 2.0)
Para proteger a API em produção:
1. Adicionar `spring-boot-starter-security`.
2. Configurar um `SecurityFilterChain` para exigir autenticação nos endpoints `/api/**`.
3. Implementar um filtro de validação de JWT para extrair o token do cabeçalho `Authorization`.
4. Utilizar um Servidor de Autorização (como Keycloak) para emissão de tokens OAuth 2.0.

## Análise de Desempenho e Hipóteses
### Investigação de Lentidão
1. **Monitoramento**: Utilizar Spring Boot Actuator e Prometheus/Grafana para observar latência e taxa de erros.
2. **Logs**: Analisar logs de tempo de execução dos métodos do service e chamadas externas.
3. **Banco de Dados**: Verificar se há queries lentas ou falta de índices além do `external_id`.
4. **Integração Externa**: A causa mais comum de lentidão em sistemas que chamam APIs externas é o tempo de resposta do terceiro.

### Causa Comum e Hipótese
Um pool de conexões HTTP (ou de banco) exaurido pode causar lentidão generalizada e erros de timeout intermitentes. Isso pode ser confirmado observando métricas de `hikaricp` ou do cliente HTTP. Uma hipótese comum para lentidão súbita após aumento de carga é a contenção em travas de banco ou falta de recursos de rede.

### Validação da Correção
Para verificar se uma correção resolveu o problema, deve-se comparar as métricas de latência e taxa de sucesso (Apdex score) antes e depois da publicação em um ambiente de staging sob carga similar, ou via canário em produção.

### Melhorias
- Implementar processamento assíncrono (RabbitMQ/Kafka) para a chamada ao provedor.
- Utilizar cache para consultas frequentes de status.
- Implementar Circuit Breaker (Resilience4j) para falhas no provedor.
