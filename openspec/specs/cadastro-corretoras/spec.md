# Cadastro de corretoras Specification

## Objetivo

Manter um cadastro de corretoras a partir de CNPJ, enriquecendo os dados por fontes publicas e identificando se a instituicao e participante do mercado financeiro.

## Regras de negocio

- Apenas usuarios `ADMIN` podem cadastrar corretoras.
- Usuarios autenticados podem listar e consultar corretoras por ID ou CNPJ.
- O cadastro recebe somente o CNPJ informado pelo cliente.
- Pontuacao do CNPJ deve ser removida antes de validar, consultar ou persistir.
- O CNPJ deve ter 14 digitos, nao pode ter todos os digitos iguais e deve possuir digitos verificadores validos.
- CNPJ duplicado nao pode ser cadastrado.
- Dados empresariais devem ser obtidos pela Brasil API antes da persistencia.
- O CNPJ consultado deve possuir CEP.
- O CEP retornado pela fonte de CNPJ deve ser validado no ViaCEP antes da persistencia.
- A flag `validadaNaCvm` deve ser verdadeira quando a validacao direta indicar participante de mercado ou quando os dados cadastrais indicarem corretora, distribuidora, titulos e valores mobiliarios, CCTVM, DTVM ou banco em situacao ativa.
- Um CNPJ valido que nao indique instituicao financeira pode ser persistido com `validadaNaCvm = false`.

## Fluxos

- Cadastro por CNPJ:
  1. O administrador informa CNPJ.
  2. A API limpa a pontuacao e valida os digitos.
  3. A API verifica se o CNPJ ja existe.
  4. A API consulta dados empresariais na Brasil API.
  5. A API classifica `validadaNaCvm`.
  6. A API valida o CEP no ViaCEP.
  7. A API persiste a corretora e retorna `201 Created`.

- Consulta:
  1. Um usuario autenticado solicita lista, ID ou CNPJ.
  2. A API retorna os dados cadastrais conhecidos.
  3. Buscas por ID ou CNPJ inexistentes retornam `404`.

## Requisitos funcionais

- RF-COR-001: O sistema deve cadastrar corretoras por `POST /corretoras`.
- RF-COR-002: O sistema deve listar corretoras por `GET /corretoras` e `GET /api/dashboard/corretoras`.
- RF-COR-003: O sistema deve buscar corretora por `GET /corretoras/{id}`.
- RF-COR-004: O sistema deve buscar corretora por `GET /corretoras/cnpj/{cnpj}`.
- RF-COR-005: O sistema deve validar formato e digitos verificadores do CNPJ.
- RF-COR-006: O sistema deve obter dados de CNPJ em fonte externa.
- RF-COR-007: O sistema deve validar CEP da corretora em fonte externa.
- RF-COR-008: O sistema deve registrar a classificacao `validadaNaCvm`.

## Requisitos nao funcionais

- RNF-COR-001: Consultas de CNPJ e CEP devem usar cache simples quando disponivel.
- RNF-COR-002: CNPJ invalido ou duplicado deve ser rejeitado antes de chamadas externas desnecessarias.
- RNF-COR-003: Falhas de integracao externa devem ser convertidas para erro JSON padronizado.
- RNF-COR-004: A persistencia deve manter CNPJ unico.
- RNF-COR-005: O cadastro deve ser transacional para evitar corretoras parcialmente persistidas.

## Comportamento esperado

- CNPJs formatados e nao formatados sao tratados da mesma forma.
- Um cadastro sem retorno de Brasil API ou sem CEP validavel nao e persistido.
- A tela de corretoras informa ao usuario se a corretora foi validada como CVM ou cadastrada como nao CVM.
- Corretoras nao validadas podem aparecer na listagem, mas nao podem ser usadas para cadastrar ativos ou contas de carteira.

## Requirements

### Requirement: Cadastrar corretora por CNPJ

The system SHALL permitir que administradores cadastrem corretoras usando um CNPJ valido.

#### Scenario: Cadastro de corretora validada

- **GIVEN** um usuario `ADMIN` autenticado e um CNPJ valido ainda nao cadastrado
- **WHEN** a Brasil API retorna dados empresariais e o ViaCEP valida o CEP
- **AND** os dados indicam participante do mercado financeiro
- **THEN** o sistema persiste a corretora com `validadaNaCvm = true`
- **AND** retorna `201 Created`

#### Scenario: Cadastro de CNPJ nao financeiro

- **GIVEN** um usuario `ADMIN` autenticado e um CNPJ valido ainda nao cadastrado
- **WHEN** os dados cadastrais nao indicam instituicao financeira
- **THEN** o sistema persiste a corretora com `validadaNaCvm = false`

#### Scenario: CNPJ invalido

- **GIVEN** um CNPJ com tamanho, repeticao ou digitos verificadores invalidos
- **WHEN** o administrador tenta cadastrar a corretora
- **THEN** o sistema rejeita a requisicao
- **AND** nao consulta a Brasil API

#### Scenario: CNPJ duplicado

- **GIVEN** ja existe corretora com o CNPJ limpo informado
- **WHEN** o administrador tenta cadastrar novamente
- **THEN** o sistema rejeita a requisicao
- **AND** nao consulta a Brasil API

### Requirement: Validar endereco por CEP

The system SHALL validar o CEP retornado pela fonte de CNPJ antes de persistir a corretora.

#### Scenario: CEP ausente

- **GIVEN** a Brasil API retorna dados sem CEP
- **WHEN** o cadastro e processado
- **THEN** o sistema rejeita a corretora

#### Scenario: CEP nao validado

- **GIVEN** a Brasil API retorna um CEP
- **WHEN** o ViaCEP nao retorna dados validos
- **THEN** o sistema rejeita a corretora
- **AND** nao salva o registro

### Requirement: Consultar corretoras

The system SHALL permitir consulta de corretoras para usuarios autenticados.

#### Scenario: Listar corretoras

- **GIVEN** um usuario autenticado
- **WHEN** ele acessa `GET /corretoras`
- **THEN** o sistema retorna todas as corretoras cadastradas

#### Scenario: Buscar por CNPJ

- **GIVEN** um usuario autenticado e um CNPJ existente, com ou sem pontuacao
- **WHEN** ele acessa `GET /corretoras/cnpj/{cnpj}`
- **THEN** o sistema retorna a corretora correspondente

