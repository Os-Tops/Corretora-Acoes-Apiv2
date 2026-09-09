# Contas de corretora Specification

## Objetivo

Permitir que investidores vinculem contas de corretora validadas as suas carteiras para organizar movimentacoes por instituicao.

## Regras de negocio

- Somente usuarios `USER` podem listar ou criar contas de corretora em carteiras.
- A carteira informada deve pertencer ao usuario autenticado.
- A corretora informada deve existir.
- Apenas corretoras com `validadaNaCvm = true` podem ser vinculadas a carteiras.
- O apelido da conta e obrigatorio e deve ter no maximo 80 caracteres.
- O identificador da conta e opcional e deve ter no maximo 80 caracteres.
- O par `carteira_id` e `apelido` deve ser unico na persistencia.
- A listagem de contas deve ser ordenada por apelido ascendente.
- Uma movimentacao so pode referenciar conta de corretora pertencente a mesma carteira.

## Fluxos

- Criacao de conta:
  1. O investidor seleciona uma carteira propria.
  2. O investidor seleciona uma corretora validada.
  3. O investidor informa apelido e opcionalmente identificador.
  4. A API valida titularidade da carteira e status da corretora.
  5. A API persiste a conta ativa.

- Uso em movimentacao:
  1. O investidor registra um lancamento.
  2. Se informar `contaCorretoraId`, a API valida que a conta pertence a carteira.
  3. A movimentacao armazena a referencia a conta.

## Requisitos funcionais

- RF-CTA-001: O sistema deve listar contas por `GET /carteiras/{id}/contas`.
- RF-CTA-002: O sistema deve criar conta por `POST /carteiras/{id}/contas`.
- RF-CTA-003: O sistema deve validar a titularidade da carteira antes de listar ou criar contas.
- RF-CTA-004: O sistema deve aceitar somente corretoras validadas.
- RF-CTA-005: O sistema deve retornar resumo da corretora no contrato de conta.
- RF-CTA-006: O sistema deve permitir associar conta a movimentacoes da mesma carteira.

## Requisitos nao funcionais

- RNF-CTA-001: A regra de unicidade de apelido por carteira deve ser reforcada por constraint de banco.
- RNF-CTA-002: Operacoes de criacao devem ser transacionais.
- RNF-CTA-003: Erros de integridade devem retornar `409 Conflict` via tratamento centralizado.
- RNF-CTA-004: A entidade de conta nao deve expor a carteira por JSON para evitar vazamento de relacoes internas.

## Comportamento esperado

- O investidor ve apenas as contas das proprias carteiras.
- Corretoras cadastradas como nao CVM nao aparecem como opcoes validas no frontend de carteira.
- Uma tentativa de vincular conta a corretora nao validada e rejeitada.
- Movimentacoes com conta de outra carteira sao rejeitadas.

## Requirements

### Requirement: Vincular conta de corretora

The system SHALL permitir que investidores vinculem corretoras validadas as suas carteiras.

#### Scenario: Conta valida

- **GIVEN** um usuario `USER`, uma carteira propria e uma corretora com `validadaNaCvm = true`
- **WHEN** ele envia `POST /carteiras/{id}/contas` com apelido valido
- **THEN** o sistema cria uma conta ativa vinculada a carteira e a corretora
- **AND** retorna `201 Created`

#### Scenario: Corretora inexistente

- **GIVEN** um usuario `USER` e uma carteira propria
- **WHEN** ele informa `corretoraId` inexistente
- **THEN** o sistema rejeita a requisicao

#### Scenario: Corretora nao validada

- **GIVEN** uma corretora existente com `validadaNaCvm = false`
- **WHEN** o investidor tenta vincula-la a carteira
- **THEN** o sistema rejeita a requisicao

### Requirement: Listar contas da carteira

The system SHALL listar contas de corretora de uma carteira propria ordenadas por apelido.

#### Scenario: Listagem valida

- **GIVEN** um usuario `USER` autenticado e uma carteira propria com contas
- **WHEN** ele acessa `GET /carteiras/{id}/contas`
- **THEN** o sistema retorna as contas ordenadas por apelido ascendente

### Requirement: Validar conta em movimentacao

The system SHALL impedir que movimentacoes referenciem contas de outra carteira.

#### Scenario: Conta pertence a carteira

- **GIVEN** uma conta vinculada a carteira da movimentacao
- **WHEN** o investidor registra o lancamento
- **THEN** o sistema aceita a referencia da conta

#### Scenario: Conta pertence a outra carteira

- **GIVEN** uma conta de corretora vinculada a outra carteira
- **WHEN** o investidor registra movimentacao usando esse ID
- **THEN** o sistema rejeita a requisicao

