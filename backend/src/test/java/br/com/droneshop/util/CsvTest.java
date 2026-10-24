package br.com.droneshop.util;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("issue-61")
class CsvTest {
    @Test
    void preservaAcentosAspasSeparadoresEQuebrasDeLinha() {
        try (var resposta = Csv.resposta("teste.csv", List.of("Descrição", "Valor"),
                Stream.of(Arrays.<Object>asList("Peça; \"A\"\nsegunda linha", null)))) {
            byte[] bytes = (byte[]) resposta.getEntity();
            assertArrayEquals(new byte[] {(byte) 0xef, (byte) 0xbb, (byte) 0xbf}, Arrays.copyOf(bytes, 3));
            String csv = new String(bytes, StandardCharsets.UTF_8);
            assertTrue(csv.contains("\"Descrição\";\"Valor\"\r\n"));
            assertTrue(csv.contains("\"Peça; \"\"A\"\"\nsegunda linha\";\"\"\r\n"));
        }
    }

    @Test
    void textoNaoViraFormulaMasNumeroContinuaNumero() {
        try (var resposta = Csv.resposta("teste.csv", List.of("Texto", "Número"),
                Stream.of(List.<Object>of("  =1+1", -10)))) {
            String csv = new String((byte[]) resposta.getEntity(), StandardCharsets.UTF_8);
            assertTrue(csv.contains("\"'  =1+1\";\"-10\""));
        }
    }
}
