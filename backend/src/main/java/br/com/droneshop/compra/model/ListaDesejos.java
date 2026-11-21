package br.com.droneshop.compra.model;

import java.util.ArrayList;
import java.util.List;

import br.com.droneshop.identidade.model.PerfilCliente;
import br.com.droneshop.model.DefaultEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/** Lista de desejos do cliente (0..1), criada no primeiro item (RF19). */
@Entity
@Table(name = "lista_desejos")
public class ListaDesejos extends DefaultEntity {

    @OneToOne(optional = false)
    @JoinColumn(name = "id_perfil_cliente", nullable = false, unique = true)
    private PerfilCliente cliente;

    @OneToMany(mappedBy = "lista", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("adicionadoEm DESC, id DESC")
    private List<ItemListaDesejos> itens = new ArrayList<>();

    public PerfilCliente getCliente() {
        return cliente;
    }

    public void setCliente(PerfilCliente cliente) {
        this.cliente = cliente;
    }

    public List<ItemListaDesejos> getItens() {
        return itens;
    }
}
