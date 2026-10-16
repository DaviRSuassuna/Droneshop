package br.com.droneshop.catalogo.service;

import java.util.List;

import br.com.droneshop.catalogo.dto.VarianteRequestDTO;
import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.model.VarianteProduto;
import br.com.droneshop.catalogo.repository.VarianteProdutoRepository;
import br.com.droneshop.exception.Problem;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class VarianteServiceImpl implements VarianteService {

    @Inject
    VarianteProdutoRepository varianteRepository;

    @Inject
    ProdutoCadastro cadastro;

    // as mesmas regras do corpo do produto, conforme o status dele; estoqueFisico e ignorado
    @Override
    @Transactional
    public void update(long id, VarianteRequestDTO dto) {
        VarianteProduto variante = buscarOuFalhar(id);

        if (variante.getProduto().getStatus() != StatusProduto.RASCUNHO) {
            List<Problem.FieldError> erros = cadastro.obrigatoriosDaVariante(dto, "");
            if (!erros.isEmpty())
                throw new ValidationException("Preencha os campos obrigatórios da variante.", erros);
        }

        String sku = cadastro.normalizarSku(dto.sku());
        if (sku != null && varianteRepository.findBySkuExceptId(sku, id) != null)
            throw ValidationException.of("sku", "Já existe uma variante com esse SKU.");

        cadastro.aplicarVariante(variante, dto);
    }

    @Override
    public VarianteProduto findById(long id) {
        return buscarOuFalhar(id);
    }

    @Override
    public List<VarianteProduto> findComFiltro(String busca, Boolean ativa, int page, int pageSize) {
        return varianteRepository.findComFiltro(busca, ativa).page(page, pageSize).list();
    }

    @Override
    public long countComFiltro(String busca, Boolean ativa) {
        return varianteRepository.findComFiltro(busca, ativa).count();
    }

    private VarianteProduto buscarOuFalhar(long id) {
        VarianteProduto variante = varianteRepository.findById(id);
        if (variante == null)
            throw new ResourceNotFoundException("Variante não encontrada.");
        return variante;
    }

}
