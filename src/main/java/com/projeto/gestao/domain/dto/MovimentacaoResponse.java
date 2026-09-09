package com.projeto.gestao.domain.dto;

import com.projeto.gestao.domain.model.Movimentacao;
import com.projeto.gestao.domain.model.TipoMovimentacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record MovimentacaoResponse(
        UUID id,
        TipoMovimentacao tipo,
        LocalDate dataOperacao,
        UUID acaoId,
        String ticker,
        String nomeEmpresa,
        UUID contaCorretoraId,
        String contaCorretora,
        BigDecimal quantidade,
        BigDecimal precoUnitario,
        BigDecimal valor,
        BigDecimal taxas,
        BigDecimal impostos,
        String observacao,
        boolean cancelada,
        LocalDateTime registradaEm
) {
    public static MovimentacaoResponse from(Movimentacao movimentacao) {
        return new MovimentacaoResponse(
                movimentacao.getId(), movimentacao.getTipo(), movimentacao.getDataOperacao(),
                movimentacao.getAcao() == null ? null : movimentacao.getAcao().getId(),
                movimentacao.getAcao() == null ? null : movimentacao.getAcao().getTicker(),
                movimentacao.getAcao() == null ? null : movimentacao.getAcao().getNomeEmpresa(),
                movimentacao.getContaCorretora() == null ? null : movimentacao.getContaCorretora().getId(),
                movimentacao.getContaCorretora() == null ? null : movimentacao.getContaCorretora().getApelido(),
                movimentacao.getQuantidade(), movimentacao.getPrecoUnitario(), movimentacao.getValor(),
                movimentacao.getTaxas(), movimentacao.getImpostos(), movimentacao.getObservacao(),
                movimentacao.isCancelada(), movimentacao.getRegistradaEm()
        );
    }
}
