package br.com.droneshop.estoque.service;

import java.util.List;

import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.estoque.dto.MovimentoEstoqueRequestDTO;
import br.com.droneshop.estoque.dto.SituacaoEstoque;
import br.com.droneshop.estoque.model.MovimentoEstoque;
import br.com.droneshop.estoque.model.TipoMovimento;
import jakarta.validation.Valid;

// Unico caminho que altera o estoque fisico do produto depois da criacao (RF14)
public interface MovimentoEstoqueService {

    MovimentoEstoque registrar(@Valid MovimentoEstoqueRequestDTO dto);

    // saldo inicial do produto novo (D40): ENTRADA MANUAL do usuario logado, gravada junto com ele
    void entradaInicial(Produto produto, int quantidade);

    TipoMovimento tipo(String idTipo);
    List<MovimentoEstoque> findComFiltro(Long idProduto, TipoMovimento tipo, int page, int pageSize);
    long countComFiltro(Long idProduto, TipoMovimento tipo);

    SituacaoEstoque situacao(String situacao);
    // o mesmo filtro com outro nome de parametro (GET /produtos?estoque=)
    SituacaoEstoque situacao(String situacao, String campo);
    List<Produto> findEstoque(SituacaoEstoque situacao, String busca, Long idMarca, int page, int pageSize);
    long countEstoque(SituacaoEstoque situacao, String busca, Long idMarca);

}
