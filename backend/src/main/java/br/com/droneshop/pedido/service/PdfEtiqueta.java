package br.com.droneshop.pedido.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** PDF de texto em A5, com fonte padrão WinAnsi; não depende de serviço externo. */
final class PdfEtiqueta {
    private static final Charset CODIFICACAO = Charset.forName("windows-1252");
    private PdfEtiqueta() {}

    static byte[] gerar(List<String> linhas) {
        StringBuilder conteudo = new StringBuilder("BT /F1 11 Tf 16 TL 32 555 Td\n");
        for (String linha : linhas) {
            String texto = linha == null ? "" : linha.replaceAll("[\\p{Cntrl}]", " ");
            // Limita a largura sem cortar os campos longos aceitos pelo cadastro.
            do {
                int fim = Math.min(texto.length(), 52);
                String parte = texto.substring(0, fim).replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
                conteudo.append('(').append(parte).append(") Tj T*\n");
                texto = texto.substring(fim);
            } while (!texto.isEmpty());
        }
        conteudo.append("ET\n");
        byte[] stream = conteudo.toString().getBytes(CODIFICACAO);
        var objetos = List.of(
                "<< /Type /Catalog /Pages 2 0 R >>".getBytes(CODIFICACAO),
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>".getBytes(CODIFICACAO),
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 420 595] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>".getBytes(CODIFICACAO),
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>".getBytes(CODIFICACAO),
                concatenar(("<< /Length " + stream.length + " >>\nstream\n").getBytes(CODIFICACAO), stream, "endstream".getBytes(CODIFICACAO)));
        var pdf = new ByteArrayOutputStream();
        pdf.writeBytes("%PDF-1.4\n%âãÏÓ\n".getBytes(CODIFICACAO));
        var offsets = new ArrayList<Integer>();
        for (int i = 0; i < objetos.size(); i++) {
            offsets.add(pdf.size());
            pdf.writeBytes(((i + 1) + " 0 obj\n").getBytes(CODIFICACAO));
            pdf.writeBytes(objetos.get(i));
            pdf.writeBytes("\nendobj\n".getBytes(CODIFICACAO));
        }
        int inicioXref = pdf.size();
        pdf.writeBytes(("xref\n0 6\n0000000000 65535 f \n").getBytes(CODIFICACAO));
        for (int offset : offsets)
            pdf.writeBytes(String.format(Locale.ROOT, "%010d 00000 n \n", offset).getBytes(CODIFICACAO));
        pdf.writeBytes(("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n" + inicioXref + "\n%%EOF\n").getBytes(CODIFICACAO));
        return pdf.toByteArray();
    }

    private static byte[] concatenar(byte[]... partes) {
        var resultado = new ByteArrayOutputStream();
        for (byte[] parte : partes) resultado.writeBytes(parte);
        return resultado.toByteArray();
    }
}
