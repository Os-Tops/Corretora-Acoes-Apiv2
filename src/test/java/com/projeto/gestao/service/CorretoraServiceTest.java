package com.projeto.gestao.service;

import com.projeto.gestao.domain.model.Corretora;
import com.projeto.gestao.domain.port.BuscaCepPort;
import com.projeto.gestao.domain.port.ValidacaoCnpjPort;
import com.projeto.gestao.repository.CorretoraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorretoraServiceTest {

    @Mock
    private CorretoraRepository corretoraRepository;

    @Mock
    private ValidacaoCnpjPort validacaoCnpjPort;

    @Mock
    private BuscaCepPort buscaCepPort;

    @InjectMocks
    private CorretoraService corretoraService;

    private ValidacaoCnpjPort.CnpjInfo cnpjInfoMock;

    @BeforeEach
    void setUp() {
        cnpjInfoMock = new ValidacaoCnpjPort.CnpjInfo(
            "XP INVESTIMENTOS CCTVM SA",
            "XP Investimentos",
            "Corretoras de Títulos e Valores Mobiliários",
            "04538-133",
            "contato@xpi.com.br",
            "1134769600",
            "Av. Brigadeiro Faria Lima",
            "201",
            "6o andar",
            "Itaim Bibi",
            "São Paulo",
            "SP",
            "ATIVA"
        );
    }

    @Test
    @DisplayName("Deve cadastrar corretora com sucesso quando CNPJ válido e validada na CVM")
    void deveCadastrarCorretoraComSucesso() {
        String cnpj = "02332886000104";

        when(corretoraRepository.existsByCnpj(cnpj)).thenReturn(false);
        when(validacaoCnpjPort.getCnpjInfo(cnpj)).thenReturn(cnpjInfoMock);
        when(validacaoCnpjPort.isValidAndMarketParticipant(cnpj)).thenReturn(true);
        when(buscaCepPort.getCepInfo(anyString())).thenReturn(
            new BuscaCepPort.CepInfo("04538-133", "Av. Brigadeiro Faria Lima", "6o andar", "Itaim Bibi", "São Paulo", "SP")
        );
        when(corretoraRepository.save(any(Corretora.class))).thenAnswer(inv -> {
            Corretora c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        Corretora result = corretoraService.cadastrarCorretora(cnpj);

        assertNotNull(result);
        assertEquals(cnpj, result.getCnpj());
        assertEquals("XP INVESTIMENTOS CCTVM SA", result.getRazaoSocial());
        assertTrue(result.getValidadaNaCvm());
        verify(corretoraRepository).save(any(Corretora.class));
    }

    @Test
    @DisplayName("Deve salvar com validadaNaCvm=false quando CNAE não é financeiro")
    void deveSalvarComValidadaNaCvmFalseQuandoCnaeNaoFinanceiro() {
        String cnpj = "11222333000181";

        when(corretoraRepository.existsByCnpj(cnpj)).thenReturn(false);
        when(validacaoCnpjPort.getCnpjInfo(cnpj)).thenReturn(cnpjInfoMock);
        when(validacaoCnpjPort.isValidAndMarketParticipant(cnpj)).thenReturn(false);
        when(buscaCepPort.getCepInfo(anyString())).thenReturn(null);
        when(corretoraRepository.save(any(Corretora.class))).thenAnswer(inv -> inv.getArgument(0));

        Corretora result = corretoraService.cadastrarCorretora(cnpj);

        assertFalse(result.getValidadaNaCvm());
    }

    @Test
    @DisplayName("Deve lançar exceção quando CNPJ já cadastrado")
    void deveLancarExcecaoQuandoCnpjDuplicado() {
        String cnpj = "02332886000104";
        when(corretoraRepository.existsByCnpj(cnpj)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> corretoraService.cadastrarCorretora(cnpj)
        );

        assertTrue(ex.getMessage().contains("já existe"));
        verify(validacaoCnpjPort, never()).getCnpjInfo(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção quando Brasil API não retorna dados")
    void deveLancarExcecaoQuandoBrasilApiNaoRetornaDados() {
        String cnpj = "99999999000199";
        when(corretoraRepository.existsByCnpj(cnpj)).thenReturn(false);
        when(validacaoCnpjPort.getCnpjInfo(cnpj)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> corretoraService.cadastrarCorretora(cnpj)
        );

        assertTrue(ex.getMessage().contains("Brasil API"));
        verify(corretoraRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve remover pontuação do CNPJ antes de processar")
    void deveRemoverPontuacaoDoCnpj() {
        String cnpjFormatado = "02.332.886/0001-04";
        String cnpjLimpo = "02332886000104";

        when(corretoraRepository.existsByCnpj(cnpjLimpo)).thenReturn(false);
        when(validacaoCnpjPort.getCnpjInfo(cnpjLimpo)).thenReturn(cnpjInfoMock);
        when(validacaoCnpjPort.isValidAndMarketParticipant(cnpjLimpo)).thenReturn(true);
        when(buscaCepPort.getCepInfo(anyString())).thenReturn(null);
        when(corretoraRepository.save(any(Corretora.class))).thenAnswer(inv -> inv.getArgument(0));

        Corretora result = corretoraService.cadastrarCorretora(cnpjFormatado);

        assertEquals(cnpjLimpo, result.getCnpj());
    }

    @Test
    @DisplayName("Deve buscar corretora por ID")
    void deveBuscarPorId() {
        UUID id = UUID.randomUUID();
        Corretora corretora = new Corretora();
        corretora.setId(id);
        when(corretoraRepository.findById(id)).thenReturn(Optional.of(corretora));

        Optional<Corretora> result = corretoraService.buscarPorId(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    @DisplayName("Deve retornar vazio quando corretora não encontrada por ID")
    void deveRetornarVazioQuandoNaoEncontradoPorId() {
        UUID id = UUID.randomUUID();
        when(corretoraRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Corretora> result = corretoraService.buscarPorId(id);

        assertTrue(result.isEmpty());
    }
}
