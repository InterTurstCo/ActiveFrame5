package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.form.FormState;

/**
 * @author Denis Mitavskiy
 *         Date: 24.10.13
 *         Time: 13:53
 */
public interface IsDomainObjectEditor {
    FormState getFormState();

    void setFormState(FormState formState);

    void setFormToolbarContext(ToolbarContext toolbarContext);

    DomainObject getRootDomainObject();

    void replaceForm(FormPluginConfig formPluginConfig);

    FormPluginState getFormPluginState();

    FormViewerConfig getFormViewerConfig();

}
