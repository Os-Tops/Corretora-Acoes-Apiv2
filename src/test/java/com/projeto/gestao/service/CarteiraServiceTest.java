package com.projeto.gestao.service;

import com.projeto.gestao.domain.dto.MovimentacaoRequest;
import com.projeto.gestao.domain.model.Acao;
import com.projeto.gestao.domain.model.Carteira;
import com.projeto.gestao.domain.model.Movimentacao;
import com.projeto.gestao.domain.model.PosicaoCarteira;
import com.projeto.gestao.domain.model.TipoMovimentacao;
import com.projeto.gestao.domain.model.Usuario;
import com.projeto.gestao.repository.CarteiraRepository;
import com.projeto.gestao.repository.ContaCorretoraRepository;
import com.projeto.gestao.repository.CorretoraRepository;
import com.projeto.gestao.repository.MovimentacaoRepository;
import com.projeto.gestao.repository.PosicaoCarteiraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CarteiraServiceTest {

    @Mock
    private CarteiraRepository carteiraRepository;

    @Mock
    private PosicaoCarteiraRepository posicaoCarteiraRepository;

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @Mock
    private ContaCorretoraRepository contaCorretoraRepository;

    @Mock
    private CorretoraRepository corretoraRepository;

    @Mock
    private AcaoService acaoService;

    @InjectMocks
    private CarteiraService carteiraService;

    private Usuario usuario;
    private Carteira carteira;
    private Acao petr4;
    private List<Movimentacao> movimentacoes;
    private List<PosicaoCarteira> posicoes;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());

        carteira = new Carteira();
        carteira.setId(7L);
        carteira.setUsuario(usuario);
        carteira.setNome("Carteira Principal");
        carteira.setPrincipal(true);
        carteira.setSaldoAcao(BigDecimal.ZERO);
        carteira.setSaldoEmConta(BigDecimal.ZERO);

        petr4 = acao(UUID.randomUUID(), "PETR4", "15.00");
        movimentacoes = new ArrayList<>();
        posicoes = new ArrayList<>();

        when(carteiraRepository.findFirstByUsuarioIdOrderByPrincipalDescIdAsc(usuario.getId())).thenReturn(Optional.of(carteira));
        when(carteiraRepository.findByIdAndUsuarioId(7L, usuario.getId())).thenReturn(Optional.of(carteira));
        when(carteiraRepository.save(any(Carteira.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movimentacaoRepository.existsByCarteiraId(7L)).thenAnswer(invocation -> !movimentacoes.isEmpty());
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(invocation -> {
            Movimentacao movimentacao = invocation.getArgument(0);
            if (!movimentacoes.contains(movimentacao)) {
                movimentacoes.add(movimentacao);
            }
            return movimentacao;
        });
        when(movimentacaoRepository.findAllByCarteiraIdAndCanceladaFalseOrderByDataOperacaoAscRegistradaEmAsc(7L))
                .thenAnswer(invocation -> movimentacoes.stream().filter(movimentacao -> !movimentacao.isCancelada()).toList());
        when(movimentacaoRepository.findAllByCarteiraIdOrderByDataOperacaoAscRegistradaEmAsc(7L))
                .thenAnswer(invocation -> List.copyOf(movimentacoes));
        when(posicaoCarteiraRepository.findAllByCarteiraId(7L)).thenAnswer(invocation -> List.copyOf(posicoes));
        when(posicaoCarteiraRepository.save(any(PosicaoCarteira.class))).thenAnswer(invocation -> {
            PosicaoCarteira posicao = invocation.getArgument(0);
            posicoes.removeIf(existente -> existente.getAcao().getId().equals(posicao.getAcao().getId()));
            posicoes.add(posicao);
            return posicao;
        });
        org.mockito.Mockito.doAnswer(invocation -> {
            PosicaoCarteira posicao = invocation.getArgument(0);
            posicoes.remove(posicao);
            return null;
        }).when(posicaoCarteiraRepository).delete(any(PosicaoCarteira.class));
        when(acaoService.buscarPorId(petr4.getId())).thenReturn(Optional.of(petr4));
    }

    @Test
    @DisplayName("Deve criar carteira principal zerada quando o usuario ainda nao possui uma")
    void deveCriarCarteiraZerada() {
        when(carteiraRepository.findFirstByUsuarioIdOrderByPrincipalDescIdAsc(usuario.getId())).thenReturn(Optional.empty());

        Carteira resultado = carteiraService.buscarCarteiraPrincipal(usuario);

        assertEquals(usuario, resultado.getUsuario());
        assertEquals("Carteira Principal", resultado.getNome());
        assertEquals(true, resultado.isPrincipal());
        assertEquals(BigDecimal.ZERO, resultado.getSaldoAcao());
        assertEquals(BigDecimal.ZERO, resultado.getSaldoEmConta());
    }

    @Test
    @DisplayName("Deve calcular saldo e preco medio pela sequencia de movimentacoes")
    void deveCalcularPosicaoPeloHistorico() {
        registrar(TipoMovimentacao.APORTE, null, null, null, "100.00", "0", "0");
        registrar(TipoMovimentacao.COMPRA, petr4.getId(), "2", "10.00", null, "1.50", "0.50");

        PosicaoCarteira posicao = posicoes.get(0);

        assertEquals(0, new BigDecimal("2").compareTo(posicao.getQuantidadeTotal()));
        assertEquals(0, new BigDecimal("11.00").compareTo(posicao.getPrecoMedio()));
        assertEquals(0, new BigDecimal("78.00").compareTo(carteira.getSaldoEmConta()));
        assertEquals(0, new BigDecimal("30.00").compareTo(carteira.getSaldoAcao()));
        assertEquals(2, movimentacoes.size());
    }

    @Test
    @DisplayName("Nao deve registrar venda acima da quantidade disponivel")
    void naoDeveVenderMaisDoQueAPosicao() {
        registrar(TipoMovimentacao.APORTE, null, null, null, "100.00", "0", "0");
        registrar(TipoMovimentacao.COMPRA, petr4.getId(), "2", "10.00", null, "0", "0");

        assertThrows(IllegalArgumentException.class, () -> registrar(TipoMovimentacao.VENDA, petr4.getId(), "3", "12.00", null, "0", "0"));
    }

    @Test
    @DisplayName("Deve recalcular a carteira ao cancelar uma movimentacao")
    void deveRecalcularQuandoCancelaMovimentacao() {
        registrar(TipoMovimentacao.APORTE, null, null, null, "100.00", "0", "0");
        Movimentacao compra = carteiraService.registrarMovimentacao(usuario, 7L, request(
                TipoMovimentacao.COMPRA, petr4.getId(), "2", "10.00", null, "0", "0"
        ));
        compra.setId(UUID.randomUUID());
        when(movimentacaoRepository.findByIdAndCarteiraId(compra.getId(), 7L)).thenReturn(Optional.of(compra));

        carteiraService.cancelarMovimentacao(usuario, 7L, compra.getId());

        assertEquals(0, new BigDecimal("100.00").compareTo(carteira.getSaldoEmConta()));
        assertEquals(BigDecimal.ZERO, carteira.getSaldoAcao());
        assertEquals(0, posicoes.size());
        assertEquals(true, compra.isCancelada());
    }

    @Test
    @DisplayName("Deve criar uma carteira adicional sem saldo inicial")
    void deveCriarCarteiraAdicional() {
        when(carteiraRepository.findAllByUsuarioId(usuario.getId())).thenReturn(List.of(carteira));

        Carteira adicional = carteiraService.criarCarteira(usuario, "Exterior");

        assertEquals("Exterior", adicional.getNome());
        assertEquals(false, adicional.isPrincipal());
        assertEquals(BigDecimal.ZERO, adicional.getSaldoEmConta());
    }

    @Test
    @DisplayName("Deve registrar ajustes iniciais para preservar saldo e posicoes legadas")
    void devePreservarDadosLegadosAoRegistrarPrimeiroLancamento() {
        carteira.setSaldoEmConta(new BigDecimal("50.00"));
        PosicaoCarteira legado = new PosicaoCarteira();
        legado.setCarteira(carteira);
        legado.setAcao(petr4);
        legado.setQuantidadeTotal(new BigDecimal("1"));
        legado.setPrecoMedio(new BigDecimal("8.00"));
        posicoes.add(legado);

        registrar(TipoMovimentacao.APORTE, null, null, null, "20.00", "0", "0");

        assertEquals(0, new BigDecimal("70.00").compareTo(carteira.getSaldoEmConta()));
        assertEquals(1, posicoes.size());
        assertEquals(3, movimentacoes.size());
        assertEquals(TipoMovimentacao.AJUSTE_SALDO, movimentacoes.get(0).getTipo());
        assertEquals(TipoMovimentacao.AJUSTE_POSICAO, movimentacoes.get(1).getTipo());
    }

    private Movimentacao registrar(
            TipoMovimentacao tipo,
            UUID acaoId,
            String quantidade,
            String precoUnitario,
            String valor,
            String taxas,
            String impostos
    ) {
        return carteiraService.registrarMovimentacao(usuario, 7L, request(tipo, acaoId, quantidade, precoUnitario, valor, taxas, impostos));
    }

    private MovimentacaoRequest request(
            TipoMovimentacao tipo,
            UUID acaoId,
            String quantidade,
            String precoUnitario,
            String valor,
            String taxas,
            String impostos
    ) {
        return new MovimentacaoRequest(
                tipo,
                acaoId,
                null,
                LocalDate.now(),
                decimal(quantidade),
                decimal(precoUnitario),
                decimal(valor),
                decimal(taxas),
                decimal(impostos),
                null
        );
    }

    private BigDecimal decimal(String valor) {
        return valor == null ? null : new BigDecimal(valor);
    }

    private Acao acao(UUID id, String ticker, String cotacao) {
        Acao acao = new Acao();
        acao.setId(id);
        acao.setTicker(ticker);
        acao.setMercado("BR");
        acao.setCotacaoAtual(new BigDecimal(cotacao));
        return acao;
    }
}
