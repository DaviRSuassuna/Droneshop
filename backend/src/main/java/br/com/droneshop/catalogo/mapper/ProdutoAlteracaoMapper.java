package br.com.droneshop.catalogo.mapper;

import br.com.droneshop.catalogo.dto.DroneRequestDTO;
import br.com.droneshop.catalogo.dto.AcessorioRequestDTO;
import br.com.droneshop.catalogo.dto.KitRequestDTO;
import br.com.droneshop.catalogo.dto.ComponenteKitRequestDTO;
import br.com.droneshop.catalogo.model.Drone;
import br.com.droneshop.catalogo.model.Acessorio;
import br.com.droneshop.catalogo.model.Kit;
import br.com.droneshop.catalogo.model.Produto;
import java.math.BigDecimal;

/** Reaproveita os contratos de cadastro para validar alterações parciais em lote. */
public final class ProdutoAlteracaoMapper {
    private ProdutoAlteracaoMapper() {}

    public static DroneRequestDTO toRequest(Drone p, Long idStatus, Long idSubcategoria, BigDecimal preco) {
        return new DroneRequestDTO(
            p.getNome(),
            p.getSku(),
            p.getDescricaoCurta(),
            p.getDescricao(),
            p.getSlug(),
            p.getMetaTitle(),
            p.getMetaDescription(),
            p.getTags(),
            p.getGarantiaMeses(),
            p.getDataLancamento(),
            p.getMarca() == null ? null : p.getMarca().getId(),
            idSubcategoria,
            p.getFornecedor() == null ? null : p.getFornecedor().getId(),
            idStatus,
            p.isDestaque(),
            preco,
            p.getCustoAquisicao(),
            null,
            p.getEstoqueMinimo(),
            p.getPesoEmbalagemKg(),
            p.getAlturaEmbalagemCm(),
            p.getLarguraEmbalagemCm(),
            p.getComprimentoEmbalagemCm(),
            p.getGrupo() == null ? null : p.getGrupo().getId(),
            p.getRotuloNoGrupo(),
            p.getOrdemNoGrupo(),
            p.getPesoGramas(),
            p.getDimensoesDobrado(),
            p.getDimensoesAberto(),
            p.getAutonomiaMinutos(),
            p.getAlcanceMetros(),
            p.getVelocidadeMaximaKmh(),
            p.getResolucaoFotoMp(),
            p.getTipoSensor(),
            p.getResolucaoVideo(),
            p.getEstabilizacao(),
            p.getTipoBateria(),
            p.getCapacidadeBateriaMah(),
            p.getTempoCarregamentoMinutos(),
            p.isGpsIntegrado(),
            p.isSensoresAnticolisao(),
            p.isExigeRegistroAnac(),
            p.getClasseAnac(),
            p.getRegistroSisant());
    }

    public static AcessorioRequestDTO toRequest(Acessorio p, Long idStatus, Long idSubcategoria, BigDecimal preco) {
        return new AcessorioRequestDTO(
            p.getNome(),
            p.getSku(),
            p.getDescricaoCurta(),
            p.getDescricao(),
            p.getSlug(),
            p.getMetaTitle(),
            p.getMetaDescription(),
            p.getTags(),
            p.getGarantiaMeses(),
            p.getDataLancamento(),
            p.getMarca() == null ? null : p.getMarca().getId(),
            idSubcategoria,
            p.getFornecedor() == null ? null : p.getFornecedor().getId(),
            idStatus,
            p.isDestaque(),
            preco,
            p.getCustoAquisicao(),
            null,
            p.getEstoqueMinimo(),
            p.getPesoEmbalagemKg(),
            p.getAlturaEmbalagemCm(),
            p.getLarguraEmbalagemCm(),
            p.getComprimentoEmbalagemCm(),
            p.getGrupo() == null ? null : p.getGrupo().getId(),
            p.getRotuloNoGrupo(),
            p.getOrdemNoGrupo(),
            p.getTipoAcessorio(),
            p.getPesoItemG(),
            p.isSugerirNosCompativeis(),
            p.getCompativeis().stream().map(Produto::getId).toList());
    }

    public static KitRequestDTO toRequest(Kit p, Long idStatus, Long idSubcategoria, BigDecimal preco) {
        return new KitRequestDTO(
            p.getNome(),
            p.getSku(),
            p.getDescricaoCurta(),
            p.getDescricao(),
            p.getSlug(),
            p.getMetaTitle(),
            p.getMetaDescription(),
            p.getTags(),
            p.getGarantiaMeses(),
            p.getDataLancamento(),
            p.getMarca() == null ? null : p.getMarca().getId(),
            idSubcategoria,
            p.getFornecedor() == null ? null : p.getFornecedor().getId(),
            idStatus,
            p.isDestaque(),
            preco,
            p.getCustoAquisicao(),
            null,
            p.getPesoEmbalagemKg(),
            p.getAlturaEmbalagemCm(),
            p.getLarguraEmbalagemCm(),
            p.getComprimentoEmbalagemCm(),
            p.getGrupo() == null ? null : p.getGrupo().getId(),
            p.getRotuloNoGrupo(),
            p.getOrdemNoGrupo(),
            p.isExibirEmOfertas(),
            p.getComponentes().stream().map(c -> new ComponenteKitRequestDTO(c.getProduto().getId(), c.getQuantidade())).toList());
    }
}
