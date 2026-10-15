package br.com.droneshop.testes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class JwtTestKeys implements QuarkusTestResourceLifecycleManager {

    private Path privateKey;

    @Override
    public Map<String, String> start() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();

            Path directory = Path.of("target", "test-jwt");
            Files.createDirectories(directory);
            privateKey = directory.resolve("privateKey.pem");
            Files.writeString(privateKey, pem("PRIVATE KEY", pair.getPrivate().getEncoded()));

            return Map.of(
                    "smallrye.jwt.sign.key.location", privateKey.toAbsolutePath().toUri().toString(),
                    "mp.jwt.verify.publickey", pem("PUBLIC KEY", pair.getPublic().getEncoded()));
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível criar as chaves JWT temporárias dos testes.", e);
        }
    }

    @Override
    public void stop() {
    }

    private String pem(String type, byte[] encoded) {
        String body = Base64.getMimeEncoder(64, new byte[] { '\n' }).encodeToString(encoded);
        return "-----BEGIN " + type + "-----\n" + body + "\n-----END " + type + "-----\n";
    }
}
