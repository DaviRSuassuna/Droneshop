package br.com.droneshop.catalogo.service;

import java.io.IOException;
import java.util.List;

import br.com.droneshop.catalogo.dto.MidiaProdutoAlteracaoRequestDTO;
import br.com.droneshop.catalogo.dto.MidiaProdutoOrdemRequestDTO;
import br.com.droneshop.catalogo.dto.MidiaProdutoRequestDTO;
import br.com.droneshop.catalogo.model.MidiaProduto;
import br.com.droneshop.catalogo.model.TipoMidia;

public interface MidiaProdutoService {

    MidiaProduto criar(long idProduto, MidiaProdutoRequestDTO dto);

    List<MidiaProduto> listar(long idProduto);

    void alterar(long id, MidiaProdutoAlteracaoRequestDTO dto);

    void reordenar(long idProduto, MidiaProdutoOrdemRequestDTO dto);

    void excluir(long id);

    void excluirDoProduto(long idProduto);

    ArquivoMidia buscarArquivo(String nome) throws IOException;

    /** Grava um arquivo da carga de dados (#87) só se ainda não existe; devolve se gravou. */
    boolean gravarCargaSeAusente(String nome, byte[] conteudo);

    record ArquivoMidia(byte[] conteudo, String contentType) {
    }
}
