package com.projeto.gestao.service;

import com.projeto.gestao.domain.enums.Mercado;
import com.projeto.gestao.service.AcaoService;
import com.projeto.gestao.domain.model.*;
import com.projeto.gestao.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Transactional
public class DBService {

    @Autowired
    private CarteiraRepository carteiraRepository;

    public final CarteiraService carteiraService;

    @Autowired
    private AcaoRepository acaoRepo;

    private final AcaoService acaoService;

    @Autowired
    private CorretoraRepository corretoraRepo;
    @Autowired
    private CorretoraService corretoraService;

    public DBService(CarteiraService carteiraService, AcaoService acaoService) {
        this.carteiraService = carteiraService;
        this.acaoService = acaoService;
    }


    public void initDB() {

        try {

            Corretora corretora1 = corretoraService.cadastrarCorretora("47847884000188");
            corretoraRepo.save(corretora1);

            Acao acao1 = acaoService.cadastrarAcao("PETR4", 0, 2.0, corretora1);
            acaoRepo.save(acao1);

            Carteira carteira = new Carteira();
            carteira = carteiraRepository.save(carteira);
            carteiraService.calcularSaldoAcao();

        } catch (Exception e) {
            // Logar o erro ou lançar uma exceção customizada
            System.err.println("Erro ao inicializar o banco de dados: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
