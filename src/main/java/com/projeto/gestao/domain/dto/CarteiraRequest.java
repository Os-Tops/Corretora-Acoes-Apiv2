package com.projeto.gestao.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CarteiraRequest(
        @NotBlank(message = "Nome da carteira e obrigatorio.")
        @Size(max = 80, message = "Nome da carteira deve ter no maximo 80 caracteres.")
        String nome
) {
}
