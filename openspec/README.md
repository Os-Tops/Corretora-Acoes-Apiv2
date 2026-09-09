# OpenSpec - Gestao de Acoes e Corretoras

Este diretorio documenta as funcionalidades existentes do sistema em formato de especificacao OpenSpec. As specs foram derivadas do backend Spring Boot, do frontend React e da documentacao atual do repositorio.

## Capacidades documentadas

- [Autenticacao de usuarios](specs/autenticacao-usuarios/spec.md)
- [Cadastro de corretoras](specs/cadastro-corretoras/spec.md)
- [Catalogo de ativos e cotacoes](specs/catalogo-ativos-cotacoes/spec.md)
- [Gerenciamento de carteiras](specs/gerenciamento-carteiras/spec.md)
- [Contas de corretora](specs/contas-corretora/spec.md)
- [Livro de movimentacoes](specs/livro-movimentacoes/spec.md)
- [Operacoes de compra e venda](specs/operacoes-compra-venda/spec.md)
- [Proventos, dividendos e JCP](specs/proventos-dividendos-jcp/spec.md)
- [Dashboard financeiro](specs/dashboard-financeiro/spec.md)

## Observacoes de escopo

- As specs representam funcionalidades ja presentes no sistema atual.
- Nao foram criadas specs separadas para relatorios ou graficos porque nao ha endpoints ou telas especificas para essas funcionalidades alem dos indicadores do dashboard.
- As regras de erro comuns usam o tratamento centralizado da API, com resposta JSON contendo `timestamp`, `status`, `error` e `message`.
- Rotas protegidas usam o cabecalho `Authorization: Bearer <token>`.

