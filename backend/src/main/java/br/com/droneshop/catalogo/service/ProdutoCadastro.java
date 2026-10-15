package br.com.droneshop.catalogo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.droneshop.catalogo.dto.ProdutoRequestDTO;
import br.com.droneshop.catalogo.model.Fornecedor;
import br.com.droneshop.catalogo.model.Marca;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.model.Subcategoria;
import br.com.droneshop.catalogo.model.TipoProduto;
import br.com.droneshop.catalogo.repository.FornecedorRepository;
import br.com.droneshop.catalogo.repository.MarcaRepository;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.catalogo.repository.SubcategoriaRepository;
import br.com.droneshop.exception.Problem;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.util.Textos;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

// Parte comum do cadastro de drone e acessorio: status, obrigatorios, unicidade
// de modelo/slug e os campos herdados de Produto. Roda dentro da transacao de quem chama.
@ApplicationScoped
public class ProdutoCadastro {

    // provisorio: quando ConfiguracaoLoja existir, ler prazoGarantiaDias dela e remover esta constante
    private static final int PRAZO_GARANTIA_PADRAO_DIAS = 365;

    public static final String OBRIGATORIO = "O campo deve ser informado (back).";

    @Inject
    ProdutoRepository produtoRepository;

    @Inject
    MarcaRepository marcaRepository;

    @Inject
    SubcategoriaRepository subcategoriaRepository;

    @Inject
    FornecedorRepository fornecedorRepository;

    public StatusProduto status(Long idStatus) {
        StatusProduto status = StatusProduto.valueOf(idStatus);
        if (idStatus != null && status == null)
            throw ValidationException.of("idStatus", "Status não encontrado.");
        return status;
    }

    // Busca pelo id e confere o tipo: id de acessorio em /drones (e vice-versa) e 404.
    public <P extends Produto> P buscarOuFalhar(long id, Class<P> tipo) {
        Produto produto = produtoRepository.findById(id);
        if (!tipo.isInstance(produto))
            throw new ResourceNotFoundException("Produto não encontrado.");
        return tipo.cast(produto);
    }

    // Rascunho so exige o status (D28). Fora dele, junta os obrigatorios comuns com os
    // do tipo (errosDoTipo) e responde tudo num 400 so.
    public void validarObrigatorios(ProdutoRequestDTO dto, StatusProduto status, List<Problem.FieldError> errosDoTipo) {
        if (status == StatusProduto.RASCUNHO)
            return;

        List<Problem.FieldError> erros = new ArrayList<>();
        if (dto.nome() == null)
            erros.add(new Problem.FieldError("nome", OBRIGATORIO));
        if (dto.modelo() == null)
            erros.add(new Problem.FieldError("modelo", OBRIGATORIO));
        if (dto.idMarca() == null)
            erros.add(new Problem.FieldError("idMarca", "Selecione a marca (back)."));
        if (dto.idSubcategoria() == null)
            erros.add(new Problem.FieldError("idSubcategoria", "Selecione a subcategoria (back)."));
        if (dto.idFornecedor() == null)
            erros.add(new Problem.FieldError("idFornecedor", "Selecione o fornecedor (back)."));
        if (status == null)
            erros.add(new Problem.FieldError("idStatus", "O status deve ser informado (back)."));
        erros.addAll(errosDoTipo);

        if (!erros.isEmpty())
            throw new ValidationException("Preencha os campos obrigatórios para publicar o produto.", erros);
    }

    public void aplicar(Produto destino, ProdutoRequestDTO dto, StatusProduto status, Long idAtual) {
        String modelo = (dto.modelo() == null) ? null : dto.modelo().trim().toUpperCase();
        if (modelo != null && produtoRepository.findByModeloExceptId(modelo, idAtual) != null)
            throw ValidationException.of("modelo", "Já existe um produto com esse modelo/SKU.");

        String slug = resolverSlug(dto.slug(), dto.nome());
        if (produtoRepository.findBySlugExceptId(slug, idAtual) != null)
            throw ValidationException.of("slug", "Já existe um produto com esse slug.");

        destino.setNome(dto.nome() == null ? null : dto.nome().trim());
        destino.setModelo(modelo);
        destino.setDescricaoCurta(dto.descricaoCurta());
        destino.setDescricao(dto.descricao());
        destino.setSlug(slug);
        destino.setMetaTitle(dto.metaTitle());
        destino.setMetaDescription(dto.metaDescription());
        destino.setTags(dto.tags());
        destino.setGarantiaMeses(dto.garantiaMeses() != null ? dto.garantiaMeses() : garantiaPadraoMeses());
        destino.setDataLancamento(dto.dataLancamento());
        destino.setPreco(dto.preco());
        destino.setMarca(dto.idMarca() == null ? null : buscarMarcaOuFalhar(dto.idMarca()));
        destino.setSubcategoria(dto.idSubcategoria() == null ? null : buscarSubcategoriaOuFalhar(dto.idSubcategoria(), destino.getTipo()));
        destino.setFornecedor(dto.idFornecedor() == null ? null : buscarFornecedorOuFalhar(dto.idFornecedor()));
        destino.setStatus(status);
        destino.setDestaque(Boolean.TRUE.equals(dto.destaque()));
    }

    public void persistir(Produto produto) {
        produtoRepository.persist(produto);
    }

    private String resolverSlug(String slug, String nome) {
        if (slug != null)
            return Textos.slug(slug);
        if (nome != null)
            return Textos.slug(nome);
        return "rascunho-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private int garantiaPadraoMeses() {
        return (int) Math.ceil(PRAZO_GARANTIA_PADRAO_DIAS / 30.0);
    }

    private Marca buscarMarcaOuFalhar(Long idMarca) {
        Marca marca = marcaRepository.findById(idMarca);
        if (marca == null)
            throw ValidationException.of("idMarca", "Marca não encontrada.");
        return marca;
    }

    // O tipo vem do produto; a categoria so organiza dentro dele, entao precisa ser do mesmo tipo
    private Subcategoria buscarSubcategoriaOuFalhar(Long idSubcategoria, TipoProduto tipo) {
        Subcategoria subcategoria = subcategoriaRepository.findById(idSubcategoria);
        if (subcategoria == null)
            throw ValidationException.of("idSubcategoria", "Subcategoria não encontrada.");
        if (subcategoria.getCategoria().getTipo() != tipo)
            throw ValidationException.of("idSubcategoria", tipo == TipoProduto.DRONE
                    ? "Escolha uma subcategoria de drones."
                    : "Escolha uma subcategoria de acessórios.");
        return subcategoria;
    }

    private Fornecedor buscarFornecedorOuFalhar(Long idFornecedor) {
        Fornecedor fornecedor = fornecedorRepository.findById(idFornecedor);
        if (fornecedor == null)
            throw ValidationException.of("idFornecedor", "Fornecedor não encontrado.");
        return fornecedor;
    }

}
