import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

// Gera as chaves JWT só de desenvolvimento em target/jwt-dev (ignoradas pelo Git).
// Uso, a partir de backend/: java scripts/GerarChavesJwtDev.java
public class GerarChavesJwtDev {

    public static void main(String[] args) throws Exception {
        Path diretorio = Path.of("target", "jwt-dev");
        Path privada = diretorio.resolve("privateKey.pem");
        Path publica = diretorio.resolve("publicKey.pem");

        if (Files.exists(privada) && Files.exists(publica)) {
            System.out.println("Chaves JWT de desenvolvimento já existem em " + diretorio.toAbsolutePath());
            return;
        }

        KeyPairGenerator gerador = KeyPairGenerator.getInstance("RSA");
        gerador.initialize(2048);
        KeyPair par = gerador.generateKeyPair();

        Files.createDirectories(diretorio);
        Files.writeString(privada, pem("PRIVATE KEY", par.getPrivate().getEncoded()));
        Files.writeString(publica, pem("PUBLIC KEY", par.getPublic().getEncoded()));
        System.out.println("Chaves JWT de desenvolvimento criadas em " + diretorio.toAbsolutePath());
    }

    private static String pem(String tipo, byte[] conteudo) {
        String corpo = Base64.getMimeEncoder(64, new byte[] { '\n' }).encodeToString(conteudo);
        return "-----BEGIN " + tipo + "-----\n" + corpo + "\n-----END " + tipo + "-----\n";
    }
}
