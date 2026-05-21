package com.projeto.gestao.api.controller;

import com.projeto.gestao.domain.model.Acao;
import com.projeto.gestao.service.AcaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/acoes")
public class AcaoController {

    private final AcaoService acaoService;

    public AcaoController(AcaoService acaoService) {
        this.acaoService = acaoService;
    }

    @PostMapping
    public ResponseEntity<Acao> cadastrarAcao(@RequestBody Map<String, Object> payload) {
        String ticker = getAsText(payload, "ticker");
        String mercado = getAsText(payload, "mercado");
        String quantidadeCompraText = getAsText(payload, "quantidadeCompra");
        String corretoraIdText = getAsText(payload, "corretoraId");

        if (isBlank(ticker) || isBlank(mercado) || isBlank(quantidadeCompraText) || isBlank(corretoraIdText)) {
            throw new IllegalArgumentException("Ticker, Mercado, Quantidade e Corretora sao obrigatorios");
        }

        Double quantidadeCompra = parseQuantidade(quantidadeCompraText);
        UUID corretoraId = parseCorretoraId(corretoraIdText);
        Acao acao = acaoService.cadastrarAcao(ticker, mercado, quantidadeCompra, corretoraId);
        return ResponseEntity.status(HttpStatus.CREATED).body(acao);
    }

    @PostMapping("/{ticker}")
    public ResponseEntity<Acao> venderAcao(@PathVariable String ticker, @RequestBody Map<String, Object> payload) {
        Double quantidadeVenda = parseQuantidade(getAsText(payload, "quantidadeVenda"));
        Acao acao = acaoService.venderAcao(ticker, quantidadeVenda);
        return ResponseEntity.status(HttpStatus.CREATED).body(acao);
    }

    @GetMapping
    public ResponseEntity<List<Acao>> listarAcoes() {
        return ResponseEntity.ok(acaoService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Acao> buscarPorId(@PathVariable UUID id) {
        return acaoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ticker/{ticker}")
    public ResponseEntity<Acao> buscarPorTicker(@PathVariable String ticker) {
        return acaoService.buscarPorTicker(ticker)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/atualizar-cotacao")
    public ResponseEntity<Acao> atualizarCotacao(@PathVariable UUID id) {
        Acao acao = acaoService.atualizarCotacao(id);
        return ResponseEntity.ok(acao);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Acao> adicionarAcao(@PathVariable UUID id, @RequestBody Map<String, Object> payload) {
        Double quantidadeCompra = parseQuantidade(getAsText(payload, "quantidadeCompra"));
        Acao acao = acaoService.adicionarAcao(id, quantidadeCompra);
        return ResponseEntity.ok(acao);
    }

    private String getAsText(Map<String, Object> payload, String field) {
        Object value = payload.get(field);
        return value == null ? null : value.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Double parseQuantidade(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException | NullPointerException ex) {
            throw new IllegalArgumentException("Quantidade deve ser um numero valido");
        }
    }

    private UUID parseCorretoraId(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException("Corretora informada e invalida");
        }
    }
}
