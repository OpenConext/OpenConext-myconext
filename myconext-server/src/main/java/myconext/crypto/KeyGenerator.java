package myconext.crypto;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
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
        Base64.Encoder encoder = Base64.getEncoder();

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "BC");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        //PrivateKey is PKCS8 format and we need to end up in PEM format
        PrivateKey priv = kp.getPrivate();
        byte[] privBytes = priv.getEncoded();

        PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(privBytes);
        ASN1Encodable encodable = pkInfo.parsePrivateKey();
        ASN1Primitive primitive = encodable.toASN1Primitive();
        byte[] privateKeyPKCS1 = primitive.getEncoded();

//            PKCS8EncodedKeySpec keyspec = new PKCS8EncodedKeySpec(kp.getPrivate().getEncoded());

        String privateKey = "-----BEGIN PRIVATE KEY-----\n";
        privateKey += encoder.encodeToString(privateKeyPKCS1);
        privateKey += "\n-----END PRIVATE KEY-----\n";

        PemObject pemObject = new PemObject("RSA PRIVATE KEY", privateKeyPKCS1);
        StringWriter stringWriter = new StringWriter();
        PemWriter pemWriter = new PemWriter(stringWriter);
        pemWriter.writeObject(pemObject);
        pemWriter.close();
        String pemString = stringWriter.toString();

//            StringBuilder sb = new StringBuilder();
//            sb.append("-----BEGIN PRIVATE KEY-----");
//            sb.append(new String(Base64.encode(keyspec.getEncoded())));
//            sb.append("-----END PRIVATE KEY-----");

//            PrivateKeyInfo pkInfo = PrivateKeyInfoFactory.createPrivateKeyInfo((AsymmetricKeyParameter) kp.getPrivate());
//            String privateKey = encoder.encodeToString(pkInfo.get);
////https://stackoverflow.com/questions/48333877/how-to-write-private-key-to-password-protected-der-format-in-java
//            String privateKey = "-----BEGIN RSA PRIVATE KEY-----\n";
//            privateKey += encoder.encodeToString(kp.getPrivate().getEncoded());
//            privateKey += "\n-----END RSA PRIVATE KEY-----\n";

        Certificate certificate = selfSign(kp, "CN=test,O=Test Certificate", bcProvider);

        String certificateS = "-----BEGIN CERTIFICATE-----\n";
        certificateS += encoder.encodeToString(certificate.getEncoded());
        certificateS += "\n-----END CERTIFICATE-----\n";
        return new String[]{pemString, certificateS};
    }



    private Certificate selfSign(KeyPair keyPair, String subjectDN, Provider bcProvider) throws OperatorCreationException, CertificateException, CertIOException {

        Date startDate = new Date();

        X500Name dnName = new X500Name(subjectDN);
        BigInteger certSerialNumber = new BigInteger(Long.toString(System.currentTimeMillis())); // <-- Using the current timestamp as the certificate serial number
        Date endDate = Date.from(LocalDateTime.now().plusYears(1).toInstant(ZoneOffset.UTC));
        String signatureAlgorithm = "SHA256WithRSA";

        ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgorithm).build(keyPair.getPrivate());

        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(dnName, certSerialNumber, startDate, endDate, dnName, keyPair.getPublic());

        // Extensions --------------------------

        // Basic Constraints
//        BasicConstraints basicConstraints = new BasicConstraints(true); // <-- true for CA, false for EndEntity
//
//        certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints); // Basic Constraints is usually marked as critical.
//
        // -------------------------------------

        return new JcaX509CertificateConverter().setProvider(bcProvider).getCertificate(certBuilder.build(contentSigner));
    }

}
