package br.com.droneshop.catalogo.mapper;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import br.com.droneshop.catalogo.dto.AcessorioResponseDTO;
import br.com.droneshop.catalogo.dto.DroneResponseDTO;
import br.com.droneshop.catalogo.dto.FornecedorResponseDTO;
import br.com.droneshop.catalogo.dto.MarcaResponseDTO;
import br.com.droneshop.catalogo.dto.ProdutoResponseDTO;
import br.com.droneshop.catalogo.dto.ProdutoResumoDTO;
import br.com.droneshop.catalogo.dto.SubcategoriaResponseDTO;
import br.com.droneshop.catalogo.model.Acessorio;
import br.com.droneshop.catalogo.model.Drone;
import br.com.droneshop.catalogo.model.Produto;

public final class ProdutoResponseMapper {

    private ProdutoResponseMapper() {
    }

    public static ProdutoResponseDTO toResponse(Produto produto) {
        return switch (produto) {
            case Drone drone -> toResponse(drone);
            case Acessorio acessorio -> toResponse(acessorio);
            default -> throw new IllegalStateException("Tipo de produto sem mapper: " + produto.getClass());
        };
    }

    public static DroneResponseDTO toResponse(Drone d) {
        return new DroneResponseDTO(
                d.getId(),
                d.getTipo(),
                d.getNome(),
                d.getModelo(),
                d.getDescricaoCurta(),
                d.getDescricao(),
                d.getSlug(),
                d.getMetaTitle(),
                d.getMetaDescription(),
                d.getTags(),
                d.getGarantiaMeses(),
                d.getDataLancamento(),
                d.getPreco(),
                marca(d),
                subcategoria(d),
                fornecedor(d),
                d.getStatus(),
                d.isDestaque(),
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
                a.getModelo(),
                a.getDescricaoCurta(),
                a.getDescricao(),
                a.getSlug(),
                a.getMetaTitle(),
                a.getMetaDescription(),
                a.getTags(),
                a.getGarantiaMeses(),
                a.getDataLancamento(),
                a.getPreco(),
                marca(a),
                subcategoria(a),
                fornecedor(a),
                a.getStatus(),
                a.isDestaque(),
                a.getTipoAcessorio(),
                resumos(a.getCompativeis()),
                resumos(a.getAcessoriosCompativeis()));
    }

    // ordem por nome para a lista sair estavel; rascunho sem nome vai para o fim
    private static List<ProdutoResumoDTO> resumos(Collection<? extends Produto> produtos) {
        return produtos.stream()
                .sorted(Comparator.comparing(Produto::getNome, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(Produto::getId))
                .map(p -> new ProdutoResumoDTO(p.getId(), p.getNome(), p.getTipo()))
                .toList();
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
