package br.com.droneshop.catalogo.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import br.com.droneshop.administracao.model.ConfiguracaoLoja;
import br.com.droneshop.administracao.repository.ConfiguracaoLojaRepository;
import br.com.droneshop.catalogo.dto.ProdutoRequestDTO;
import br.com.droneshop.catalogo.dto.VarianteRequestDTO;
import br.com.droneshop.catalogo.model.Fornecedor;
import br.com.droneshop.catalogo.model.Marca;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.model.Subcategoria;
import br.com.droneshop.catalogo.model.TipoProduto;
import br.com.droneshop.catalogo.model.VarianteProduto;
import br.com.droneshop.catalogo.repository.FornecedorRepository;
import br.com.droneshop.catalogo.repository.MarcaRepository;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.catalogo.repository.SubcategoriaRepository;
import br.com.droneshop.catalogo.repository.VarianteProdutoRepository;
import br.com.droneshop.exception.Problem;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.util.Textos;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

// Parte comum do cadastro de drone e acessorio: status, obrigatorios, unicidade
// de modelo/slug/SKU, os campos herdados de Produto e as variantes (D13). Roda dentro
// da transacao de quem chama.
@ApplicationScoped
public class ProdutoCadastro {

    public static final String OBRIGATORIO = "O campo deve ser informado (back).";

    @Inject
    ProdutoRepository produtoRepository;

    @Inject
    MarcaRepository marcaRepository;

    @Inject
    SubcategoriaRepository subcategoriaRepository;

    @Inject
    FornecedorRepository fornecedorRepository;

    @Inject
    VarianteProdutoRepository varianteRepository;

    @Inject
    ConfiguracaoLojaRepository configuracaoLojaRepository;

    @Inject
    Validator validator;

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
        validarFormatoDasVariantes(dto.variantes());
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
        erros.addAll(obrigatoriosDasVariantes(dto.variantes()));

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
        destino.setMarca(dto.idMarca() == null ? null : buscarMarcaOuFalhar(dto.idMarca()));
        destino.setSubcategoria(dto.idSubcategoria() == null ? null : buscarSubcategoriaOuFalhar(dto.idSubcategoria(), destino.getTipo()));
        destino.setFornecedor(dto.idFornecedor() == null ? null : buscarFornecedorOuFalhar(dto.idFornecedor()));
        destino.setStatus(status);
        destino.setDestaque(Boolean.TRUE.equals(dto.destaque()));
        aplicarVariantes(destino, dto.variantes(), idAtual);
    }

    // Fora do rascunho (D28): variante precisa de nome, SKU e preco; a ativa precisa
    // tambem de peso e dimensoes da embalagem, senao nao cota frete (RN18).
    // prefixo e "variantes[i]." no corpo do produto e "" no PUT /variantes/{id}.
    public List<Problem.FieldError> obrigatoriosDaVariante(VarianteRequestDTO dto, String prefixo) {
        List<Problem.FieldError> erros = new ArrayList<>();
        if (dto.nome() == null)
            erros.add(new Problem.FieldError(prefixo + "nome", OBRIGATORIO));
        if (dto.sku() == null)
            erros.add(new Problem.FieldError(prefixo + "sku", OBRIGATORIO));
        if (dto.preco() == null)
            erros.add(new Problem.FieldError(prefixo + "preco", "Informe o preço da variante (back)."));
        if (!Boolean.FALSE.equals(dto.ativa())) {
            String embalagem = "Informe o peso e as dimensões da embalagem para publicar a variante (back).";
            if (dto.pesoEmbalagemKg() == null)
                erros.add(new Problem.FieldError(prefixo + "pesoEmbalagemKg", embalagem));
            if (dto.alturaEmbalagemCm() == null)
                erros.add(new Problem.FieldError(prefixo + "alturaEmbalagemCm", embalagem));
            if (dto.larguraEmbalagemCm() == null)
                erros.add(new Problem.FieldError(prefixo + "larguraEmbalagemCm", embalagem));
            if (dto.comprimentoEmbalagemCm() == null)
                erros.add(new Problem.FieldError(prefixo + "comprimentoEmbalagemCm", embalagem));
        }
        return erros;
    }

    // Campos que o PUT /variantes/{id} tambem altera. estoqueFisico fica de fora: depois
    // da criacao ele so muda por movimento de estoque (#13).
    public void aplicarVariante(VarianteProduto destino, VarianteRequestDTO dto) {
        destino.setNome(dto.nome() == null ? null : dto.nome().trim());
        destino.setSku(normalizarSku(dto.sku()));
        destino.setPreco(dto.preco());
        destino.setCustoAquisicao(dto.custoAquisicao());
        destino.setEstoqueMinimo(dto.estoqueMinimo() == null ? 0 : dto.estoqueMinimo());
        destino.setPesoEmbalagemKg(dto.pesoEmbalagemKg());
        destino.setAlturaEmbalagemCm(dto.alturaEmbalagemCm());
        destino.setLarguraEmbalagemCm(dto.larguraEmbalagemCm());
        destino.setComprimentoEmbalagemCm(dto.comprimentoEmbalagemCm());
        destino.setAtiva(!Boolean.FALSE.equals(dto.ativa()));
    }

    public String normalizarSku(String sku) {
        return (sku == null) ? null : sku.trim().toUpperCase();
    }

    // Bean Validation de cada item, com o indice no campo do erro
    private void validarFormatoDasVariantes(List<VarianteRequestDTO> variantes) {
        if (variantes == null)
            return;
        List<Problem.FieldError> erros = new ArrayList<>();
        for (int i = 0; i < variantes.size(); i++) {
            VarianteRequestDTO v = variantes.get(i);
            if (v == null) {
                erros.add(new Problem.FieldError("variantes[" + i + "]", "Variante inválida (back)."));
                continue;
            }
            for (ConstraintViolation<VarianteRequestDTO> cv : validator.validate(v))
                erros.add(new Problem.FieldError("variantes[" + i + "]." + cv.getPropertyPath(), cv.getMessage()));
        }
        if (!erros.isEmpty())
            throw new ValidationException("Um ou mais campos não passaram na validação.", erros);
    }

    private List<Problem.FieldError> obrigatoriosDasVariantes(List<VarianteRequestDTO> variantes) {
        if (variantes == null || variantes.isEmpty())
            return List.of(new Problem.FieldError("variantes",
                    "Cadastre ao menos uma variante para publicar o produto (back)."));
        List<Problem.FieldError> erros = new ArrayList<>();
        for (int i = 0; i < variantes.size(); i++)
            erros.addAll(obrigatoriosDaVariante(variantes.get(i), "variantes[" + i + "]."));
        return erros;
    }

    // Variante com id altera, sem id cria e a que sumiu da lista e excluida.
    // Quando pedido e reserva existirem, excluir variante com eles vira 409 (D28).
    private void aplicarVariantes(Produto produto, List<VarianteRequestDTO> dtos, Long idAtual) {
        List<VarianteRequestDTO> lista = (dtos == null) ? List.of() : dtos;

        Set<String> skus = new HashSet<>();
        for (int i = 0; i < lista.size(); i++) {
            String sku = normalizarSku(lista.get(i).sku());
            if (sku != null && (!skus.add(sku) || varianteRepository.findBySkuForaDoProduto(sku, idAtual) != null))
                throw ValidationException.of("variantes[" + i + "].sku", "Já existe uma variante com esse SKU.");
        }

        Map<Long, VarianteProduto> atuais = produto.getVariantes().stream()
                .collect(Collectors.toMap(VarianteProduto::getId, Function.identity()));
        Set<Long> mantidas = new HashSet<>();
        for (int i = 0; i < lista.size(); i++) {
            Long id = lista.get(i).id();
            if (id == null)
                continue;
            if (!atuais.containsKey(id))
                throw ValidationException.of("variantes[" + i + "].id", "Variante não encontrada neste produto.");
            mantidas.add(id);
        }

        // apaga antes de inserir: um SKU removido pode voltar numa variante nova no mesmo PUT
        if (produto.getVariantes().removeIf(v -> !mantidas.contains(v.getId())))
            produtoRepository.flush();

        for (VarianteRequestDTO dto : lista) {
            VarianteProduto variante = (dto.id() == null) ? null : atuais.get(dto.id());
            if (variante == null) {
                variante = new VarianteProduto();
                variante.setProduto(produto);
                // saldo inicial; o movimento ENTRADA correspondente chega com a #13
                variante.setEstoqueFisico(dto.estoqueFisico() == null ? 0 : dto.estoqueFisico());
                produto.getVariantes().add(variante);
            }
            aplicarVariante(variante, dto);
        }
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
        ConfiguracaoLoja configuracao = configuracaoLojaRepository.buscarUnica();
        if (configuracao == null)
            throw new ResourceNotFoundException("Configuração da loja não encontrada.");
        return (int) Math.ceil(configuracao.getPrazoGarantiaDias() / 30.0);
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
