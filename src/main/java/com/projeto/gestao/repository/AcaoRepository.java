package com.projeto.gestao.repository;

import com.projeto.gestao.domain.model.Acao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AcaoRepository extends JpaRepository<Acao, UUID> {
    Optional<Acao> findByTicker(String ticker);
    boolean existsByTicker(String ticker);
}
