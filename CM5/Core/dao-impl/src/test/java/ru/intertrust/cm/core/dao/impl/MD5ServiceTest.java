package ru.intertrust.cm.core.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ru.intertrust.cm.core.dao.api.MD5Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Тест реализации MD5Service.
 * @author atsvetkov
 *
 */
public class MD5ServiceTest {

    private static final String MD5_CODE = "21232f297a57a5a743894a0e4a801fc3";
    private static final String MESSAGE = "admin";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private MD5Service md5Service;

    @Before
    public void setUp() {
        md5Service = new MD5ServiceImpl();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetMD5() {
        String message = MESSAGE;
        assertTrue(MD5_CODE.equals(md5Service.getMD5AsHex(message)));
    }

    @Test
    public void testGetMD5IfMessageNull() {
        String message = null;
        assertTrue(null == md5Service.getMD5AsHex(message));
    }

    @Test
    public void bytesToHex() throws NoSuchAlgorithmException {
        final MessageDigest md = MessageDigest.getInstance("MD5");
        final byte[] digest = md.digest(MESSAGE.getBytes(StandardCharsets.UTF_8));
        final String hex = md5Service.bytesToHex(digest);

        assertEquals(MD5_CODE, hex);
    }

    @Test
    public void newMessageDigest() throws NoSuchAlgorithmException {
        final MessageDigest md = MessageDigest.getInstance("MD5");
        final MessageDigest md2 = md5Service.newMessageDigest();
        assertEquals(md.getAlgorithm(), md2.getAlgorithm());
    }
}
