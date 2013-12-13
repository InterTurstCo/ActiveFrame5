package ru.intertrust.cm.core.business.api;

/**
 * Сервис загрузки данных
 * @author larin
 *
 */
public interface ImportDataService {
    public static final String TYPE_NAME = "TYPE_NAME";
    public static final String KEYS = "KEYS";    
    
    /**
     * Удаленный интерфейс
     * @author larin
     *
     */
    public interface Remote extends ImportDataService{        
    }
    /**
     * Метод загрузки данных из файла.
     * @param loadFileAsByteArray зачитанный массив данных из файла
     */
    void importData(byte[] importFileAsByteArray);
}
