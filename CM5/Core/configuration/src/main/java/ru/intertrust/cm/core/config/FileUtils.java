package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.model.FatalException;

import java.io.*;
import java.net.URL;

/**
 * Служебный класс для загрузки файлов
 * @author vmatsukevich
 *         Date: 5/22/13
 *         Time: 1:46 PM
 */
public class FileUtils {
    /**
     * Возвращает эффективный поток чтения файла (в данной реализации BufferedInputStream)
     * @param file файл
     * @return эффективный поток чтения файла
     * @throws FileNotFoundException, если файл не найден
     */
    public static InputStream fileInputStream(File file) throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(file));
    }

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

    private static void validateResult(Object object, String path) {
        if(object == null) {
            throw new FatalException("File not found for path '" + path + "'");
        }
    }
}
