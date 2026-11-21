package br.com.droneshop.estoque.mapper;

import br.com.droneshop.catalogo.model.Kit;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.estoque.dto.EstoqueResponseDTO;
import br.com.droneshop.estoque.dto.MovimentoEstoqueResponseDTO;
import br.com.droneshop.estoque.model.MovimentoEstoque;
import br.com.droneshop.estoque.service.ReservaSaldo;
import br.com.droneshop.identidade.model.Usuario;

public final class EstoqueResponseMapper {

    private EstoqueResponseMapper() {
    }

    public static MovimentoEstoqueResponseDTO toResponse(MovimentoEstoque m) {
        Usuario autor = m.getAutor();
        Produto p = m.getProduto();
        return new MovimentoEstoqueResponseDTO(
                m.getId(),
                m.getTipo(),
                m.getOrigem(),
                m.getQuantidade(),
                m.getSaldoResultante(),
                m.getObservacao(),
                (autor == null) ? null : new MovimentoEstoqueResponseDTO.Autor(autor.getId(), autor.getNome()),
                m.getDataCadastro(),
                new MovimentoEstoqueResponseDTO.Produto(p.getId(), p.getSku(), p.getNome()));
    }

    // Kit nao entra no alerta de minimo: quem se repoe e o componente
    public static EstoqueResponseDTO toEstoque(Produto p) {
        int disponivel = ReservaSaldo.disponivel(p);
        boolean kit = p instanceof Kit;
        return new EstoqueResponseDTO(
                new EstoqueResponseDTO.Produto(p.getId(), p.getSku(), p.getNome(), p.getTipo(), p.getStatus()),
                p.getEstoqueFisico(),
                ReservaSaldo.reservado(p),
                disponivel,
                p.getEstoqueMinimo(),
                !kit && disponivel < p.getEstoqueMinimo());
    }
}
