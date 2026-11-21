package br.com.droneshop.compra.service;

import java.time.LocalDateTime;
import java.util.List;

import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.compra.dto.ItemListaDesejosResponseDTO;
import br.com.droneshop.compra.model.ItemListaDesejos;
import br.com.droneshop.compra.model.ListaDesejos;
import br.com.droneshop.compra.repository.ListaDesejosRepository;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.identidade.model.PerfilCliente;
import br.com.droneshop.identidade.repository.PerfilClienteRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ListaDesejosServiceImpl implements ListaDesejosService {

    @Inject
    ListaDesejosRepository listas;

    @Inject
    PerfilClienteRepository clientes;

    @Inject
    ProdutoRepository produtos;

    @Override
    @Transactional
    public List<ItemListaDesejosResponseDTO> listar(Long idUsuario) {
        ListaDesejos lista = listas.doUsuario(idUsuario);
        return lista == null ? List.of() : lista.getItens().stream().map(ItemListaDesejosResponseDTO::de).toList();
    }

    @Override
    @Transactional
    public void adicionar(Long idUsuario, Long idProduto) {
        Produto produto = produtos.findById(idProduto);
        if (produto == null)
            throw ValidationException.of("idProduto", "Produto não encontrado (back)");
        ListaDesejos lista = listas.doUsuario(idUsuario);
        if (lista == null) {
            PerfilCliente cliente = clientes.porUsuario(idUsuario);
            lista = new ListaDesejos();
            lista.setCliente(cliente);
            listas.persist(lista);
        }
        boolean jaTem = lista.getItens().stream().anyMatch(i -> i.getProduto().getId().equals(idProduto));
        if (jaTem)
            return;
        ItemListaDesejos item = new ItemListaDesejos();
        item.setLista(lista);
        item.setProduto(produto);
        item.setAdicionadoEm(LocalDateTime.now());
        item.setPrecoAoAdicionar(produto.getPreco());
        lista.getItens().add(item);
    }

    @Override
    @Transactional
    public void remover(Long idUsuario, Long idProduto) {
        ListaDesejos lista = listas.doUsuario(idUsuario);
        boolean removeu = lista != null
                && lista.getItens().removeIf(i -> i.getProduto().getId().equals(idProduto));
        if (!removeu)
            throw new ResourceNotFoundException("Produto não está na lista de desejos.");
    }
}
