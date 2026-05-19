package com.projeto.gestao.mapper;

import com.projeto.gestao.domain.dto.AcaoDTO;
import com.projeto.gestao.domain.model.Acao;
import com.projeto.gestao.domain.model.Corretora;
import com.projeto.gestao.domain.enums.Moeda;
import com.projeto.gestao.domain.enums.Mercado;
import com.projeto.gestao.domain.port.CotacaoAcaoPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AcaoMapper {

    private final CotacaoAcaoPort cotacaoAcaoPort;


    private AcaoMapper(CotacaoAcaoPort cotacaoAcaoPort) {
        this.cotacaoAcaoPort = cotacaoAcaoPort;
    }

    /* ======================= Entity -> DTO ======================= */

    /** Converte uma Entity em DTO. */
    public static AcaoDTO toDto(Acao e) {
        if (e == null) return null;

        // idAcao (Long) -> Long do DTO
        UUID idDto = e.getId();

        Integer corretoraId = Math.toIntExact((e.getCorretora() == null) ? null : e.getCorretora().getId().compareTo(idDto));
        int moedaInt = (e.getMoeda() == null) ? 0 : e.getMoeda().getId();
        int mercadoInt = (e.getMercado() == null) ? 0 : e.getMoeda().getId();

        return new AcaoDTO(

                idDto,
                e.getTicker(),
                e.getNomeEmpresa(),
                mercadoInt,
                moedaInt,
                e.getQuantidadeCompra(),
                e.getQuantidadeTotal(),
                e.getCotacaoAtual(),
                e.getPosicao(),
                e.getPrecoMedio(),
                e.getDataHoraCotacao(),
                corretoraId

        );
    }

    /** Converte uma coleção de Entities em lista de DTOs. */
    public static List<AcaoDTO> toDtoList(Collection<Acao> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .filter(Objects::nonNull)
                .map(AcaoMapper::toDto)
                .collect(Collectors.toList());
    }

    /** Converte Page<Entity> em Page<DTO> preservando a paginação. */
    public static Page<AcaoDTO> toDtoPage(Page<Acao> page) {
        List<AcaoDTO> content = toDtoList(page.getContent());
        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }

    /* ======================= DTO -> Entity ======================= */

    /**
     * Cria uma nova Entity a partir do DTO, usando o Corretora já carregado.
     * Não seta valorEstoque (é calculado na Entity/serviço).
     */
    public Acao toEntity(AcaoDTO dto, String ticker, Integer mercado, Double quantidadeCompra, Corretora corretora) {
        if (dto == null) return null;

        Mercado mercadoEnum = Mercado.toEnum(mercado);

        String mercadoString = mercadoEnum.name();

        CotacaoAcaoPort.CotacaoInfo cotacaoInfo = cotacaoAcaoPort.getCotacao(ticker, mercadoString);

        Acao e = new Acao();
        // idAcao do DTO (Long) -> Long da Entity
        e.setId(dto.getId());
        e.setTicker(ticker.toUpperCase());
        e.setNomeEmpresa(cotacaoInfo.nomeEmpresa() != null ? cotacaoInfo.nomeEmpresa() : ticker);
        e.setMercado(Mercado.toEnum(dto.getMercado())); // int -> enum
        e.setMoeda(Moeda.toEnum(dto.getMoeda())); // int -> enum
        e.setQuantidadeCompra(BigDecimal.valueOf(quantidadeCompra));
        e.setQuantidadeTotal(e.getQuantidadeTotal().add(e.getQuantidadeCompra()));
        e.setCotacaoAtual(cotacaoInfo.cotacaoAtual());
        e.calcularPosicaoAtualizada();
        e.setPrecoMedio(e.getPrecoMedio().
                add(cotacaoInfo.cotacaoAtual().
                        multiply(e.getQuantidadeCompra())).
                divide(e.getQuantidadeTotal(), 2, RoundingMode.HALF_UP));
        e.setDataHoraCotacao(LocalDateTime.now());
        e.setCorretora(corretora);

        return e;
    }

    /**
     * Cria uma nova Entity a partir do DTO, resolvendo o Corretora via função (repo).
     * Ex.: toEntity(dto, corretoraRepo::getReferenceById) ou findById(...).orElseThrow(...)
     */
    public Acao toEntity(AcaoDTO dto, Function<Integer, Corretora> corretoraResolver) {
        if (dto == null) return null;
        Corretora corretora = (dto.getCorretoraId() == null) ? null : corretoraResolver.apply(dto.getCorretoraId());
        return toEntity(
                dto,
                dto.getTicker(),
                dto.getMercado(),
                dto.getQuantidadeCompra() != null ? dto.getQuantidadeCompra().doubleValue() : 0.0,
                corretora
        );
    }

    /**
     * Atualiza uma Entity existente a partir do DTO (PUT completo),
     * usando o Corretora já carregado. Não altera o id do target.
     * NÃO seta valorEstoque (é calculado no domínio).
     */
    public static void copyToEntity(AcaoDTO dto, Acao target, Corretora corretora) {
        if (dto == null || target == null) return;

        target.setTicker(trim(dto.getTicker()));
        target.setNomeEmpresa(trim(dto.getNomeEmpresa()));
        target.setCorretora(corretora);
        target.setMoeda(Moeda.toEnum(dto.getMoeda()));
        target.setMercado(Mercado.toEnum(dto.getMercado()));
    }

    /**
     * Atualiza uma Entity existente a partir do DTO (PUT completo),
     * resolvendo o Corretora via função. Não altera o id do target.
     */
    public static void copyToEntity(AcaoDTO dto, Acao target, Function<Integer, Corretora> corretoraResolver) {
        if (dto == null || target == null) return;
        Corretora corretora = (dto.getCorretoraId() == null) ? null : corretoraResolver.apply(dto.getCorretoraId());
        copyToEntity(dto, target, corretora);
    }

    /* ======================= Helpers ======================= */

    private static String trim(String s) {
        return (s == null) ? null : s.trim();
    }
}
