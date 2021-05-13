package ru.intertrust.cm.core.dao.api;

import java.util.List;

import org.springframework.context.ApplicationContext;

/**
 * интерфейс сервиса точек расширения
 * 
 * @author larin
 * 
 */
public interface ExtensionService {
    public static final String PLATFORM_CONTEXT = "platform-context";
    
    /**
     * Получение точки расширения в месте ее вызова
     * 
     * @param extentionPointInterface
     * @return
     */
    <T> T getExtensionPoint(Class<T> extentionPointInterface, String filter);

    /**
     * Инициализация точек расширения из другого спринг контекста
     * @param contextName
     * @param applicationContext
     */
    void init(String contextName, ApplicationContext applicationContext);
}
