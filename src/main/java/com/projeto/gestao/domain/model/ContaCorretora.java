package com.projeto.gestao.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "contas_corretora",
        uniqueConstraints = @UniqueConstraint(columnNames = {"carteira_id", "apelido"})
)
public class ContaCorretora {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "carteira_id", nullable = false)
    private Carteira carteira;

    @ManyToOne(optional = false)
    @JoinColumn(name = "corretora_id", nullable = false)
    private Corretora corretora;

    @Column(nullable = false)
    private String apelido;

    private String identificadorConta;

    @Column(nullable = false)
    private boolean ativa = true;

    @Column(nullable = false)
    private LocalDateTime criadaEm;

    @PrePersist
    void aoCriar() {
        criadaEm = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Carteira getCarteira() { return carteira; }
    public void setCarteira(Carteira carteira) { this.carteira = carteira; }

    public Corretora getCorretora() { return corretora; }
    public void setCorretora(Corretora corretora) { this.corretora = corretora; }

    public String getApelido() { return apelido; }
    public void setApelido(String apelido) { this.apelido = apelido; }

    public String getIdentificadorConta() { return identificadorConta; }
    public void setIdentificadorConta(String identificadorConta) { this.identificadorConta = identificadorConta; }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }

    public LocalDateTime getCriadaEm() { return criadaEm; }
}
