package com.projeto.gestao.domain.dto;

import com.projeto.gestao.domain.model.TipoMovimentacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MovimentacaoRequest(
        @NotNull(message = "Tipo da movimentacao e obrigatorio.")
        TipoMovimentacao tipo,
        UUID acaoId,
        UUID contaCorretoraId,
        @NotNull(message = "Data da operacao e obrigatoria.")
        LocalDate dataOperacao,
        BigDecimal quantidade,
        BigDecimal precoUnitario,
        BigDecimal valor,
        BigDecimal taxas,
        BigDecimal impostos,
        @Size(max = 500, message = "Observacao deve ter no maximo 500 caracteres.")
        String observacao
) {
}
