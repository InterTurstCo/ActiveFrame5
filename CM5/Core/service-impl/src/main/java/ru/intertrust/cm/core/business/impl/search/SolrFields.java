package ru.intertrust.cm.core.business.impl.search;

import ru.intertrust.cm.core.business.api.dto.SearchFilter;

/**
 * Набор констант, задающих имена полей в Solr.
 *  
 * @author apirozhkov
 */
public class SolrFields {

    /**
     * Поле, хранящее идентификатор проиндексированного доменного объекта
     */
    public static final String OBJECT_ID = "cm_id";
    /**
     * Поле, хранящее имя области поиска, в которой проиндексирован данный объект
     */
    public static final String AREA = "cm_area";
    /**
     * Поле, хранящее имя типа целевых (запрошенных) объектов, как оно задано в конфигурации области поиска
     */
    public static final String TARGET_TYPE = "cm_type";
    /**
     * Поле, хранящее идентификатор целевого (находимого) объекта
     */
    public static final String MAIN_OBJECT_ID = "cm_main";
    /**
     * Поле, хранящее временную отметку модификации проиндексированного доменного объекта
     */
    public static final String MODIFIED = "cm_modified";

    /**
     * Префикс для имён динамически создаваемых полей
     */
    public static final String FIELD_PREFIX = "cm_";
    /**
     * Поле, хранящее копии всех текстовых полей. Используется при поиске по всем полям
     * (см. {@link SearchFilter#EVERYWHERE}), включая простой поиск
     */
    public static final String EVERYTHING = "cm_text";
    /**
     * Поле, хранящее содержимое файлов (вложений)
     */
    public static final String CONTENT = "cm_content";
}
