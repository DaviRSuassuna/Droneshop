package br.com.droneshop.catalogo.service;

import java.math.BigDecimal;
import java.util.List;

import br.com.droneshop.catalogo.dto.CategoriaLojaDTO;
import br.com.droneshop.catalogo.dto.FiltrosLojaDTO;
import br.com.droneshop.catalogo.dto.MarcaLojaDTO;
import br.com.droneshop.catalogo.dto.ProdutoLojaDetalheDTO;
import br.com.droneshop.catalogo.dto.ProdutoLojaItemDTO;
import br.com.droneshop.dto.PageResponse;

/** Rotas públicas da vitrine (#21). Só ATIVO aparece (RN17). */
public interface LojaService {

    List<CategoriaLojaDTO> listarCategorias(String tipo);

    List<MarcaLojaDTO> listarMarcas(Boolean destaque);

    PageResponse<ProdutoLojaItemDTO> listarProdutos(Filtro filtro, String ordem, int page, int pageSize);

    FiltrosLojaDTO buscarFiltros(String categoria);

    ProdutoLojaDetalheDTO buscarProduto(String slug);

    record Filtro(String categoria, String subcategoria, String marca, String tipo, BigDecimal precoMin,
            BigDecimal precoMax, Integer autonomiaMin, Integer alcanceMin, String nome) {
    }
}
