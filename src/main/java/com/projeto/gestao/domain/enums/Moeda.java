package com.projeto.gestao.domain.enums;

public enum Moeda {
    BR (0, "BR"), USD (1, "USD");
    private Integer id;
    private String descricao;

    Moeda(Integer id, String descricao) {
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

    public static Moeda toEnum(Integer id){
        if(id == null) return null;
        for(Moeda provimento : Moeda.values()){
            if(id.equals(provimento.getId())){
                return provimento;
            }
        }
        throw new IllegalArgumentException("Moeda inválida!");
    }

    public static Moeda fromString(String sigla) {
        for (Moeda m : Moeda.values()) {
            if (m.name().equalsIgnoreCase(sigla)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Moeda inválida: " + sigla);
    }
}
