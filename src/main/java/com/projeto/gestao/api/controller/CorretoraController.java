package com.projeto.gestao.api.controller;

import com.projeto.gestao.domain.model.Corretora;
import com.projeto.gestao.service.CorretoraService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/corretoras")
public class CorretoraController {

    private final CorretoraService corretoraService;

    public CorretoraController(CorretoraService corretoraService) {
        this.corretoraService = corretoraService;
    }

    @PostMapping
    public ResponseEntity<Corretora> cadastrarCorretora(@RequestBody Map<String, String> payload) {
        String cnpj = payload.get("cnpj");
        if (cnpj == null || cnpj.isEmpty()) {
            throw new IllegalArgumentException("CNPJ é obrigatório");
        }
        Corretora corretora = corretoraService.cadastrarCorretora(cnpj);
        return ResponseEntity.status(HttpStatus.CREATED).body(corretora);
    }

    @GetMapping
    public ResponseEntity<List<Corretora>> listarCorretoras() {
        return ResponseEntity.ok(corretoraService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Corretora> buscarPorId(@PathVariable UUID id) {
        return corretoraService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cnpj/{cnpj}")
    public ResponseEntity<Corretora> buscarPorCnpj(@PathVariable String cnpj) {
        return corretoraService.buscarPorCnpj(cnpj)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
