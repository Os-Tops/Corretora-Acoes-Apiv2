package com.projeto.gestao.api.controller;

import com.projeto.gestao.domain.dto.AporteRequest;
import com.projeto.gestao.domain.dto.CarteiraRequest;
import com.projeto.gestao.domain.dto.ContaCorretoraRequest;
import com.projeto.gestao.domain.dto.ContaCorretoraResponse;
import com.projeto.gestao.domain.dto.MovimentacaoRequest;
import com.projeto.gestao.domain.dto.MovimentacaoResponse;
import com.projeto.gestao.domain.dto.PosicaoAcaoResponse;
import com.projeto.gestao.domain.model.Carteira;
import com.projeto.gestao.domain.model.Usuario;
import com.projeto.gestao.service.AutenticacaoService;
import com.projeto.gestao.service.CarteiraService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/carteiras")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class CarteiraController {

    private final CarteiraService carteiraService;
    private final AutenticacaoService autenticacaoService;

    public CarteiraController(CarteiraService carteiraService, AutenticacaoService autenticacaoService) {
        this.carteiraService = carteiraService;
        this.autenticacaoService = autenticacaoService;
    }

    @GetMapping
    public ResponseEntity<List<Carteira>> listarCarteiras(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String autorizacao
    ) {
        return ResponseEntity.ok(carteiraService.listarTodas(usuarioAutenticado(autorizacao)));
    }

    @PostMapping
    public ResponseEntity<Carteira> criarCarteira(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String autorizacao,
            @Valid @RequestBody CarteiraRequest payload
    ) {
        Usuario usuario = investidorAutenticado(autorizacao);
        return ResponseEntity.status(201).body(carteiraService.criarCarteira(usuario, payload.nome()));
    }

    @GetMapping("/principal")
    public ResponseEntity<Carteira> buscarCarteiraPrincipal(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String autorizacao
    ) {
        return ResponseEntity.ok(carteiraService.buscarCarteiraPrincipal(usuarioAutenticado(autorizacao)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carteira> buscarPorId(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String autorizacao,
            @PathVariable Long id
    ) {
        return carteiraService.buscarPorId(usuarioAutenticado(autorizacao), id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/posicoes")
    public ResponseEntity<List<PosicaoAcaoResponse>> listarPosicoes(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String autorizacao,
            @PathVariable Long id
    ) {
        Usuario usuario = investidorAutenticado(autorizacao);
        return ResponseEntity.ok(carteiraService.listarPosicoes(usuario, id).stream().map(PosicaoAcaoResponse::from).toList());
    }

    @GetMapping("/{id}/contas")
    public ResponseEntity<List<ContaCorretoraResponse>> listarContas(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String autorizacao,
            @PathVariable Long id
    ) {
        Usuario usuario = investidorAutenticado(autorizacao);
        return ResponseEntity.ok(carteiraService.listarContas(usuario, id).stream().map(ContaCorretoraResponse::from).toList());
    }

    @PostMapping("/{id}/contas")
    public ResponseEntity<ContaCorretoraResponse> criarConta(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String autorizacao,
            @PathVariable Long id,
            @Valid @RequestBody ContaCorretoraRequest payload
    ) {
        Usuario usuario = investidorAutenticado(autorizacao);
        return ResponseEntity.status(201).body(ContaCorretoraResponse.from(carteiraService.criarConta(usuario, id, payload)));
    }

    @GetMapping("/{id}/movimentacoes")
    public ResponseEntity<List<MovimentacaoResponse>> listarMovimentacoes(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String autorizacao,
            @PathVariable Long id
    ) {
        Usuario usuario = investidorAutenticado(autorizacao);
        return ResponseEntity.ok(carteiraService.listarMovimentacoes(usuario, id).stream().map(MovimentacaoResponse::from).toList());
    }

    @PostMapping("/{id}/movimentacoes")
    public ResponseEntity<MovimentacaoResponse> registrarMovimentacao(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String autorizacao,
            @PathVariable Long id,
            @Valid @RequestBody MovimentacaoRequest payload
    ) {
        Usuario usuario = investidorAutenticado(autorizacao);
        return ResponseEntity.status(201).body(MovimentacaoResponse.from(carteiraService.registrarMovimentacao(usuario, id, payload)));
    }

    @DeleteMapping("/{id}/movimentacoes/{movimentacaoId}")
    public ResponseEntity<MovimentacaoResponse> cancelarMovimentacao(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String autorizacao,
            @PathVariable Long id,
            @PathVariable UUID movimentacaoId
    ) {
        Usuario usuario = investidorAutenticado(autorizacao);
        return ResponseEntity.ok(MovimentacaoResponse.from(carteiraService.cancelarMovimentacao(usuario, id, movimentacaoId)));
    }

    @PutMapping("/principal/recalcular")
    public ResponseEntity<Carteira> recalcularSaldoAcao(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String autorizacao
    ) {
        Usuario usuario = usuarioAutenticado(autorizacao);
        autenticacaoService.exigirAdministrador(usuario);
        return ResponseEntity.ok(carteiraService.calcularSaldoAcao(usuario));
    }

    @PostMapping("/principal/aportes")
    public ResponseEntity<Carteira> realizarAporte(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String autorizacao,
            @Valid @RequestBody AporteRequest payload
    ) {
        Usuario usuario = investidorAutenticado(autorizacao);
        return ResponseEntity.ok(carteiraService.realizarAporte(usuario, payload.valor()));
    }

    @PutMapping("/{id}/saldo-conta")
    public ResponseEntity<Carteira> atualizarSaldoEmConta(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String autorizacao,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        Usuario usuario = usuarioAutenticado(autorizacao);
        autenticacaoService.exigirAdministrador(usuario);
        return ResponseEntity.ok(carteiraService.atualizarSaldoEmConta(usuario, id, saldo(payload.get("saldoEmConta"))));
    }

    private Usuario usuarioAutenticado(String autorizacao) {
        return autenticacaoService.obterUsuarioAutenticado(autorizacao);
    }

    private Usuario investidorAutenticado(String autorizacao) {
        Usuario usuario = usuarioAutenticado(autorizacao);
        autenticacaoService.exigirInvestidor(usuario);
        return usuario;
    }

    private BigDecimal saldo(Object valor) {
        try {
            if (valor == null || valor.toString().isBlank()) {
                throw new NumberFormatException("blank");
            }
            return new BigDecimal(valor.toString());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Saldo em conta deve ser um número válido.");
        }
    }
}
