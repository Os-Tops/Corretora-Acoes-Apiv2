# Proventos, dividendos e JCP Specification

## Objetivo

Registrar recebimentos de dividendos e juros sobre capital proprio em carteiras de investidores, permitindo associacao opcional a ativo e conta de corretora.

## Regras de negocio

- Dividendos e JCP sao tipos de movimentacao do livro da carteira.
- Somente usuarios `USER` podem registrar proventos.
- A carteira informada deve pertencer ao investidor autenticado.
- `DIVIDENDO` e `JCP` exigem valor positivo.
- A data da operacao e obrigatoria e nao pode estar no futuro.
- Ativo relacionado e opcional para `DIVIDENDO` e `JCP`.
- Conta de corretora e opcional, mas se informada deve pertencer a carteira.
- Taxas e impostos ausentes devem ser tratados como zero.
- Taxas e impostos nao podem ser negativos.
- Proventos aumentam saldo em conta pelo valor informado menos taxas e impostos.
- Proventos nao alteram quantidade nem preco medio da posicao.
- Cancelar provento recalcula a carteira removendo seu efeito financeiro.

## Fluxos

- Registro de dividendo:
  1. O investidor seleciona carteira, tipo `DIVIDENDO`, data e valor.
  2. Opcionalmente informa ativo e conta de corretora.
  3. A API valida valor, data, titularidade e referencias.
  4. A API salva a movimentacao e recalcula o saldo em conta.

- Registro de JCP:
  1. O investidor seleciona carteira, tipo `JCP`, data e valor.
  2. Opcionalmente informa ativo e conta de corretora.
  3. A API valida os mesmos criterios de dividendos.
  4. A API salva a movimentacao e recalcula o saldo em conta.

- Cancelamento:
  1. O investidor cancela a movimentacao de provento.
  2. A API marca o registro como cancelado.
  3. A API recalcula a carteira sem o provento cancelado.

## Requisitos funcionais

- RF-PRO-001: O sistema deve registrar `DIVIDENDO` por `POST /carteiras/{id}/movimentacoes`.
- RF-PRO-002: O sistema deve registrar `JCP` por `POST /carteiras/{id}/movimentacoes`.
- RF-PRO-003: O sistema deve permitir ativo opcional para proventos.
- RF-PRO-004: O sistema deve permitir conta de corretora opcional da mesma carteira.
- RF-PRO-005: O sistema deve listar proventos no historico de movimentacoes.
- RF-PRO-006: O sistema deve permitir cancelar proventos como qualquer movimentacao externa ativa.

## Requisitos nao funcionais

- RNF-PRO-001: Proventos devem participar do mesmo recalc transacional do livro de movimentacoes.
- RNF-PRO-002: Valores devem usar `BigDecimal`.
- RNF-PRO-003: O historico deve preservar proventos cancelados para auditoria.
- RNF-PRO-004: Erros devem seguir o contrato JSON centralizado.

## Comportamento esperado

- Dividendos e JCP entram como caixa disponivel da carteira.
- Proventos com imposto ou taxa reduzem o valor liquido adicionado ao saldo.
- Associar um ativo a um provento nao aumenta nem reduz posicao.
- A tela de carteiras mostra dividendos e JCP no historico de movimentacoes.

## Requirements

### Requirement: Registrar dividendo

The system SHALL registrar dividendos como movimentacoes financeiras positivas de carteira.

#### Scenario: Dividendo sem ativo

- **GIVEN** um usuario `USER` e uma carteira propria
- **WHEN** ele registra `DIVIDENDO` com valor positivo e sem ativo
- **THEN** o sistema salva a movimentacao
- **AND** aumenta o saldo em conta pelo valor liquido

#### Scenario: Dividendo com ativo

- **GIVEN** um ativo cadastrado
- **WHEN** o investidor registra `DIVIDENDO` informando `acaoId`
- **THEN** o sistema associa o ativo ao historico
- **AND** nao altera quantidade ou preco medio da posicao

### Requirement: Registrar JCP

The system SHALL registrar JCP como movimentacao financeira positiva de carteira.

#### Scenario: JCP valido

- **GIVEN** um usuario `USER`, uma carteira propria e valor positivo
- **WHEN** ele registra `JCP`
- **THEN** o sistema salva a movimentacao
- **AND** recalcula o saldo em conta somando o valor e subtraindo encargos

### Requirement: Reverter provento cancelado

The system SHALL remover o efeito financeiro de proventos cancelados no recalc.

#### Scenario: Cancelamento de dividendo

- **GIVEN** um dividendo ativo no historico
- **WHEN** o investidor cancela a movimentacao
- **THEN** o sistema marca a movimentacao como cancelada
- **AND** recalcula a carteira sem o valor desse dividendo

#### Scenario: Provento com data futura

- **GIVEN** um provento com data posterior a data atual do servidor
- **WHEN** o investidor tenta registrar
- **THEN** o sistema rejeita a requisicao

