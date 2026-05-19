package com.projeto.gestao.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class AcaoDTO {

    public interface Create {
    }

    public interface Update {
    }

    @Null(groups = AcaoDTO.Create.class, message = "Id deve ser omitido na criação")
    @NotNull(groups = AcaoDTO.Update.class, message = "Id é obrigatório na atualização")
    private UUID id;

    @NotBlank(message = "Ticker da acao é obrigatório")
    @Size(max = 150, message = "Ticker da acao deve ter no máximo 150 caracteres")
    private String ticker;

    @NotBlank(message = "Empresa da acao é obrigatório")
    @Size(max = 150, message = "Empresa da acao deve ter no máximo 150 caracteres")
    private String nomeEmpresa;

    @Min(value = 0, message = "Mercado inválido: use 0 (BR) ou 1 (US)")
    @Max(value = 1, message = "Mercado inválido: use 0 (BR) ou 1 (US)")
    private Integer mercado;

    @Min(value = 0, message = "Moeda inválida: use 0 (BR) ou 1 (USD)")
    @Max(value = 1, message = "Moeda inválida: use 0 (BR) ou 1 (USD)")
    private Integer moeda;

    @Digits(integer = 12, fraction = 3, message = "Posição deve ter no máximo 12 inteiros e 3 decimais")
    @PositiveOrZero(message = "Posição não pode ser negativo")
    private BigDecimal posicao;

    @Digits(integer = 12, fraction = 3, message = "quantidadeCompra deve ter no máximo 12 inteiros e 3 decimais")
    @PositiveOrZero(message = "quantidadeCompra não pode ser negativo")
    private BigDecimal quantidadeCompra;

    @Digits(integer = 12, fraction = 3, message = "quantidadeTotal deve ter no máximo 12 inteiros e 3 decimais")
    @PositiveOrZero(message = "quantidadeTotal não pode ser negativo")
    private BigDecimal quantidadeTotal;

    @Digits(integer = 12, fraction = 3, message = "cotacaoAtual deve ter no máximo 12 inteiros e 3 decimais")
    @PositiveOrZero(message = "cotacaoAtual não pode ser negativo")
    private BigDecimal cotacaoAtual;

    @Digits(integer = 12, fraction = 3, message = "precoMedio deve ter no máximo 12 inteiros e 3 decimais")
    @PositiveOrZero(message = "precoMedio não pode ser negativo")
    private BigDecimal precoMedio;

    @Column(nullable = false)
    private LocalDateTime dataHoraCotacao = LocalDateTime.now();

    @NotNull(message = "Corretora é obrigatório")
    private Integer corretoraId;

    public AcaoDTO() {
    }

    public AcaoDTO(UUID id, String ticker, String nomeEmpresa, Integer mercado,
                   Integer moeda, BigDecimal posicao, BigDecimal quantidadeCompra,
                   BigDecimal quantidadeTotal, BigDecimal cotacaoAtual, BigDecimal precoMedio,
                   LocalDateTime dataHoraCotacao, Integer corretoraId) {
        this.id = id;
        this.ticker = ticker;
        this.nomeEmpresa = nomeEmpresa;
        this.mercado = mercado;
        this.moeda = moeda;
        this.posicao = posicao;
        this.quantidadeCompra = quantidadeCompra;
        this.quantidadeTotal = quantidadeTotal;
        this.cotacaoAtual = cotacaoAtual;
        this.precoMedio = precoMedio;
        this.dataHoraCotacao = dataHoraCotacao;
        this.corretoraId = corretoraId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getNomeEmpresa() {
        return nomeEmpresa;
    }

    public void setNomeEmpresa(String nomeEmpresa) {
        this.nomeEmpresa = nomeEmpresa;
    }

    public Integer getMercado() {
        return mercado;
    }

    public void setMercado(Integer mercado) {
        this.mercado = mercado;
    }

    public Integer getMoeda() {
        return moeda;
    }

    public void setMoeda(Integer moeda) {
        this.moeda = moeda;
    }

    public BigDecimal getPosicao() {
        return posicao;
    }

    public void setPosicao(BigDecimal posicao) {
        this.posicao = posicao;
    }

    public BigDecimal getQuantidadeCompra() {
        return quantidadeCompra;
    }

    public void setQuantidadeCompra(BigDecimal quantidadeCompra) {
        this.quantidadeCompra = quantidadeCompra;
    }

    public BigDecimal getQuantidadeTotal() {
        return quantidadeTotal;
    }

    public void setQuantidadeTotal(BigDecimal quantidadeTotal) {
        this.quantidadeTotal = quantidadeTotal;
    }

    public BigDecimal getCotacaoAtual() {
        return cotacaoAtual;
    }

    public void setCotacaoAtual(BigDecimal cotacaoAtual) {
        this.cotacaoAtual = cotacaoAtual;
    }

    public BigDecimal getPrecoMedio() {
        return precoMedio;
    }

    public void setPrecoMedio(BigDecimal precoMedio) {
        this.precoMedio = precoMedio;
    }

    public LocalDateTime getDataHoraCotacao() {
        return dataHoraCotacao;
    }

    public void setDataHoraCotacao(LocalDateTime dataHoraCotacao) {
        this.dataHoraCotacao = dataHoraCotacao;
    }

    public Integer getCorretoraId() {
        return corretoraId;
    }

    public void setCorretoraId(Integer corretoraId) {
        this.corretoraId = corretoraId;
    }
}