package com.projeto.gestao.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Carteira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome = "Carteira Principal";

    private boolean principal = true;

    private BigDecimal saldoAcao =  BigDecimal.ZERO;

    private BigDecimal saldoEmConta = BigDecimal.ZERO;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private LocalDateTime criadaEm;

    private LocalDateTime atualizadaEm;

    public Carteira() {
    }

    public Carteira(Long id, BigDecimal saldoAcao, BigDecimal saldoEmConta) {
        this.id = id;
        this.saldoAcao = saldoAcao;
        this.saldoEmConta = saldoEmConta;
    }

    @PrePersist
    void aoCriar() {
        LocalDateTime agora = LocalDateTime.now();
        criadaEm = agora;
        atualizadaEm = agora;
    }

    @PreUpdate
    void aoAtualizar() {
        atualizadaEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isPrincipal() {
        return principal;
    }

    public void setPrincipal(boolean principal) {
        this.principal = principal;
    }

    public BigDecimal getSaldoAcao() {
        return saldoAcao;
    }

    public void setSaldoAcao(BigDecimal saldoAcao) {
        this.saldoAcao = saldoAcao;
    }

    public BigDecimal getSaldoEmConta() {
        return saldoEmConta;
    }

    public void setSaldoEmConta(BigDecimal saldoEmConta) {
        this.saldoEmConta = saldoEmConta;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public LocalDateTime getAtualizadaEm() {
        return atualizadaEm;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Carteira carteira = (Carteira) o;
        return Objects.equals(saldoAcao, carteira.saldoAcao) && Objects.equals(saldoEmConta, carteira.saldoEmConta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(saldoAcao, saldoEmConta);
    }
}
