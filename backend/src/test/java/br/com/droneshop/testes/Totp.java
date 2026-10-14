package br.com.droneshop.testes;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Código TOTP (RFC 6238: SHA-1, 30 s, 6 dígitos) para os testes de 2FA (#45). */
public final class Totp {

    private Totp() {
    }

    public static String codigo(String segredoBase32) {
        return codigo(segredoBase32, System.currentTimeMillis() / 1000 / 30);
    }

    public static String codigo(String segredoBase32, long passo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(base32(segredoBase32), "HmacSHA1"));
            byte[] h = mac.doFinal(ByteBuffer.allocate(8).putLong(passo).array());
            int o = h[h.length - 1] & 0x0f;
            int bin = ((h[o] & 0x7f) << 24) | ((h[o + 1] & 0xff) << 16) | ((h[o + 2] & 0xff) << 8) | (h[o + 3] & 0xff);
            return String.format("%06d", bin % 1_000_000);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static byte[] base32(String s) {
        String alfabeto = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int buffer = 0, bits = 0;
        for (char c : s.replace("=", "").toUpperCase().toCharArray()) {
            buffer = (buffer << 5) | alfabeto.indexOf(c);
            bits += 5;
            if (bits >= 8) {
                out.write((buffer >> (bits - 8)) & 0xff);
                bits -= 8;
            }
        }
        return out.toByteArray();
    }
}
