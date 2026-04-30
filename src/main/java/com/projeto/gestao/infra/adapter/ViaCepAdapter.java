package com.projeto.gestao.infra.adapter;

import com.projeto.gestao.domain.port.BuscaCepPort;
import com.projeto.gestao.infra.client.ViaCepClient;
import com.projeto.gestao.infra.client.dto.ViaCepDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class ViaCepAdapter implements BuscaCepPort {

    private final ViaCepClient viaCepClient;

    public ViaCepAdapter(ViaCepClient viaCepClient) {
        this.viaCepClient = viaCepClient;
    }

    @Override
    @Cacheable(value = "cep", key = "#cep")
    public CepInfo getCepInfo(String cep) {
        try {
            ViaCepDto dto = viaCepClient.consultarCep(cep);
            if (dto == null || Boolean.TRUE.equals(dto.getErro())) return null;
            return new CepInfo(dto.getCep(), dto.getLogradouro(), dto.getComplemento(), dto.getBairro(), dto.getLocalidade(), dto.getUf());
        } catch (Exception e) {
            return null;
        }
    }
}
