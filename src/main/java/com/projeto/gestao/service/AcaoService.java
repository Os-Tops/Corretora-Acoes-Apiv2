package com.projeto.gestao.service;

import com.projeto.gestao.domain.model.Acao;
import com.projeto.gestao.domain.model.Corretora;
import com.projeto.gestao.domain.port.CotacaoAcaoPort;
import com.projeto.gestao.repository.AcaoRepository;
import com.projeto.gestao.repository.CorretoraRepository;
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
    private final CorretoraRepository corretoraRepository;

    public AcaoService(AcaoRepository acaoRepository, CotacaoAcaoPort cotacaoAcaoPort, @Lazy CarteiraService carteiraService, CorretoraRepository corretoraRepository) {
        this.acaoRepository = acaoRepository;
        this.cotacaoAcaoPort = cotacaoAcaoPort;
        this.carteiraService = carteiraService;
        this.corretoraRepository = corretoraRepository;
    }

    @Transactional
    public Acao cadastrarAcao(String ticker, String mercado, Double quantidadeCompra, UUID corretoraId) {
        validarDadosCadastro(ticker, mercado, quantidadeCompra, corretoraId);

        String tickerNormalizado = ticker.trim().toUpperCase();
        String mercadoNormalizado = mercado.trim().toUpperCase();
        Corretora corretora = buscarCorretoraObrigatoria(corretoraId);

        if (acaoRepository.existsByTicker(tickerNormalizado)) {
            UUID id = buscarPorTicker(tickerNormalizado)
                    .map(Acao::getId)
                    .orElseThrow(() -> new IllegalArgumentException("Acao cadastrada nao encontrada."));
            return adicionarAcao(id, quantidadeCompra, corretora);
        }

        CotacaoAcaoPort.CotacaoInfo cotacaoInfo = cotacaoAcaoPort.getCotacao(tickerNormalizado, mercadoNormalizado);
        if (cotacaoInfo == null) {
            throw new IllegalArgumentException("Não foi possível obter dados para este Ticker.");
        }

        Acao acao = new Acao();
        acao.setTicker(tickerNormalizado);
        acao.setNomeEmpresa(cotacaoInfo.nomeEmpresa() != null ? cotacaoInfo.nomeEmpresa() : tickerNormalizado);
        acao.setMercado(mercadoNormalizado);
        acao.setMoeda(cotacaoInfo.moeda());
        acao.setQuantidadeCompra(BigDecimal.valueOf(quantidadeCompra));
        acao.setQuantidadeTotal(acao.getQuantidadeTotal().add(BigDecimal.valueOf(quantidadeCompra)));
        acao.setCotacaoAtual(cotacaoInfo.cotacaoAtual());
        acao.setCorretoraRelacionada(corretora);
        acao.calcularPosicaoAtualizada();
        acao.setPrecoMedio(acao.getPrecoMedio().
                add(cotacaoInfo.cotacaoAtual().
                        multiply(BigDecimal.valueOf(quantidadeCompra))).
                divide(acao.getQuantidadeTotal(), 2, RoundingMode.HALF_UP));
        acao.setDataHoraCotacao(LocalDateTime.now());

        Acao acaoSalva = acaoRepository.save(acao);
        carteiraService.calcularSaldoAcao();

        return acaoSalva;
    }

    private void validarDadosCadastro(String ticker, String mercado, Double quantidadeCompra, UUID corretoraId) {
        if (ticker == null || ticker.isBlank() || mercado == null || mercado.isBlank() || quantidadeCompra == null || corretoraId == null) {
            throw new IllegalArgumentException("Ticker, mercado, quantidade e corretora sao obrigatorios.");
        }
        if (quantidadeCompra <= 0) {
            throw new IllegalArgumentException("Quantidade de compra deve ser maior que zero.");
        }
    }

    private Corretora buscarCorretoraObrigatoria(UUID corretoraId) {
        return corretoraRepository.findById(corretoraId)
                .orElseThrow(() -> new IllegalArgumentException("Corretora nao encontrada."));
    }

    private Acao buscarAcaoObrigatoria(UUID id) {
        return acaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ação não encontrada."));
    }

    private void validarCorretoraDaAcao(Acao acao, Corretora corretora) {
        if (acao.getCorretoraRelacionada() == null) {
            acao.setCorretoraRelacionada(corretora);
            return;
        }

        if (!corretora.getId().equals(acao.getCorretoraRelacionada().getId())) {
            throw new IllegalArgumentException("Acao ja cadastrada em outra corretora.");
        }
    }

    private Acao adicionarAcao(UUID id, Double quantidadeCompra, Corretora corretora) {
        Acao acao = buscarAcaoObrigatoria(id);
        validarCorretoraDaAcao(acao, corretora);
        return adicionarQuantidade(acao, quantidadeCompra);
    }

    //Adicionar mais da mesma acao
    @Transactional
    public Acao adicionarAcao(UUID id, Double quantidadeCompra) {
        Acao acao = buscarAcaoObrigatoria(id);
        return adicionarQuantidade(acao, quantidadeCompra);
    }

    private Acao adicionarQuantidade(Acao acao, Double quantidadeCompra) {
        if (quantidadeCompra == null || quantidadeCompra <= 0) {
            throw new IllegalArgumentException("Quantidade de compra deve ser maior que zero.");
        }
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

            Acao acaoSalva = acaoRepository.save(acao);
            carteiraService.calcularSaldoAcao();

            return acaoSalva;
        }
        throw new IllegalArgumentException("Não foi possível atualizar a cotação no momento.");
    }

    @Transactional
    public Acao venderAcao(String ticker, Double quantidadeVenda) {
        if (ticker == null || ticker.isBlank()) {
            throw new IllegalArgumentException("Ticker e obrigatorio.");
        }

        if (quantidadeVenda == null || quantidadeVenda <= 0) {
            throw new IllegalArgumentException("Quantidade de venda deve ser maior que zero.");
        }

        Acao acao = buscarPorTicker(ticker)
                .orElseThrow(() -> new IllegalArgumentException("Acao nao encontrada."));

        BigDecimal quantidadeAtual = acao.getQuantidadeTotal() == null ? BigDecimal.ZERO : acao.getQuantidadeTotal();
        BigDecimal quantidadeVendaDecimal = BigDecimal.valueOf(quantidadeVenda);

        if (quantidadeVendaDecimal.compareTo(quantidadeAtual) > 0) {
            throw new IllegalArgumentException("Quantidade de venda nao pode ser maior que a quantidade disponivel.");
        }

        CotacaoAcaoPort.CotacaoInfo cotacaoInfo = cotacaoAcaoPort.getCotacao(acao.getTicker(), acao.getMercado());
        if (cotacaoInfo != null && cotacaoInfo.cotacaoAtual() != null) {
            acao.setQuantidadeTotal(quantidadeAtual.subtract(quantidadeVendaDecimal));
            acao.setCotacaoAtual(cotacaoInfo.cotacaoAtual());
            acao.calcularPosicaoAtualizada();
            acao.setDataHoraCotacao(LocalDateTime.now());
            Acao acaoSalva = acaoRepository.save(acao);

            carteiraService.calcularSaldoAcao();

            return acaoSalva;
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

    public List<Acao> listarAtivas() {
        return listarTodas().stream()
                .filter(this::temQuantidadeDisponivel)
                .toList();
    }

    private boolean temQuantidadeDisponivel(Acao acao) {
        return acao.getQuantidadeTotal() != null && acao.getQuantidadeTotal().compareTo(BigDecimal.ZERO) > 0;
    }

    public Optional<Acao> buscarPorId(UUID id) {
        return acaoRepository.findById(id);
    }

    public Optional<Acao> buscarPorTicker(String ticker) {
        if (ticker == null) {
            return Optional.empty();
        }
        return acaoRepository.findByTicker(ticker.trim().toUpperCase());
    }
}
