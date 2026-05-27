package com.projeto.gestao.service;

import com.projeto.gestao.domain.model.Carteira;
import com.projeto.gestao.domain.model.Corretora;
import com.projeto.gestao.repository.AcaoRepository;
import com.projeto.gestao.repository.CarteiraRepository;
import com.projeto.gestao.repository.CorretoraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

            /*Corretora corretora = corretoraRepo.findByCnpj("00000000000191")
                    .orElseGet(() -> {
                        Corretora novaCorretora = new Corretora();
                        novaCorretora.setCnpj("00000000000191");
                        novaCorretora.setRazaoSocial("Corretora Demo");
                        novaCorretora.setNomeFantasia("Corretora Demo");
                        novaCorretora.setSituacaoCadastral("ATIVA");
                        novaCorretora.setValidadaNaCvm(true);
                        novaCorretora.setDataCadastro(LocalDateTime.now());
                        return corretoraRepo.save(novaCorretora);
                    });*/

            if(corretoraService.listarTodas().isEmpty()){
                corretoraService.cadastrarCorretora("47847884000188");
            }

            if (!acaoRepo.existsByTicker("PETR4")) {
                acaoService.cadastrarAcao("PETR4", "BR", 2.0, corretoraService.listarTodas().get(0).getId() );
            }

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
