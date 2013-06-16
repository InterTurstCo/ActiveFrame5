package ru.intertrust.cm.core.business.impl;

import org.junit.Test;
import ru.intertrust.cm.core.config.FileUtils;

import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author vmatsukevich
 *         Date: 5/27/13
 *         Time: 2:38 PM
 */
public class FileUtilsTest {

    public static final String CONFIG_PATH = "test-config/domain-objects.xml";

    @Test
    public void testGetFileInputStream() throws Exception {
        InputStream inputStream = FileUtils.getFileInputStream(CONFIG_PATH);
        assertNotNull(inputStream);
    }

    @Test
    public void testGetFileURL() throws Exception {
        URL url = FileUtils.getFileURL(CONFIG_PATH);
        assertNotNull(url);
        assertTrue(url.getPath().contains("domain-objects.xml"));
    }
}
