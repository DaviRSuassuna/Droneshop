package br.com.droneshop.compra.model;

import java.time.LocalDateTime;

import br.com.droneshop.identidade.model.PerfilCliente;
import br.com.droneshop.model.DefaultEntity;
import br.com.droneshop.pedido.model.Pedido;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "uso_cupom")
public class UsoCupom extends DefaultEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cupom", nullable = false)
    private Cupom cupom;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private PerfilCliente cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @Column(nullable = false, name = "data_uso")
    private LocalDateTime dataUso;

    @PrePersist
    void definirDataUso() {
        if (dataUso == null)
            dataUso = LocalDateTime.now();
    }

    public Cupom getCupom() { return cupom; }
    public void setCupom(Cupom cupom) { this.cupom = cupom; }
    public PerfilCliente getCliente() { return cliente; }
    public void setCliente(PerfilCliente cliente) { this.cliente = cliente; }
    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }
    public LocalDateTime getDataUso() { return dataUso; }
    public void setDataUso(LocalDateTime dataUso) { this.dataUso = dataUso; }
}
