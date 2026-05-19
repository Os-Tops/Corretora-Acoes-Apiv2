package com.projeto.gestao.domain.enums;

public enum Mercado {
    BR (0, "BR"), US (1, "US");
    private Integer id;
    private String descricao;

    Mercado(Integer id, String descricao) {
        this.id = id;
        this.descricao = descricao;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public static Mercado toEnum(Integer id){
        if(id == null) return null;
        for(Mercado provimento : Mercado.values()){
            if(id.equals(provimento.getId())){
                return provimento;
            }
        }
        throw new IllegalArgumentException("Mercado inválido!");
    }
}
