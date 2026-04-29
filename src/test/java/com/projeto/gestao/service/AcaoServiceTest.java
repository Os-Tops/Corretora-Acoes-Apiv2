package com.projeto.gestao.service;

import com.projeto.gestao.domain.model.Acao;
import com.projeto.gestao.domain.port.CotacaoAcaoPort;
import com.projeto.gestao.domain.repository.AcaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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

    @InjectMocks
    private AcaoService acaoService;

    @Test
    @DisplayName("Deve cadastrar ação BR com sucesso")
    void deveCadastrarAcaoBrComSucesso() {
        String ticker = "PETR4";
        String mercado = "BR";
        CotacaoAcaoPort.CotacaoInfo cotacaoMock = new CotacaoAcaoPort.CotacaoInfo(
            "PETR4", "Petrobras PN", "BRL", new BigDecimal("38.50")
        );

        when(acaoRepository.existsByTicker(ticker)).thenReturn(false);
        when(cotacaoAcaoPort.getCotacao(ticker, mercado)).thenReturn(cotacaoMock);
        when(acaoRepository.save(any(Acao.class))).thenAnswer(inv -> {
            Acao a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        Acao result = acaoService.cadastrarAcao(ticker, mercado);

        assertNotNull(result);
        assertEquals("PETR4", result.getTicker());
        assertEquals("BR", result.getMercado());
        assertEquals("BRL", result.getMoeda());
        assertEquals(new BigDecimal("38.50"), result.getCotacaoAtual());
        assertNotNull(result.getDataHoraCotacao());
    }

    @Test
    @DisplayName("Deve cadastrar ação US com sucesso")
    void deveCadastrarAcaoUsComSucesso() {
        String ticker = "AAPL";
        String mercado = "US";
        CotacaoAcaoPort.CotacaoInfo cotacaoMock = new CotacaoAcaoPort.CotacaoInfo(
            "AAPL", "Apple Inc.", "USD", new BigDecimal("213.70")
        );

        when(acaoRepository.existsByTicker(ticker)).thenReturn(false);
        when(cotacaoAcaoPort.getCotacao(ticker, mercado)).thenReturn(cotacaoMock);
        when(acaoRepository.save(any(Acao.class))).thenAnswer(inv -> inv.getArgument(0));

        Acao result = acaoService.cadastrarAcao(ticker, mercado);

        assertEquals("USD", result.getMoeda());
        assertEquals("US", result.getMercado());
        assertEquals(new BigDecimal("213.70"), result.getCotacaoAtual());
    }

    @Test
    @DisplayName("Deve lançar exceção quando Ticker já cadastrado")
    void deveLancarExcecaoQuandoTickerDuplicado() {
        String ticker = "PETR4";
        when(acaoRepository.existsByTicker(ticker)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> acaoService.cadastrarAcao(ticker, "BR")
        );

        assertTrue(ex.getMessage().contains("já existe"));
        verify(cotacaoAcaoPort, never()).getCotacao(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção quando API de cotação não retorna dados")
    void deveLancarExcecaoQuandoApiCotacaoFalha() {
        String ticker = "XPTO99";
        when(acaoRepository.existsByTicker(ticker)).thenReturn(false);
        when(cotacaoAcaoPort.getCotacao(ticker, "BR"))
            .thenThrow(new IllegalArgumentException("Ticker 'XPTO99' nao encontrado no mercado BR"));

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> acaoService.cadastrarAcao(ticker, "BR")
        );

        assertTrue(ex.getMessage().contains("XPTO99"));
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
        CotacaoAcaoPort.CotacaoInfo cotacaoMock = new CotacaoAcaoPort.CotacaoInfo(
            "PETR4", "Petrobras PN", "BRL", new BigDecimal("38.50")
        );

        when(acaoRepository.existsByTicker(ticker)).thenReturn(false);
        when(cotacaoAcaoPort.getCotacao(ticker, "BR")).thenReturn(cotacaoMock);
        when(acaoRepository.save(any(Acao.class))).thenAnswer(inv -> inv.getArgument(0));

        Acao result = acaoService.cadastrarAcao(ticker, "BR");

        assertEquals("PETR4", result.getTicker());
    }
}
