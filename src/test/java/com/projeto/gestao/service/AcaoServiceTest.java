package com.projeto.gestao.service;

import com.projeto.gestao.domain.model.Acao;
import com.projeto.gestao.domain.model.Corretora;
import com.projeto.gestao.domain.port.CotacaoAcaoPort;
import com.projeto.gestao.repository.AcaoRepository;
import com.projeto.gestao.repository.CorretoraRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcaoServiceTest {

    @Mock
    private AcaoRepository acaoRepository;

    @Mock
    private CotacaoAcaoPort cotacaoAcaoPort;

    @Mock
    private CarteiraService carteiraService;

    @Mock
    private CorretoraRepository corretoraRepository;

    @InjectMocks
    private AcaoService acaoService;

    @Test
    @DisplayName("Deve cadastrar ação BR com sucesso")
    void deveCadastrarAcaoBrComSucesso() {
        String ticker = "PETR4";
        String mercado = "BR";
        Double quantidadeCompra = 1.00;
        CotacaoAcaoPort.CotacaoInfo cotacaoMock = new CotacaoAcaoPort.CotacaoInfo(
            "PETR4", "Petrobras PN", "BRL", new BigDecimal("38.50")
        );
        UUID corretoraId = UUID.randomUUID();
        Corretora corretora = buildCorretora(corretoraId, "Corretora Teste");

        when(acaoRepository.existsByTicker(ticker)).thenReturn(false);
        when(corretoraRepository.findById(corretoraId)).thenReturn(Optional.of(corretora));
        when(cotacaoAcaoPort.getCotacao(ticker, mercado)).thenReturn(cotacaoMock);
        when(acaoRepository.save(any(Acao.class))).thenAnswer(inv -> {
            Acao a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        Acao result = acaoService.cadastrarAcao(ticker, mercado, quantidadeCompra, corretoraId);

        assertNotNull(result);
        assertEquals("PETR4", result.getTicker());
        assertEquals("BR", result.getMercado());
        assertEquals("BRL", result.getMoeda());
        assertEquals(corretora, result.getCorretoraRelacionada());
        assertEquals(new BigDecimal("38.50"), result.getCotacaoAtual());
        assertNotNull(result.getDataHoraCotacao());
        verify(carteiraService).calcularSaldoAcao();
    }

    @Test
    @DisplayName("Deve cadastrar ação US com sucesso")
    void deveCadastrarAcaoUsComSucesso() {
        String ticker = "AAPL";
        String mercado = "US";
        Double quantidadeCompra = 1.00;
        CotacaoAcaoPort.CotacaoInfo cotacaoMock = new CotacaoAcaoPort.CotacaoInfo(
            "AAPL", "Apple Inc.", "USD", new BigDecimal("213.70")
        );
        UUID corretoraId = UUID.randomUUID();
        Corretora corretora = buildCorretora(corretoraId, "Corretora US");

        when(acaoRepository.existsByTicker(ticker)).thenReturn(false);
        when(corretoraRepository.findById(corretoraId)).thenReturn(Optional.of(corretora));
        when(cotacaoAcaoPort.getCotacao(ticker, mercado)).thenReturn(cotacaoMock);
        when(acaoRepository.save(any(Acao.class))).thenAnswer(inv -> inv.getArgument(0));

        Acao result = acaoService.cadastrarAcao(ticker, mercado, quantidadeCompra, corretoraId);

        assertEquals("USD", result.getMoeda());
        assertEquals("US", result.getMercado());
        assertEquals(new BigDecimal("213.70"), result.getCotacaoAtual());
        assertEquals(corretora, result.getCorretoraRelacionada());
    }

    @Test
    @DisplayName("Deve lançar exceção quando ticker existente nao e localizado")
    void deveLancarExcecaoQuandoTickerExistenteNaoELocalizado() {
        String ticker = "PETR4";
        UUID corretoraId = UUID.randomUUID();
        Corretora corretora = buildCorretora(corretoraId, "Corretora Teste");
        when(acaoRepository.existsByTicker(ticker)).thenReturn(true);
        when(acaoRepository.findByTicker(ticker)).thenReturn(Optional.empty());
        when(corretoraRepository.findById(corretoraId)).thenReturn(Optional.of(corretora));

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> acaoService.cadastrarAcao(ticker, "BR", 1.0, corretoraId)
        );

        assertTrue(ex.getMessage().contains("cadastrada"));
        verify(cotacaoAcaoPort, never()).getCotacao(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção quando API de cotação não retorna dados")
    void deveLancarExcecaoQuandoApiCotacaoFalha() {
        String ticker = "XPTO99";
        UUID corretoraId = UUID.randomUUID();
        Corretora corretora = buildCorretora(corretoraId, "Corretora Teste");
        when(acaoRepository.existsByTicker(ticker)).thenReturn(false);
        when(corretoraRepository.findById(corretoraId)).thenReturn(Optional.of(corretora));
        when(cotacaoAcaoPort.getCotacao(ticker, "BR"))
            .thenThrow(new IllegalArgumentException("Ticker 'XPTO99' nao encontrado no mercado BR"));

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> acaoService.cadastrarAcao(ticker, "BR", 1.0, corretoraId)
        );

        assertTrue(ex.getMessage().contains("XPTO99"));
        verify(acaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando corretora nao existe")
    void deveLancarExcecaoQuandoCorretoraNaoExiste() {
        UUID corretoraId = UUID.randomUUID();
        when(corretoraRepository.findById(corretoraId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> acaoService.cadastrarAcao("PETR4", "BR", 1.0, corretoraId)
        );

        assertTrue(ex.getMessage().contains("Corretora"));
        verify(cotacaoAcaoPort, never()).getCotacao(anyString(), anyString());
        verify(acaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar cotação com sucesso")
    void deveAtualizarCotacaoComSucesso() {
        UUID id = UUID.randomUUID();
        Acao acao = new Acao();
        acao.setId(id);
        acao.setTicker("PETR4");
        acao.setMercado("BR");
        acao.setCotacaoAtual(new BigDecimal("35.00"));

        CotacaoAcaoPort.CotacaoInfo novaCotacao = new CotacaoAcaoPort.CotacaoInfo(
            "PETR4", "Petrobras PN", "BRL", new BigDecimal("40.00")
        );

        when(acaoRepository.findById(id)).thenReturn(Optional.of(acao));
        when(cotacaoAcaoPort.getCotacao("PETR4", "BR")).thenReturn(novaCotacao);
        when(acaoRepository.save(any(Acao.class))).thenAnswer(inv -> inv.getArgument(0));

        Acao result = acaoService.atualizarCotacao(id);

        assertEquals(new BigDecimal("40.00"), result.getCotacaoAtual());
        assertNotNull(result.getDataHoraCotacao());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar cotação de ação inexistente")
    void deveLancarExcecaoAoAtualizarCotacaoDeAcaoInexistente() {
        UUID id = UUID.randomUUID();
        when(acaoRepository.findById(id)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> acaoService.atualizarCotacao(id)
        );

        assertTrue(ex.getMessage().contains("não encontrada"));
    }

    @Test
    @DisplayName("Deve normalizar ticker para maiúsculas ao cadastrar")
    void deveNormalizarTickerParaMaiusculas() {
        String ticker = "petr4";
        String tickerNormalizado = "PETR4";
        UUID corretoraId = UUID.randomUUID();
        Corretora corretora = buildCorretora(corretoraId, "Corretora Teste");
        CotacaoAcaoPort.CotacaoInfo cotacaoMock = new CotacaoAcaoPort.CotacaoInfo(
            "PETR4", "Petrobras PN", "BRL", new BigDecimal("38.50")
        );

        when(acaoRepository.existsByTicker(tickerNormalizado)).thenReturn(false);
        when(corretoraRepository.findById(corretoraId)).thenReturn(Optional.of(corretora));
        when(cotacaoAcaoPort.getCotacao(tickerNormalizado, "BR")).thenReturn(cotacaoMock);
        when(acaoRepository.save(any(Acao.class))).thenAnswer(inv -> inv.getArgument(0));

        Acao result = acaoService.cadastrarAcao(ticker, "BR", 1.0, corretoraId);

        assertEquals("PETR4", result.getTicker());
    }

    @Test
    @DisplayName("Deve vender acao com sucesso")
    void deveVenderAcaoComSucesso() {
        Acao acao = new Acao();
        acao.setTicker("PETR4");
        acao.setMercado("BR");
        acao.setQuantidadeTotal(new BigDecimal("10.00"));

        CotacaoAcaoPort.CotacaoInfo cotacaoMock = new CotacaoAcaoPort.CotacaoInfo(
            "PETR4", "Petrobras PN", "BRL", new BigDecimal("40.00")
        );

        when(acaoRepository.findByTicker("PETR4")).thenReturn(Optional.of(acao));
        when(cotacaoAcaoPort.getCotacao("PETR4", "BR")).thenReturn(cotacaoMock);
        when(acaoRepository.save(any(Acao.class))).thenAnswer(inv -> inv.getArgument(0));

        Acao result = acaoService.venderAcao("PETR4", 4.0);

        assertEquals(0, new BigDecimal("6.00").compareTo(result.getQuantidadeTotal()));
        assertEquals(0, new BigDecimal("240.000").compareTo(result.getPosicao()));
        assertNotNull(result.getDataHoraCotacao());
        verify(carteiraService).calcularSaldoAcao();
        verify(acaoRepository, times(1)).save(acao);
    }

    @Test
    @DisplayName("Nao deve vender quantidade maior que a disponivel")
    void naoDeveVenderQuantidadeMaiorQueDisponivel() {
        Acao acao = new Acao();
        acao.setTicker("PETR4");
        acao.setMercado("BR");
        acao.setQuantidadeTotal(new BigDecimal("3.00"));

        when(acaoRepository.findByTicker("PETR4")).thenReturn(Optional.of(acao));

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> acaoService.venderAcao("PETR4", 4.0)
        );

        assertTrue(ex.getMessage().contains("maior"));
        verify(cotacaoAcaoPort, never()).getCotacao(anyString(), anyString());
        verify(acaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Nao deve vender quantidade menor ou igual a zero")
    void naoDeveVenderQuantidadeMenorOuIgualAZero() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> acaoService.venderAcao("PETR4", 0.0)
        );

        assertTrue(ex.getMessage().contains("maior que zero"));
        verify(acaoRepository, never()).findByTicker(anyString());
        verify(acaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar apenas acoes com quantidade disponivel")
    void deveListarApenasAcoesComQuantidadeDisponivel() {
        Acao acaoAtiva = new Acao();
        acaoAtiva.setTicker("PETR4");
        acaoAtiva.setQuantidadeTotal(new BigDecimal("2.00"));

        Acao acaoZerada = new Acao();
        acaoZerada.setTicker("VALE3");
        acaoZerada.setQuantidadeTotal(BigDecimal.ZERO);

        when(acaoRepository.findAll()).thenReturn(List.of(acaoAtiva, acaoZerada));

        List<Acao> result = acaoService.listarAtivas();

        assertEquals(1, result.size());
        assertEquals("PETR4", result.get(0).getTicker());
    }

    private Corretora buildCorretora(UUID id, String nomeFantasia) {
        Corretora corretora = new Corretora();
        corretora.setId(id);
        corretora.setCnpj("53931905000141");
        corretora.setNomeFantasia(nomeFantasia);
        corretora.setRazaoSocial(nomeFantasia);
        return corretora;
    }
}
