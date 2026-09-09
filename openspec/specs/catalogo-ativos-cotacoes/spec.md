# Catalogo de ativos e cotacoes Specification

## Objetivo

Manter um catalogo global de acoes com ticker unico, corretora relacionada validada e cotacao atual obtida de provedores externos.

## Regras de negocio

- Apenas usuarios `ADMIN` podem cadastrar ativos no catalogo.
- Apenas usuarios `ADMIN` podem atualizar cotacao de ativos.
- Usuarios autenticados podem listar e consultar ativos cadastrados.
- O cadastro exige ticker, mercado e corretora.
- Ticker e mercado devem ser normalizados para letras maiusculas.
- Ticker deve ser unico no catalogo global.
- A corretora relacionada deve existir e possuir `validadaNaCvm = true`.
- A cotacao deve ser consultada no cadastro e deve conter valor atual.
- Mercado `BR` usa brapi; mercado `US` usa Alpha Vantage.
- Mercado diferente de `BR` ou `US` deve ser rejeitado.
- A consulta de mercado `US` exige chave `ALPHAVANTAGE_API_KEY` configurada.
- A moeda e o nome da empresa devem vir da integracao quando disponiveis.
- A data/hora da cotacao deve ser atualizada no cadastro e na atualizacao manual.

## Fluxos

- Cadastro de ativo:
  1. O administrador seleciona uma corretora validada, informa ticker e mercado.
  2. A API normaliza ticker e mercado.
  3. A API valida unicidade do ticker e status da corretora.
  4. A API consulta a cotacao externa adequada ao mercado.
  5. A API salva o ativo com cotacao, moeda, nome e timestamp.

- Atualizacao de cotacao:
  1. O administrador solicita `PUT /acoes/{id}/atualizar-cotacao`.
  2. A API busca o ativo por ID.
  3. A API consulta novamente o provedor do mercado do ativo.
  4. A API atualiza cotacao, moeda, nome quando informado e timestamp.

- Consulta:
  1. O usuario autenticado lista todos os ativos ou busca por ID/ticker.
  2. A API retorna os dados do catalogo e resumo da corretora vinculada.

## Requisitos funcionais

- RF-ATI-001: O sistema deve cadastrar ativo por `POST /acoes`.
- RF-ATI-002: O sistema deve listar ativos por `GET /acoes`.
- RF-ATI-003: O sistema deve buscar ativo por `GET /acoes/{id}`.
- RF-ATI-004: O sistema deve buscar ativo por `GET /acoes/ticker/{ticker}`.
- RF-ATI-005: O sistema deve atualizar cotacao por `PUT /acoes/{id}/atualizar-cotacao`.
- RF-ATI-006: O sistema deve consultar brapi para ativos `BR`.
- RF-ATI-007: O sistema deve consultar Alpha Vantage para ativos `US`.
- RF-ATI-008: O sistema deve retornar resumo da corretora relacionada no contrato de ativo.

## Requisitos nao funcionais

- RNF-ATI-001: O token da brapi e a chave da Alpha Vantage devem ser configurados por variaveis de ambiente.
- RNF-ATI-002: Falhas, limites e credenciais invalidas de APIs externas devem ser representadas em erro JSON padronizado.
- RNF-ATI-003: O cadastro deve ser transacional para evitar ativo sem cotacao inicial.
- RNF-ATI-004: A unicidade de ticker deve ser reforcada por regra de banco de dados.
- RNF-ATI-005: Valores financeiros devem preservar precisao decimal.

## Comportamento esperado

- Ativos cadastrados aparecem no catalogo global para administradores e investidores autenticados.
- Ativos nao sao cadastrados quando a cotacao externa nao retorna preco.
- Ativos nao sao cadastrados em corretoras nao validadas.
- O frontend de administrador mostra somente corretoras validadas como opcoes para cadastro de ativos.

## Requirements

### Requirement: Cadastrar ativo no catalogo

The system SHALL permitir que administradores criem ativos com ticker unico e cotacao inicial valida.

#### Scenario: Cadastro BR valido

- **GIVEN** um usuario `ADMIN`, uma corretora validada e um ticker `BR` inexistente
- **WHEN** a brapi retorna cotacao valida
- **THEN** o sistema salva o ativo com ticker em maiusculas
- **AND** grava mercado, moeda, cotacao atual, nome da empresa, corretora e data/hora da cotacao

#### Scenario: Cadastro US valido

- **GIVEN** um usuario `ADMIN`, uma corretora validada, mercado `US` e chave Alpha Vantage configurada
- **WHEN** a Alpha Vantage retorna cotacao valida
- **THEN** o sistema salva o ativo com moeda `USD` e cotacao atual

#### Scenario: Ticker duplicado

- **GIVEN** ja existe ativo com ticker normalizado
- **WHEN** o administrador tenta cadastrar o mesmo ticker
- **THEN** o sistema rejeita a requisicao
- **AND** nao consulta provedor de cotacao

#### Scenario: Corretora nao validada

- **GIVEN** uma corretora existente com `validadaNaCvm = false`
- **WHEN** o administrador tenta cadastrar ativo nela
- **THEN** o sistema rejeita a requisicao
- **AND** nao consulta provedor de cotacao

### Requirement: Atualizar cotacao

The system SHALL permitir que administradores atualizem a cotacao de ativos cadastrados.

#### Scenario: Atualizacao valida

- **GIVEN** um ativo existente
- **WHEN** o administrador solicita atualizacao de cotacao
- **THEN** o sistema consulta o provedor do mercado do ativo
- **AND** atualiza `cotacaoAtual`, `moeda`, `nomeEmpresa` quando informado e `dataHoraCotacao`

#### Scenario: Ativo inexistente

- **GIVEN** um ID de ativo inexistente
- **WHEN** o administrador solicita atualizacao
- **THEN** o sistema retorna erro de negocio informando que a acao nao foi encontrada

### Requirement: Consultar catalogo

The system SHALL permitir consulta de catalogo para usuarios autenticados.

#### Scenario: Listagem autenticada

- **GIVEN** um usuario autenticado
- **WHEN** ele acessa `GET /acoes`
- **THEN** o sistema retorna todos os ativos cadastrados

#### Scenario: Busca por ticker

- **GIVEN** um usuario autenticado e um ticker existente
- **WHEN** ele acessa `GET /acoes/ticker/{ticker}`
- **THEN** o sistema normaliza o ticker e retorna o ativo correspondente

