package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.FormMappingHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;

import java.util.Collection;

/**
 * Ппример реализации кастомного FormMappingHandler-а.
 * Возвращает конифигурацию формы с именем "my-custom-form"
 *
 * @author Lesia Puhova
 *         Date: 12.08.14
 *         Time: 12:17
 */

@ComponentName("custom.mapping.component")
public class CustomFormMappingHandler implements FormMappingHandler {

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Override
    public FormConfig findEditingFormConfig(DomainObject root, String userUid) {
        Collection<FormConfig> formConfigs = configurationExplorer.getConfigs(FormConfig.class);
        for (FormConfig config : formConfigs) {
            if ("my-custom-form".equals(config.getName())) {
                return config;
            }
        }
        return null;
    }
}
