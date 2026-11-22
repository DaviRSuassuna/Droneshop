package br.com.droneshop.catalogo.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * #87: as fotos dos produtos de exemplo (import-b.sql) ficam em resources/midias-carga e vão
 * para o armazenamento de mídias na subida, só quando ainda não estão lá.
 */
@ApplicationScoped
public class CargaMidias {

    static final String PASTA = "midias-carga/";

    /** Um arquivo por produto da carga (import-b.sql). */
    static final List<String> ARQUIVOS = List.of(
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d01.webp",
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d02.webp",
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d03.webp",
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d04.webp",
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d05.webp",
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d06.webp",
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d07.webp",
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d08.webp",
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d09.webp",
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d10.webp",
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d11.webp",
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d12.webp",
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d13.webp",
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d14.webp",
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d15.webp",
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d16.webp",
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d17.webp",
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d18.webp",
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d19.webp",
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d20.webp",
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d21.webp",
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d22.webp",
            "3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d23.webp");

    @Inject
    MidiaProdutoService midias;

    void aoSubir(@Observes StartupEvent evento) {
        int gravados = 0;
        for (String nome : ARQUIVOS) {
            try (InputStream in = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(PASTA + nome)) {
                if (in == null) {
                    Log.warnf("Foto da carga ausente: %s", nome);
                    continue;
                }
                if (midias.gravarCargaSeAusente(nome, in.readAllBytes()))
                    gravados++;
            } catch (IOException | RuntimeException e) {
                // sem a foto o produto só mostra o quadrado vazio; a aplicação sobe assim mesmo
                Log.warnf(e, "Não foi possível copiar a foto da carga %s", nome);
            }
        }
        if (gravados > 0)
            Log.infof("Fotos da carga de dados copiadas: %d", gravados);
    }
}
