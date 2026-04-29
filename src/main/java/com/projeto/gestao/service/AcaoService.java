package com.projeto.gestao.service;

import com.projeto.gestao.domain.model.Acao;
import com.projeto.gestao.domain.port.CotacaoAcaoPort;
import com.projeto.gestao.domain.repository.AcaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AcaoService {

    private final AcaoRepository acaoRepository;
    private final CotacaoAcaoPort cotacaoAcaoPort;

    public AcaoService(AcaoRepository acaoRepository, CotacaoAcaoPort cotacaoAcaoPort) {
        this.acaoRepository = acaoRepository;
        this.cotacaoAcaoPort = cotacaoAcaoPort;
    }

    @Transactional
    public Acao cadastrarAcao(String ticker, String mercado) {
        if (acaoRepository.existsByTicker(ticker)) {
            throw new IllegalArgumentException("Ação com este Ticker já existe.");
        }

        CotacaoAcaoPort.CotacaoInfo cotacaoInfo = cotacaoAcaoPort.getCotacao(ticker, mercado);
        if (cotacaoInfo == null) {
            throw new IllegalArgumentException("Não foi possível obter dados para este Ticker.");
        }

        Acao acao = new Acao();
        acao.setTicker(ticker.toUpperCase());
        acao.setNomeEmpresa(cotacaoInfo.nomeEmpresa() != null ? cotacaoInfo.nomeEmpresa() : ticker);
        acao.setMercado(mercado.toUpperCase());
        acao.setMoeda(cotacaoInfo.moeda());
        acao.setCotacaoAtual(cotacaoInfo.cotacaoAtual());
        acao.setDataHoraCotacao(LocalDateTime.now());

        return acaoRepository.save(acao);
    }

    @Transactional
    public Acao atualizarCotacao(UUID id) {
        Acao acao = acaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ação não encontrada."));

        CotacaoAcaoPort.CotacaoInfo cotacaoInfo = cotacaoAcaoPort.getCotacao(acao.getTicker(), acao.getMercado());
        if (cotacaoInfo != null && cotacaoInfo.cotacaoAtual() != null) {
            acao.setCotacaoAtual(cotacaoInfo.cotacaoAtual());
            acao.setDataHoraCotacao(LocalDateTime.now());
            return acaoRepository.save(acao);
        }
        throw new IllegalArgumentException("Não foi possível atualizar a cotação no momento.");
    }

    public List<Acao> listarTodas() {
        return acaoRepository.findAll();
    }

    public Optional<Acao> buscarPorId(UUID id) {
        return acaoRepository.findById(id);
    }

    public Optional<Acao> buscarPorTicker(String ticker) {
        return acaoRepository.findByTicker(ticker);
    }
}
