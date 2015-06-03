package ru.intertrust.cm.core.config.event;

import ru.intertrust.cm.core.config.ConfigurationStorage;
import ru.intertrust.cm.core.config.gui.form.FormConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 15.05.2015
 *         Time: 9:15
 */
public class FormConfigUpdateHandler extends ConfigurationUpdateHandler<FormConfig> {
    @Override
    protected void doUpdate(ConfigurationUpdateEvent configurationUpdateEvent) {
        ConfigurationStorage configStorage = configurationUpdateEvent.getConfigurationStorage();
        configStorage.localizedCollectedFormConfigMap.clear();
        configStorage.collectedFormConfigMap.clear();
    }

    @Override
    protected Class<FormConfig> getClazz() {
        return FormConfig.class;
    }
}
