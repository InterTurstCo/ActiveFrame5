package ru.intertrust.cm.core.dao.api;

/**
 * интерфейс сервиса точек расширения
 * 
 * @author larin
 * 
 */
public interface ExtensionService {
    /**
     * Получение точки расширения в месте ее вызова
     * 
     * @param extentionPointInterface
     * @return
     */
    <T> T getExtentionPoint(Class<T> extentionPointInterface, String filter);

}
