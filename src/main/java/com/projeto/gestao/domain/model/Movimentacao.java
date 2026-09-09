package com.projeto.gestao.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "movimentacoes")
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "carteira_id", nullable = false)
    private Carteira carteira;

    @ManyToOne
    @JoinColumn(name = "acao_id")
    private Acao acao;

    @ManyToOne
    @JoinColumn(name = "conta_corretora_id")
    private ContaCorretora contaCorretora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimentacao tipo;

    @Column(nullable = false)
    private LocalDate dataOperacao;

    private BigDecimal quantidade;

    private BigDecimal precoUnitario;

    @Column(nullable = false)
    private BigDecimal valor = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal taxas = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal impostos = BigDecimal.ZERO;

    @Column(length = 500)
    private String observacao;

    @Column(nullable = false)
    private boolean cancelada = false;

    private LocalDateTime canceladaEm;

    @Column(nullable = false)
    private LocalDateTime registradaEm;

    @PrePersist
    void aoCriar() {
        if (registradaEm == null) {
            registradaEm = LocalDateTime.now();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Carteira getCarteira() { return carteira; }
    public void setCarteira(Carteira carteira) { this.carteira = carteira; }

    public Acao getAcao() { return acao; }
    public void setAcao(Acao acao) { this.acao = acao; }

    public ContaCorretora getContaCorretora() { return contaCorretora; }
    public void setContaCorretora(ContaCorretora contaCorretora) { this.contaCorretora = contaCorretora; }

    public TipoMovimentacao getTipo() { return tipo; }
    public void setTipo(TipoMovimentacao tipo) { this.tipo = tipo; }

    public LocalDate getDataOperacao() { return dataOperacao; }
    public void setDataOperacao(LocalDate dataOperacao) { this.dataOperacao = dataOperacao; }

    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }

    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor == null ? BigDecimal.ZERO : valor; }

    public BigDecimal getTaxas() { return taxas; }
    public void setTaxas(BigDecimal taxas) { this.taxas = taxas == null ? BigDecimal.ZERO : taxas; }

    public BigDecimal getImpostos() { return impostos; }
    public void setImpostos(BigDecimal impostos) { this.impostos = impostos == null ? BigDecimal.ZERO : impostos; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public boolean isCancelada() { return cancelada; }
    public void setCancelada(boolean cancelada) { this.cancelada = cancelada; }

    public LocalDateTime getCanceladaEm() { return canceladaEm; }
    public void setCanceladaEm(LocalDateTime canceladaEm) { this.canceladaEm = canceladaEm; }

    public LocalDateTime getRegistradaEm() { return registradaEm; }
    public void setRegistradaEm(LocalDateTime registradaEm) { this.registradaEm = registradaEm; }
}
