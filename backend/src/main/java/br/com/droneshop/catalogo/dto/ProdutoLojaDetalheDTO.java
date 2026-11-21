package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;
import java.util.List;

import br.com.droneshop.catalogo.model.TipoProduto;

/**
 * Página do produto na loja (#21). Sem custo, fornecedor nem estoque físico.
 * As specs de drone ficam nulas no acessório e tipoAcessorio fica nulo no drone.
 */
public record ProdutoLojaDetalheDTO(
        Long id,
        String nome,
        String slug,
        TipoProduto tipo,
        String descricaoCurta,
        String descricao,
        Referencia marca,
        Referencia subcategoria,
        int garantiaMeses,
        String metaTitle,
        String metaDescription,
        Integer pesoGramas,
        String dimensoesDobrado,
        String dimensoesAberto,
        Integer autonomiaMinutos,
        Integer alcanceMetros,
        Integer velocidadeMaximaKmh,
        Integer resolucaoFotoMp,
        String tipoSensor,
        String resolucaoVideo,
        String estabilizacao,
        String tipoBateria,
        Integer capacidadeBateriaMah,
        Integer tempoCarregamentoMinutos,
        Boolean gpsIntegrado,
        Boolean sensoresAnticolisao,
        Boolean exigeRegistroAnac,
        String classeAnac,
        String tipoAcessorio,
        Integer pesoItemG,
        String sku,
        BigDecimal preco,
        int disponivel,
        String imagemPrincipal,
        Grupo grupo,
        List<MidiaProdutoResponseDTO> midias
) {
    public record Referencia(Long id, String nome, String slug) {
    }

    public record Grupo(String nome, String slug, List<Versao> versoes) {
    }

    public record Versao(Long id, String rotulo, String slug, BigDecimal preco, int disponivel) {
    }
}
