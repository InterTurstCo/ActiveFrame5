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
import ru.intertrust.cm.crypto.BouncycastleCryptoBean;

public class TestBouncycastleCryptoBean {

    @Test
    //@Ignore
    public void testVerifySignature() throws IOException {
        BouncycastleCryptoBean bean = new BouncycastleCryptoBean();
        try (InputStream document = getClass().getClassLoader().getResourceAsStream("TC_pdf.pdf")) {
            VerifyResult result = bean.verify(document, readResource("ddcb5fe4-b07f-425f-847b-9bf2a2db9bfa.sig"));
            System.out.println(result);
            assertTrue(result.getSignerInfos().get(0).isValid());
        }
    }

    @Test
    //@Ignore
    public void testVerifyInvalidSignature() throws IOException {
        BouncycastleCryptoBean bean = new BouncycastleCryptoBean();
        try (InputStream document = getClass().getClassLoader().getResourceAsStream("invalid-document.png");) {
            VerifyResult result = bean.verify(document, readResource("validSignatureVipNetScp.sig"));
            System.out.println(result);
            assertFalse(result.getSignerInfos().get(0).isValid());
        }
    }

    @Test
    //@Ignore
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
            if (input != null){
                input.close();
            }
        }
    }
}
