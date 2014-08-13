package ru.intertrust.cm.core.gui.api.server.plugin;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;

/**
 * @author Lesia Puhova
 *         Date: 11.08.14
 *         Time: 16:33
 */
public interface FormMappingHandler extends ComponentHandler {
    public FormConfig findEditingFormConfig(DomainObject root, String userUid);
}
