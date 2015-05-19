package ru.intertrust.cm.core.dao.api;

/**
 * Итерфейс для получения/установления контекста используемого источника данных
 * Created by vmatsukevich on 8.4.15.
 */
public interface CurrentDataSourceContext {

    /**
     * Возвращает контекст используемого источника данных
     * @return контекст используемого источника данных
     */
    String get();

    /**
     * Устанавливает контекст используемого источника данных в источник данных коллекций
     */
    void setToCollections();

    /**
     * Устанавливает контекст используемого источника данных в источник данных отчетов
     */
    void setToReports();

    /**
     * Устанавливает контекст используемого источника данных в основной источник данных
     */
    void setToMaster();

    /**
     * Оменяет установленный контекст источника данных
     */
    void reset();
}
