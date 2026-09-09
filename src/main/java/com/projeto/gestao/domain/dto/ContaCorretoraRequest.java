package com.projeto.gestao.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ContaCorretoraRequest(
        @NotNull(message = "Corretora e obrigatoria.")
        UUID corretoraId,
        @NotBlank(message = "Apelido da conta e obrigatorio.")
        @Size(max = 80, message = "Apelido deve ter no maximo 80 caracteres.")
        String apelido,
        @Size(max = 80, message = "Identificador deve ter no maximo 80 caracteres.")
        String identificadorConta
) {
}
