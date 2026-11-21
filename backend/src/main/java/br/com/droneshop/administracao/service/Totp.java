package br.com.droneshop.administracao.service;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** TOTP da RFC 6238: passo de 30 s, 6 dígitos, HMAC-SHA1, segredo em Base32 (#45). */
public final class Totp {

    private static final String ALFABETO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Totp() {
    }

    /** Segredo novo de 160 bits em Base32. */
    public static String novoSegredo() {
        byte[] b = new byte[20];
        ALEATORIO.nextBytes(b);
        StringBuilder s = new StringBuilder();
        int buffer = 0, bits = 0;
        for (byte x : b) {
            buffer = (buffer << 8) | (x & 0xff);
            bits += 8;
            while (bits >= 5) {
                s.append(ALFABETO.charAt((buffer >> (bits - 5)) & 31));
                bits -= 5;
            }
        }
        if (bits > 0)
            s.append(ALFABETO.charAt((buffer << (5 - bits)) & 31));
        return s.toString();
    }

    static byte[] base32(String s) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int buffer = 0, bits = 0;
        for (char c : s.replace("=", "").toUpperCase().toCharArray()) {
            buffer = (buffer << 5) | ALFABETO.indexOf(c);
            bits += 5;
            if (bits >= 8) {
                out.write((buffer >> (bits - 8)) & 0xff);
                bits -= 8;
            }
        }
        return out.toByteArray();
    }

    static String codigo(String segredo, long passo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(base32(segredo), "HmacSHA1"));
            byte[] h = mac.doFinal(ByteBuffer.allocate(8).putLong(passo).array());
            int o = h[h.length - 1] & 0x0f;
            int bin = ((h[o] & 0x7f) << 24) | ((h[o + 1] & 0xff) << 16) | ((h[o + 2] & 0xff) << 8) | (h[o + 3] & 0xff);
            return String.format("%06d", bin % 1_000_000);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Aceita o passo atual e um de cada lado (relógio do celular adiantado ou atrasado). */
    public static boolean valido(String segredo, String codigo) {
        if (segredo == null || codigo == null || !codigo.matches("\\d{6}"))
            return false;
        long passo = System.currentTimeMillis() / 1000 / 30;
        for (long d = -1; d <= 1; d++)
            if (codigo(segredo, passo + d).equals(codigo))
                return true;
        return false;
    }
}
