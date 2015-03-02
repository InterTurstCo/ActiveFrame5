package ru.intertrust.cm.crypto.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.junit.Ignore;
import org.junit.Test;

import ru.intertrust.cm.core.business.api.dto.crypto.VerifyResult;
import ru.intertrust.cm.crypto.CryptoProCryptoBean;

public class TestCryptoProCryptoBean {

    /**
     * Тесты нормально запускаются в случае установленного Cryptopro JCP и установленного сертификата криптопрошного тестового УЦ
     * Для запуска закомментировать @Ignore
     * @throws IOException
     */
    @Test
    @Ignore
    public void testVerifySignature() throws IOException {
        CryptoProCryptoBean bean = new CryptoProCryptoBean();
        try(InputStream document = getClass().getClassLoader().getResourceAsStream("document.png");){        
            VerifyResult result = bean.verify(document, readResource("valid-sctipt-formed.sig"));
            System.out.println(result);
            assertTrue(result.getSignerInfos().get(0).isValid());
        }
    }

    /**
     * Тесты нормально запускаются в случае установленного Cryptopro JCP и установленного сертификата криптопрошного тестового УЦ
     * Для запуска закомментировать @Ignore 
     * @throws IOException
     */
    @Test
    @Ignore
    public void testVerifyInvalidSignature() throws IOException {
        CryptoProCryptoBean bean = new CryptoProCryptoBean();
        try(InputStream document = getClass().getClassLoader().getResourceAsStream("invalid-document.png");){        
            VerifyResult result = bean.verify(document, readResource("valid-sctipt-formed.sig"));
            System.out.println(result);
            assertFalse(result.getSignerInfos().get(0).isValid());
        }
    }

    @Test
    @Ignore
    public void testHash() throws IOException {
        CryptoProCryptoBean bean = new CryptoProCryptoBean();
        try (InputStream document = getClass().getClassLoader().getResourceAsStream("document.png");) {
            byte[] hash = bean.hash(document);
            assertEquals(Base64.encodeBase64String(hash), "xu15NuPCbh5q4p77Kzlp6/1SpGLdsrlSGiKT/69Ro30=");
        }
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
            input.close();
        }
    }    
}
