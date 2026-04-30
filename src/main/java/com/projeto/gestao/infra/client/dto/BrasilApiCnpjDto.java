package com.projeto.gestao.infra.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BrasilApiCnpjDto {
    private String cnpj;
    
    @JsonProperty("razao_social")
    private String razaoSocial;
    
    @JsonProperty("nome_fantasia")
    private String nomeFantasia;
    
    @JsonProperty("situacao_cadastral")
    private Integer situacaoCadastral;
    
    @JsonProperty("descricao_situacao_cadastral")
    private String descricaoSituacaoCadastral;
    
    private String cep;
    private String email;
    private String ddd_telefone_1;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String municipio;
    private String uf;
    
    private CnaePrincipal cnae_principal;

    public static class CnaePrincipal {
        private Integer codigo;
        private String descricao;
        
        public Integer getCodigo() { return codigo; }
        public void setCodigo(Integer codigo) { this.codigo = codigo; }
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
    }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }

    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }

    public Integer getSituacaoCadastral() { return situacaoCadastral; }
    public void setSituacaoCadastral(Integer situacaoCadastral) { this.situacaoCadastral = situacaoCadastral; }

    public String getDescricaoSituacaoCadastral() { return descricaoSituacaoCadastral; }
    public void setDescricaoSituacaoCadastral(String descricaoSituacaoCadastral) { this.descricaoSituacaoCadastral = descricaoSituacaoCadastral; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDdd_telefone_1() { return ddd_telefone_1; }
    public void setDdd_telefone_1(String ddd_telefone_1) { this.ddd_telefone_1 = ddd_telefone_1; }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public CnaePrincipal getCnae_principal() { return cnae_principal; }
    public void setCnae_principal(CnaePrincipal cnae_principal) { this.cnae_principal = cnae_principal; }
}
