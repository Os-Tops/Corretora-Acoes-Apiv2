# Dashboard financeiro Specification

## Objetivo

Apresentar uma visao consolidada do patrimonio do usuario autenticado, com indicadores de carteira, posicoes abertas, corretoras cadastradas e resultado nao realizado.

## Regras de negocio

- O dashboard exige usuario autenticado.
- A contagem de corretoras considera todas as corretoras cadastradas no sistema.
- A contagem de posicoes abertas soma as posicoes positivas de todas as carteiras do usuario autenticado.
- A contagem de carteiras considera somente carteiras do usuario autenticado.
- `saldoAcao` consolidado soma o valor de mercado das posicoes das carteiras do usuario.
- `saldoEmConta` consolidado soma o caixa disponivel das carteiras do usuario.
- `saldoTotal` e a soma de `saldoAcao` e `saldoEmConta`.
- `custoInvestido` consolidado soma `quantidadeTotal * precoMedio` das posicoes abertas.
- `resultadoNaoRealizado` e `saldoAcao - custoInvestido`.
- A rota de acoes do dashboard retorna posicoes da carteira principal e a lista de carteiras do usuario.
- A rota de carteiras do dashboard retorna carteiras do usuario autenticado.

## Fluxos

- Carregamento do dashboard:
  1. O frontend chama `GET /api/dashboard/stats` com token Bearer.
  2. A API calcula saldos e contagens do usuario autenticado.
  3. O frontend exibe cards de patrimonio, resultado nao realizado, corretoras e posicoes abertas.

- Navegacao:
  1. O usuario clica nos cards do dashboard.
  2. O frontend navega para carteiras, corretoras ou acoes.

- Consultas auxiliares:
  1. Rotas `/api/dashboard/corretoras`, `/api/dashboard/acoes` e `/api/dashboard/carteiras` retornam dados agregados ou listas usadas pelas telas.

## Requisitos funcionais

- RF-DAS-001: O sistema deve retornar estatisticas por `GET /api/dashboard/stats`.
- RF-DAS-002: O sistema deve retornar corretoras por `GET /api/dashboard/corretoras`.
- RF-DAS-003: O sistema deve retornar posicoes e carteiras por `GET /api/dashboard/acoes`.
- RF-DAS-004: O sistema deve retornar carteiras por `GET /api/dashboard/carteiras`.
- RF-DAS-005: O frontend deve exibir patrimonio consolidado.
- RF-DAS-006: O frontend deve exibir resultado nao realizado.
- RF-DAS-007: O frontend deve exibir quantidade de corretoras cadastradas.
- RF-DAS-008: O frontend deve exibir quantidade de posicoes abertas.

## Requisitos nao funcionais

- RNF-DAS-001: Os calculos devem usar valores consolidados das carteiras do usuario autenticado.
- RNF-DAS-002: Valores monetarios devem ser formatados no frontend em `pt-BR` e moeda BRL para os cards consolidados.
- RNF-DAS-003: Rotas do dashboard devem rejeitar requisicoes sem token valido.
- RNF-DAS-004: O resultado negativo deve ser visualmente diferenciado no frontend.
- RNF-DAS-005: O dashboard nao deve expor carteiras de outros usuarios.

## Comportamento esperado

- O dashboard mostra os indicadores atualizados conforme saldos e posicoes recalculados.
- Cards do dashboard funcionam como atalhos para as telas operacionais.
- Um usuario sem sessao e redirecionado pelo frontend para `/login`.
- Resultados negativos aparecem com classe visual de destaque negativo.

## Requirements

### Requirement: Expor estatisticas consolidadas

The system SHALL retornar indicadores consolidados do usuario autenticado.

#### Scenario: Estatisticas validas

- **GIVEN** um usuario autenticado com carteiras, saldo e posicoes
- **WHEN** o frontend solicita `GET /api/dashboard/stats`
- **THEN** o sistema retorna `corretorasCount`, `acoesCount`, `carteirasCount`, `saldoAcao`, `saldoEmConta`, `saldoTotal`, `custoInvestido` e `resultadoNaoRealizado`

#### Scenario: Usuario sem token

- **GIVEN** uma requisicao sem Bearer token valido
- **WHEN** ela acessa `GET /api/dashboard/stats`
- **THEN** o sistema retorna `401 Unauthorized`

### Requirement: Calcular resultado nao realizado

The system SHALL calcular resultado nao realizado como valor de mercado menos custo investido.

#### Scenario: Resultado positivo ou negativo

- **GIVEN** posicoes abertas com cotacao atual e preco medio
- **WHEN** o dashboard calcula os indicadores
- **THEN** `custoInvestido` e a soma de `quantidadeTotal * precoMedio`
- **AND** `resultadoNaoRealizado` e `saldoAcao - custoInvestido`

### Requirement: Renderizar cards do dashboard

The frontend SHALL apresentar os indicadores principais em cards navegaveis.

#### Scenario: Dashboard carregado

- **GIVEN** a API retorna estatisticas
- **WHEN** a pagina inicial e renderizada
- **THEN** o frontend mostra patrimonio consolidado, resultado nao realizado, corretoras cadastradas e posicoes abertas
- **AND** cada card direciona para a tela correspondente

