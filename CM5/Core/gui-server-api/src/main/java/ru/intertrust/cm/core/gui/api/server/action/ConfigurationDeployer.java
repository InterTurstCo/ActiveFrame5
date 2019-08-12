package ru.intertrust.cm.core.gui.api.server.action;

import java.io.File;

import ru.intertrust.cm.core.gui.model.plugin.DeployConfigType;

/**
 * Интерфейс сервиса по установке конфигурации
 * @author larin
 *
 */
public interface ConfigurationDeployer {

    /**
     * Получение информации о поддерживаемом типе конфигурации
     * @return
     */
    DeployConfigType getDeployConfigType();
    
    /**
     * Запуск установки
     * @param file
     */
    void deploy(String name, File file);
}
