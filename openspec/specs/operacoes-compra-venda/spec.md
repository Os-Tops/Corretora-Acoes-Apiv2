# Operacoes de compra e venda Specification

## Objetivo

Permitir que investidores comprem e vendam ativos usando atalhos da tela de negociacao ou lancamentos manuais, mantendo posicoes e saldo consistentes.

## Regras de negocio

- Somente usuarios `USER` podem comprar ou vender ativos.
- Compra rapida usa `PUT /acoes/{id}`.
- Venda rapida usa `POST /acoes/{ticker}`.
- Atalhos de compra e venda sempre atuam na carteira principal do investidor.
- Compra rapida atualiza a cotacao do ativo antes de registrar a movimentacao.
- Venda rapida exige que o investidor possua posicao aberta para o ticker informado.
- Venda rapida atualiza a cotacao do ativo antes de registrar a movimentacao.
- Atalhos registram movimentacoes com a cotacao atual, taxas zero, impostos zero e observacao automatica.
- Compras e vendas manuais podem ser registradas pelo livro de movimentacoes com preco unitario informado pelo usuario.
- Uma venda nao pode exceder a quantidade disponivel no momento do recalc.
- Compras aumentam quantidade e atualizam preco medio considerando valor, taxas e impostos.
- Vendas reduzem quantidade e preservam preco medio da posicao remanescente.
- Quando a quantidade chega a zero, a posicao e removida das posicoes abertas.

## Fluxos

- Compra rapida:
  1. O investidor escolhe um ativo do catalogo e informa quantidade.
  2. A API busca a carteira principal.
  3. A API atualiza a cotacao do ativo.
  4. A API registra uma movimentacao `COMPRA` com preco igual a cotacao atual.
  5. A API recalcula a carteira e retorna a posicao.

- Venda rapida:
  1. O investidor escolhe uma posicao aberta e informa quantidade.
  2. A API busca a posicao por ticker na carteira principal.
  3. A API atualiza a cotacao do ativo.
  4. A API registra uma movimentacao `VENDA` com preco igual a cotacao atual.
  5. A API recalcula a carteira e retorna a posicao restante ou posicao zerada.

- Compra/venda manual:
  1. O investidor abre o modulo de carteiras.
  2. O investidor seleciona tipo `COMPRA` ou `VENDA`, ativo, data, quantidade e preco unitario.
  3. A API registra no livro de movimentacoes e recalcula a carteira.

## Requisitos funcionais

- RF-NEG-001: O sistema deve comprar ativo por `PUT /acoes/{id}`.
- RF-NEG-002: O sistema deve vender ativo por `POST /acoes/{ticker}`.
- RF-NEG-003: O sistema deve listar posicoes do investidor por `GET /acoes/minhas`.
- RF-NEG-004: O sistema deve registrar compras e vendas manuais por `POST /carteiras/{id}/movimentacoes`.
- RF-NEG-005: O sistema deve atualizar cotacao antes de compra ou venda rapida.
- RF-NEG-006: O sistema deve impedir venda acima da quantidade disponivel.
- RF-NEG-007: O sistema deve retornar posicao com ticker, quantidade, preco medio, cotacao e valor de mercado.

## Requisitos nao funcionais

- RNF-NEG-001: Operacoes devem ser transacionais, garantindo registro e recalc atomicos.
- RNF-NEG-002: Calculos devem usar `BigDecimal`.
- RNF-NEG-003: Preco medio deve usar precisao de 8 casas e arredondamento `HALF_UP`.
- RNF-NEG-004: Falhas de cotacao externa nas operacoes rapidas devem impedir o registro da compra ou venda.
- RNF-NEG-005: Mensagens de erro devem seguir o padrao centralizado da API.

## Comportamento esperado

- A tela de acoes permite compra para investidores e cadastro de ativo para administradores.
- O modal de venda do frontend valida quantidade positiva e nao permite informar quantidade maior que a disponivel.
- O backend tambem valida venda acima da posicao durante o recalc.
- Apos compra ou venda, a carteira principal reflete o novo saldo e as novas posicoes.

## Requirements

### Requirement: Comprar ativo rapidamente

The system SHALL registrar compra na carteira principal usando a cotacao atual do ativo.

#### Scenario: Compra rapida valida

- **GIVEN** um usuario `USER`, uma carteira principal e um ativo cadastrado
- **WHEN** ele envia `PUT /acoes/{id}` com quantidade positiva
- **THEN** o sistema atualiza a cotacao do ativo
- **AND** registra movimentacao `COMPRA` na carteira principal
- **AND** recalcula e retorna a posicao comprada

#### Scenario: Compra sem saldo suficiente

- **GIVEN** uma compra cuja aplicacao deixaria o saldo em conta negativo
- **WHEN** o recalc da carteira e executado
- **THEN** o sistema rejeita a operacao

### Requirement: Vender ativo rapidamente

The system SHALL registrar venda na carteira principal usando a cotacao atual do ativo.

#### Scenario: Venda rapida valida

- **GIVEN** um usuario `USER` com posicao aberta para o ticker
- **WHEN** ele envia `POST /acoes/{ticker}` com quantidade positiva e disponivel
- **THEN** o sistema atualiza a cotacao do ativo
- **AND** registra movimentacao `VENDA`
- **AND** recalcula e retorna a posicao remanescente

#### Scenario: Ticker fora da carteira

- **GIVEN** um usuario `USER` sem posicao para o ticker
- **WHEN** ele tenta vender por `POST /acoes/{ticker}`
- **THEN** o sistema rejeita a requisicao informando que a acao nao foi encontrada na carteira

#### Scenario: Venda acima da posicao

- **GIVEN** uma posicao com quantidade menor que a venda solicitada
- **WHEN** o investidor tenta vender
- **THEN** o sistema rejeita a operacao

### Requirement: Calcular preco medio de compras

The system SHALL atualizar preco medio de posicao com base no custo acumulado de compras.

#### Scenario: Compra com encargos

- **GIVEN** uma compra com valor, taxas e impostos
- **WHEN** o sistema recalcula a posicao
- **THEN** o preco medio inclui valor da compra mais taxas e impostos dividido pela quantidade total

#### Scenario: Venda parcial

- **GIVEN** uma posicao existente
- **WHEN** uma venda parcial e registrada
- **THEN** a quantidade e reduzida
- **AND** o preco medio da posicao remanescente e mantido

