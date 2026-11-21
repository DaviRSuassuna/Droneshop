package br.com.droneshop.catalogo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import br.com.droneshop.catalogo.dto.OfertaLojaResponseDTO;
import br.com.droneshop.catalogo.dto.ProdutoLojaResumoDTO;
import br.com.droneshop.catalogo.dto.PromocaoLojaResumoDTO;
import br.com.droneshop.catalogo.model.Acessorio;
import br.com.droneshop.catalogo.model.EscopoPromocao;
import br.com.droneshop.catalogo.model.Kit;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.Promocao;
import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.repository.AcessorioRepository;
import br.com.droneshop.catalogo.repository.KitRepository;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.catalogo.repository.PromocaoRepository;
import br.com.droneshop.exception.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class LojaComplementoServiceImpl implements LojaComplementoService {

    @Inject
    ProdutoRepository produtoRepository;

    @Inject
    AcessorioRepository acessorioRepository;

    @Inject
    KitRepository kitRepository;

    @Inject
    PromocaoRepository promocaoRepository;

    @Override
    @Transactional
    public List<ProdutoLojaResumoDTO> buscarCompativeis(String slug) {
        Produto produto = produtoRepository.find("slug = ?1 and status = ?2", slug, StatusProduto.ATIVO)
                .firstResult();
        if (produto == null)
            throw new ResourceNotFoundException("Produto não encontrado.");

        return acessorioRepository.findSugeridosPara(produto.getId()).stream()
                .map(this::resumo)
                .toList();
    }

    @Override
    @Transactional
    public List<OfertaLojaResponseDTO> buscarOfertas() {
        List<OfertaLojaResponseDTO> ofertas = new ArrayList<>();
        LocalDateTime agora = LocalDateTime.now();
        for (Promocao promocao : promocaoRepository.findBySituacao("VIGENTE", agora).list()) {
            if (promocao.getEscopo() != EscopoPromocao.PRODUTO)
                continue;
            for (Produto produto : promocao.getProdutos()) {
                if (produto.getStatus() != StatusProduto.ATIVO || produto.getPreco() == null)
                    continue;
                ofertas.add(new OfertaLojaResponseDTO(
                        resumo(produto),
                        produto.getPreco(),
                        precoPromocional(produto.getPreco(), promocao),
                        new PromocaoLojaResumoDTO(promocao.getId(), promocao.getNome(), promocao.getDataFim())));
            }
        }

        for (Kit kit : kitRepository.findExibidosEmOfertas()) {
            BigDecimal somaComponentes = kit.getComponentes().stream()
                    .map(componente -> componente.getProduto().getPreco()
                            .multiply(BigDecimal.valueOf(componente.getQuantidade())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            ofertas.add(new OfertaLojaResponseDTO(
                    resumo(kit), somaComponentes, kit.getPreco(), null));
        }
        return ofertas;
    }

    private ProdutoLojaResumoDTO resumo(Produto produto) {
        BigDecimal precoMinimo = produto.getPreco();
        if (produto.getGrupo() != null) {
            precoMinimo = produto.getGrupo().getProdutos().stream()
                    .filter(versao -> versao.getStatus() == StatusProduto.ATIVO)
                    .map(Produto::getPreco)
                    .filter(Objects::nonNull)
                    .min(Comparator.naturalOrder())
                    .orElse(precoMinimo);
        }
        return new ProdutoLojaResumoDTO(produto.getId(), produto.getNome(), produto.getSlug(), precoMinimo);
    }

    private BigDecimal precoPromocional(BigDecimal preco, Promocao promocao) {
        BigDecimal desconto = promocao.getPercentualDesconto() != null
                ? preco.multiply(promocao.getPercentualDesconto()).movePointLeft(2)
                : promocao.getValorDesconto();
        return preco.subtract(desconto).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }
}
