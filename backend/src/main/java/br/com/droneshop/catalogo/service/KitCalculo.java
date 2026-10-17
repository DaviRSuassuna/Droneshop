package br.com.droneshop.catalogo.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

import br.com.droneshop.catalogo.model.VarianteProduto;

// Valores derivados da variante (D7, D16). O kit nao tem estoque nem embalagem propria:
// tudo vem dos componentes, salvo peso/dimensoes informados no proprio kit.
public final class KitCalculo {

    public record Parte(VarianteProduto variante, int quantidade) {
    }

    private KitCalculo() {
    }

    public static List<Parte> partes(VarianteProduto kit) {
        return kit.getComponentes().stream()
                .map(c -> new Parte(c.getComponente(), c.getQuantidade()))
                .toList();
    }

    // Fisico menos reservas ativas; as reservas chegam com a #23, ate la e o fisico.
    // No kit: o menor (disponivel do componente / quantidade exigida), arredondado para
    // baixo. Componente inativo nao se vende, entao conta como zero.
    public static int disponivel(VarianteProduto v) {
        if (!v.isEhKit())
            return (v.getEstoqueFisico() == null) ? 0 : Math.max(v.getEstoqueFisico(), 0);
        List<Parte> partes = partes(v);
        if (partes.isEmpty())
            return 0;
        int menor = Integer.MAX_VALUE;
        for (Parte p : partes) {
            int disponivel = p.variante().isAtiva() ? disponivel(p.variante()) : 0;
            menor = Math.min(menor, disponivel / p.quantidade());
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
            BigDecimal peso = p.variante().getPesoEmbalagemKg();
            if (peso == null)
                return null;
            total = total.add(peso.multiply(BigDecimal.valueOf(p.quantidade())));
        }
        return total;
    }

    public static Integer alturaDerivada(List<Parte> partes) {
        return maior(partes, VarianteProduto::getAlturaEmbalagemCm);
    }

    public static Integer larguraDerivada(List<Parte> partes) {
        return maior(partes, VarianteProduto::getLarguraEmbalagemCm);
    }

    public static Integer comprimentoDerivado(List<Parte> partes) {
        if (partes.isEmpty())
            return null;
        int total = 0;
        for (Parte p : partes) {
            Integer medida = p.variante().getComprimentoEmbalagemCm();
            if (medida == null)
                return null;
            total += medida * p.quantidade();
        }
        return total;
    }

    // Efetivos: o informado na variante; no kit sem valor informado, o derivado
    public static BigDecimal peso(VarianteProduto v) {
        if (!v.isEhKit() || v.getPesoEmbalagemKg() != null)
            return v.getPesoEmbalagemKg();
        return pesoDerivado(partes(v));
    }

    public static Integer altura(VarianteProduto v) {
        if (!v.isEhKit() || v.getAlturaEmbalagemCm() != null)
            return v.getAlturaEmbalagemCm();
        return alturaDerivada(partes(v));
    }

    public static Integer largura(VarianteProduto v) {
        if (!v.isEhKit() || v.getLarguraEmbalagemCm() != null)
            return v.getLarguraEmbalagemCm();
        return larguraDerivada(partes(v));
    }

    public static Integer comprimento(VarianteProduto v) {
        if (!v.isEhKit() || v.getComprimentoEmbalagemCm() != null)
            return v.getComprimentoEmbalagemCm();
        return comprimentoDerivado(partes(v));
    }

    private static Integer maior(List<Parte> partes, Function<VarianteProduto, Integer> medida) {
        if (partes.isEmpty())
            return null;
        int maior = 0;
        for (Parte p : partes) {
            Integer valor = medida.apply(p.variante());
            if (valor == null)
                return null;
            maior = Math.max(maior, valor);
        }
        return maior;
    }
}
