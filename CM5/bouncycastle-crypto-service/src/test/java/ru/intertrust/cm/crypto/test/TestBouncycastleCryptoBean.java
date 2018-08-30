package ru.intertrust.cm.crypto.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Ignore;
import org.junit.Test;

import ru.intertrust.cm.core.business.api.dto.crypto.SignerInfo;
import ru.intertrust.cm.core.business.api.dto.crypto.VerifyResult;
import ru.intertrust.cm.crypto.BouncycastleCryptoBean;
import ru.intertrust.cm.crypto.CertificateVerifier;

public class TestBouncycastleCryptoBean {

    @Test
    @Ignore
    public void testVerifySignature() throws IOException {
        BouncycastleCryptoBean bean = new BouncycastleCryptoBean();
        try (InputStream document = getClass().getClassLoader().getResourceAsStream("b26e4669-612e-492d-8266-bca2528e2c00.pdf")) {
            VerifyResult result = bean.verify(document, readResource("8def016d-8728-45dd-99be-67299b7619c5.sig"));
            System.out.println(result);
            assertTrue(result.getSignerInfos().get(0).isValid());
        }
    }

    @Test
    @Ignore
    public void testVerifyInvalidSignature() throws IOException {
        BouncycastleCryptoBean bean = new BouncycastleCryptoBean();
        try (InputStream document = getClass().getClassLoader().getResourceAsStream("invalid-document.png");) {
            VerifyResult result = bean.verify(document, readResource("validSignatureVipNetScp.sig"));
            System.out.println(result);
            assertFalse(result.getSignerInfos().get(0).isValid());
        }
    }

    @Test
    @Ignore
    public void testHash() throws IOException {
        BouncycastleCryptoBean bean = new BouncycastleCryptoBean();
        try (InputStream document = getClass().getClassLoader().getResourceAsStream("document.png");) {
            //Сейчас алгоритм хэширования забит в код
            byte[] hash = bean.hash(document);
            //3411
            //assertEquals(Base64.encodeBase64String(hash), "xu15NuPCbh5q4p77Kzlp6/1SpGLdsrlSGiKT/69Ro30=");
            //3411-2012-256
            assertEquals(Base64.encodeBase64String(hash), "KZeAILku4iKfMvKmAhU5gLctrdNZ0MgVkIcpBzLgeWM=");
        }
    }

    @Test
    @Ignore
    public void testVerifyCertificate() throws Exception {
        CertificateVerifier certificateVerifier = new CertificateVerifier();

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(this.getClass().getClassLoader().getResourceAsStream("cert/chursin.cer"));

        X509Certificate addCert = (X509Certificate) cf.generateCertificate(this.getClass().getClassLoader().getResourceAsStream("cert/add-cert.cer"));

        certificateVerifier.verifyCertificate(cert, Collections.singleton(addCert));
    }

    /**
     * Получение файла в виде массива байт
     * @param file
     * @return
     * @throws IOException
     */
    protected byte[] readResource(String path) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream input = null;
        try {
            input = getClass().getClassLoader().getResourceAsStream(path);
            int read = 0;
            byte[] buffer = new byte[1024];
            while ((read = input.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }

            return Base64.decodeBase64(out.toByteArray());
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    @Test
    @Ignore
    public void test3410_2012_512() throws Exception {
        //Исходный текст
        byte[] source = Base64.decodeBase64("VGVzdCBzdHJpbmc=");
        //CADES подпись
        byte[] cadesSignature = Base64.decodeBase64("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");
        //То что подписываем исходный текст плюс атрибуты (из недр ВС)
        byte[] message = Base64.decodeBase64(
                "MYICJzAYBgkqhkiG9w0BCQMxCwYJKoZIhvcNAQcBMBwGCSqGSIb3DQEJBTEPFw0xODA4MzAwOTAzNTBaME8GCSqGSIb3DQEJBDFCBECha1feZsN38x3Im7EwWN8nsoGEVpqCNluxE9gSRjeXRQXHDLMqhPoRPobB+CnvOiwkj4Ilds87COUFoxrnwdSMMIIBmgYLKoZIhvcNAQkQAi8xggGJMIIBhTCCAYEwggF9MAoGCCqFAwcBAQIDBEAiU0xuYVw1b80Y7dZgCTW/m9lb0exsiIK2kM7PSerJlksdjXcuBy+YOnaZYxfqYhx+479RLXeuIP492z7PCnGFMIIBKzCCARKkggEOMIIBCjEYMBYGBSqFA2QBEg0xMjM0NTY3ODkwMTIzMRowGAYIKoUDA4EDAQESDDAwMTIzNDU2Nzg5MDEvMC0GA1UECQwm0YPQuy4g0KHRg9GJ0ZHQstGB0LrQuNC5INCy0LDQuyDQtC4gMTgxCzAJBgNVBAYTAlJVMRkwFwYDVQQIDBDQsy4g0JzQvtGB0LrQstCwMRUwEwYDVQQHDAzQnNC+0YHQutCy0LAxJTAjBgNVBAoMHNCe0J7QniAi0JrQoNCY0J/QotCeLdCf0KDQniIxOzA5BgNVBAMMMtCi0LXRgdGC0L7QstGL0Lkg0KPQpiDQntCe0J4gItCa0KDQmNCf0KLQni3Qn9Cg0J4iAhN8AAAPWU70nnc0naKlAAAAAA9Z");
        //ХЭШ того что подписываем (из недр ВС)
        byte[] hash = Base64.decodeBase64("aqWB4qz7t7Kj9MRsHSKl7Ev19RfRd7BdB05rM/p7832wO6ao3I/hCj+X+bXMCCRSHmjNZs22NPf5r9iEoPTkRA==");
        //Проверяемая Подпись (из недр ВС)
        byte[] signature = Base64.decodeBase64(
                "1IPOS3uiPNhbhlcmWqRtN+ZsXhoLPmjietxALrLIMIg5x+Q8M/l21KHEJPsEdbTKfDIOoaAvihWb5mUGi29bWb3DVBSqZhmZVhe/cV6zOJI4sUfc5yEFAvemYf/FLD69sStFcAdZLk99l8nmfkQr65oDbGeSOJ3o21G2Q1VJDXE=");
        
        
        // читаю публичный ключ из файла сертификата
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(this.getClass().getClassLoader().getResourceAsStream("cert/sklochko.cer"));
        PublicKey publicKey = cert.getPublicKey();

        //Формирую ХЭШ сообщения, которое поймал под отладчиком в ВС
        final MessageDigest digest = MessageDigest.getInstance("GOST3411_2012_512");
        digest.update(message);
        byte[] hash0 = digest.digest();

        //Проверяю вычисленный идентичновть вычисленного ХЭШ и пойманного под отладчиком в ВС
        assertEquals(hash0.length, hash.length);
        for (int i = 0; i < hash.length; i++) {
            assertTrue(hash0[i] == hash[i]);
        }

        //Инициирую проверку подписи сообщения пойманных под отладчиком в ВС с помощью CP 
        Signature sig = Signature.getInstance("GOST3411_2012_512withGOST3410_2012_512", "JCP");
        sig.initVerify(publicKey);
        sig.update(message);
        assertTrue(sig.verify(signature));

        //Инициирую проверку подписи сообщения пойманных под отладчиком в ВС с помощью ВС
        Security.addProvider(new BouncyCastleProvider());
        sig = Signature.getInstance("GOST3411-2012-512WITHECGOST3410-2012-512", "BC");
        sig.initVerify(publicKey);
        sig.update(message);
        assertTrue(sig.verify(signature));

        //А теперь через BouncycastleCryptoBean
        BouncycastleCryptoBean cryptoBean = new BouncycastleCryptoBean();
        VerifyResult verifyResult = cryptoBean.verify(new ByteArrayInputStream(source), cadesSignature);
        for(SignerInfo signerInfo : verifyResult.getSignerInfos()) {
            assertTrue(signerInfo.isValid());
        }
        
    }

    public void listAlgoritms() {
        // Security.addProvider(new
        // org.bouncycastle.jce.provider.BouncyCastleProvider());

        // get a list of services and their respective providers.
        final Map<String, List<Provider>> services = new TreeMap<>();

        for (Provider provider : Security.getProviders()) {
            for (Provider.Service service : provider.getServices()) {
                if (services.containsKey(service.getType())) {
                    final List<Provider> providers = services.get(service
                            .getType());
                    if (!providers.contains(provider)) {
                        providers.add(provider);
                    }
                } else {
                    final List<Provider> providers = new ArrayList<>();
                    providers.add(provider);
                    services.put(service.getType(), providers);
                }
            }
        }

        // now get a list of algorithms and their respective providers
        for (String type : services.keySet()) {
            final Map<String, List<Provider>> algs = new TreeMap<>();
            for (Provider provider : Security.getProviders()) {
                for (Provider.Service service : provider.getServices()) {
                    if (service.getType().equals(type)) {
                        final String algorithm = service.getAlgorithm();
                        if (algs.containsKey(algorithm)) {
                            final List<Provider> providers = algs
                                    .get(algorithm);
                            if (!providers.contains(provider)) {
                                providers.add(provider);
                            }
                        } else {
                            final List<Provider> providers = new ArrayList<>();
                            providers.add(provider);
                            algs.put(algorithm, providers);
                        }
                    }
                }
            }

            // write the results to standard out.
            System.out.printf("%20s : %s\n", "", type);
            for (String algorithm : algs.keySet()) {
                System.out.printf("%-20s : %s\n", algorithm,
                        Arrays.toString(algs.get(algorithm).toArray()));
            }
            System.out.println();
        }
    }

}
