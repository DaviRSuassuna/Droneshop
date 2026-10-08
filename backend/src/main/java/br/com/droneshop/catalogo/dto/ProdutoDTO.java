package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class ProdutoDTO {

    @NotBlank(message = "O campo deve ser informado (back).")
    @Length(min = 2, max = 120, message = "O campo deve conter entre 2 e 120 caracteres (back).")
    private final String nome;

    @NotBlank(message = "O campo deve ser informado (back).")
    @Length(max = 40, message = "O campo deve ter no máximo 40 caracteres (back).")
    private final String modelo;

    @Length(max = 300, message = "O campo deve ter no máximo 300 caracteres (back).")
    private final String descricaoCurta;

    @NotNull(message = "O campo deve ser informado (back).")
    @DecimalMin(value = "0.0", message = "O preço não pode ser negativo (back).")
    private final BigDecimal preco;

    @NotNull(message = "Selecione a marca (back).")
    private final Long marcaId;

    @NotNull(message = "Selecione a subcategoria (back).")
    private final Long subcategoriaId;

    @NotNull(message = "Selecione o fornecedor (back).")
    private final Long fornecedorId;

    // valor esperado: "RASCUNHO", "ATIVO" ou "INATIVO"
    @NotBlank(message = "O campo deve ser informado (back).")
    private final String status;

    private final Boolean destaque;

    public ProdutoDTO(String nome, String modelo, String descricaoCurta, BigDecimal preco,
                      Long marcaId, Long subcategoriaId, Long fornecedorId, String status, Boolean destaque) {
        this.nome = nome;
        this.modelo = modelo;
        this.descricaoCurta = descricaoCurta;
        this.preco = preco;
        this.marcaId = marcaId;
        this.subcategoriaId = subcategoriaId;
        this.fornecedorId = fornecedorId;
        this.status = status;
        this.destaque = destaque;
    }

    public String getNome() {
        return nome;
    }

    public String getModelo() {
        return modelo;
    }

    public String getDescricaoCurta() {
        return descricaoCurta;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public Long getMarcaId() {
        return marcaId;
    }

    public Long getSubcategoriaId() {
        return subcategoriaId;
    }

    public Long getFornecedorId() {
        return fornecedorId;
    }

    public String getStatus() {
        return status;
    }

    public Boolean getDestaque() {
        return destaque;
    }
}
