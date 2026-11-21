package br.com.droneshop.catalogo.service;

import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import br.com.droneshop.exception.ServicoExternoIndisponivelException;
import io.quarkus.arc.profile.UnlessBuildProfile;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Armazenamento S3 (D42): RustFS no compose.yaml, ou a AWS só trocando a configuração
 * (quarkus.s3.endpoint-override e credenciais, por variável de ambiente AERIA_S3_*).
 * Bucket privado: quem serve o arquivo é a API.
 */
// Nos testes o falso em memória (testes/ArmazenamentoMemoria) ocupa o lugar.
@UnlessBuildProfile("test")
@ApplicationScoped
public class ArmazenamentoS3 implements ArmazenamentoArquivos {

    @Inject
    S3Client s3;

    @ConfigProperty(name = "aeria.s3.bucket")
    String bucket;

    // a carga de dados (CargaMidias) também observa a subida; o bucket precisa vir antes
    void aoSubir(@Observes @jakarta.annotation.Priority(1) StartupEvent evento) {
        try {
            criarBucketSeAusente();
        } catch (ServicoExternoIndisponivelException e) {
            Log.warnf(e, "Armazenamento S3 fora do ar na subida; o bucket %s será criado no primeiro uso", bucket);
        }
    }

    @Override
    public void gravar(String chave, byte[] conteudo, String contentType) {
        executar(() -> s3.putObject(b -> b.bucket(bucket).key(chave).contentType(contentType),
                RequestBody.fromBytes(conteudo)));
    }

    @Override
    public Optional<byte[]> ler(String chave) {
        try {
            return Optional.of(s3.getObjectAsBytes(b -> b.bucket(bucket).key(chave)).asByteArray());
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        } catch (S3Exception e) {
            if (e.statusCode() == 404)
                return Optional.empty();
            throw indisponivel(e);
        } catch (SdkException e) {
            throw indisponivel(e);
        }
    }

    @Override
    public boolean existe(String chave) {
        try {
            s3.headObject(b -> b.bucket(bucket).key(chave));
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404)
                return false;
            throw indisponivel(e);
        } catch (SdkException e) {
            throw indisponivel(e);
        }
    }

    @Override
    public void remover(String chave) {
        executar(() -> s3.deleteObject(b -> b.bucket(bucket).key(chave)));
    }

    private void criarBucketSeAusente() {
        try {
            s3.headBucket(b -> b.bucket(bucket));
        } catch (S3Exception e) {
            if (e.statusCode() != 404)
                throw indisponivel(e);
            executar(() -> s3.createBucket(b -> b.bucket(bucket)));
            Log.infof("Bucket %s criado", bucket);
        } catch (SdkException e) {
            throw indisponivel(e);
        }
    }

    private void executar(Runnable chamada) {
        try {
            chamada.run();
        } catch (SdkException e) {
            throw indisponivel(e);
        }
    }

    private static ServicoExternoIndisponivelException indisponivel(Exception causa) {
        return new ServicoExternoIndisponivelException("Armazenamento de arquivos indisponível.", causa);
    }
}
