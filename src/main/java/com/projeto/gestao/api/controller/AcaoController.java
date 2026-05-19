package com.projeto.gestao.api.controller;

import com.projeto.gestao.domain.dto.AcaoDTO;
import com.projeto.gestao.domain.enums.Mercado;
import com.projeto.gestao.domain.model.Acao;
import com.projeto.gestao.domain.model.Corretora;
import com.projeto.gestao.service.AcaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
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
    public ResponseEntity<AcaoDTO> cadastrarAcao(@RequestBody @Valid AcaoDTO acaoDTO) {

        AcaoDTO created = acaoService.create(dto);

        // 2. Monta a URI de retorno (Header Location) apontando para o recurso criado
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}") // Adiciona o caminho do ID na URL (ex: /acoes/123e4567...)
                .buildAndExpand(created.getId())
                .toUri();

        // 3. Retorna o status 201 (Created) com a URI e o corpo da resposta
        return ResponseEntity.created(location).body(created);
    }

    @PostMapping("/{ticker}")
    public ResponseEntity<Acao> venderAcao(@PathVariable String ticker, @RequestBody Map<String, String> payload) {
        Double quantidadeVenda = Double.parseDouble(payload.get("quantidadeVenda"));
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

    @PutMapping
    public ResponseEntity<Acao> adicionarAcao(@PathVariable UUID id, Double quantidadeCompra) {
        Acao acao = acaoService.adicionarAcao(id, quantidadeCompra);
        return ResponseEntity.ok(acao);
    }
}
