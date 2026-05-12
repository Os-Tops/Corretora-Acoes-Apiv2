package com.projeto.gestao.service;

import com.projeto.gestao.domain.model.Acao;
import com.projeto.gestao.domain.model.Carteira;
import com.projeto.gestao.domain.port.CotacaoAcaoPort;
import com.projeto.gestao.repository.AcaoRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AcaoService {

    private final AcaoRepository acaoRepository;
    private final CotacaoAcaoPort cotacaoAcaoPort;
    private final CarteiraService carteiraService;

    public AcaoService(AcaoRepository acaoRepository, CotacaoAcaoPort cotacaoAcaoPort, @Lazy CarteiraService carteiraService) {
        this.acaoRepository = acaoRepository;
        this.cotacaoAcaoPort = cotacaoAcaoPort;
        this.carteiraService = carteiraService;
    }

    @Transactional
    public Acao cadastrarAcao(String ticker, String mercado, Double quantidadeCompra) {
        if (acaoRepository.existsByTicker(ticker)) {
            UUID id = buscarPorTicker(ticker)
                    .map(Acao::getId)
                    .orElse(null);
            return adicionarAcao(id, quantidadeCompra);
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
        acao.setQuantidadeCompra(BigDecimal.valueOf(quantidadeCompra));
        acao.setQuantidadeTotal(acao.getQuantidadeTotal().add(BigDecimal.valueOf(quantidadeCompra)));
        acao.setCotacaoAtual(cotacaoInfo.cotacaoAtual());
        acao.calcularPosicaoAtualizada();
        acao.setPrecoMedio(acao.getPrecoMedio().
                add(cotacaoInfo.cotacaoAtual().
                        multiply(BigDecimal.valueOf(quantidadeCompra))).
                divide(acao.getQuantidadeTotal(), 2, RoundingMode.HALF_UP));
        acao.setDataHoraCotacao(LocalDateTime.now());
        acaoRepository.save(acao);

        carteiraService.calcularSaldoAcao();

        return acaoRepository.save(acao);
    }

    //Adicionar mais da mesma ação
    @Transactional
    public Acao adicionarAcao(UUID id, Double quantidadeCompra) {

        Acao acao = acaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ação não encontrada."));

        CotacaoAcaoPort.CotacaoInfo cotacaoInfo = cotacaoAcaoPort.getCotacao(acao.getTicker(), acao.getMercado());
        if (cotacaoInfo != null && cotacaoInfo.cotacaoAtual() != null) {
            acao.setQuantidadeCompra(BigDecimal.valueOf(quantidadeCompra));
            acao.setQuantidadeTotal(acao.getQuantidadeTotal().add(BigDecimal.valueOf(quantidadeCompra)));
            acao.setCotacaoAtual(cotacaoInfo.cotacaoAtual());
            acao.calcularPosicaoAtualizada();
            acao.setPrecoMedio(acao.getPrecoMedio().
                    multiply(acao.getQuantidadeTotal().
                            subtract(BigDecimal.valueOf(quantidadeCompra))).
                    add(cotacaoInfo.cotacaoAtual().
                            multiply(BigDecimal.valueOf(quantidadeCompra))).
                    divide(acao.getQuantidadeTotal(), 2, RoundingMode.HALF_UP));
            acao.setDataHoraCotacao(LocalDateTime.now());
            acaoRepository.save(acao);

            carteiraService.calcularSaldoAcao();

            return acaoRepository.save(acao);
        }
        throw new IllegalArgumentException("Não foi possível atualizar a cotação no momento.");
    }

    @Transactional
    public Acao venderAcao(String ticker, Double quantidadeVenda) {

        UUID id = buscarPorTicker(ticker)
                .map(Acao::getId)
                .orElse(null);

        Acao acao = acaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ação não encontrada."));

        CotacaoAcaoPort.CotacaoInfo cotacaoInfo = cotacaoAcaoPort.getCotacao(acao.getTicker(), acao.getMercado());
        if (cotacaoInfo != null && cotacaoInfo.cotacaoAtual() != null) {
            acao.setQuantidadeTotal(acao.getQuantidadeTotal().subtract(BigDecimal.valueOf(quantidadeVenda)));
            acao.setCotacaoAtual(cotacaoInfo.cotacaoAtual());
            acao.calcularPosicaoAtualizada();
            acao.setDataHoraCotacao(LocalDateTime.now());
            acaoRepository.save(acao);

            carteiraService.calcularSaldoAcao();

            return acaoRepository.save(acao);
        }
        throw new IllegalArgumentException("Não foi possível atualizar a cotação no momento.");
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
