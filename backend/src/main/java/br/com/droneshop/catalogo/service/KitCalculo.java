package br.com.droneshop.catalogo.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

import br.com.droneshop.catalogo.model.Kit;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.StatusProduto;

// Valores derivados do produto (D7, D16). O kit nao tem estoque nem embalagem propria:
// tudo vem dos componentes, salvo peso/dimensoes informados no proprio kit.
public final class KitCalculo {

    public record Parte(Produto produto, int quantidade) {
    }

    private KitCalculo() {
    }

    public static List<Parte> partes(Kit kit) {
        return kit.getComponentes().stream()
                .map(c -> new Parte(c.getProduto(), c.getQuantidade()))
                .toList();
    }

    // Fisico menos reservas ativas; as reservas chegam com a #23, ate la e o fisico.
    // No kit: o menor (disponivel do componente / quantidade exigida), arredondado para
    // baixo. Componente que nao esta ATIVO nao se vende, entao conta como zero.
    public static int disponivel(Produto p) {
        if (!(p instanceof Kit kit))
            return (p.getEstoqueFisico() == null) ? 0 : Math.max(p.getEstoqueFisico(), 0);
        List<Parte> partes = partes(kit);
        if (partes.isEmpty())
            return 0;
        int menor = Integer.MAX_VALUE;
        for (Parte parte : partes) {
            int disponivel = parte.produto().getStatus() == StatusProduto.ATIVO ? disponivel(parte.produto()) : 0;
            menor = Math.min(menor, disponivel / parte.quantidade());
        }
        return menor;
    }

    // Derivados: peso somado; altura e largura, a maior; comprimento somado (caixas
    // enfileiradas). Nulo quando nao ha componente ou algum esta sem a medida.
    public static BigDecimal pesoDerivado(List<Parte> partes) {
        if (partes.isEmpty())
            return null;
        BigDecimal total = BigDecimal.ZERO;
        for (Parte p : partes) {
            BigDecimal peso = p.produto().getPesoEmbalagemKg();
            if (peso == null)
                return null;
            total = total.add(peso.multiply(BigDecimal.valueOf(p.quantidade())));
        }
        return total;
    }

    public static Integer alturaDerivada(List<Parte> partes) {
        return maior(partes, Produto::getAlturaEmbalagemCm);
    }

    public static Integer larguraDerivada(List<Parte> partes) {
        return maior(partes, Produto::getLarguraEmbalagemCm);
    }

    public static Integer comprimentoDerivado(List<Parte> partes) {
        if (partes.isEmpty())
            return null;
        int total = 0;
        for (Parte p : partes) {
            Integer medida = p.produto().getComprimentoEmbalagemCm();
            if (medida == null)
                return null;
            total += medida * p.quantidade();
        }
        return total;
    }

    // Efetivos: o informado no produto; no kit sem valor informado, o derivado
    public static BigDecimal peso(Produto p) {
        if (!(p instanceof Kit kit) || p.getPesoEmbalagemKg() != null)
            return p.getPesoEmbalagemKg();
        return pesoDerivado(partes(kit));
    }

    public static Integer altura(Produto p) {
        if (!(p instanceof Kit kit) || p.getAlturaEmbalagemCm() != null)
            return p.getAlturaEmbalagemCm();
        return alturaDerivada(partes(kit));
    }

    public static Integer largura(Produto p) {
        if (!(p instanceof Kit kit) || p.getLarguraEmbalagemCm() != null)
            return p.getLarguraEmbalagemCm();
        return larguraDerivada(partes(kit));
    }

    public static Integer comprimento(Produto p) {
        if (!(p instanceof Kit kit) || p.getComprimentoEmbalagemCm() != null)
            return p.getComprimentoEmbalagemCm();
        return comprimentoDerivado(partes(kit));
    }

    private static Integer maior(List<Parte> partes, Function<Produto, Integer> medida) {
        if (partes.isEmpty())
            return null;
        int maior = 0;
        for (Parte p : partes) {
            Integer valor = medida.apply(p.produto());
            if (valor == null)
                return null;
            maior = Math.max(maior, valor);
        }
        return maior;
    }
}
