# login-service — MS-AV-02 Segurança e Observabilidade

Serviço de login em Spring Boot 4.1.1 (Java 17, H2 em memória) com Spring Security + `JdbcUserDetailsManager`, indicadores no Micrometer/Prometheus, dashboard no Grafana e documentação OpenAPI/Swagger.

## Como rodar

Na pasta do projeto (CMD):

```
mvnw.cmd spring-boot:run
```

- Swagger: http://localhost:8080/swagger-ui/index.html
- Console H2: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:logindb`, usuário `sa`, senha `password`)
- Métricas: http://localhost:8080/actuator/prometheus

## Usuários iniciais

| Usuário | Senha | Role |
|---|---|---|
| `admin` | `admin` | ROLE_ADMIN |
| `manut` | `manut` | ROLE_MANUTENCAO |
| `joao` | `123` | ROLE_USER |

Usuários cadastrados em `POST /` recebem `ROLE_USER`.

## Endpoints

| Método e URL | Acesso | Corpo | Respostas |
|---|---|---|---|
| `POST /login` | público | `{"username":"joao","senha":"123"}` | 200 válido, 401 inválido ou bloqueado |
| `PUT /trocarsenha/{userid}` | público | `{"atual":"123","nova":"nova123"}` | 200, 400, 401 (atual errada), 404 |
| `POST /` | ROLE_MANUTENCAO (Basic) | `{"username":"maria","senha":"abc123"}` | 201, 400, 401, 403, 409 |
| `PUT /unlock/{userid}` | ROLE_ADMIN (Basic) | — | 200, 401, 403, 404 |
| `GET /` | ROLE_MANUTENCAO (Basic) | — | 200 (lista sem senhas), 401, 403 |

Os endpoints protegidos usam **HTTP Basic** (no Thunder Client: aba Auth → Basic).

## Regra de bloqueio

Na **segunda falha seguida** de login o usuário é bloqueado (`login_attempts.locked = true`). Um login válido zera o contador de falhas. Usuário bloqueado recebe 401 mesmo com a senha certa, até um ADMIN chamar `PUT /unlock/{userid}`.

## Indicadores (Micrometer)

| Métrica no Prometheus | Tipo | Significado |
|---|---|---|
| `login_validos_total` | Counter | logins válidos |
| `login_invalidos_total` | Counter | logins inválidos |
| `usuarios_bloqueados` | Gauge | usuários bloqueados agora (sobe e desce) |

## Prometheus e Grafana (Docker)

Com o Docker Desktop aberto e a aplicação rodando, na pasta do projeto:

```
docker compose up -d
```

- Prometheus: http://localhost:9090 (em Status → Targets, o `login-service` deve estar **UP**)
- Grafana: http://localhost:3000 (usuário `admin`, senha `admin`; pode pular a troca de senha)
- O data source e o dashboard **Login Service** já são criados automaticamente (pasta `monitoramento/grafana`).

Para parar: `docker compose down`.

## Roteiro de teste

1. `POST /login` com `joao/123` → 200
2. `POST /login` com `joao/errada` → 401 (1ª falha)
3. `POST /login` com `joao/errada` → 401 (2ª falha: **bloqueado**)
4. `POST /login` com `joao/123` → 401 (bloqueado mesmo com a senha certa)
5. `GET /` com `manut/manut` → 200, `joao` aparece com `bloqueado: true`
6. `GET /` com `joao/123` → 403 (sem a role) e sem autenticação → 401
7. `PUT /unlock/joao` com `manut/manut` → 403; com `admin/admin` → 200
8. `POST /login` com `joao/123` → 200 de novo
9. `POST /` com `manut/manut` e `{"username":"maria","senha":"abc123"}` → 201; repetir → 409
10. `PUT /trocarsenha/maria` com `{"atual":"abc123","nova":"xyz"}` → 200; com atual errada → 401; usuário inexistente → 404
11. Abra `/actuator/prometheus` e procure `login_validos_total`, `login_invalidos_total`, `usuarios_bloqueados`

## Entrega

1. Print do dashboard (com valores diferentes de zero): salvar em `docs/dashboard-grafana.png`
2. Exportar o dashboard: no Grafana, abrir **Login Service** → ícone **Export** → **Export as code** → **Download file**. Salvar como `monitoramento/grafana/dashboard-exportado.json`
3. Subir tudo para um repositório **público** pelo GitHub Desktop e enviar o link

## Observações de projeto

- A coluna `senha` tem 10 caracteres (enunciado), então não cabe hash BCrypt. As senhas ficam em texto puro **apenas por ser um exercício**; num sistema real use `BCryptPasswordEncoder` e uma coluna maior.
- O script SQL do enunciado tinha uma vírgula sobrando no fim da tabela `login_attempts`, que daria erro; ela foi removida em `schema.sql`.
- O `JdbcUserDetailsManager` lê `usuarios` (em vez da tabela padrão `users`) por meio de consultas customizadas, e as roles ficam na tabela `authorities`. Usuário bloqueado vem com `enabled = false`.
- `/actuator/**`, Swagger e o console do H2 estão liberados sem login para facilitar o estudo.
