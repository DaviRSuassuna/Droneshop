package br.com.droneshop.catalogo.model;

import java.util.ArrayList;
import java.util.List;

import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

// Junta versoes parecidas numa pagina de venda (D40). Cada versao continua sendo um
// produto; na vitrine o grupo vira um card so, "a partir de" o menor preco ativo.
// Pode misturar tipos (drone e kit, por exemplo).
@Entity
@Table(name = "grupo_produto")
public class GrupoProduto extends DefaultEntity {

    @Column(length = 120, nullable = false)
    private String nome;

    @Column(length = 140, nullable = false, unique = true)
    private String slug;

    @Column(length = 300, name = "descricao_curta")
    private String descricaoCurta;

    @Column(nullable = false)
    private boolean ativo;

    // lado so de leitura: quem grava o vinculo e o produto
    @OneToMany(mappedBy = "grupo")
    @OrderBy("ordemNoGrupo, id")
    private List<Produto> produtos = new ArrayList<>();

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescricaoCurta() {
        return descricaoCurta;
    }

    public void setDescricaoCurta(String descricaoCurta) {
        this.descricaoCurta = descricaoCurta;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public List<Produto> getProdutos() {
        return produtos;
    }

}
