package com.projeto.gestao.repository;

import com.projeto.gestao.domain.model.Corretora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CorretoraRepository extends JpaRepository<Corretora, UUID> {
    Optional<Corretora> findByCnpj(String cnpj);
    boolean existsByCnpj(String cnpj);
}
