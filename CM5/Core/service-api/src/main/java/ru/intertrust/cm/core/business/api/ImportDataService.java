package ru.intertrust.cm.core.business.api;

/**
 * Сервис загрузки данных
 * @author larin
 *
 */
public interface ImportDataService {
    public static final String TYPE_NAME = "TYPE_NAME";
    public static final String KEYS = "KEYS";    
    public static final String EMPTY_STRING_SYMBOL = "EMPTY_STRING_SYMBOL";
    
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
    
    /**
     * Метод загрузки данных при отличие кодировки в CSV от дефалтовой ANSI-1251
     * @param importFileAsByteArray
     * @param encoding
     */
    void importData(byte[] importFileAsByteArray, String encoding);
    
    /**
     * Метод загрузки данных при отличие кодировки в CSV от дефалтовой ANSI-1251 и флагом который позволяет\запрещает перезаписывать данные
     * @param importFileAsByteArray
     * @param encoding
     */
    void importData(byte[] importFileAsByteArray, String encoding, boolean owerwrite);
}
