package ru.intertrust.cm.core.dao.api;

import org.springframework.context.ApplicationContext;

/**
 * Интерфейс сервиса точек расширения
 *
 * @author larin
 */
public interface ExtensionService {

    String PLATFORM_CONTEXT = "platform-context";

    /**
     * Получение точки расширения в месте ее вызова
     */
    <T> T getExtensionPoint(Class<T> extensionPointInterface, String filter);

    /**
     * Инициализация точек расширения из другого Spring контекста
     */
    void init(String contextName, ApplicationContext applicationContext);
}
