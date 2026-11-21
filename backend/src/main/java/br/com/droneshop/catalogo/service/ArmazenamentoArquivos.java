package br.com.droneshop.catalogo.service;

import java.util.Optional;

/**
 * Onde ficam os arquivos das mídias (D42). A chave do objeto é
 * {@code produtos/{idProduto}/{uuid}.{ext}}; a URL pública continua /midias/arquivos/{nome}.
 * Armazenamento fora do ar: lança ServicoExternoIndisponivelException (503).
 */
public interface ArmazenamentoArquivos {

    void gravar(String chave, byte[] conteudo, String contentType);

    /** Conteúdo do objeto, ou vazio se ele não existe. */
    Optional<byte[]> ler(String chave);

    boolean existe(String chave);

    /** Remove o objeto; não falha se ele já não existe. */
    void remover(String chave);
}
