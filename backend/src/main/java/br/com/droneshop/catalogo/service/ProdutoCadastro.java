package br.com.droneshop.catalogo.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import br.com.droneshop.administracao.model.ConfiguracaoLoja;
import br.com.droneshop.administracao.repository.ConfiguracaoLojaRepository;
import br.com.droneshop.catalogo.dto.ComponenteKitRequestDTO;
import br.com.droneshop.catalogo.dto.ProdutoRequestDTO;
import br.com.droneshop.catalogo.model.ComponenteKit;
import br.com.droneshop.catalogo.model.Fornecedor;
import br.com.droneshop.catalogo.model.GrupoProduto;
import br.com.droneshop.catalogo.model.Kit;
import br.com.droneshop.catalogo.model.Marca;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.model.Subcategoria;
import br.com.droneshop.catalogo.model.TipoProduto;
import br.com.droneshop.catalogo.repository.ComponenteKitRepository;
import br.com.droneshop.catalogo.repository.FornecedorRepository;
import br.com.droneshop.catalogo.repository.GrupoProdutoRepository;
import br.com.droneshop.catalogo.repository.MarcaRepository;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.catalogo.repository.SubcategoriaRepository;
import br.com.droneshop.estoque.service.MovimentoEstoqueService;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.Problem;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.util.Textos;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

// Parte comum do cadastro de drone, acessorio e kit: status, obrigatorios, unicidade
// de SKU/slug, preco, estoque, embalagem e grupo (D40). Roda dentro da transacao de quem chama.
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
    GrupoProdutoRepository grupoRepository;

    @Inject
    ComponenteKitRepository componenteKitRepository;

    @Inject
    ConfiguracaoLojaRepository configuracaoLojaRepository;

    @Inject
    MovimentoEstoqueService movimentoEstoqueService;

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

    // Rascunho so exige o status (D28). Fora dele, junta os obrigatorios comuns com os do
    // tipo (errosDoTipo) e responde tudo num 400 so. ATIVO/INATIVO precisa de preco e de
    // peso e dimensoes da embalagem, senao nao cota frete (RN18). partesDoKit e nulo no
    // produto com embalagem propria; no kit, o que nao for informado vem dos componentes (D16).
    public void validarObrigatorios(ProdutoRequestDTO dto, StatusProduto status, List<KitCalculo.Parte> partesDoKit,
            List<Problem.FieldError> errosDoTipo) {
        if (status == StatusProduto.RASCUNHO)
            return;

        List<Problem.FieldError> erros = new ArrayList<>();
        if (dto.nome() == null)
            erros.add(new Problem.FieldError("nome", OBRIGATORIO));
        if (dto.sku() == null)
            erros.add(new Problem.FieldError("sku", OBRIGATORIO));
        if (dto.idMarca() == null)
            erros.add(new Problem.FieldError("idMarca", "Selecione a marca (back)."));
        if (dto.idSubcategoria() == null)
            erros.add(new Problem.FieldError("idSubcategoria", "Selecione a subcategoria (back)."));
        if (dto.idFornecedor() == null)
            erros.add(new Problem.FieldError("idFornecedor", "Selecione o fornecedor (back)."));
        if (status == null)
            erros.add(new Problem.FieldError("idStatus", "O status deve ser informado (back)."));
        if (dto.preco() == null)
            erros.add(new Problem.FieldError("preco", "Informe o preço de venda para publicar o produto (back)."));
        erros.addAll(errosDaEmbalagem(dto, partesDoKit));
        erros.addAll(errosDoTipo);

        if (!erros.isEmpty())
            throw new ValidationException("Preencha os campos obrigatórios para publicar o produto.", erros);
    }

    public void aplicar(Produto destino, ProdutoRequestDTO dto, StatusProduto status, Long idAtual) {
        String sku = (dto.sku() == null) ? null : dto.sku().trim().toUpperCase();
        if (sku != null && produtoRepository.findBySkuExceptId(sku, idAtual) != null)
            throw ValidationException.of("sku", "Já existe um produto com esse SKU.");

        String slug = resolverSlug(dto.slug(), dto.nome());
        if (produtoRepository.findBySlugExceptId(slug, idAtual) != null)
            throw ValidationException.of("slug", "Já existe um produto com esse slug.");

        GrupoProduto grupo = resolverGrupo(dto, idAtual);

        destino.setNome(dto.nome() == null ? null : dto.nome().trim());
        destino.setSku(sku);
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
        destino.setPreco(dto.preco());
        destino.setCustoAquisicao(dto.custoAquisicao());
        destino.setPesoEmbalagemKg(dto.pesoEmbalagemKg());
        destino.setAlturaEmbalagemCm(dto.alturaEmbalagemCm());
        destino.setLarguraEmbalagemCm(dto.larguraEmbalagemCm());
        destino.setComprimentoEmbalagemCm(dto.comprimentoEmbalagemCm());
        destino.setGrupo(grupo);
        destino.setRotuloNoGrupo(grupo == null ? null : dto.rotuloNoGrupo().trim());
        destino.setOrdemNoGrupo(grupo == null || dto.ordemNoGrupo() == null ? 0 : dto.ordemNoGrupo());

        // estoqueFisico so muda na criacao, com o movimento ENTRADA do saldo inicial (#13);
        // no PUT ele e ignorado. Kit nao tem estoque proprio (D7).
        if (destino instanceof Kit)
            return;
        destino.setEstoqueMinimo(dto.estoqueMinimo() == null ? 0 : dto.estoqueMinimo());
        if (idAtual == null)
            movimentoEstoqueService.entradaInicial(destino, dto.estoqueFisico() == null ? 0 : dto.estoqueFisico());
    }

    // Regras do kit (D7) que valem ate no rascunho: sem estoque proprio, componente existe,
    // nao e kit, nao e o proprio kit e nao se repete. Devolve as partes validas, de onde
    // saem peso e dimensoes derivados.
    public List<KitCalculo.Parte> validarComponentes(Integer estoqueFisico, List<ComponenteKitRequestDTO> componentes,
            Long idKit) {
        List<Problem.FieldError> erros = new ArrayList<>();
        if (estoqueFisico != null)
            erros.add(new Problem.FieldError("estoqueFisico",
                    "O kit não tem estoque próprio: a disponibilidade vem dos componentes (back)."));

        List<ComponenteKitRequestDTO> lista = (componentes == null) ? List.of() : componentes;
        List<KitCalculo.Parte> partes = new ArrayList<>();
        Set<Long> vistos = new HashSet<>();
        for (int i = 0; i < lista.size(); i++) {
            ComponenteKitRequestDTO c = lista.get(i);
            String prefixo = "componentes[" + i + "]";
            if (c == null) {
                erros.add(new Problem.FieldError(prefixo, "Componente inválido (back)."));
                continue;
            }
            Set<ConstraintViolation<ComponenteKitRequestDTO>> violacoes = validator.validate(c);
            for (ConstraintViolation<ComponenteKitRequestDTO> cv : violacoes)
                erros.add(new Problem.FieldError(prefixo + "." + cv.getPropertyPath(), cv.getMessage()));
            if (c.idProduto() == null)
                continue;

            String campo = prefixo + ".idProduto";
            if (!vistos.add(c.idProduto())) {
                erros.add(new Problem.FieldError(campo, "O mesmo produto aparece duas vezes no kit; some as quantidades (back)."));
                continue;
            }
            if (c.idProduto().equals(idKit)) {
                erros.add(new Problem.FieldError(campo, "O kit não pode conter a si mesmo (back)."));
                continue;
            }
            Produto componente = produtoRepository.findById(c.idProduto());
            if (componente == null)
                erros.add(new Problem.FieldError(campo, "Produto componente não encontrado (back)."));
            else if (componente instanceof Kit)
                erros.add(new Problem.FieldError(campo, "Um kit não pode conter outro kit (back)."));
            else if (violacoes.isEmpty())
                partes.add(new KitCalculo.Parte(componente, c.quantidade()));
        }
        if (!erros.isEmpty())
            throw new ValidationException("Revise a composição do kit.", erros);
        return partes;
    }

    // Atualiza no lugar em vez de apagar e recriar: o Hibernate insere antes de apagar,
    // e o mesmo produto bateria na chave unica (kit, produto)
    public void sincronizarComponentes(Kit kit, List<KitCalculo.Parte> partes) {
        Map<Long, KitCalculo.Parte> pedidas = new LinkedHashMap<>();
        for (KitCalculo.Parte p : partes)
            pedidas.put(p.produto().getId(), p);
        kit.getComponentes().removeIf(c -> !pedidas.containsKey(c.getProduto().getId()));
        for (ComponenteKit c : kit.getComponentes())
            c.setQuantidade(pedidas.remove(c.getProduto().getId()).quantidade());
        pedidas.values().forEach(p -> {
            ComponenteKit c = new ComponenteKit();
            c.setKit(kit);
            c.setProduto(p.produto());
            c.setQuantidade(p.quantidade());
            kit.getComponentes().add(c);
        });
    }

    // 409 se o produto e componente de algum kit (D24); o kit em si pode sair
    public void conferirUsoEmKits(Produto produto) {
        ComponenteKit uso = componenteKitRepository.findUsoDoProduto(produto.getId());
        if (uso == null)
            return;
        throw new ConflitoException("O produto " + rotulo(produto) + " faz parte do kit " + rotulo(uso.getKit())
                + ". Tire-o do kit antes de excluir, ou inative-o.");
    }

    public void persistir(Produto produto) {
        produtoRepository.persist(produto);
    }

    private List<Problem.FieldError> errosDaEmbalagem(ProdutoRequestDTO dto, List<KitCalculo.Parte> partesDoKit) {
        List<KitCalculo.Parte> partes = (partesDoKit == null) ? List.of() : partesDoKit;
        String mensagem = (partesDoKit == null)
                ? "Informe o peso e as dimensões da embalagem para publicar o produto (back)."
                : "Informe a medida do kit ou complete a dos componentes para publicar (back).";
        List<Problem.FieldError> erros = new ArrayList<>();
        if (dto.pesoEmbalagemKg() == null && KitCalculo.pesoDerivado(partes) == null)
            erros.add(new Problem.FieldError("pesoEmbalagemKg", mensagem));
        if (dto.alturaEmbalagemCm() == null && KitCalculo.alturaDerivada(partes) == null)
            erros.add(new Problem.FieldError("alturaEmbalagemCm", mensagem));
        if (dto.larguraEmbalagemCm() == null && KitCalculo.larguraDerivada(partes) == null)
            erros.add(new Problem.FieldError("larguraEmbalagemCm", mensagem));
        if (dto.comprimentoEmbalagemCm() == null && KitCalculo.comprimentoDerivado(partes) == null)
            erros.add(new Problem.FieldError("comprimentoEmbalagemCm", mensagem));
        return erros;
    }

    // Com grupo, o rotulo e obrigatorio (em qualquer status) e unico dentro dele (D40).
    // Sem grupo, rotulo e ordem sao descartados.
    private GrupoProduto resolverGrupo(ProdutoRequestDTO dto, Long idAtual) {
        if (dto.idGrupo() == null)
            return null;
        GrupoProduto grupo = grupoRepository.findById(dto.idGrupo());
        if (grupo == null)
            throw ValidationException.of("idGrupo", "Grupo não encontrado.");
        if (dto.rotuloNoGrupo() == null)
            throw ValidationException.of("rotuloNoGrupo", "Informe o rótulo da versão no grupo (back).");
        if (produtoRepository.findByRotuloNoGrupoExceptId(grupo.getId(), dto.rotuloNoGrupo().trim(), idAtual) != null)
            throw ValidationException.of("rotuloNoGrupo", "Já existe uma versão com esse rótulo no grupo.");
        return grupo;
    }

    private static String rotulo(Produto p) {
        if (p.getSku() != null)
            return p.getSku();
        return (p.getNome() != null) ? p.getNome() : "#" + p.getId();
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

    // O tipo vem do produto; a categoria so organiza dentro dele, entao precisa ser do mesmo
    // tipo. O kit entra em subcategoria de qualquer tipo (o Fly More fica em drones).
    private Subcategoria buscarSubcategoriaOuFalhar(Long idSubcategoria, TipoProduto tipo) {
        Subcategoria subcategoria = subcategoriaRepository.findById(idSubcategoria);
        if (subcategoria == null)
            throw ValidationException.of("idSubcategoria", "Subcategoria não encontrada.");
        if (subcategoria.getCategoria() == null)
            throw ValidationException.of("idSubcategoria", "Escolha uma subcategoria com categoria definida (back).");
        if (tipo != TipoProduto.KIT && subcategoria.getCategoria().getTipo() != tipo)
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
