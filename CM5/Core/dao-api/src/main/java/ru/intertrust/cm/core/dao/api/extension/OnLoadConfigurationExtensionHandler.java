package ru.intertrust.cm.core.dao.api.extension;

/**
 * Точка расширения загрузки конфигурации
 * @author larin
 *
 */
public interface OnLoadConfigurationExtensionHandler extends ExtensionPointHandler{
    
    /**
     * Метод вызывается после загрузки конфигурации при старте ядра
     */
    void onLoad();

}
