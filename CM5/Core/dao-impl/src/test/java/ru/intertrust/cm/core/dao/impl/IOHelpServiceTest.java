package ru.intertrust.cm.core.dao.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.UnixLineEndingInputStream;
import org.apache.commons.io.input.WindowsLineEndingInputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.intertrust.cm.core.business.api.util.MD5Utils;
import ru.intertrust.cm.core.dao.api.MD5Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IOHelpServiceTest {

    private static final String HELP_FILE_MD5_SUM_LF = "a035872bf84171d5fc069e78728f5072";
    private static final String HELP_FILE_MD5_SUM_CRLF = "0f93c6836d3cd732a60b729900c2c0d9";

    @Mock
    private MD5Service md5Service;

    @InjectMocks
    private IOHelpServiceImpl service;

    private byte[] crlf;
    private byte[] lf;

    @Before
    public void setUp() throws IOException, NoSuchAlgorithmException {
        InputStream inputStream = getTestFile();
        crlf = prepareCrlf(inputStream);

        inputStream = getTestFile();
        lf = prepareLf(inputStream);

        when(md5Service.newMessageDigest()).thenReturn(MessageDigest.getInstance("MD5"));

    }

    private InputStream getTestFile() {
        return getClass().getClassLoader().getResourceAsStream("io-help-service-test-file.txt");
    }

    private byte[] prepareCrlf(InputStream inputStream) throws IOException {
        try (InputStream is = new WindowsLineEndingInputStream(inputStream, false)) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            IOUtils.copy(is, os);
            return os.toByteArray();
        }
    }

    private byte[] prepareLf(InputStream inputStream) throws IOException {
        try (InputStream is = new UnixLineEndingInputStream(inputStream, false)) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            IOUtils.copy(is, os);
            return os.toByteArray();
        }
    }

    @Test
    public void copyWithEolControl_crlf_to_lf() throws IOException, NoSuchAlgorithmException {
        assertEquals(HELP_FILE_MD5_SUM_CRLF, MD5Utils.getMD5AsHex(crlf));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        service.copyWithEolControl(() -> new ByteArrayInputStream(crlf), os);

        final String newHash = MD5Utils.getMD5AsHex(os.toByteArray());

        assertEquals(HELP_FILE_MD5_SUM_LF, newHash);
    }

    @Test
    public void copyWithEolControl_lf_to_lf() throws IOException, NoSuchAlgorithmException {
        assertEquals(HELP_FILE_MD5_SUM_LF, MD5Utils.getMD5AsHex(lf));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        service.copyWithEolControl(() -> new ByteArrayInputStream(lf), os);

        final String newHash = MD5Utils.getMD5AsHex(os.toByteArray());

        assertEquals(HELP_FILE_MD5_SUM_LF, newHash);
    }

    @Test
    public void copyWithEolControlAndMd5_crlf_to_lf() throws NoSuchAlgorithmException {
        assertEquals(HELP_FILE_MD5_SUM_CRLF, MD5Utils.getMD5AsHex(crlf));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        service.copyWithEolControlAndMd5(() -> new ByteArrayInputStream(crlf), os);

        ArgumentCaptor<byte[]> argumentCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(md5Service).bytesToHex(argumentCaptor.capture());

        // Нужно сравнить, что новый массив байт равен lf
        // А так же, что MD5 сумма, которая в captor в виде байт - будет равна нашей сумме
        // (либо проверять сам массив байт, что более честно, но особого смысла не вижу)
        assertArrayEquals(lf, os.toByteArray());
        assertEquals(HELP_FILE_MD5_SUM_LF, MD5Utils.bytesToHex(argumentCaptor.getValue()));
    }

    @Test
    public void copyWithEolControlAndMd5_lf_to_lf() {
        assertEquals(HELP_FILE_MD5_SUM_LF, MD5Utils.getMD5AsHex(lf));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        service.copyWithEolControlAndMd5(() -> new ByteArrayInputStream(lf), os);

        ArgumentCaptor<byte[]> argumentCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(md5Service).bytesToHex(argumentCaptor.capture());

        // Нужно сравнить, что новый массив байт равен lf
        // А так же, что MD5 сумма, которая в captor в виде байт - будет равна нашей сумме
        // (либо проверять сам массив байт, что более честно, но особого смысла не вижу)
        assertArrayEquals(lf, os.toByteArray());
        assertEquals(HELP_FILE_MD5_SUM_LF, MD5Utils.bytesToHex(argumentCaptor.getValue()));
    }
}