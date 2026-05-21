package com.projeto.gestao.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "acoes")
public class Acao {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String ticker;

    private String nomeEmpresa;
    
    private String mercado; // BR, US

    private String moeda; // BRL, USD

    private BigDecimal posicao = BigDecimal.ZERO;

    private BigDecimal quantidadeCompra = BigDecimal.ZERO;

    private BigDecimal quantidadeTotal = BigDecimal.ZERO;

    private BigDecimal cotacaoAtual;

    private BigDecimal precoMedio = BigDecimal.ZERO;
    
    private LocalDateTime dataHoraCotacao;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "corretora_id", nullable = false)
    private Corretora corretoraRelacionada;

    public Acao() {
    }

    public Acao(BigDecimal quantidadeTotal) {
        if (quantidadeTotal == null) {
            this.quantidadeTotal = BigDecimal.ZERO;
        } else {
            this.quantidadeTotal = quantidadeTotal;
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public String getNomeEmpresa() { return nomeEmpresa; }
    public void setNomeEmpresa(String nomeEmpresa) { this.nomeEmpresa = nomeEmpresa; }

    public String getMercado() { return mercado; }
    public void setMercado(String mercado) { this.mercado = mercado; }

    public String getMoeda() { return moeda; }
    public void setMoeda(String moeda) { this.moeda = moeda; }

    public BigDecimal getPosicao() { return posicao; }
    public void setPosicao(BigDecimal posicao) { this.posicao = this.quantidadeTotal.multiply(this.cotacaoAtual);}

    public BigDecimal getQuantidadeCompra() { return quantidadeCompra; }
    public void setQuantidadeCompra(BigDecimal quantidadeCompra) { this.quantidadeCompra = quantidadeCompra; }

    public BigDecimal getQuantidadeTotal() { return quantidadeTotal; }
    public void setQuantidadeTotal(BigDecimal quantidadeTotal) {
        if (quantidadeTotal == null) {
            this.quantidadeTotal = BigDecimal.ZERO;
        } else {
            this.quantidadeTotal = quantidadeTotal;
        }
    }

    public BigDecimal getCotacaoAtual() { return cotacaoAtual; }
    public void setCotacaoAtual(BigDecimal cotacaoAtual) { this.cotacaoAtual = cotacaoAtual; }

    public BigDecimal getPrecoMedio() { return precoMedio; }
    public void setPrecoMedio(BigDecimal precoMedio) { this.precoMedio = precoMedio; }

    public LocalDateTime getDataHoraCotacao() { return dataHoraCotacao; }
    public void setDataHoraCotacao(LocalDateTime dataHoraCotacao) { this.dataHoraCotacao = dataHoraCotacao; }

    public Corretora getCorretoraRelacionada() { return corretoraRelacionada; }
    public void setCorretoraRelacionada(Corretora corretoraRelacionada) { this.corretoraRelacionada = corretoraRelacionada; }

    public void calcularPosicaoAtualizada() {
        if (this.quantidadeTotal != null && this.cotacaoAtual != null) {
            this.posicao = this.quantidadeTotal.multiply(this.cotacaoAtual);
        } else {
            this.posicao = BigDecimal.ZERO;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Acao acao = (Acao) o;
        return Objects.equals(id, acao.id) && Objects.equals(ticker, acao.ticker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ticker);
    }
}
