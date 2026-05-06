package com.projeto.gestao.service;

import com.projeto.gestao.domain.model.Acao;
import com.projeto.gestao.domain.model.Carteira;
import com.projeto.gestao.repository.CarteiraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CarteiraService {

    private final CarteiraRepository carteiraRepository;
    private final AcaoService acaoService;

    public CarteiraService(CarteiraRepository carteiraRepository, AcaoService acaoService) {
        this.carteiraRepository = carteiraRepository;
        this.acaoService = acaoService;
    }

    @Transactional
    public Carteira calcularSaldoAcao() {

        List<Carteira> carteiras = listarTodas();

        if (carteiras.isEmpty()) {
            return null;
        }

        Long carteiraId = carteiras.get(0).getId();

        List<Acao> acoes = acaoService.listarTodas();

        Carteira carteira = carteiraRepository.findById(carteiraId)
                .orElseThrow(() -> new IllegalArgumentException("Ação não encontrada."));

        BigDecimal saldoAcao = acoes.stream().
                map(Acao::getPosicao).
                reduce(BigDecimal.ZERO, BigDecimal::add);

        carteira.setSaldoAcao(saldoAcao);

        return carteiraRepository.save(carteira);
    }

    public List<Carteira> listarTodas() {
        return carteiraRepository.findAll();
    }

    public Optional<Carteira> buscarPorId(Long id) {
        return carteiraRepository.findById(id);
    }

}
