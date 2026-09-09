package com.projeto.gestao.infra.client;

import com.projeto.gestao.infra.client.dto.BrasilApiCnpjDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "brasilApiClient", url = "https://brasilapi.com.br/api/cnpj/v1")
public interface BrasilApiClient {

    @GetMapping("/{cnpj}")
    BrasilApiCnpjDto consultarCnpj(@PathVariable("cnpj") String cnpj);
}
