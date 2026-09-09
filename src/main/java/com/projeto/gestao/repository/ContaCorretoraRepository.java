package com.projeto.gestao.repository;

import com.projeto.gestao.domain.model.ContaCorretora;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContaCorretoraRepository extends JpaRepository<ContaCorretora, UUID> {
    List<ContaCorretora> findAllByCarteiraIdOrderByApelidoAsc(Long carteiraId);
    Optional<ContaCorretora> findByIdAndCarteiraId(UUID id, Long carteiraId);
}
