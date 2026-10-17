package br.com.droneshop.estoque.service;

import java.util.List;

import br.com.droneshop.catalogo.model.VarianteProduto;
import br.com.droneshop.estoque.dto.MovimentoEstoqueRequestDTO;
import br.com.droneshop.estoque.dto.SituacaoEstoque;
import br.com.droneshop.estoque.model.MovimentoEstoque;
import br.com.droneshop.estoque.model.TipoMovimento;
import jakarta.validation.Valid;

// Unico caminho que altera o estoque fisico da variante depois da criacao (RF14)
public interface MovimentoEstoqueService {

    MovimentoEstoque registrar(@Valid MovimentoEstoqueRequestDTO dto);

    // saldo inicial da variante nova (#9): ENTRADA MANUAL do usuario logado, gravada junto com ela
    void entradaInicial(VarianteProduto variante, int quantidade);

    TipoMovimento tipo(String idTipo);
    List<MovimentoEstoque> findComFiltro(Long idVariante, TipoMovimento tipo, int page, int pageSize);
    long countComFiltro(Long idVariante, TipoMovimento tipo);

    SituacaoEstoque situacao(String situacao);
    List<VarianteProduto> findEstoque(SituacaoEstoque situacao, String busca, int page, int pageSize);
    long countEstoque(SituacaoEstoque situacao, String busca);

}
