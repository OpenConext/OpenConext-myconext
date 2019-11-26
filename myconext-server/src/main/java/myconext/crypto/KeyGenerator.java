package myconext.crypto;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;

public class KeyGenerator {

    private final BouncyCastleProvider bcProvider;

    public KeyGenerator() {
        this.bcProvider = new BouncyCastleProvider();
        Security.addProvider(bcProvider);
    }

    public String[] generateKeys() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "BC");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        //PrivateKey is PKCS8 format and we need to end up in PEM format
        Writer writer = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(writer);
        pemWriter.writeObject(kp.getPrivate());
        pemWriter.close();

        String pemString = writer.toString();
        String certificate = certificate(kp);

        return new String[]{pemString, certificate};
    }


    private String certificate(KeyPair keyPair) throws OperatorCreationException, CertificateException, CertIOException {
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

}
