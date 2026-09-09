package com.projeto.gestao.service;

import com.projeto.gestao.domain.dto.ContaCorretoraRequest;
import com.projeto.gestao.domain.dto.MovimentacaoRequest;
import com.projeto.gestao.domain.model.Acao;
import com.projeto.gestao.domain.model.Carteira;
import com.projeto.gestao.domain.model.ContaCorretora;
import com.projeto.gestao.domain.model.Corretora;
import com.projeto.gestao.domain.model.Movimentacao;
import com.projeto.gestao.domain.model.PosicaoCarteira;
import com.projeto.gestao.domain.model.TipoMovimentacao;
import com.projeto.gestao.domain.model.Usuario;
import com.projeto.gestao.repository.CarteiraRepository;
import com.projeto.gestao.repository.ContaCorretoraRepository;
import com.projeto.gestao.repository.CorretoraRepository;
import com.projeto.gestao.repository.MovimentacaoRepository;
import com.projeto.gestao.repository.PosicaoCarteiraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class CarteiraService {

    private static final int PRECISAO_MONETARIA = 8;

    private final CarteiraRepository carteiraRepository;
    private final PosicaoCarteiraRepository posicaoCarteiraRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final ContaCorretoraRepository contaCorretoraRepository;
    private final CorretoraRepository corretoraRepository;
    private final AcaoService acaoService;

    public CarteiraService(
            CarteiraRepository carteiraRepository,
            PosicaoCarteiraRepository posicaoCarteiraRepository,
            MovimentacaoRepository movimentacaoRepository,
            ContaCorretoraRepository contaCorretoraRepository,
            CorretoraRepository corretoraRepository,
            AcaoService acaoService
    ) {
        this.carteiraRepository = carteiraRepository;
        this.posicaoCarteiraRepository = posicaoCarteiraRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.contaCorretoraRepository = contaCorretoraRepository;
        this.corretoraRepository = corretoraRepository;
        this.acaoService = acaoService;
    }

    /**
     * Atalho mantido para a tela de negociacao: registra uma compra na carteira principal
     * usando a cotacao atual. O historico continua sendo a fonte da posicao.
     */
    @Transactional
    public PosicaoCarteira comprarAcao(Usuario usuario, UUID acaoId, BigDecimal quantidade) {
        Carteira carteira = buscarCarteiraPrincipal(usuario);
        Acao acao = acaoService.atualizarCotacao(acaoId);
        registrarMovimentacao(usuario, carteira.getId(), new MovimentacaoRequest(
                TipoMovimentacao.COMPRA, acao.getId(), null, LocalDate.now(), quantidade,
                acao.getCotacaoAtual(), null, BigDecimal.ZERO, BigDecimal.ZERO, "Compra rapida pela cotacao atual"
        ));
        return buscarPosicaoObrigatoria(carteira.getId(), acaoId);
    }

    /**
     * Atalho mantido para a tela de negociacao: registra uma venda na carteira principal
     * usando a cotacao atual.
     */
    @Transactional
    public PosicaoCarteira venderAcao(Usuario usuario, String ticker, BigDecimal quantidade) {
        Carteira carteira = buscarCarteiraPrincipal(usuario);
        PosicaoCarteira posicao = posicaoCarteiraRepository
                .findByCarteiraIdAndAcaoTickerIgnoreCase(carteira.getId(), ticker)
                .orElseThrow(() -> new IllegalArgumentException("Acao nao encontrada na sua carteira."));
        Acao acao = acaoService.atualizarCotacao(posicao.getAcao().getId());
        registrarMovimentacao(usuario, carteira.getId(), new MovimentacaoRequest(
                TipoMovimentacao.VENDA, acao.getId(), null, LocalDate.now(), quantidade,
                acao.getCotacaoAtual(), null, BigDecimal.ZERO, BigDecimal.ZERO, "Venda rapida pela cotacao atual"
        ));
        return posicaoCarteiraRepository.findByCarteiraIdAndAcaoId(carteira.getId(), acao.getId())
                .orElseGet(() -> posicaoZerada(carteira, acao));
    }

    @Transactional
    public Carteira realizarAporte(Usuario usuario, BigDecimal valor) {
        Carteira carteira = buscarCarteiraPrincipal(usuario);
        registrarMovimentacao(usuario, carteira.getId(), new MovimentacaoRequest(
                TipoMovimentacao.APORTE, null, null, LocalDate.now(), null, null,
                valor, BigDecimal.ZERO, BigDecimal.ZERO, "Aporte em conta"
        ));
        return buscarCarteiraObrigatoria(usuario, carteira.getId());
    }

    @Transactional
    public Carteira atualizarSaldoEmConta(Usuario usuario, Long id, BigDecimal saldoEmConta) {
        if (saldoEmConta == null || saldoEmConta.signum() < 0) {
            throw new IllegalArgumentException("Saldo em conta deve ser um numero maior ou igual a zero.");
        }
        Carteira carteira = buscarCarteiraObrigatoria(usuario, id);
        sincronizarLegadoSeNecessario(carteira);
        BigDecimal diferenca = saldoEmConta.subtract(carteira.getSaldoEmConta());
        if (diferenca.signum() == 0) {
            return carteira;
        }
        registrarMovimentacao(usuario, id, new MovimentacaoRequest(
                diferenca.signum() > 0 ? TipoMovimentacao.APORTE : TipoMovimentacao.RETIRADA,
                null, null, LocalDate.now(), null, null, diferenca.abs(), BigDecimal.ZERO,
                BigDecimal.ZERO, "Ajuste administrativo de saldo"
        ));
        return buscarCarteiraObrigatoria(usuario, id);
    }

    @Transactional
    public Carteira criarCarteira(Usuario usuario, String nome) {
        String nomeNormalizado = nome == null ? "" : nome.trim();
        if (nomeNormalizado.isBlank()) {
            throw new IllegalArgumentException("Nome da carteira e obrigatorio.");
        }
        Carteira carteira = new Carteira();
        carteira.setUsuario(usuario);
        carteira.setNome(nomeNormalizado);
        carteira.setPrincipal(carteiraRepository.findAllByUsuarioId(usuario.getId()).isEmpty());
        carteira.setSaldoAcao(BigDecimal.ZERO);
        carteira.setSaldoEmConta(BigDecimal.ZERO);
        return carteiraRepository.save(carteira);
    }

    public List<Carteira> listarTodas(Usuario usuario) {
        return carteiraRepository.findAllByUsuarioIdOrderByPrincipalDescNomeAsc(usuario.getId());
    }

    public Optional<Carteira> buscarPorId(Usuario usuario, Long id) {
        return carteiraRepository.findByIdAndUsuarioId(id, usuario.getId());
    }

    @Transactional
    public Carteira buscarCarteiraPrincipal(Usuario usuario) {
        return carteiraRepository.findFirstByUsuarioIdOrderByPrincipalDescIdAsc(usuario.getId())
                .orElseGet(() -> criarCarteiraPrincipal(usuario));
    }

    public List<ContaCorretora> listarContas(Usuario usuario, Long carteiraId) {
        buscarCarteiraObrigatoria(usuario, carteiraId);
        return contaCorretoraRepository.findAllByCarteiraIdOrderByApelidoAsc(carteiraId);
    }

    @Transactional
    public ContaCorretora criarConta(Usuario usuario, Long carteiraId, ContaCorretoraRequest request) {
        Carteira carteira = buscarCarteiraObrigatoria(usuario, carteiraId);
        Corretora corretora = corretoraRepository.findById(request.corretoraId())
                .orElseThrow(() -> new IllegalArgumentException("Corretora nao encontrada."));
        if (!Boolean.TRUE.equals(corretora.getValidadaNaCvm())) {
            throw new IllegalArgumentException("Somente corretoras validadas na CVM podem ser vinculadas a carteira.");
        }

        ContaCorretora conta = new ContaCorretora();
        conta.setCarteira(carteira);
        conta.setCorretora(corretora);
        conta.setApelido(request.apelido().trim());
        conta.setIdentificadorConta(normalizarTextoOpcional(request.identificadorConta()));
        return contaCorretoraRepository.save(conta);
    }

    public List<Movimentacao> listarMovimentacoes(Usuario usuario, Long carteiraId) {
        buscarCarteiraObrigatoria(usuario, carteiraId);
        return movimentacaoRepository.findAllByCarteiraIdOrderByDataOperacaoAscRegistradaEmAsc(carteiraId);
    }

    @Transactional
    public Movimentacao registrarMovimentacao(Usuario usuario, Long carteiraId, MovimentacaoRequest request) {
        Carteira carteira = buscarCarteiraObrigatoria(usuario, carteiraId);
        sincronizarLegadoSeNecessario(carteira);
        validarMovimentacaoExterna(request);

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setCarteira(carteira);
        movimentacao.setTipo(request.tipo());
        movimentacao.setDataOperacao(request.dataOperacao());
        movimentacao.setTaxas(valorPositivoOuZero(request.taxas(), "Taxas"));
        movimentacao.setImpostos(valorPositivoOuZero(request.impostos(), "Impostos"));
        movimentacao.setObservacao(normalizarTextoOpcional(request.observacao()));

        if (requerAcao(request.tipo())) {
            movimentacao.setAcao(acaoObrigatoria(request.acaoId()));
            movimentacao.setQuantidade(valorObrigatorioPositivo(request.quantidade(), "Quantidade"));
            movimentacao.setPrecoUnitario(valorObrigatorioPositivo(request.precoUnitario(), "Preco unitario"));
            movimentacao.setValor(movimentacao.getQuantidade().multiply(movimentacao.getPrecoUnitario()));
        } else {
            if (request.acaoId() != null) {
                movimentacao.setAcao(acaoObrigatoria(request.acaoId()));
            }
            movimentacao.setValor(valorObrigatorioPositivo(request.valor(), "Valor"));
        }

        if (request.contaCorretoraId() != null) {
            movimentacao.setContaCorretora(contaCorretoraRepository.findByIdAndCarteiraId(request.contaCorretoraId(), carteiraId)
                    .orElseThrow(() -> new IllegalArgumentException("Conta de corretora nao pertence a esta carteira.")));
        }

        Movimentacao salva = movimentacaoRepository.save(movimentacao);
        recalcularCarteira(carteira);
        return salva;
    }

    @Transactional
    public Movimentacao cancelarMovimentacao(Usuario usuario, Long carteiraId, UUID movimentacaoId) {
        Carteira carteira = buscarCarteiraObrigatoria(usuario, carteiraId);
        Movimentacao movimentacao = movimentacaoRepository.findByIdAndCarteiraId(movimentacaoId, carteiraId)
                .orElseThrow(() -> new IllegalArgumentException("Movimentacao nao encontrada."));
        if (movimentacao.isCancelada()) {
            throw new IllegalArgumentException("Esta movimentacao ja foi cancelada.");
        }
        if (movimentacao.getTipo() == TipoMovimentacao.AJUSTE_SALDO || movimentacao.getTipo() == TipoMovimentacao.AJUSTE_POSICAO) {
            throw new IllegalArgumentException("Movimentacoes de migracao nao podem ser canceladas.");
        }
        movimentacao.setCancelada(true);
        movimentacao.setCanceladaEm(LocalDateTime.now());
        Movimentacao salva = movimentacaoRepository.save(movimentacao);
        recalcularCarteira(carteira);
        return salva;
    }

    @Transactional
    public Carteira calcularSaldoAcao(Usuario usuario) {
        Carteira carteira = buscarCarteiraPrincipal(usuario);
        sincronizarLegadoSeNecessario(carteira);
        return recalcularCarteira(carteira);
    }

    public List<PosicaoCarteira> listarPosicoes(Usuario usuario) {
        return listarPosicoes(usuario, buscarCarteiraPrincipal(usuario).getId());
    }

    public List<PosicaoCarteira> listarPosicoes(Usuario usuario, Long carteiraId) {
        buscarCarteiraObrigatoria(usuario, carteiraId);
        return posicaoCarteiraRepository.findAllByCarteiraId(carteiraId).stream()
                .filter(posicao -> posicao.getQuantidadeTotal().signum() > 0)
                .toList();
    }

    public BigDecimal valorAtual(PosicaoCarteira posicao) {
        BigDecimal cotacao = posicao.getAcao().getCotacaoAtual() == null ? BigDecimal.ZERO : posicao.getAcao().getCotacaoAtual();
        return posicao.getQuantidadeTotal().multiply(cotacao);
    }

    public BigDecimal saldoAcaoConsolidado(Usuario usuario) {
        return listarTodas(usuario).stream().map(Carteira::getSaldoAcao).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal saldoEmContaConsolidado(Usuario usuario) {
        return listarTodas(usuario).stream().map(Carteira::getSaldoEmConta).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal custoInvestidoConsolidado(Usuario usuario) {
        return listarTodas(usuario).stream()
                .flatMap(carteira -> listarPosicoes(usuario, carteira.getId()).stream())
                .map(posicao -> posicao.getQuantidadeTotal().multiply(posicao.getPrecoMedio()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Carteira recalcularCarteira(Carteira carteira) {
        BigDecimal saldoEmConta = BigDecimal.ZERO;
        Map<UUID, PosicaoCalculada> calculadas = new HashMap<>();

        for (Movimentacao movimentacao : movimentacaoRepository
                .findAllByCarteiraIdAndCanceladaFalseOrderByDataOperacaoAscRegistradaEmAsc(carteira.getId())) {
            saldoEmConta = aplicarMovimentacao(movimentacao, calculadas, saldoEmConta);
            if (saldoEmConta.signum() < 0) {
                throw new IllegalArgumentException("A movimentacao deixa o saldo da carteira negativo em " + movimentacao.getDataOperacao() + ".");
            }
        }

        Map<UUID, PosicaoCarteira> existentes = new HashMap<>();
        for (PosicaoCarteira posicao : posicaoCarteiraRepository.findAllByCarteiraId(carteira.getId())) {
            existentes.put(posicao.getAcao().getId(), posicao);
        }

        BigDecimal saldoAcao = BigDecimal.ZERO;
        for (Map.Entry<UUID, PosicaoCalculada> entrada : calculadas.entrySet()) {
            PosicaoCalculada calculada = entrada.getValue();
            if (calculada.quantidade.signum() == 0) {
                continue;
            }
            PosicaoCarteira posicao = existentes.remove(entrada.getKey());
            if (posicao == null) {
                posicao = new PosicaoCarteira();
                posicao.setCarteira(carteira);
                posicao.setAcao(calculada.acao);
            }
            posicao.setQuantidadeTotal(calculada.quantidade);
            posicao.setPrecoMedio(calculada.precoMedio);
            posicaoCarteiraRepository.save(posicao);
            saldoAcao = saldoAcao.add(valorAtual(posicao));
        }

        existentes.values().forEach(posicaoCarteiraRepository::delete);
        carteira.setSaldoEmConta(saldoEmConta);
        carteira.setSaldoAcao(saldoAcao);
        return carteiraRepository.save(carteira);
    }

    private BigDecimal aplicarMovimentacao(
            Movimentacao movimentacao,
            Map<UUID, PosicaoCalculada> calculadas,
            BigDecimal saldoEmConta
    ) {
        TipoMovimentacao tipo = movimentacao.getTipo();
        BigDecimal encargos = movimentacao.getTaxas().add(movimentacao.getImpostos());

        return switch (tipo) {
            case APORTE, DIVIDENDO, JCP, AJUSTE_SALDO -> saldoEmConta.add(movimentacao.getValor()).subtract(encargos);
            case RETIRADA, TAXA, IMPOSTO -> saldoEmConta.subtract(movimentacao.getValor()).subtract(encargos);
            case COMPRA -> {
                adicionarPosicao(calculadas, movimentacao);
                yield saldoEmConta.subtract(movimentacao.getValor()).subtract(encargos);
            }
            case VENDA -> {
                reduzirPosicao(calculadas, movimentacao);
                yield saldoEmConta.add(movimentacao.getValor()).subtract(encargos);
            }
            case AJUSTE_POSICAO -> {
                adicionarPosicao(calculadas, movimentacao);
                yield saldoEmConta;
            }
        };
    }

    private void adicionarPosicao(Map<UUID, PosicaoCalculada> calculadas, Movimentacao movimentacao) {
        UUID acaoId = movimentacao.getAcao().getId();
        PosicaoCalculada atual = calculadas.getOrDefault(acaoId, new PosicaoCalculada(movimentacao.getAcao()));
        BigDecimal novaQuantidade = atual.quantidade.add(movimentacao.getQuantidade());
        BigDecimal custoAdicional = movimentacao.getValor().add(movimentacao.getTaxas()).add(movimentacao.getImpostos());
        BigDecimal novoCusto = atual.precoMedio.multiply(atual.quantidade).add(custoAdicional);
        atual.quantidade = novaQuantidade;
        atual.precoMedio = novoCusto.divide(novaQuantidade, PRECISAO_MONETARIA, RoundingMode.HALF_UP);
        calculadas.put(acaoId, atual);
    }

    private void reduzirPosicao(Map<UUID, PosicaoCalculada> calculadas, Movimentacao movimentacao) {
        UUID acaoId = movimentacao.getAcao().getId();
        PosicaoCalculada atual = calculadas.get(acaoId);
        if (atual == null || atual.quantidade.compareTo(movimentacao.getQuantidade()) < 0) {
            throw new IllegalArgumentException("A venda informada e maior que a quantidade disponivel para " + movimentacao.getAcao().getTicker() + ".");
        }
        atual.quantidade = atual.quantidade.subtract(movimentacao.getQuantidade());
        if (atual.quantidade.signum() == 0) {
            atual.precoMedio = BigDecimal.ZERO;
        }
    }

    private void sincronizarLegadoSeNecessario(Carteira carteira) {
        if (movimentacaoRepository.existsByCarteiraId(carteira.getId())) {
            return;
        }

        if (carteira.getSaldoEmConta().signum() > 0) {
            movimentacaoRepository.save(movimentacaoAjusteSaldo(carteira, carteira.getSaldoEmConta()));
        }
        for (PosicaoCarteira posicao : posicaoCarteiraRepository.findAllByCarteiraId(carteira.getId())) {
            if (posicao.getQuantidadeTotal().signum() > 0) {
                movimentacaoRepository.save(movimentacaoAjustePosicao(carteira, posicao));
            }
        }
    }

    private Movimentacao movimentacaoAjusteSaldo(Carteira carteira, BigDecimal saldo) {
        Movimentacao movimentacao = movimentacaoBase(carteira, TipoMovimentacao.AJUSTE_SALDO);
        movimentacao.setValor(saldo);
        movimentacao.setObservacao("Saldo inicial migrado para o historico");
        return movimentacao;
    }

    private Movimentacao movimentacaoAjustePosicao(Carteira carteira, PosicaoCarteira posicao) {
        Movimentacao movimentacao = movimentacaoBase(carteira, TipoMovimentacao.AJUSTE_POSICAO);
        movimentacao.setAcao(posicao.getAcao());
        movimentacao.setQuantidade(posicao.getQuantidadeTotal());
        movimentacao.setPrecoUnitario(posicao.getPrecoMedio());
        movimentacao.setValor(posicao.getQuantidadeTotal().multiply(posicao.getPrecoMedio()));
        movimentacao.setObservacao("Posicao inicial migrada para o historico");
        return movimentacao;
    }

    private Movimentacao movimentacaoBase(Carteira carteira, TipoMovimentacao tipo) {
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setCarteira(carteira);
        movimentacao.setTipo(tipo);
        movimentacao.setDataOperacao(LocalDate.now());
        movimentacao.setTaxas(BigDecimal.ZERO);
        movimentacao.setImpostos(BigDecimal.ZERO);
        return movimentacao;
    }

    private Carteira buscarCarteiraObrigatoria(Usuario usuario, Long carteiraId) {
        return buscarPorId(usuario, carteiraId)
                .orElseThrow(() -> new IllegalArgumentException("Carteira nao encontrada."));
    }

    private Carteira criarCarteiraPrincipal(Usuario usuario) {
        Carteira carteira = new Carteira();
        carteira.setUsuario(usuario);
        carteira.setNome("Carteira Principal");
        carteira.setPrincipal(true);
        carteira.setSaldoAcao(BigDecimal.ZERO);
        carteira.setSaldoEmConta(BigDecimal.ZERO);
        return carteiraRepository.save(carteira);
    }

    private PosicaoCarteira buscarPosicaoObrigatoria(Long carteiraId, UUID acaoId) {
        return posicaoCarteiraRepository.findByCarteiraIdAndAcaoId(carteiraId, acaoId)
                .orElseThrow(() -> new IllegalArgumentException("A posicao nao foi encontrada apos registrar a compra."));
    }

    private PosicaoCarteira posicaoZerada(Carteira carteira, Acao acao) {
        PosicaoCarteira posicao = new PosicaoCarteira();
        posicao.setCarteira(carteira);
        posicao.setAcao(acao);
        posicao.setQuantidadeTotal(BigDecimal.ZERO);
        posicao.setPrecoMedio(BigDecimal.ZERO);
        return posicao;
    }

    private Acao acaoObrigatoria(UUID acaoId) {
        if (acaoId == null) {
            throw new IllegalArgumentException("Ativo e obrigatorio para esta movimentacao.");
        }
        return acaoService.buscarPorId(acaoId)
                .orElseThrow(() -> new IllegalArgumentException("Ativo nao encontrado."));
    }

    private void validarMovimentacaoExterna(MovimentacaoRequest request) {
        if (request.tipo() == TipoMovimentacao.AJUSTE_SALDO || request.tipo() == TipoMovimentacao.AJUSTE_POSICAO) {
            throw new IllegalArgumentException("Ajustes de migracao sao controlados pelo sistema.");
        }
        if (request.dataOperacao().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data da operacao nao pode estar no futuro.");
        }
    }

    private boolean requerAcao(TipoMovimentacao tipo) {
        return tipo == TipoMovimentacao.COMPRA || tipo == TipoMovimentacao.VENDA || tipo == TipoMovimentacao.AJUSTE_POSICAO;
    }

    private BigDecimal valorObrigatorioPositivo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException(campo + " deve ser maior que zero.");
        }
        return valor;
    }

    private BigDecimal valorPositivoOuZero(BigDecimal valor, String campo) {
        if (valor == null) {
            return BigDecimal.ZERO;
        }
        if (valor.signum() < 0) {
            throw new IllegalArgumentException(campo + " nao pode ser negativo.");
        }
        return valor;
    }

    private String normalizarTextoOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    private static class PosicaoCalculada {
        private final Acao acao;
        private BigDecimal quantidade = BigDecimal.ZERO;
        private BigDecimal precoMedio = BigDecimal.ZERO;

        private PosicaoCalculada(Acao acao) {
            this.acao = acao;
        }
    }
}
