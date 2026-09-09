package com.projeto.gestao.repository;

import com.projeto.gestao.domain.model.Movimentacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, UUID> {
    boolean existsByCarteiraId(Long carteiraId);
    List<Movimentacao> findAllByCarteiraIdOrderByDataOperacaoAscRegistradaEmAsc(Long carteiraId);
    List<Movimentacao> findAllByCarteiraIdAndCanceladaFalseOrderByDataOperacaoAscRegistradaEmAsc(Long carteiraId);
    Optional<Movimentacao> findByIdAndCarteiraId(UUID id, Long carteiraId);
}
