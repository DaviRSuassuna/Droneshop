package br.com.droneshop.estoque.mapper;

import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.VarianteProduto;
import br.com.droneshop.catalogo.service.KitCalculo;
import br.com.droneshop.estoque.dto.EstoqueResponseDTO;
import br.com.droneshop.estoque.dto.MovimentoEstoqueResponseDTO;
import br.com.droneshop.estoque.model.MovimentoEstoque;
import br.com.droneshop.identidade.model.Usuario;

public final class EstoqueResponseMapper {

    private EstoqueResponseMapper() {
    }

    public static MovimentoEstoqueResponseDTO toResponse(MovimentoEstoque m) {
        Usuario autor = m.getAutor();
        VarianteProduto v = m.getVariante();
        return new MovimentoEstoqueResponseDTO(
                m.getId(),
                m.getTipo(),
                m.getOrigem(),
                m.getQuantidade(),
                m.getSaldoResultante(),
                m.getObservacao(),
                (autor == null) ? null : new MovimentoEstoqueResponseDTO.Autor(autor.getId(), autor.getNome()),
                m.getDataCadastro(),
                new MovimentoEstoqueResponseDTO.Variante(v.getId(), v.getSku(), v.getNome()));
    }

    // Kit nao entra no alerta de minimo: quem se repoe e o componente
    public static EstoqueResponseDTO toEstoque(VarianteProduto v) {
        Produto p = v.getProduto();
        int disponivel = KitCalculo.disponivel(v);
        return new EstoqueResponseDTO(
                new EstoqueResponseDTO.Variante(v.getId(), v.getSku(), v.getNome(), v.isEhKit(), v.isAtiva()),
                new EstoqueResponseDTO.Produto(p.getId(), p.getNome()),
                v.getEstoqueFisico(),
                0,
                disponivel,
                v.getEstoqueMinimo(),
                !v.isEhKit() && disponivel < v.getEstoqueMinimo());
    }
}
