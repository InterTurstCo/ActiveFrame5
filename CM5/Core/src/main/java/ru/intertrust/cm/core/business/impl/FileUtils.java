package ru.intertrust.cm.core.business.impl;

import java.io.InputStream;
import java.net.URL;

/**
 * @author vmatsukevich
 *         Date: 5/22/13
 *         Time: 1:46 PM
 */
public class FileUtils {

    public static InputStream getFileInputStream(String relativePath) {
        return FileUtils.class.getClassLoader().getResourceAsStream(relativePath);
    }

    public static URL getFileURL(String relativePath) {
        return FileUtils.class.getClassLoader().getResource(relativePath);
    }
}
