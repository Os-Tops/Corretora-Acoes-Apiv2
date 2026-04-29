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
    public ResponseEntity<Acao> cadastrarAcao(@RequestBody Map<String, String> payload) {
        String ticker = payload.get("ticker");
        String mercado = payload.get("mercado");
        if (ticker == null || mercado == null) {
            throw new IllegalArgumentException("Ticker e Mercado são obrigatórios");
        }
        Acao acao = acaoService.cadastrarAcao(ticker, mercado);
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
}
