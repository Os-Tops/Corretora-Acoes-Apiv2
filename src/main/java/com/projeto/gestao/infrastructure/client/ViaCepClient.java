package com.projeto.gestao.infrastructure.client;

import com.projeto.gestao.infrastructure.client.dto.ViaCepDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "viaCepClient", url = "https://viacep.com.br/ws")
public interface ViaCepClient {

    @GetMapping("/{cep}/json")
    ViaCepDto consultarCep(@PathVariable("cep") String cep);
}
