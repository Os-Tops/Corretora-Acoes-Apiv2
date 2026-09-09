package com.projeto.gestao.domain.port;

import java.math.BigDecimal;

public interface CotacaoAcaoPort {
    CotacaoInfo getCotacao(String ticker, String mercado);

    record CotacaoInfo(String ticker, String nomeEmpresa, String moeda, BigDecimal cotacaoAtual) {}
}
