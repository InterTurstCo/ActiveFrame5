package ru.intertrust.cm.core.business.api;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Сервис загрузки данных
 * @author larin
 *
 */
public interface ImportDataService {
    public static final String TYPE_NAME = "TYPE_NAME";
    public static final String KEYS = "KEYS";    
    public static final String EMPTY_STRING_SYMBOL = "EMPTY_STRING_SYMBOL";
    public static final String DELETE_OTHER = "DELETE_OTHER";
    public static final String ATTACHMENT_FIELD_NAME = "_ATTACHMENT_";
    public static final String DEFAULT_ENCODING = "ANSI-1251";
    public static final String SERVICE_FIELDS = "SERVICE_FIELDS";
    
    
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
    List<Id> importData(byte[] importFileAsByteArray);
    
    /**
     * Метод загрузки данных при отличие кодировки в CSV от дефалтовой ANSI-1251
     * @param importFileAsByteArray
     * @param encoding
     */
    List<Id> importData(byte[] importFileAsByteArray, String encoding);
    
    /**
     * Метод загрузки данных при отличие кодировки в CSV от дефалтовой ANSI-1251 и флагом который позволяет\запрещает перезаписывать данные
     * @param importFileAsByteArray
     * @param encoding
     */
    List<Id> importData(byte[] importFileAsByteArray, String encoding, boolean owerwrite);
    
    /**
     * Метод загрузки данных при отличие кодировки в CSV от дефалтовой ANSI-1251 и флагом который позволяет\запрещает 
     * перезаписывать данные и возможностью подгрузки вложений с файловой системы, где расположен сервер
     * @param importFileAsByteArray
     * @param encoding
     * @param owerwrite
     * @param attachmentBasePath
     * @return
     */
    List<Id> importData(byte[] importFileAsByteArray, String encoding, boolean owerwrite, String attachmentBasePath);
}
