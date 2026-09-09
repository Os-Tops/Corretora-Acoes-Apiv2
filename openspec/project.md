# Project

## Contexto

O projeto e uma aplicacao de gestao financeira para acoes e corretoras, composta por uma API REST Java 17 com Spring Boot e um frontend React/Vite.

## Arquitetura atual

- Backend: `Controller -> Service -> Repository -> Entity`.
- Contratos HTTP: DTOs em `src/main/java/com/projeto/gestao/domain/dto`.
- Persistencia: Spring Data JPA.
- Integracoes externas: portas de dominio e adapters para Brasil API, ViaCEP, brapi e Alpha Vantage.
- Frontend: React Router com rotas protegidas e armazenamento local da sessao.

## Convencoes comuns

- Usuarios autenticados enviam `Authorization: Bearer <token>`.
- Perfis reconhecidos: `ADMIN` e `USER`.
- Erros de validacao, negocio, permissao e integracoes externas sao retornados em JSON estruturado.
- Valores financeiros sao manipulados com `BigDecimal`.
- Dados sensiveis como senha hash e token persistido nao devem ser expostos em respostas JSON de entidades.

## Fontes principais

- `README.md`
- `src/main/java/com/projeto/gestao/api/controller`
- `src/main/java/com/projeto/gestao/controller/DashboardController.java`
- `src/main/java/com/projeto/gestao/service`
- `src/main/java/com/projeto/gestao/domain/model`
- `src/main/front/pages`

