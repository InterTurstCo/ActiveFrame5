package ru.intertrust.cm.core.business.impl;

import org.junit.Test;
import ru.intertrust.cm.core.business.impl.FileUtils;

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
    @Test
    public void testGetFileInputStream() throws Exception {
        InputStream inputStream = FileUtils.getFileInputStream("config/business-objects.xml");
        assertNotNull(inputStream);
    }

    @Test
    public void testGetFileURL() throws Exception {
        URL url = FileUtils.getFileURL("config/business-objects.xml");
        assertNotNull(url);
        assertTrue(url.getPath().contains("business-objects.xml"));
    }
}
