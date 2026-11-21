package br.com.droneshop.testes;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import br.com.droneshop.catalogo.service.ArmazenamentoArquivos;
import br.com.droneshop.exception.ServicoExternoIndisponivelException;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

/**
 * Falso do armazenamento S3 nos testes (#95): os objetos ficam num mapa, nenhum teste depende
 * do RustFS. {@link #foraDoAr} simula o armazenamento indisponível (503).
 */
@Alternative
@Priority(1)
@ApplicationScoped
public class ArmazenamentoMemoria implements ArmazenamentoArquivos {

    private final Map<String, byte[]> objetos = new ConcurrentHashMap<>();
    private volatile boolean foraDoAr;

    public void foraDoAr(boolean valor) {
        foraDoAr = valor;
    }

    public boolean contem(String chave) {
        return objetos.containsKey(chave);
    }

    public boolean contemArquivo(String nome) {
        return objetos.keySet().stream().anyMatch(k -> k.endsWith("/" + nome));
    }

    @Override
    public void gravar(String chave, byte[] conteudo, String contentType) {
        conferir();
        objetos.put(chave, conteudo.clone());
    }

    @Override
    public Optional<byte[]> ler(String chave) {
        conferir();
        return Optional.ofNullable(objetos.get(chave));
    }

    @Override
    public boolean existe(String chave) {
        conferir();
        return objetos.containsKey(chave);
    }

    @Override
    public void remover(String chave) {
        conferir();
        objetos.remove(chave);
    }

    private void conferir() {
        if (foraDoAr)
            throw new ServicoExternoIndisponivelException("Armazenamento de arquivos indisponível.", null);
    }
}
