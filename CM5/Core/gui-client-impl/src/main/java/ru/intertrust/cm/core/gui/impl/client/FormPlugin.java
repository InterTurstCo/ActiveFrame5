package ru.intertrust.cm.core.gui.impl.client;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.Form;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;

import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 15:28
 */
@ComponentName("form.plugin")
public class FormPlugin extends Plugin {
    @Override
    public PluginView createView() {
        FormPluginData initialData = getInitialData();
        return new FormPluginView(this, initialData.getForm());
    }

    @Override
    public FormPlugin createNew() {
        return new FormPlugin();
    }

    public void update(Form form) {
        ((FormPluginView) getView()).update(form);
        FormPluginData initialData = getInitialData();
        initialData.setForm(form);
    }

    @Override
    public Form getCurrentState() {
        Form initialForm = this.<FormPluginData>getInitialData().getForm();
        FormPluginView view = (FormPluginView) getView();
        Map<String, WidgetData> widgetData = view.getWidgetData();

        return new Form(initialForm.getName(), null, widgetData, initialForm.getObjects(), initialForm.getDebug());
    }

    public DomainObject getRootDomainObject() {
        return this.<FormPluginData>getInitialData().getForm().getObjects().getRootObject();
    }
}
