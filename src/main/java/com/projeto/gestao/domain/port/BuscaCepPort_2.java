package com.projeto.gestao.domain.port;

public interface BuscaCepPort {
    CepInfo getCepInfo(String cep);

    record CepInfo(String cep, String logradouro, String complemento, String bairro, String localidade, String uf) {}
}
