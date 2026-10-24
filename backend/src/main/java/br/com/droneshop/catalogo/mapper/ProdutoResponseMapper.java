package br.com.droneshop.catalogo.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import br.com.droneshop.catalogo.dto.AcessorioResponseDTO;
import br.com.droneshop.catalogo.dto.ComponenteKitResponseDTO;
import br.com.droneshop.catalogo.dto.DroneResponseDTO;
import br.com.droneshop.catalogo.dto.FornecedorResponseDTO;
import br.com.droneshop.catalogo.dto.GrupoProdutoResumoDTO;
import br.com.droneshop.catalogo.dto.KitResponseDTO;
import br.com.droneshop.catalogo.dto.MarcaResponseDTO;
import br.com.droneshop.catalogo.dto.ProdutoResponseDTO;
import br.com.droneshop.catalogo.dto.ProdutoResumoDTO;
import br.com.droneshop.catalogo.dto.SubcategoriaResponseDTO;
import br.com.droneshop.catalogo.model.Acessorio;
import br.com.droneshop.catalogo.model.ComponenteKit;
import br.com.droneshop.catalogo.model.Drone;
import br.com.droneshop.catalogo.model.Kit;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.service.KitCalculo;

public final class ProdutoResponseMapper {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private ProdutoResponseMapper() {
    }

    public static ProdutoResponseDTO toResponse(Produto produto) {
        return switch (produto) {
            case Drone drone -> toResponse(drone);
            case Acessorio acessorio -> toResponse(acessorio);
            case Kit kit -> toResponse(kit);
            default -> throw new IllegalStateException("Tipo de produto sem mapper: " + produto.getClass());
        };
    }

    public static DroneResponseDTO toResponse(Drone d) {
        return new DroneResponseDTO(
                d.getId(),
                d.getTipo(),
                d.getNome(),
                d.getSku(),
                d.getDescricaoCurta(),
                d.getDescricao(),
                d.getSlug(),
                d.getMetaTitle(),
                d.getMetaDescription(),
                d.getTags(),
                d.getGarantiaMeses(),
                d.getDataLancamento(),
                marca(d),
                subcategoria(d),
                fornecedor(d),
                d.getStatus(),
                d.isDestaque(),
                d.getPreco(),
                d.getCustoAquisicao(),
                margemPercentual(d.getPreco(), d.getCustoAquisicao()),
                d.getEstoqueFisico(),
                KitCalculo.disponivel(d),
                d.getEstoqueMinimo(),
                d.getPesoEmbalagemKg(),
                d.getAlturaEmbalagemCm(),
                d.getLarguraEmbalagemCm(),
                d.getComprimentoEmbalagemCm(),
                grupo(d),
                d.getRotuloNoGrupo(),
                d.getOrdemNoGrupo(),
                d.getPesoGramas(),
                d.getDimensoesDobrado(),
                d.getDimensoesAberto(),
                d.getAutonomiaMinutos(),
                d.getAlcanceMetros(),
                d.getVelocidadeMaximaKmh(),
                d.getResolucaoFotoMp(),
                d.getTipoSensor(),
                d.getResolucaoVideo(),
                d.getEstabilizacao(),
                d.getTipoBateria(),
                d.getCapacidadeBateriaMah(),
                d.getTempoCarregamentoMinutos(),
                d.isGpsIntegrado(),
                d.isSensoresAnticolisao(),
                d.isExigeRegistroAnac(),
                d.getClasseAnac(),
                d.getRegistroSisant(),
                resumos(d.getAcessoriosCompativeis()));
    }

    public static AcessorioResponseDTO toResponse(Acessorio a) {
        return new AcessorioResponseDTO(
                a.getId(),
                a.getTipo(),
                a.getNome(),
                a.getSku(),
                a.getDescricaoCurta(),
                a.getDescricao(),
                a.getSlug(),
                a.getMetaTitle(),
                a.getMetaDescription(),
                a.getTags(),
                a.getGarantiaMeses(),
                a.getDataLancamento(),
                marca(a),
                subcategoria(a),
                fornecedor(a),
                a.getStatus(),
                a.isDestaque(),
                a.getPreco(),
                a.getCustoAquisicao(),
                margemPercentual(a.getPreco(), a.getCustoAquisicao()),
                a.getEstoqueFisico(),
                KitCalculo.disponivel(a),
                a.getEstoqueMinimo(),
                a.getPesoEmbalagemKg(),
                a.getAlturaEmbalagemCm(),
                a.getLarguraEmbalagemCm(),
                a.getComprimentoEmbalagemCm(),
                grupo(a),
                a.getRotuloNoGrupo(),
                a.getOrdemNoGrupo(),
                a.getTipoAcessorio(),
                resumos(a.getCompativeis()),
                resumos(a.getAcessoriosCompativeis()));
    }

    // No kit, peso e dimensoes saem efetivos: o informado ou, sem ele, o derivado (D16)
    public static KitResponseDTO toResponse(Kit k) {
        return new KitResponseDTO(
                k.getId(),
                k.getTipo(),
                k.getNome(),
                k.getSku(),
                k.getDescricaoCurta(),
                k.getDescricao(),
                k.getSlug(),
                k.getMetaTitle(),
                k.getMetaDescription(),
                k.getTags(),
                k.getGarantiaMeses(),
                k.getDataLancamento(),
                marca(k),
                subcategoria(k),
                fornecedor(k),
                k.getStatus(),
                k.isDestaque(),
                k.getPreco(),
                k.getCustoAquisicao(),
                margemPercentual(k.getPreco(), k.getCustoAquisicao()),
                k.getEstoqueFisico(),
                KitCalculo.disponivel(k),
                KitCalculo.peso(k),
                KitCalculo.altura(k),
                KitCalculo.largura(k),
                KitCalculo.comprimento(k),
                grupo(k),
                k.getRotuloNoGrupo(),
                k.getOrdemNoGrupo(),
                k.getPesoEmbalagemKg() != null,
                k.getAlturaEmbalagemCm() != null || k.getLarguraEmbalagemCm() != null
                        || k.getComprimentoEmbalagemCm() != null,
                k.getComponentes().stream().map(ProdutoResponseMapper::componente).toList(),
                resumos(k.getAcessoriosCompativeis()));
    }

    // D26: (preco - custo) / preco * 100, com 2 casas; sem preco ou sem custo nao ha margem
    public static BigDecimal margemPercentual(BigDecimal preco, BigDecimal custo) {
        if (preco == null || custo == null || preco.signum() <= 0)
            return null;
        return preco.subtract(custo).multiply(CEM).divide(preco, 2, RoundingMode.HALF_UP);
    }

    private static ComponenteKitResponseDTO componente(ComponenteKit c) {
        Produto p = c.getProduto();
        return new ComponenteKitResponseDTO(p.getId(), p.getSku(), p.getNome(), p.getTipo(), p.getStatus(),
                p.getEstoqueFisico(), KitCalculo.disponivel(p), c.getQuantidade());
    }

    // ordem por nome para a lista sair estavel; rascunho sem nome vai para o fim
    private static List<ProdutoResumoDTO> resumos(Collection<? extends Produto> produtos) {
        return produtos.stream()
                .sorted(Comparator.comparing(Produto::getNome, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(Produto::getId))
                .map(p -> new ProdutoResumoDTO(p.getId(), p.getNome(), p.getTipo()))
                .toList();
    }

    private static GrupoProdutoResumoDTO grupo(Produto p) {
        return p.getGrupo() == null ? null : new GrupoProdutoResumoDTO(p.getGrupo().getId(), p.getGrupo().getNome());
    }

    private static MarcaResponseDTO marca(Produto p) {
        return p.getMarca() == null ? null : MarcaResponseMapper.toResponse(p.getMarca());
    }

    private static SubcategoriaResponseDTO subcategoria(Produto p) {
        return p.getSubcategoria() == null ? null : SubcategoriaResponseMapper.toResponse(p.getSubcategoria());
    }

    private static FornecedorResponseDTO fornecedor(Produto p) {
        return p.getFornecedor() == null ? null : FornecedorResponseMapper.toResponse(p.getFornecedor());
    }
}
