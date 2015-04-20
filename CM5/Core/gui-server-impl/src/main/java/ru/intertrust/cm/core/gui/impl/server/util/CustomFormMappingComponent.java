package ru.intertrust.cm.core.gui.impl.server.util;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.FormMappingHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.04.2015
 *         Time: 10:06
 */
@ComponentName("custom.platform.form.mapping")
public class CustomFormMappingComponent implements FormMappingHandler {

    @Override
    public FormConfig findEditingFormConfig(DomainObject root, String userUid) {

        return null;
    }
}
