# Livro de movimentacoes Specification

## Objetivo

Registrar o historico financeiro das carteiras e recalcular saldos, posicoes e preco medio a partir das movimentacoes ativas.

## Regras de negocio

- Somente usuarios `USER` podem listar, registrar ou cancelar movimentacoes de carteira.
- A carteira informada deve pertencer ao usuario autenticado.
- Tipos externos permitidos: `APORTE`, `RETIRADA`, `COMPRA`, `VENDA`, `DIVIDENDO`, `JCP`, `TAXA` e `IMPOSTO`.
- Tipos `AJUSTE_SALDO` e `AJUSTE_POSICAO` sao controlados pelo sistema e nao podem ser registrados externamente.
- A data da operacao e obrigatoria e nao pode estar no futuro.
- Taxas e impostos ausentes devem ser tratados como zero.
- Taxas e impostos nao podem ser negativos.
- Movimentacoes de `COMPRA` e `VENDA` exigem ativo, quantidade positiva e preco unitario positivo.
- Demais movimentacoes exigem valor positivo.
- Movimentacoes nao negociaveis podem referenciar ativo opcionalmente.
- Conta de corretora e opcional, mas quando informada deve pertencer a carteira.
- O recalc deve considerar apenas movimentacoes nao canceladas, ordenadas por data da operacao e data de registro.
- O saldo em conta nao pode ficar negativo em nenhum ponto da sequencia recalculada.
- A rota de aporte rapido registra `APORTE` na carteira principal do investidor.
- Ajustes administrativos de saldo registram `APORTE` ou `RETIRADA` pela diferenca entre saldo informado e saldo atual.
- Cancelamento marca a movimentacao como cancelada e recalcula a carteira; nao remove o registro.
- Movimentacoes ja canceladas nao podem ser canceladas novamente.
- Movimentacoes de ajuste/migracao nao podem ser canceladas.
- Ao registrar o primeiro lancamento de carteira com saldos/posicoes legadas, o sistema cria ajustes internos para preservar os dados existentes.

## Fluxos

- Registro de movimentacao:
  1. O investidor seleciona carteira propria.
  2. O investidor informa tipo, data e campos exigidos pelo tipo.
  3. A API valida titularidade, dados financeiros, conta e ativo.
  4. A API salva a movimentacao.
  5. A API recalcula saldo em conta, posicoes e saldo de acoes.

- Cancelamento:
  1. O investidor solicita cancelamento de uma movimentacao da carteira.
  2. A API valida existencia, titularidade e se a movimentacao pode ser cancelada.
  3. A API marca `cancelada = true` e registra `canceladaEm`.
  4. A API recalcula a carteira usando apenas movimentacoes ativas.

- Migracao de legado:
  1. Ao primeiro lancamento, a API verifica se a carteira ainda nao possui historico.
  2. Se houver saldo em conta ou posicoes existentes, cria ajustes internos.
  3. O novo lancamento e aplicado depois dos ajustes.

## Requisitos funcionais

- RF-MOV-001: O sistema deve listar movimentacoes por `GET /carteiras/{id}/movimentacoes`.
- RF-MOV-002: O sistema deve registrar movimentacao por `POST /carteiras/{id}/movimentacoes`.
- RF-MOV-003: O sistema deve cancelar movimentacao por `DELETE /carteiras/{id}/movimentacoes/{movimentacaoId}`.
- RF-MOV-004: O sistema deve recalcular carteira apos cada registro ou cancelamento.
- RF-MOV-005: O sistema deve calcular saldo em conta pela sequencia de movimentacoes.
- RF-MOV-006: O sistema deve calcular posicoes por ativo e remover posicoes zeradas.
- RF-MOV-007: O sistema deve migrar dados legados para ajustes internos quando necessario.
- RF-MOV-008: O sistema deve impedir saldo financeiro negativo durante o recalc.
- RF-MOV-009: O sistema deve registrar aporte rapido na carteira principal.
- RF-MOV-010: O sistema deve representar ajuste administrativo de saldo como movimentacao financeira.

## Requisitos nao funcionais

- RNF-MOV-001: Registro, cancelamento e recalc devem ocorrer em transacao.
- RNF-MOV-002: Preco medio deve ser calculado com precisao monetaria de 8 casas e arredondamento `HALF_UP`.
- RNF-MOV-003: O historico deve preservar registros cancelados para auditoria.
- RNF-MOV-004: Valores financeiros devem usar `BigDecimal`.
- RNF-MOV-005: A listagem deve ser deterministica por data da operacao e data de registro.

## Comportamento esperado

- `APORTE`, `DIVIDENDO` e `JCP` aumentam saldo em conta, descontando taxas e impostos.
- `RETIRADA`, `TAXA` e `IMPOSTO` reduzem saldo em conta, somando o efeito de taxas e impostos.
- `COMPRA` aumenta posicao e reduz saldo em conta pelo valor da compra mais taxas e impostos.
- `VENDA` reduz posicao e aumenta saldo em conta pelo valor da venda menos taxas e impostos.
- Uma venda maior que a quantidade disponivel e rejeitada.
- Uma movimentacao que deixaria saldo negativo e rejeitada.

## Requirements

### Requirement: Registrar movimentacao manual

The system SHALL registrar movimentacoes validas em carteiras do investidor autenticado.

#### Scenario: Aporte valido

- **GIVEN** um usuario `USER` e uma carteira propria
- **WHEN** ele registra `APORTE` com valor positivo e data nao futura
- **THEN** o sistema salva a movimentacao
- **AND** recalcula `saldoEmConta` somando o valor e subtraindo encargos

#### Scenario: Compra valida

- **GIVEN** uma carteira propria com saldo suficiente e um ativo existente
- **WHEN** o investidor registra `COMPRA` com quantidade e preco positivos
- **THEN** o sistema salva a movimentacao
- **AND** recalcula quantidade, preco medio, saldo em conta e saldo de acoes

#### Scenario: Tipo de ajuste externo

- **GIVEN** uma requisicao com tipo `AJUSTE_SALDO` ou `AJUSTE_POSICAO`
- **WHEN** o cliente tenta registrar a movimentacao
- **THEN** o sistema rejeita a requisicao

#### Scenario: Data futura

- **GIVEN** uma data de operacao posterior a data atual do servidor
- **WHEN** o investidor tenta registrar a movimentacao
- **THEN** o sistema rejeita a requisicao

### Requirement: Recalcular carteira pelo historico

The system SHALL tratar o historico ativo como fonte de verdade para saldos e posicoes.

#### Scenario: Sequencia de aporte e compra

- **GIVEN** uma carteira com aporte de 100 e compra de 2 unidades a 10 com 2 de encargos
- **WHEN** o recalc e executado
- **THEN** o saldo em conta fica 78
- **AND** o preco medio fica 11

#### Scenario: Saldo negativo

- **GIVEN** uma sequencia de movimentacoes
- **WHEN** alguma movimentacao deixa o saldo da carteira negativo
- **THEN** o sistema rejeita a operacao de recalc

### Requirement: Cancelar movimentacao

The system SHALL cancelar movimentacoes permitidas sem apagar o historico.

#### Scenario: Cancelamento valido

- **GIVEN** uma movimentacao ativa que nao e ajuste interno
- **WHEN** o investidor cancela a movimentacao
- **THEN** o sistema marca a movimentacao como cancelada
- **AND** recalcula a carteira sem essa movimentacao

#### Scenario: Movimentacao ja cancelada

- **GIVEN** uma movimentacao com `cancelada = true`
- **WHEN** o investidor tenta cancelar novamente
- **THEN** o sistema rejeita a requisicao

#### Scenario: Ajuste interno

- **GIVEN** uma movimentacao `AJUSTE_SALDO` ou `AJUSTE_POSICAO`
- **WHEN** o investidor tenta cancelar
- **THEN** o sistema rejeita a requisicao

### Requirement: Preservar dados legados

The system SHALL criar ajustes internos quando uma carteira sem historico possui saldo ou posicoes preexistentes.

#### Scenario: Primeiro lancamento com legado

- **GIVEN** uma carteira sem movimentacoes, com saldo em conta ou posicoes ja preenchidas
- **WHEN** o investidor registra a primeira movimentacao externa
- **THEN** o sistema cria ajustes internos para saldo e posicoes existentes
- **AND** aplica o novo lancamento preservando o patrimonio anterior

### Requirement: Registrar aporte rapido

The system SHALL converter aporte rapido da carteira principal em movimentacao `APORTE`.

#### Scenario: Aporte principal

- **GIVEN** um usuario `USER` autenticado e valor positivo
- **WHEN** ele envia `POST /carteiras/principal/aportes`
- **THEN** o sistema registra `APORTE` com data atual e observacao `Aporte em conta`
- **AND** recalcula a carteira principal

### Requirement: Representar ajuste administrativo de saldo

The system SHALL registrar diferencas de saldo administrativo como aporte ou retirada.

#### Scenario: Aumento administrativo de saldo

- **GIVEN** saldo informado maior que o saldo atual
- **WHEN** o administrador atualiza `saldoEmConta`
- **THEN** o sistema registra `APORTE` com a diferenca

#### Scenario: Reducao administrativa de saldo

- **GIVEN** saldo informado menor que o saldo atual e maior ou igual a zero
- **WHEN** o administrador atualiza `saldoEmConta`
- **THEN** o sistema registra `RETIRADA` com a diferenca absoluta

#### Scenario: Saldo sem diferenca

- **GIVEN** saldo informado igual ao saldo atual
- **WHEN** o administrador atualiza `saldoEmConta`
- **THEN** o sistema retorna a carteira sem registrar nova movimentacao
