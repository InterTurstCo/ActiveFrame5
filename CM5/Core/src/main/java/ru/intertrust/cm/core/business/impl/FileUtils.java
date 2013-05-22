package ru.intertrust.cm.core.business.impl;

import java.net.URL;

/**
 * @author vmatsukevich
 *         Date: 5/22/13
 *         Time: 1:46 PM
 */
public class FileUtils {

    public static String getFileAbsolutePath(Class loader, String relativePath) {
        URL resource = loader.getResource(relativePath);
        if(resource == null) {
            throw new RuntimeException("File '" + relativePath + "' not found by " + loader);
        }

        return resource.getPath();
    }
}
