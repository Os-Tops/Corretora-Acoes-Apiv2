# Gerenciamento de carteiras Specification

## Objetivo

Permitir que investidores mantenham uma ou mais carteiras, acompanhem saldos e consultem posicoes calculadas a partir do historico de movimentacoes.

## Regras de negocio

- Somente usuarios `USER` podem criar carteiras e acessar operacoes detalhadas de carteira.
- Cada usuario acessa apenas carteiras vinculadas ao proprio ID.
- O cadastro de usuario cria uma carteira inicial zerada.
- Caso um usuario nao possua carteira principal ao consultar, o sistema cria uma carteira principal automaticamente.
- A primeira carteira criada para um investidor deve ser marcada como principal quando ainda nao houver outras carteiras.
- Carteiras adicionais devem iniciar sem saldo e sem posicoes.
- O nome da carteira e obrigatorio e deve ser normalizado com `trim`.
- A listagem de carteiras deve ordenar a principal primeiro e depois por nome ascendente.
- `saldoAcao` representa valor de mercado das posicoes abertas.
- `saldoEmConta` representa saldo financeiro disponivel.
- Os saldos consolidados devem ser calculados somando as carteiras do usuario.
- Posicoes listadas devem ter quantidade positiva.
- Investidores podem registrar aporte rapido na carteira principal por rota dedicada.
- Administradores podem solicitar recalculo da carteira principal autenticada.
- Administradores podem ajustar saldo em conta de uma carteira autenticada por diferenca, gerando aporte ou retirada administrativa.
- Ajuste administrativo de saldo nao aceita valor negativo nem valor nao numerico.

## Fluxos

- Criacao de carteira:
  1. O investidor informa o nome da carteira.
  2. A API valida o perfil e a posse do usuario.
  3. A API cria a carteira zerada.
  4. A API marca como principal apenas quando for a primeira carteira do usuario.

- Consulta de carteira principal:
  1. O usuario autenticado solicita `/carteiras/principal`.
  2. A API busca a primeira carteira pela prioridade `principal desc, id asc`.
  3. Se nao houver carteira, cria `Carteira Principal` zerada.

- Consulta de posicoes:
  1. O investidor seleciona uma carteira.
  2. A API valida que a carteira pertence ao usuario.
  3. A API retorna posicoes com quantidade positiva.

- Aporte rapido:
  1. O investidor informa um valor de aporte para a carteira principal.
  2. A API registra uma movimentacao `APORTE`.
  3. A API recalcula e retorna a carteira principal atualizada.

- Ajuste administrativo de saldo:
  1. O administrador informa `saldoEmConta` para uma carteira acessivel ao usuario autenticado.
  2. A API valida que o saldo e numerico e maior ou igual a zero.
  3. A API calcula a diferenca entre o saldo informado e o saldo atual.
  4. Se houver diferenca, registra `APORTE` ou `RETIRADA` com observacao administrativa.

## Requisitos funcionais

- RF-CAR-001: O sistema deve listar carteiras do usuario por `GET /carteiras`.
- RF-CAR-002: O sistema deve criar carteira por `POST /carteiras`.
- RF-CAR-003: O sistema deve consultar a carteira principal por `GET /carteiras/principal`.
- RF-CAR-004: O sistema deve consultar carteira por `GET /carteiras/{id}`.
- RF-CAR-005: O sistema deve listar posicoes por `GET /carteiras/{id}/posicoes`.
- RF-CAR-006: O sistema deve calcular saldos consolidados para o dashboard.
- RF-CAR-007: O sistema deve criar carteira inicial zerada para novo usuario.
- RF-CAR-008: O sistema deve registrar aporte rapido por `POST /carteiras/principal/aportes`.
- RF-CAR-009: O sistema deve recalcular carteira principal por `PUT /carteiras/principal/recalcular`.
- RF-CAR-010: O sistema deve ajustar saldo em conta por `PUT /carteiras/{id}/saldo-conta`.

## Requisitos nao funcionais

- RNF-CAR-001: Todas as operacoes de escrita em carteira devem ser transacionais.
- RNF-CAR-002: Valores financeiros e quantidades devem usar precisao decimal.
- RNF-CAR-003: A API deve impedir acesso a carteira de outro usuario por filtro de usuario no repositorio.
- RNF-CAR-004: Respostas de erro devem seguir o padrao centralizado da API.
- RNF-CAR-005: O frontend deve esconder a gestao de carteiras de usuarios que nao sejam investidores.

## Comportamento esperado

- O investidor ve somente as proprias carteiras.
- Um administrador autenticado ve aviso no frontend de que a gestao de carteiras pertence aos investidores.
- Carteiras recem-criadas nao possuem saldo inicial.
- A carteira principal fica disponivel mesmo para usuarios antigos sem carteira associada.

## Requirements

### Requirement: Criar carteira

The system SHALL permitir que investidores criem carteiras com nome obrigatorio.

#### Scenario: Criacao valida

- **GIVEN** um usuario `USER` autenticado e um nome de carteira preenchido
- **WHEN** ele envia `POST /carteiras`
- **THEN** o sistema cria uma carteira vinculada ao usuario
- **AND** define `saldoAcao = 0` e `saldoEmConta = 0`

#### Scenario: Nome ausente

- **GIVEN** um usuario `USER` autenticado
- **WHEN** ele tenta criar carteira com nome vazio
- **THEN** o sistema rejeita a requisicao

### Requirement: Garantir carteira principal

The system SHALL retornar uma carteira principal para o usuario autenticado.

#### Scenario: Usuario possui carteira

- **GIVEN** um usuario autenticado com carteiras existentes
- **WHEN** ele consulta `/carteiras/principal`
- **THEN** o sistema retorna a carteira priorizada por `principal desc, id asc`

#### Scenario: Usuario sem carteira

- **GIVEN** um usuario autenticado sem carteiras
- **WHEN** ele consulta `/carteiras/principal`
- **THEN** o sistema cria e retorna `Carteira Principal` zerada

### Requirement: Restringir acesso por titularidade

The system SHALL permitir acesso apenas a carteiras do usuario autenticado.

#### Scenario: Consulta de carteira propria

- **GIVEN** um usuario autenticado e uma carteira vinculada ao seu ID
- **WHEN** ele consulta `GET /carteiras/{id}`
- **THEN** o sistema retorna a carteira

#### Scenario: Consulta de carteira inexistente para o usuario

- **GIVEN** um usuario autenticado e um ID que nao pertence a ele
- **WHEN** ele consulta `GET /carteiras/{id}`
- **THEN** o sistema retorna `404 Not Found`

### Requirement: Listar posicoes abertas

The system SHALL listar somente posicoes com quantidade positiva.

#### Scenario: Carteira com posicoes

- **GIVEN** uma carteira com posicoes recalculadas
- **WHEN** o investidor acessa `GET /carteiras/{id}/posicoes`
- **THEN** o sistema retorna apenas posicoes com `quantidadeTotal > 0`

### Requirement: Registrar aporte rapido

The system SHALL permitir aporte rapido na carteira principal do investidor.

#### Scenario: Aporte rapido valido

- **GIVEN** um usuario `USER` autenticado e valor positivo
- **WHEN** ele acessa `POST /carteiras/principal/aportes`
- **THEN** o sistema registra uma movimentacao `APORTE`
- **AND** retorna a carteira principal recalculada

### Requirement: Ajustar saldo administrativamente

The system SHALL permitir que administradores ajustem saldo em conta por diferenca.

#### Scenario: Saldo maior que o atual

- **GIVEN** um usuario `ADMIN` autenticado e uma carteira acessivel
- **WHEN** ele informa `saldoEmConta` maior que o saldo atual em `PUT /carteiras/{id}/saldo-conta`
- **THEN** o sistema registra uma movimentacao `APORTE` com a diferenca
- **AND** retorna a carteira recalculada

#### Scenario: Saldo menor que o atual

- **GIVEN** um usuario `ADMIN` autenticado e uma carteira acessivel
- **WHEN** ele informa `saldoEmConta` menor que o saldo atual e maior ou igual a zero
- **THEN** o sistema registra uma movimentacao `RETIRADA` com a diferenca absoluta
- **AND** retorna a carteira recalculada

#### Scenario: Saldo invalido

- **GIVEN** um valor ausente, nao numerico ou negativo
- **WHEN** o administrador tenta ajustar `saldoEmConta`
- **THEN** o sistema rejeita a requisicao

### Requirement: Recalcular carteira principal

The system SHALL permitir que administradores acionem recalc da carteira principal autenticada.

#### Scenario: Recalc administrativo

- **GIVEN** um usuario `ADMIN` autenticado
- **WHEN** ele acessa `PUT /carteiras/principal/recalcular`
- **THEN** o sistema recalcula a carteira principal a partir do historico ativo
- **AND** retorna a carteira atualizada
