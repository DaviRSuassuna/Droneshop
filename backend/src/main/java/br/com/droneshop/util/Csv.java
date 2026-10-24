package br.com.droneshop.util;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;
import jakarta.ws.rs.core.Response;

/** CSV para planilhas: UTF-8 com BOM, ponto e vírgula e células escapadas. */
public final class Csv {
    private Csv() {}

    public static Response resposta(String arquivo, List<String> cabecalho, Stream<List<?>> linhas) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        linha(csv, cabecalho);
        try (linhas) {
            linhas.forEach(valores -> linha(csv, valores));
        }
        return Response.ok(csv.toString().getBytes(StandardCharsets.UTF_8), "text/csv; charset=UTF-8")
                .header("Content-Disposition", "attachment; filename=\"" + arquivo + "\"").build();
    }

    private static void linha(StringBuilder csv, List<?> valores) {
        for (int i = 0; i < valores.size(); i++) {
            if (i > 0) csv.append(';');
            Object valor = valores.get(i);
            String texto = valor == null ? "" : valor.toString();
            // Texto externo não pode virar fórmula quando o arquivo for aberto no Excel.
            String inicio = texto.stripLeading();
            if (valor instanceof String && !inicio.isEmpty() && "=+-@".indexOf(inicio.charAt(0)) >= 0)
                texto = "'" + texto;
            csv.append('"').append(texto.replace("\"", "\"\"")).append('"');
        }
        csv.append("\r\n");
    }
}
