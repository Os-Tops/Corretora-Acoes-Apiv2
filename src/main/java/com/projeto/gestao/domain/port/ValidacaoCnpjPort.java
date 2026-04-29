package com.projeto.gestao.domain.port;

public interface ValidacaoCnpjPort {
    boolean isValidAndMarketParticipant(String cnpj);
    CnpjInfo getCnpjInfo(String cnpj);

    record CnpjInfo(String razaoSocial, String nomeFantasia, String cnaePrincipalDescricao, String cep, String email, String telefone, String logradouro, String numero, String complemento, String bairro, String municipio, String uf, String situacaoCadastral) {}
}
