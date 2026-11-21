package br.com.droneshop.catalogo.service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import br.com.droneshop.catalogo.dto.CategoriaLojaDTO;
import br.com.droneshop.catalogo.dto.FiltrosLojaDTO;
import br.com.droneshop.catalogo.dto.MarcaLojaDTO;
import br.com.droneshop.catalogo.dto.ProdutoLojaDetalheDTO;
import br.com.droneshop.catalogo.dto.ProdutoLojaItemDTO;
import br.com.droneshop.catalogo.mapper.MidiaProdutoResponseMapper;
import br.com.droneshop.catalogo.model.Acessorio;
import br.com.droneshop.catalogo.model.Categoria;
import br.com.droneshop.catalogo.model.Drone;
import br.com.droneshop.catalogo.model.Marca;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.StatusCategoria;
import br.com.droneshop.catalogo.model.StatusMarca;
import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.model.Subcategoria;
import br.com.droneshop.catalogo.model.TipoProduto;
import br.com.droneshop.catalogo.repository.CategoriaRepository;
import br.com.droneshop.catalogo.repository.MarcaRepository;
import br.com.droneshop.catalogo.repository.MidiaProdutoRepository;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.catalogo.repository.SubcategoriaRepository;
import br.com.droneshop.catalogo.repository.TermoBuscadoRepository;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.dto.PageResponse;
import br.com.droneshop.exception.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class LojaServiceImpl implements LojaService {

    @Inject
    TermoBuscadoRepository termos;

    @Inject
    CategoriaRepository categoriaRepository;

    @Inject
    SubcategoriaRepository subcategoriaRepository;

    @Inject
    MarcaRepository marcaRepository;

    @Inject
    ProdutoRepository produtoRepository;

    @Inject
    MidiaProdutoRepository midiaRepository;

    /** Item da vitrine: a versão que representa o grupo (a mais barata) e o que vale para o grupo todo. */
    private record Item(Produto representante, BigDecimal precoMinimo, boolean disponivel) {
    }

    @Override
    @Transactional
    public List<CategoriaLojaDTO> listarCategorias(String tipo) {
        List<Categoria> categorias = vazio(tipo) == null
                ? categoriaRepository.list("status = ?1 and exibirMenu = true order by ordemExibicao, nome",
                        StatusCategoria.ATIVA)
                : categoriaRepository.list(
                        "status = ?1 and exibirMenu = true and tipo = ?2 order by ordemExibicao, nome",
                        StatusCategoria.ATIVA, tipo(tipo));
        return categorias.stream()
                .map(c -> new CategoriaLojaDTO(c.getId(), c.getNome(), c.getSlug(), c.getTipo(),
                        subcategoriaRepository.findByCategoria(c.getId()).list().stream()
                                .filter(s -> s.getStatus() == StatusCategoria.ATIVA)
                                .map(s -> new CategoriaLojaDTO.Subcategoria(s.getId(), s.getNome(), s.getSlug()))
                                .toList()))
                .toList();
    }

    @Override
    @Transactional
    public List<MarcaLojaDTO> listarMarcas(Boolean destaque) {
        List<Marca> marcas = Boolean.TRUE.equals(destaque)
                ? marcaRepository.list("status = ?1 and destacarHome = true order by nome", StatusMarca.ATIVA)
                : marcaRepository.list("status = ?1 order by nome", StatusMarca.ATIVA);
        return marcas.stream().map(m -> new MarcaLojaDTO(m.getId(), m.getNome(), m.getSlug(), m.getLogoUrl()))
                .toList();
    }

    @Override
    @Transactional
    public PageResponse<ProdutoLojaItemDTO> listarProdutos(Filtro filtro, String ordem, int page, int pageSize) {
        List<Item> itens = itens(filtro).stream()
                .filter(i -> filtro.precoMin() == null
                        || (i.precoMinimo() != null && i.precoMinimo().compareTo(filtro.precoMin()) >= 0))
                .filter(i -> filtro.precoMax() == null
                        || (i.precoMinimo() != null && i.precoMinimo().compareTo(filtro.precoMax()) <= 0))
                .sorted(ordenacao(ordem))
                .toList();
        int tamanho = pageSize <= 0 ? 10 : pageSize;
        int pagina = Math.max(page, 0);
        int inicio = Math.min(pagina * tamanho, itens.size());
        List<Item> trecho = itens.subList(inicio, Math.min(inicio + tamanho, itens.size()));
        return PageResponse.of(trecho, pagina, tamanho, itens.size(), this::paraItem);
    }

    @Override
    @Transactional
    public FiltrosLojaDTO buscarFiltros(String categoria) {
        List<Item> itens = itens(new Filtro(categoria, null, null, null, null, null, null, null, null, null));
        Map<String, FiltrosLojaDTO.Marca> marcas = new LinkedHashMap<>();
        itens.stream()
                .map(i -> i.representante().getMarca())
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Marca::getNome, String.CASE_INSENSITIVE_ORDER))
                .forEach(m -> marcas.merge(m.getSlug(), new FiltrosLojaDTO.Marca(m.getSlug(), m.getNome(), 1),
                        (a, b) -> new FiltrosLojaDTO.Marca(a.slug(), a.nome(), a.quantidade() + 1)));
        List<BigDecimal> precos = itens.stream().map(Item::precoMinimo).filter(Objects::nonNull).toList();
        FiltrosLojaDTO.Preco preco = new FiltrosLojaDTO.Preco(
                precos.stream().min(Comparator.naturalOrder()).orElse(null),
                precos.stream().max(Comparator.naturalOrder()).orElse(null));
        return new FiltrosLojaDTO(new ArrayList<>(marcas.values()), preco);
    }

    @Override
    @Transactional
    public ProdutoLojaDetalheDTO buscarProduto(String slug) {
        Produto p = produtoRepository.find("slug = ?1 and status = ?2", slug, StatusProduto.ATIVO).firstResult();
        if (p == null)
            throw new ResourceNotFoundException("Produto não encontrado.");

        Drone d = p instanceof Drone drone ? drone : null;
        Acessorio a = p instanceof Acessorio acessorio ? acessorio : null;
        ProdutoLojaDetalheDTO.Grupo grupo = null;
        if (p.getGrupo() != null) {
            grupo = new ProdutoLojaDetalheDTO.Grupo(p.getGrupo().getNome(), p.getGrupo().getSlug(),
                    p.getGrupo().getProdutos().stream()
                            .filter(v -> v.getStatus() == StatusProduto.ATIVO)
                            .sorted(Comparator.comparingInt(Produto::getOrdemNoGrupo).thenComparing(Produto::getId))
                            .map(v -> new ProdutoLojaDetalheDTO.Versao(v.getId(), v.getRotuloNoGrupo(), v.getSlug(),
                                    v.getPreco(), KitCalculo.disponivel(v)))
                            .toList());
        }
        return new ProdutoLojaDetalheDTO(
                p.getId(), p.getNome(), p.getSlug(), p.getTipo(), p.getDescricaoCurta(), p.getDescricao(),
                referencia(p.getMarca()), referencia(p.getSubcategoria()), p.getGarantiaMeses(),
                p.getMetaTitle(), p.getMetaDescription(),
                d == null ? null : d.getPesoGramas(),
                d == null ? null : d.getDimensoesDobrado(),
                d == null ? null : d.getDimensoesAberto(),
                d == null ? null : d.getAutonomiaMinutos(),
                d == null ? null : d.getAlcanceMetros(),
                d == null ? null : d.getVelocidadeMaximaKmh(),
                d == null ? null : d.getResolucaoFotoMp(),
                d == null ? null : d.getTipoSensor(),
                d == null ? null : d.getResolucaoVideo(),
                d == null ? null : d.getEstabilizacao(),
                d == null ? null : d.getTipoBateria(),
                d == null ? null : d.getCapacidadeBateriaMah(),
                d == null ? null : d.getTempoCarregamentoMinutos(),
                d == null ? null : d.isGpsIntegrado(),
                d == null ? null : d.isSensoresAnticolisao(),
                d == null ? null : d.isExigeRegistroAnac(),
                d == null ? null : d.getClasseAnac(),
                a == null ? null : a.getTipoAcessorio(),
                a == null ? null : a.getPesoItemG(),
                p.getSku(), p.getPreco(), KitCalculo.disponivel(p), p.getImagemPrincipal(), grupo,
                MidiaProdutoResponseMapper.toResponse(midiaRepository.findByProdutoId(p.getId())));
    }

    private ProdutoLojaDetalheDTO.Referencia referencia(Marca m) {
        return m == null ? null : new ProdutoLojaDetalheDTO.Referencia(m.getId(), m.getNome(), m.getSlug());
    }

    private ProdutoLojaDetalheDTO.Referencia referencia(Subcategoria s) {
        return s == null ? null : new ProdutoLojaDetalheDTO.Referencia(s.getId(), s.getNome(), s.getSlug());
    }

    /** Produtos ATIVO que passam nos filtros, juntados por grupo (D40). O preço é filtrado depois. */
    private List<Item> itens(Filtro f) {
        StringBuilder hql = new StringBuilder("select p from Produto p left join p.marca m left join p.subcategoria s"
                + " left join s.categoria c where p.status = :status");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("status", StatusProduto.ATIVO);
        if (vazio(f.categoria()) != null) {
            hql.append(" and c.slug = :categoria");
            params.put("categoria", f.categoria().trim());
        }
        if (vazio(f.subcategoria()) != null) {
            hql.append(" and s.slug = :subcategoria");
            params.put("subcategoria", f.subcategoria().trim());
        }
        if (vazio(f.marca()) != null) {
            hql.append(" and m.slug = :marca");
            params.put("marca", f.marca().trim());
        }
        List<Produto> produtos = produtoRepository.find(hql.toString(), params).list();

        TipoProduto tipo = vazio(f.tipo()) == null ? null : tipo(f.tipo());
        String nome = normalizar(f.nome());
        String[] palavras = vazio(f.busca()) == null ? null : termo(f.busca()).split(" ");
        Map<String, List<Produto>> grupos = new LinkedHashMap<>();
        for (Produto p : produtos) {
            if (vazio(f.tipo()) != null && p.getTipo() != tipo)
                continue;
            if (nome != null && !normalizar(p.getNome()).contains(nome))
                continue;
            if (palavras != null && !contemTodas(p, palavras))
                continue;
            if (f.autonomiaMin() != null && !(p instanceof Drone d && d.getAutonomiaMinutos() != null
                    && d.getAutonomiaMinutos() >= f.autonomiaMin()))
                continue;
            if (f.alcanceMin() != null && !(p instanceof Drone d && d.getAlcanceMetros() != null
                    && d.getAlcanceMetros() >= f.alcanceMin()))
                continue;
            String chave = p.getGrupo() != null ? "g" + p.getGrupo().getId() : "p" + p.getId();
            grupos.computeIfAbsent(chave, k -> new ArrayList<>()).add(p);
        }
        return grupos.values().stream().map(this::item).toList();
    }

    private Item item(List<Produto> versoes) {
        Produto representante = versoes.stream()
                .min(Comparator.comparing(Produto::getPreco, Comparator.nullsLast(Comparator.<BigDecimal>naturalOrder()))
                        .thenComparing(Produto::getId))
                .orElseThrow();
        boolean disponivel = versoes.stream().anyMatch(v -> KitCalculo.disponivel(v) > 0);
        return new Item(representante, representante.getPreco(), disponivel);
    }

    private ProdutoLojaItemDTO paraItem(Item i) {
        Produto p = i.representante();
        String nome = p.getGrupo() != null && p.getGrupo().getNome() != null ? p.getGrupo().getNome() : p.getNome();
        return new ProdutoLojaItemDTO(p.getId(), nome, p.getSlug(), p.getTipo(),
                p.getMarca() == null ? null : new ProdutoLojaItemDTO.Marca(p.getMarca().getNome()),
                i.precoMinimo(), p.getImagemPrincipal(), i.disponivel());
    }

    private Comparator<Item> ordenacao(String ordem) {
        Comparator<Item> porNome = Comparator.comparing(i -> normalizar(i.representante().getNome()),
                Comparator.nullsLast(Comparator.<String>naturalOrder()));
        Comparator<Item> porPreco = Comparator.comparing(Item::precoMinimo,
                Comparator.nullsLast(Comparator.<BigDecimal>naturalOrder()));
        String o = vazio(ordem) == null ? "nome" : ordem.trim();
        return switch (o) {
            case "preco" -> porPreco.thenComparing(porNome);
            case "-preco" -> porPreco.reversed().thenComparing(porNome);
            case "lancamento" -> Comparator.<Item, LocalDate>comparing(i -> i.representante().getDataLancamento(),
                    Comparator.nullsLast(Comparator.<LocalDate>reverseOrder())).thenComparing(porNome);
            default -> porNome;
        };
    }

    /** Tipo pelo nome (DRONE, ACESSORIO, KIT); nome desconhecido não casa com nenhum produto. */
    private TipoProduto tipo(String tipo) {
        for (TipoProduto t : TipoProduto.values())
            if (t.getNome().equalsIgnoreCase(tipo.trim()))
                return t;
        return null;
    }

    @Override
    @Transactional
    public PageResponse<ProdutoLojaItemDTO> buscar(String q, int page, int pageSize) {
        if (q == null || termo(q).length() < 2)
            throw ValidationException.of("q", "Digite ao menos 2 caracteres para buscar (back)");
        PageResponse<ProdutoLojaItemDTO> r = listarProdutos(
                new Filtro(null, null, null, null, null, null, null, null, null, q), null, page, pageSize);
        termos.registrar(termo(q), (int) r.totalItems());
        return r;
    }

    /** Termo normalizado: minúsculo, sem acento e com os espaços colapsados (D23). */
    public static String termo(String q) {
        String n = normalizar(q);
        return n == null ? "" : n.replaceAll("\s+", " ");
    }

    private static boolean contemTodas(Produto p, String[] palavras) {
        String texto = normalizar(String.join(" ", p.getNome() == null ? "" : p.getNome(),
                p.getSku() == null ? "" : p.getSku(),
                p.getMarca() == null ? "" : p.getMarca().getNome(),
                p.getTags() == null ? "" : p.getTags()));
        if (texto == null)
            return false;
        for (String w : palavras)
            if (!texto.contains(w))
                return false;
        return true;
    }

    private static String vazio(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    private static String normalizar(String s) {
        if (vazio(s) == null)
            return null;
        return Normalizer.normalize(s.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
