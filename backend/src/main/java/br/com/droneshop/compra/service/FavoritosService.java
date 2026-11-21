package br.com.droneshop.compra.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.compra.dto.CarrinhoResponseDTO;
import br.com.droneshop.compra.dto.ItemCarrinhoRequestDTO;
import br.com.droneshop.compra.dto.ListaCompartilhadaResponseDTO;
import br.com.droneshop.compra.dto.MoverParaCarrinhoResponseDTO;
import br.com.droneshop.compra.dto.ProdutoResumoDTO;
import br.com.droneshop.compra.model.ItemListaDesejos;
import br.com.droneshop.compra.model.ListaDesejos;
import br.com.droneshop.compra.repository.ListaDesejosRepository;
import br.com.droneshop.estoque.service.ReservaSaldo;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.identidade.repository.PerfilClienteRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/** Favoritos: mover tudo para o carrinho e lista compartilhada (#86). */
@ApplicationScoped
public class FavoritosService {

    @Inject
    ListaDesejosRepository listas;

    @Inject
    PerfilClienteRepository clientes;

    @Inject
    CarrinhoService carrinhos;

    // Cada item disponível entra com quantidade 1 e sai da lista; o resto fica com o motivo
    @Transactional
    public MoverParaCarrinhoResponseDTO moverParaCarrinho(Long idUsuario) {
        CarrinhoResponseDTO carrinho = carrinhos.doCliente(idUsuario);
        List<MoverParaCarrinhoResponseDTO.NaoAdicionado> fora = new ArrayList<>();
        ListaDesejos lista = listas.doUsuario(idUsuario);
        if (lista != null) {
            for (ItemListaDesejos item : new ArrayList<>(lista.getItens())) {
                var p = item.getProduto();
                if (p.getStatus() != StatusProduto.ATIVO) {
                    fora.add(new MoverParaCarrinhoResponseDTO.NaoAdicionado(p.getId(), "Indisponível para venda"));
                    continue;
                }
                // confere antes: exceção dentro da transação marcaria tudo para desfazer
                int noCarrinho = carrinho.itens().stream().filter(i -> i.produto().id().equals(p.getId()))
                        .mapToInt(CarrinhoResponseDTO.Item::quantidade).sum();
                if (p.getPreco() == null || ReservaSaldo.disponivel(p) < noCarrinho + 1) {
                    fora.add(new MoverParaCarrinhoResponseDTO.NaoAdicionado(p.getId(), "Sem estoque"));
                    continue;
                }
                carrinho = carrinhos.adicionar(carrinho.token(), new ItemCarrinhoRequestDTO(p.getId(), 1));
                lista.getItens().remove(item);
            }
        }
        return new MoverParaCarrinhoResponseDTO(carrinho, fora);
    }

    @Transactional
    public String compartilhar(Long idUsuario) {
        ListaDesejos lista = listas.doUsuario(idUsuario);
        if (lista == null) {
            lista = new ListaDesejos();
            lista.setCliente(clientes.porUsuario(idUsuario));
            listas.persist(lista);
        }
        if (lista.getTokenCompartilhamento() == null)
            lista.setTokenCompartilhamento(UUID.randomUUID().toString());
        return lista.getTokenCompartilhamento();
    }

    @Transactional
    public void desligar(Long idUsuario) {
        ListaDesejos lista = listas.doUsuario(idUsuario);
        if (lista != null)
            lista.setTokenCompartilhamento(null);
    }

    // Público: só o primeiro nome (LGPD) e só produtos ATIVO (RN17)
    @Transactional
    public ListaCompartilhadaResponseDTO publica(String token) {
        ListaDesejos lista = listas.porToken(token);
        if (lista == null)
            throw new ResourceNotFoundException("Lista não encontrada.");
        String nome = lista.getCliente().getNome().trim().split("\\s+")[0];
        return new ListaCompartilhadaResponseDTO(nome, lista.getItens().stream()
                .filter(i -> i.getProduto().getStatus() == StatusProduto.ATIVO)
                .map(i -> new ListaCompartilhadaResponseDTO.Item(ProdutoResumoDTO.de(i.getProduto()))).toList());
    }
}
