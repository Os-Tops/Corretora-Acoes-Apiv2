package com.projeto.gestao.controller;

import com.projeto.gestao.service.AcaoService;
import com.projeto.gestao.service.CarteiraService;
import com.projeto.gestao.service.CorretoraService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller convertido para REST API.
 * Fornece dados em formato JSON para o Frontend em React.
 */
@RestController
@RequestMapping("/api/dashboard")
// Permite que o React (porta 5173) aceda a esta API (porta 8080)
@CrossOrigin(origins = "http://localhost:5173")
public class DashboardController {

    private final CorretoraService corretoraService;
    private final AcaoService acaoService;
    private final CarteiraService carteiraService;

    public DashboardController(CorretoraService corretoraService, AcaoService acaoService, CarteiraService carteiraService) {
        this.corretoraService = corretoraService;
        this.acaoService = acaoService;
        this.carteiraService = carteiraService;
    }

    /**
     * Retorna estatísticas simplificadas para os cards do Dashboard.
     */
    @GetMapping("/stats")
    public Map<String, Integer> getStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("corretorasCount", corretoraService.listarTodas().size());
        stats.put("acoesCount", acaoService.listarTodas().size());
        return stats;
    }

    /**
     * Retorna a lista completa de corretoras.
     */
    @GetMapping("/corretoras")
    public List<?> listarCorretoras() {
        return corretoraService.listarTodas();
    }

    /**
     * Retorna dados necessários para a página de ações (Ações + Carteiras).
     */
    @GetMapping("/acoes")
    public Map<String, Object> listarAcoesComCarteira() {
        Map<String, Object> response = new HashMap<>();
        response.put("acoes", acaoService.listarTodas());
        response.put("carteiras", carteiraService.listarTodas());
        return response;
    }

    /**
     * Retorna apenas as carteiras.
     */
    @GetMapping("/carteiras")
    public List<?> listarCarteiras() {
        return carteiraService.listarTodas();
    }
}