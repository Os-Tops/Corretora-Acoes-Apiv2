package com.projeto.gestao.controller;

import com.projeto.gestao.service.AcaoService;
import com.projeto.gestao.service.CorretoraService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final CorretoraService corretoraService;
    private final AcaoService acaoService;

    public DashboardController(CorretoraService corretoraService, AcaoService acaoService) {
        this.corretoraService = corretoraService;
        this.acaoService = acaoService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("corretorasCount", corretoraService.listarTodas().size());
        model.addAttribute("acoesCount", acaoService.listarTodas().size());
        return "index";
    }

    @GetMapping("/corretoras")
    public String corretoras(Model model) {
        model.addAttribute("corretoras", corretoraService.listarTodas());
        return "corretoras";
    }

    @GetMapping("/acoes")
    public String acoes(Model model) {
        model.addAttribute("acoes", acaoService.listarTodas());
        return "acoes";
    }
}
