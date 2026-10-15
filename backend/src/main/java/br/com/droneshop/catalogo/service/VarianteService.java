package br.com.droneshop.catalogo.service;

import java.util.List;

import br.com.droneshop.catalogo.dto.VarianteRequestDTO;
import br.com.droneshop.catalogo.model.VarianteProduto;
import jakarta.validation.Valid;

// Consulta e alteracao avulsa da variante. Criar e excluir e pelo corpo do drone/acessorio (D13).
public interface VarianteService {

    void update(long id, @Valid VarianteRequestDTO variante);
    VarianteProduto findById(long id);
    // filtros opcionais: busca no SKU, no nome da variante e no do produto
    List<VarianteProduto> findComFiltro(String busca, Boolean ativa, int page, int pageSize);
    long countComFiltro(String busca, Boolean ativa);

}
