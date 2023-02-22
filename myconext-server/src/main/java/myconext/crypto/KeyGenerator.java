package myconext.crypto;

import lombok.SneakyThrows;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;

public class KeyGenerator {

    private static final BouncyCastleProvider bcProvider = new BouncyCastleProvider();

    static {
        Security.addProvider(bcProvider);
    }

    private KeyGenerator() {
    }

    public static String[] generateKeys() throws Exception {
        KeyPair kp = keyPairGenerator();

        String pemString = privateKey(kp);
        String certificate = certificate(kp);

        return new String[]{pemString, certificate};
    }

    private static String privateKey(KeyPair kp) throws IOException {
        //PrivateKey is PKCS8 format, and we need to end up in PEM format
        Writer writer = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(writer);
        pemWriter.writeObject(kp.getPrivate());
        pemWriter.close();

        return writer.toString();
    }

    private static KeyPair keyPairGenerator() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "BC");
        kpg.initialize(2048);
        return kpg.generateKeyPair();
    }


    private static String certificate(KeyPair keyPair) throws OperatorCreationException, CertificateException {
        X500Name dnName = new X500Name("CN=test,O=Test Certificate");
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());

        JcaX509v3CertificateBuilder certBuilder =
                new JcaX509v3CertificateBuilder(
                        dnName,
                        new BigInteger(Long.toString(System.currentTimeMillis())),
                        new Date(),
                        Date.from(LocalDateTime.now().plusYears(1).toInstant(ZoneOffset.UTC)),
                        dnName,
                        keyPair.getPublic());

        X509Certificate certificate = new JcaX509CertificateConverter()
                .setProvider(bcProvider)
                .getCertificate(certBuilder.build(contentSigner));

        String result = "-----BEGIN CERTIFICATE-----\n";
        result += Base64.getEncoder().encodeToString(certificate.getEncoded());
        result += "\n-----END CERTIFICATE-----\n";
        return result;
    }

    @SneakyThrows
    public static String oneWayHash(String s) {
        return new String(Hex.encode(MessageDigest.getInstance("SHA-256").digest(s.getBytes())));
    }

}
