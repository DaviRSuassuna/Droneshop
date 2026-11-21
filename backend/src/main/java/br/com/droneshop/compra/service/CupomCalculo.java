package br.com.droneshop.compra.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.compra.exception.CupomRecusadoException;
import br.com.droneshop.compra.model.Cupom;
import br.com.droneshop.compra.model.ItemCarrinho;
import br.com.droneshop.compra.model.TipoDescontoCupom;

/** Regras do cupom (RN03), usadas no carrinho (#25) e no pedido (#29). */
public final class CupomCalculo {

    public static final String INEXISTENTE = "CUPOM_INEXISTENTE";
    public static final String FORA_DA_VIGENCIA = "FORA_DA_VIGENCIA";
    public static final String VALOR_MINIMO = "VALOR_MINIMO_NAO_ATINGIDO";
    public static final String LIMITE_TOTAL = "LIMITE_TOTAL_ATINGIDO";
    public static final String LIMITE_CLIENTE = "LIMITE_POR_CLIENTE_ATINGIDO";
    public static final String SEM_ITENS = "SEM_ITENS_ELEGIVEIS";

    private CupomCalculo() {
    }

    public static BigDecimal subtotal(List<ItemCarrinho> itens) {
        return itens.stream().map(i -> i.getPrecoUnitarioMomento().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    static boolean elegivel(Cupom c, Produto p) {
        boolean semRestricao = c.getCategorias().isEmpty() && c.getMarcas().isEmpty() && c.getProdutos().isEmpty();
        if (semRestricao)
            return true;
        if (c.getProdutos().stream().anyMatch(x -> x.getId().equals(p.getId())))
            return true;
        if (p.getMarca() != null && c.getMarcas().stream().anyMatch(m -> m.getId().equals(p.getMarca().getId())))
            return true;
        return p.getSubcategoria() != null && p.getSubcategoria().getCategoria() != null
                && c.getCategorias().stream()
                        .anyMatch(k -> k.getId().equals(p.getSubcategoria().getCategoria().getId()));
    }

    /**
     * Desconto do cupom sobre os itens; lança a recusa com o motivo.
     * usosDoCliente null: cliente anônimo, o limite por cliente fica para o checkout.
     */
    public static BigDecimal desconto(Cupom c, List<ItemCarrinho> itens, long usosTotais, Long usosDoCliente,
            LocalDateTime agora) {
        if (c == null || !c.isAtivo())
            throw new CupomRecusadoException(INEXISTENTE, "Cupom não encontrado.");
        if (agora.isBefore(c.getDataInicio()) || (c.getDataFim() != null && agora.isAfter(c.getDataFim())))
            throw new CupomRecusadoException(FORA_DA_VIGENCIA, "Este cupom não está valendo agora.");
        BigDecimal subtotal = subtotal(itens);
        if (c.getValorMinimo() != null && subtotal.compareTo(c.getValorMinimo()) < 0)
            throw new CupomRecusadoException(VALOR_MINIMO,
                    "Compra mínima de R$ " + c.getValorMinimo().setScale(2, RoundingMode.HALF_UP) + " para este cupom.");
        if (c.getLimiteTotalUso() != null && usosTotais >= c.getLimiteTotalUso())
            throw new CupomRecusadoException(LIMITE_TOTAL, "Este cupom já foi usado o máximo de vezes.");
        if (usosDoCliente != null && c.getLimitePorCliente() != null && usosDoCliente >= c.getLimitePorCliente())
            throw new CupomRecusadoException(LIMITE_CLIENTE, "Você já usou este cupom.");
        BigDecimal base = itens.stream().filter(i -> elegivel(c, i.getProduto()))
                .map(i -> i.getPrecoUnitarioMomento().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (base.signum() == 0)
            throw new CupomRecusadoException(SEM_ITENS, "Nenhum item do carrinho vale para este cupom.");
        BigDecimal d = c.getTipoDesconto() == TipoDescontoCupom.PERCENTUAL
                ? base.multiply(c.getValor()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : c.getValor().min(base);
        return d.setScale(2, RoundingMode.HALF_UP);
    }
}
