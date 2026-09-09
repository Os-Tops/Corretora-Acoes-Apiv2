package com.projeto.gestao.domain.dto;

import com.projeto.gestao.domain.model.ContaCorretora;

import java.time.LocalDateTime;
import java.util.UUID;

public record ContaCorretoraResponse(
        UUID id,
        String apelido,
        String identificadorConta,
        boolean ativa,
        LocalDateTime criadaEm,
        AcaoResponse.CorretoraResumo corretora
) {
    public static ContaCorretoraResponse from(ContaCorretora conta) {
        return new ContaCorretoraResponse(
                conta.getId(), conta.getApelido(), conta.getIdentificadorConta(), conta.isAtiva(), conta.getCriadaEm(),
                AcaoResponse.corretoraResumo(conta.getCorretora())
        );
    }
}
