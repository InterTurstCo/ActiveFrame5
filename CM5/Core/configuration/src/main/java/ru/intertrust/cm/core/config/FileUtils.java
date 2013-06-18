package ru.intertrust.cm.core.config;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 * Служебный класс для загрузки файлов
 * @author vmatsukevich
 *         Date: 5/22/13
 *         Time: 1:46 PM
 */
public class FileUtils {

    /**
     * Возвращает файл в виде потока
     * @param relativePath относительный путь к файлу
     * @return файл в виде потока
     */
    public static InputStream getFileInputStream(String relativePath) {
        InputStream stream = FileUtils.class.getClassLoader().getResourceAsStream(relativePath);
        validateResult(stream, relativePath);
        return stream;
    }

    /**
     * Возвращает {@link URL} файла
     * @param relativePath относительный путь к файлу
     * @return {@link URL} файла
     */
    public static URL getFileURL(String relativePath) {
        URL url = FileUtils.class.getClassLoader().getResource(relativePath);
        validateResult(url, relativePath);
        return url;
    }

    public static File getFile(String relativePath) {
        URL url = getFileURL(relativePath);
        return new File(url.getFile());
    }

    private static void validateResult(Object object, String path) {
        if(object == null) {
            throw new RuntimeException("File not found for path '" + path + "'");
        }
    }
}
