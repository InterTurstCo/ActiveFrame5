package ru.intertrust.cm.core.business.api.dto;

/**
 * Интерфейс реализуется классами, используемыми в запросах расширенного поиска.
 * 
 * @author apirozhkov
 */
public interface SearchFilter extends Dto {

    /**
     * Специальное имя поля, используемое для поиска в любых полях документа
     */
    String EVERYWHERE = "*";
    /**
     * Специальное имя поля, используемое для поиска в содержимом вложений
     */
    String CONTENT = "%content";

    /**
     * Возвращает имя поля, по которому осуществляется фильтрация
     * 
     * @return строка с именем поля
     */
    String getFieldName();
}
