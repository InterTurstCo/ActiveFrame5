package ru.intertrust.cm.core.business.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ru.intertrust.cm.core.business.api.MD5Service;

/**
 * Тест реализации MD5Service.
 * @author atsvetkov
 *
 */
public class MD5ServiceTest {

    private static final String MD5_CODE = "21232f297a57a5a743894a0e4a801fc3";
    private static final String MESSAGE = "admin";
    private MD5Service md5Service;

    @Before
    public void setUp() {
        md5Service = new MD5ServiceImpl();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testgetMD5() {
        String message = MESSAGE;
        assertTrue(MD5_CODE.equals(md5Service.getMD5(message)));
    }
}
