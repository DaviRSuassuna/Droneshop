package br.com.droneshop.catalogo.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import br.com.droneshop.administracao.model.ConfiguracaoLoja;
import br.com.droneshop.administracao.repository.ConfiguracaoLojaRepository;
import br.com.droneshop.catalogo.dto.ComponenteKitRequestDTO;
import br.com.droneshop.catalogo.dto.ProdutoRequestDTO;
import br.com.droneshop.catalogo.dto.VarianteRequestDTO;
import br.com.droneshop.catalogo.model.ComponenteKit;
import br.com.droneshop.catalogo.model.Fornecedor;
import br.com.droneshop.catalogo.model.Marca;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.model.Subcategoria;
import br.com.droneshop.catalogo.model.TipoProduto;
import br.com.droneshop.catalogo.model.VarianteProduto;
import br.com.droneshop.catalogo.repository.ComponenteKitRepository;
import br.com.droneshop.catalogo.repository.FornecedorRepository;
import br.com.droneshop.catalogo.repository.MarcaRepository;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.catalogo.repository.SubcategoriaRepository;
import br.com.droneshop.catalogo.repository.VarianteProdutoRepository;
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

    // Rascunho so exige o status e um kit bem formado (D28). Fora dele, junta os obrigatorios comuns com os
    // do tipo (errosDoTipo) e responde tudo num 400 so.
    public void validarObrigatorios(ProdutoRequestDTO dto, StatusProduto status, List<Problem.FieldError> errosDoTipo) {
        validarFormatoDasVariantes(dto.variantes());
        List<Problem.FieldError> errosKit = errosDosKits(dto.variantes());
        if (status == StatusProduto.RASCUNHO) {
            if (!errosKit.isEmpty())
                throw new ValidationException("Revise a composição do kit.", errosKit);
            return;
        }

        List<Problem.FieldError> erros = new ArrayList<>(errosKit);
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
    // tambem de peso e dimensoes da embalagem, senao nao cota frete (RN18). No kit, o
    // que nao for informado vem dos componentes (D16), e ele precisa de ao menos um.
    // prefixo e "variantes[i]." no corpo do produto e "" no PUT /variantes/{id}.
    public List<Problem.FieldError> obrigatoriosDaVariante(VarianteRequestDTO dto, String prefixo, boolean kit) {
        List<Problem.FieldError> erros = new ArrayList<>();
        if (dto.nome() == null)
            erros.add(new Problem.FieldError(prefixo + "nome", OBRIGATORIO));
        if (dto.sku() == null)
            erros.add(new Problem.FieldError(prefixo + "sku", OBRIGATORIO));
        if (dto.preco() == null)
            erros.add(new Problem.FieldError(prefixo + "preco", "Informe o preço da variante (back)."));
        if (kit && (dto.componentes() == null || dto.componentes().isEmpty()))
            erros.add(new Problem.FieldError(prefixo + "componentes",
                    "Escolha ao menos uma variante componente para publicar o kit (back)."));
        if (!Boolean.FALSE.equals(dto.ativa())) {
            List<KitCalculo.Parte> partes = kit ? partesDoKit(dto) : List.of();
            String embalagem = kit
                    ? "Informe a medida do kit ou complete a dos componentes para publicar (back)."
                    : "Informe o peso e as dimensões da embalagem para publicar a variante (back).";
            if (dto.pesoEmbalagemKg() == null && KitCalculo.pesoDerivado(partes) == null)
                erros.add(new Problem.FieldError(prefixo + "pesoEmbalagemKg", embalagem));
            if (dto.alturaEmbalagemCm() == null && KitCalculo.alturaDerivada(partes) == null)
                erros.add(new Problem.FieldError(prefixo + "alturaEmbalagemCm", embalagem));
            if (dto.larguraEmbalagemCm() == null && KitCalculo.larguraDerivada(partes) == null)
                erros.add(new Problem.FieldError(prefixo + "larguraEmbalagemCm", embalagem));
            if (dto.comprimentoEmbalagemCm() == null && KitCalculo.comprimentoDerivado(partes) == null)
                erros.add(new Problem.FieldError(prefixo + "comprimentoEmbalagemCm", embalagem));
        }
        return erros;
    }

    // ehKit nulo no corpo mantem o da variante salva; na criacao, nulo e false
    public boolean ehKit(VarianteRequestDTO dto, VarianteProduto atual) {
        if (dto.ehKit() != null)
            return dto.ehKit();
        if (atual != null)
            return atual.isEhKit();
        if (dto.id() != null) {
            VarianteProduto salva = varianteRepository.findById(dto.id());
            return salva != null && salva.isEhKit();
        }
        return false;
    }

    // Regras do kit (D7) que valem ate no rascunho. atual e a variante salva (nula na
    // criacao); removidas sao as variantes que saem do produto neste mesmo PUT.
    public List<Problem.FieldError> errosDoKit(VarianteRequestDTO dto, String prefixo, VarianteProduto atual,
            Set<Long> removidas) {
        List<Problem.FieldError> erros = new ArrayList<>();
        boolean kit = ehKit(dto, atual);
        if (atual != null && kit != atual.isEhKit())
            erros.add(new Problem.FieldError(prefixo + "ehKit", atual.isEhKit()
                    ? "Este kit já foi criado e não vira variante comum; crie outra variante (back)."
                    : "Esta variante já tem estoque próprio e não vira kit; crie outra variante para o kit (back)."));

        List<ComponenteKitRequestDTO> componentes = (dto.componentes() == null) ? List.of() : dto.componentes();
        String campo = prefixo + "componentes";
        if (!kit) {
            if (!componentes.isEmpty())
                erros.add(new Problem.FieldError(campo, "Só a variante marcada como kit tem componentes (back)."));
            return erros;
        }
        if (dto.estoqueFisico() != null)
            erros.add(new Problem.FieldError(prefixo + "estoqueFisico",
                    "O kit não tem estoque próprio: a disponibilidade vem dos componentes (back)."));

        Set<Long> vistos = new HashSet<>();
        for (ComponenteKitRequestDTO c : componentes) {
            if (c == null) {
                erros.add(new Problem.FieldError(campo, "Componente inválido (back)."));
                continue;
            }
            Long id = c.idVariante();
            if (id == null)
                continue; // a Bean Validation ja apontou
            if (!vistos.add(id)) {
                erros.add(new Problem.FieldError(campo, "A mesma variante aparece duas vezes no kit; some as quantidades (back)."));
                continue;
            }
            if (atual != null && id.equals(atual.getId())) {
                erros.add(new Problem.FieldError(campo, "O kit não pode conter a si mesmo (back)."));
                continue;
            }
            VarianteProduto componente = varianteRepository.findById(id);
            if (removidas.contains(id))
                erros.add(new Problem.FieldError(campo, "Esta variante está saindo do produto; tire-a do kit também (back)."));
            else if (componente == null)
                erros.add(new Problem.FieldError(campo, "Variante componente não encontrada (back)."));
            else if (componente.isEhKit())
                erros.add(new Problem.FieldError(campo, "Um kit não pode conter outro kit (back)."));
        }
        return erros;
    }

    // 409 se alguma das variantes e componente de kit de outro produto (D24)
    public void conferirUsoEmKits(Collection<Long> idsVariantes, Long idProduto) {
        ComponenteKit uso = componenteKitRepository.findUsoForaDoProduto(idsVariantes, idProduto);
        if (uso == null)
            return;
        VarianteProduto kit = uso.getKit();
        String produto = kit.getProduto().getNome() == null ? "produto sem nome" : kit.getProduto().getNome();
        throw new ConflitoException("A variante " + rotulo(uso.getComponente()) + " faz parte do kit "
                + rotulo(kit) + " (" + produto + "). Tire-a do kit antes de excluir, ou inative-a.");
    }

    // Antes de excluir o produto: solta os componentes dos kits dele, senao a chave
    // estrangeira barra a exclusao da variante que e componente de um kit do mesmo produto
    public void prepararExclusao(Produto produto) {
        conferirUsoEmKits(produto.getVariantes().stream().map(VarianteProduto::getId).toList(), produto.getId());
        produto.getVariantes().forEach(v -> v.getComponentes().clear());
        produtoRepository.flush();
    }

    // Campos que o PUT /variantes/{id} tambem altera. estoqueFisico fica de fora: depois
    // da criacao ele so muda por movimento de estoque (#13).
    public void aplicarVariante(VarianteProduto destino, VarianteRequestDTO dto) {
        boolean kit = ehKit(dto, destino);
        destino.setEhKit(kit);
        sincronizarComponentes(destino, kit ? dto.componentes() : null);
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
        for (int i = 0; i < variantes.size(); i++) {
            VarianteRequestDTO v = variantes.get(i);
            erros.addAll(obrigatoriosDaVariante(v, "variantes[" + i + "].", ehKit(v, null)));
        }
        return erros;
    }

    // aplicarVariantes confere de novo, ja sabendo quais variantes saem do produto
    private List<Problem.FieldError> errosDosKits(List<VarianteRequestDTO> variantes) {
        List<Problem.FieldError> erros = new ArrayList<>();
        if (variantes == null)
            return erros;
        for (int i = 0; i < variantes.size(); i++) {
            VarianteRequestDTO v = variantes.get(i);
            VarianteProduto atual = (v.id() == null) ? null : varianteRepository.findById(v.id());
            erros.addAll(errosDoKit(v, "variantes[" + i + "].", atual, Set.of()));
        }
        return erros;
    }

    // Componentes validos do corpo, para derivar peso e dimensoes antes de salvar
    private List<KitCalculo.Parte> partesDoKit(VarianteRequestDTO dto) {
        List<KitCalculo.Parte> partes = new ArrayList<>();
        if (dto.componentes() == null)
            return partes;
        for (ComponenteKitRequestDTO c : dto.componentes()) {
            if (c == null || c.idVariante() == null || c.quantidade() == null || c.quantidade() < 1)
                continue;
            VarianteProduto componente = varianteRepository.findById(c.idVariante());
            if (componente != null && !componente.isEhKit())
                partes.add(new KitCalculo.Parte(componente, c.quantidade()));
        }
        return partes;
    }

    // Atualiza no lugar em vez de apagar e recriar: o Hibernate insere antes de apagar,
    // e a mesma variante bateria na chave unica (kit, componente)
    private void sincronizarComponentes(VarianteProduto kit, List<ComponenteKitRequestDTO> dtos) {
        Map<Long, Integer> pedidas = new LinkedHashMap<>();
        if (dtos != null)
            for (ComponenteKitRequestDTO c : dtos)
                pedidas.put(c.idVariante(), c.quantidade());
        kit.getComponentes().removeIf(c -> !pedidas.containsKey(c.getComponente().getId()));
        for (ComponenteKit c : kit.getComponentes())
            c.setQuantidade(pedidas.remove(c.getComponente().getId()));
        pedidas.forEach((idVariante, quantidade) -> {
            ComponenteKit c = new ComponenteKit();
            c.setKit(kit);
            c.setComponente(varianteRepository.findById(idVariante));
            c.setQuantidade(quantidade);
            kit.getComponentes().add(c);
        });
    }

    private static String rotulo(VarianteProduto v) {
        if (v.getSku() != null)
            return v.getSku();
        return (v.getNome() != null) ? v.getNome() : "#" + v.getId();
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
        Set<Long> removidas = new HashSet<>(atuais.keySet());
        removidas.removeAll(mantidas);

        List<Problem.FieldError> errosKit = new ArrayList<>();
        for (int i = 0; i < lista.size(); i++) {
            VarianteRequestDTO dto = lista.get(i);
            errosKit.addAll(errosDoKit(dto, "variantes[" + i + "].", atuais.get(dto.id()), removidas));
        }
        if (!errosKit.isEmpty())
            throw new ValidationException("Revise a composição do kit.", errosKit);

        // apaga antes de inserir: um SKU removido pode voltar numa variante nova no mesmo PUT.
        // Antes, solta dos kits as ligacoes com o que sai, senao a chave estrangeira barra.
        if (!removidas.isEmpty()) {
            conferirUsoEmKits(removidas, produto.getId());
            for (VarianteProduto v : produto.getVariantes())
                v.getComponentes().removeIf(c -> removidas.contains(v.getId())
                        || removidas.contains(c.getComponente().getId()));
            produtoRepository.flush();
            produto.getVariantes().removeIf(v -> removidas.contains(v.getId()));
            produtoRepository.flush();
        }

        for (VarianteRequestDTO dto : lista) {
            VarianteProduto variante = (dto.id() == null) ? null : atuais.get(dto.id());
            if (variante == null) {
                variante = new VarianteProduto();
                variante.setProduto(produto);
                // saldo inicial com o movimento ENTRADA correspondente (#13). Kit nao tem (D7)
                if (!ehKit(dto, null))
                    movimentoEstoqueService.entradaInicial(variante, dto.estoqueFisico() == null ? 0 : dto.estoqueFisico());
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
