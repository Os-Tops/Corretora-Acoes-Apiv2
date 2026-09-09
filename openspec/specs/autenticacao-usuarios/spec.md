# Autenticacao de usuarios Specification

## Objetivo

Permitir que usuarios criem conta, iniciem sessao, encerrem sessao e acessem funcionalidades conforme o perfil associado.

## Regras de negocio

- Um novo cadastro cria sempre um usuario com perfil `USER`.
- O cadastro exige nome, email e senha preenchidos.
- O email e normalizado com `trim` e letras minusculas antes de consulta ou persistencia.
- A senha deve ter pelo menos 6 caracteres e deve ser armazenada como hash BCrypt.
- A confirmacao de senha deve coincidir com a senha no endpoint de cadastro.
- Nao pode existir mais de uma conta para o mesmo email, ignorando maiusculas e minusculas.
- O cadastro cria uma carteira inicial zerada para o usuario.
- O cadastro nao autentica automaticamente o usuario.
- O login exige email e senha validos e emite um token de sessao novo.
- O logout invalida o token de sessao do usuario autenticado.
- Rotas protegidas exigem cabecalho `Authorization` no formato `Bearer <token>`.
- Operacoes administrativas exigem perfil `ADMIN`; operacoes de investidor exigem perfil `USER`.

## Fluxos

- Cadastro:
  1. O usuario informa nome, email, senha e confirmacao de senha.
  2. A API valida os campos, normaliza o email e verifica duplicidade.
  3. A API cria o usuario `USER`, armazena a senha como hash e cria uma carteira zerada.
  4. A API retorna `201 Created` com dados publicos do usuario e token nulo.

- Login:
  1. O usuario informa email e senha.
  2. A API busca o usuario por email normalizado.
  3. A API compara a senha informada com o hash BCrypt.
  4. A API grava um novo token de sessao e retorna token, nome, email e role.

- Logout:
  1. O frontend envia o token atual no cabecalho `Authorization`.
  2. A API valida a sessao e remove o token persistido.
  3. A API retorna `204 No Content`.

- Protecao de rota no frontend:
  1. O frontend verifica se ha usuario com token no `localStorage`.
  2. Rotas privadas redirecionam usuarios sem token para `/login`.
  3. Respostas `401` removem a sessao local.

- Migracao de usuario legado no frontend:
  1. Quando o login retorna `401`, o frontend verifica usuarios legados em `localStorage`.
  2. Se encontrar email e senha correspondentes, chama o cadastro da API.
  3. Apos cadastro bem-sucedido, remove o usuario legado e tenta o login novamente.

## Requisitos funcionais

- RF-AUTH-001: O sistema deve cadastrar usuarios investidores por `POST /auth/register`.
- RF-AUTH-002: O sistema deve autenticar usuarios por `POST /auth/login`.
- RF-AUTH-003: O sistema deve encerrar sessoes por `POST /auth/logout`.
- RF-AUTH-004: O sistema deve emitir e validar token de sessao no formato Bearer.
- RF-AUTH-005: O sistema deve diferenciar permissoes de `ADMIN` e `USER`.
- RF-AUTH-006: O sistema deve criar carteira inicial zerada durante o cadastro de usuario.
- RF-AUTH-007: O frontend deve proteger as rotas principais contra usuarios sem sessao local.
- RF-AUTH-008: O frontend deve migrar usuario legado local quando o login inicial falhar e as credenciais antigas forem validas.

## Requisitos nao funcionais

- RNF-AUTH-001: Senhas nao devem ser retornadas em respostas HTTP.
- RNF-AUTH-002: Hashes de senha devem usar BCrypt.
- RNF-AUTH-003: A resposta de erro deve seguir o modelo JSON centralizado da API.
- RNF-AUTH-004: A autenticacao deve ser stateless para o cliente, usando token enviado a cada requisicao.
- RNF-AUTH-005: O frontend deve limpar a sessao local quando a API retornar `401`.

## Comportamento esperado

- Usuarios novos ficam aptos a fazer login somente apos cadastro bem-sucedido.
- Um login bem-sucedido substitui o token de sessao anterior por um novo token.
- Requisicoes protegidas sem token, com token vazio ou com token invalido sao rejeitadas.
- Usuarios `USER` nao executam operacoes de administrador.
- Usuarios `ADMIN` nao executam operacoes reservadas a investidores.

## Requirements

### Requirement: Registrar usuario investidor

The system SHALL criar uma conta `USER` com email normalizado, senha com hash e carteira inicial zerada quando receber dados validos de cadastro.

#### Scenario: Cadastro valido

- **GIVEN** um nome, email, senha e confirmacao de senha validos
- **WHEN** o cliente envia `POST /auth/register`
- **THEN** o sistema cria o usuario com role `USER`
- **AND** cria uma carteira com `saldoAcao = 0` e `saldoEmConta = 0`
- **AND** retorna `201 Created` sem token de sessao

#### Scenario: Senhas divergentes

- **GIVEN** senha e confirmacao de senha diferentes
- **WHEN** o cliente envia `POST /auth/register`
- **THEN** o sistema rejeita a requisicao com erro de regra de negocio

#### Scenario: Email duplicado

- **GIVEN** ja existe usuario com o mesmo email, ignorando maiusculas e minusculas
- **WHEN** o cliente tenta cadastrar uma nova conta com esse email
- **THEN** o sistema retorna conflito e nao cria outra conta

### Requirement: Autenticar usuario

The system SHALL emitir um token de sessao quando as credenciais informadas forem validas.

#### Scenario: Login valido

- **GIVEN** um usuario cadastrado
- **WHEN** o cliente envia email e senha corretos para `POST /auth/login`
- **THEN** o sistema grava um novo token de sessao
- **AND** retorna token, nome, email e role

#### Scenario: Credenciais invalidas

- **GIVEN** email inexistente ou senha incorreta
- **WHEN** o cliente envia `POST /auth/login`
- **THEN** o sistema retorna `401 Unauthorized`

### Requirement: Validar sessao em rotas protegidas

The system SHALL exigir `Authorization: Bearer <token>` para acessar rotas protegidas.

#### Scenario: Token ausente

- **GIVEN** uma rota protegida
- **WHEN** a requisicao nao contem cabecalho Bearer valido
- **THEN** o sistema retorna `401 Unauthorized`

#### Scenario: Token valido

- **GIVEN** um token de sessao persistido para um usuario
- **WHEN** a requisicao contem `Authorization: Bearer <token>`
- **THEN** o sistema identifica o usuario autenticado

### Requirement: Aplicar permissoes por perfil

The system SHALL bloquear operacoes quando o perfil autenticado nao corresponder ao perfil exigido.

#### Scenario: Usuario tenta operacao administrativa

- **GIVEN** um usuario autenticado com role `USER`
- **WHEN** ele tenta cadastrar corretora, cadastrar acao ou atualizar cotacao
- **THEN** o sistema retorna `403 Forbidden`

#### Scenario: Administrador tenta operacao de investidor

- **GIVEN** um usuario autenticado com role `ADMIN`
- **WHEN** ele tenta criar carteira de investidor, comprar, vender ou registrar movimentacao de carteira
- **THEN** o sistema retorna `403 Forbidden`

### Requirement: Encerrar sessao

The system SHALL invalidar o token atual no logout.

#### Scenario: Logout valido

- **GIVEN** um usuario autenticado
- **WHEN** o cliente envia `POST /auth/logout`
- **THEN** o sistema remove o token de sessao
- **AND** retorna `204 No Content`
