package br.com.droneshop.catalogo.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

// Kit ou combo (D7, D40): produto sem estoque proprio. estoqueFisico fica nulo e a
// disponibilidade e o minimo entre os componentes; peso e dimensoes nulos sao derivados
// deles (D16). A baixa acontece sempre no componente, nunca no kit.
@Entity
@DiscriminatorValue("3")
public class Kit extends Produto {

    // os produtos que ele reune, cada um com a quantidade exigida
    @OneToMany(mappedBy = "kit", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ComponenteKit> componentes = new ArrayList<>();

    // Kit ATIVO com a opcao ligada aparece em /loja/ofertas (#84)
    @Column(nullable = false, name = "exibir_em_ofertas", columnDefinition = "boolean default false")
    private boolean exibirEmOfertas;

    @Override
    public TipoProduto getTipo() {
        return TipoProduto.KIT;
    }

    public List<ComponenteKit> getComponentes() {
        return componentes;
    }

    public void setComponentes(List<ComponenteKit> componentes) {
        this.componentes = componentes;
    }

    public boolean isExibirEmOfertas() {
        return exibirEmOfertas;
    }

    public void setExibirEmOfertas(boolean exibirEmOfertas) {
        this.exibirEmOfertas = exibirEmOfertas;
    }

}
