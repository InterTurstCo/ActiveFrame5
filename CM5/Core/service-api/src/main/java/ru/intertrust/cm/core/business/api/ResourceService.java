package ru.intertrust.cm.core.business.api;

/**
 * Сервис ресурсов
 * @author larin
 *
 */
public interface ResourceService {
    
    /**
     * Получение строки ресурса по имени. Имя ресурса регистронезависимое
     * @param name
     * @return
     */
    String getString(String name);
    
    /**
     * Получение числового ресурса по имени. Имя ресурса регистронезависимое
     * @param name
     * @return
     */
    Long getNumber(String name);
    
    /**
     * Получение blob ресурса по имени. Имя ресурса регистронезависимое
     * @param name
     * @return
     */
    byte[] getBlob(String name);
    
    /**
     * Получение пути ресурса при доступе через web. Результат строка после контекста web приложения 
     * @param name
     * @return
     */
    String getResourcePath(String name);
}
