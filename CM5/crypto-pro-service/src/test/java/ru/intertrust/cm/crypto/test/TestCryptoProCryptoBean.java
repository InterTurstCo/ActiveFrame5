package ru.intertrust.cm.crypto.test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.junit.Ignore;
import org.junit.Test;

import ru.intertrust.cm.crypto.CryptoProCryptoBean;

public class TestCryptoProCryptoBean {

    @Test
    @Ignore
    public void testVerifySignature() throws IOException {
        CryptoProCryptoBean bean = new CryptoProCryptoBean();
        try(InputStream document = getClass().getClassLoader().getResourceAsStream("document.png");){        
            bean.verify(document, readResource("validSignature.sig"));
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
