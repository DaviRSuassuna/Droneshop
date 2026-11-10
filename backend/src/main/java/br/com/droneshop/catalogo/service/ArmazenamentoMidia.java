package br.com.droneshop.catalogo.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.regex.Pattern;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import br.com.droneshop.exception.ResourceNotFoundException;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;

// Disco das midias (#12, D29), usado tambem pelo logo da marca (#84). O nome salvo e
// sempre gerado aqui (uuid + extensao); o nome enviado nunca vira caminho.
@ApplicationScoped
public class ArmazenamentoMidia {

    public static final String PREFIXO_URL = "/midias/arquivos/";

    private static final Pattern NOME_ARQUIVO = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(png|jpe?g|pdf|svg)$");

    @Inject
    TransactionSynchronizationRegistry transacao;

    @ConfigProperty(name = "aeria.midias.diretorio")
    String diretorio;

    public record Arquivo(byte[] conteudo, String contentType) {
    }

    // Grava o upload e devolve a URL publica; se a transacao desfizer, o arquivo e apagado
    public String gravar(FileUpload upload, String extensao) {
        String salvo = UUID.randomUUID() + "." + extensao;
        Path destino = diretorio().resolve(salvo).normalize();
        try {
            Files.createDirectories(destino.getParent());
            Files.copy(upload.filePath(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível gravar o arquivo de mídia.", e);
        }
        apagarAoTerminar(destino, false);
        return PREFIXO_URL + salvo;
    }

    // true se a URL aponta para um arquivo gravado por este armazenamento
    public boolean ehArquivoEnviado(String url) {
        return url != null && url.startsWith(PREFIXO_URL)
                && NOME_ARQUIVO.matcher(url.substring(PREFIXO_URL.length())).matches();
    }

    // Apaga o arquivo da URL depois que a transacao confirmar
    public void apagar(String url) {
        if (!ehArquivoEnviado(url))
            throw new IllegalStateException("A mídia contém um caminho de arquivo inválido.");
        apagarAoTerminar(diretorio().resolve(url.substring(PREFIXO_URL.length())).normalize(), true);
    }

    public Arquivo buscar(String nome) throws IOException {
        if (nome == null || !NOME_ARQUIVO.matcher(nome).matches())
            throw new ResourceNotFoundException("Arquivo de mídia não encontrado.");
        Path raiz = diretorio();
        Path arquivo = raiz.resolve(nome).normalize();
        if (!raiz.equals(arquivo.getParent()) || !Files.isRegularFile(arquivo))
            throw new ResourceNotFoundException("Arquivo de mídia não encontrado.");
        return new Arquivo(Files.readAllBytes(arquivo), contentType(nome));
    }

    public byte[] inicio(Path caminho, int bytes) {
        try (InputStream input = Files.newInputStream(caminho)) {
            return input.readNBytes(bytes);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler o arquivo enviado.", e);
        }
    }

    // O disco nao participa da transacao: o arquivo so e apagado depois que o banco decide.
    // aposCommit = true apaga se confirmou (exclusao); false apaga se desfez (upload que nao foi salvo).
    private void apagarAoTerminar(Path arquivo, boolean aposCommit) {
        transacao.registerInterposedSynchronization(new Synchronization() {
            @Override
            public void beforeCompletion() {
            }

            @Override
            public void afterCompletion(int status) {
                if ((status == Status.STATUS_COMMITTED) != aposCommit)
                    return;
                try {
                    Files.deleteIfExists(arquivo);
                } catch (IOException e) {
                    Log.warnf(e, "Não foi possível apagar o arquivo de mídia %s", arquivo);
                }
            }
        });
    }

    private Path diretorio() {
        return Path.of(diretorio).toAbsolutePath().normalize();
    }

    private String contentType(String nome) {
        if (nome.endsWith(".png"))
            return "image/png";
        if (nome.endsWith(".jpg") || nome.endsWith(".jpeg"))
            return "image/jpeg";
        if (nome.endsWith(".svg"))
            return "image/svg+xml";
        return "application/pdf";
    }
}
