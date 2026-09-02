package br.com.droneshop;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Produto {
    @Id
    @GeneratedValue
    public UUID id;

    @NotBlank
    public String nome;

    @NotBlank
    @Column(unique = true, nullable = false)
    public String sku;

    @NotNull
    @DecimalMin("0.00")
    public BigDecimal preco;

    @NotNull
    @Min(0)
    public Integer estoque;

    @NotNull
    @Enumerated(EnumType.STRING)
    public StatusProduto status = StatusProduto.RASCUNHO;

    public void atualizarEstoque(int quantidade) {
        if (estoque + quantidade < 0) {
            throw new IllegalArgumentException("Estoque insuficiente");
        }
        estoque += quantidade;
    }

    public void publicar() {
        status = StatusProduto.PUBLICADO;
    }
}