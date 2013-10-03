package ru.intertrust.cm.core.gui.impl.client;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.Form;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetData;
import ru.intertrust.cm.core.gui.model.plugin.SomeActivePluginData;

import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 15:28
 */
@ComponentName("some.active.plugin")
public class SomeActivePlugin extends Plugin {
    @Override
    public PluginView createView() {
        SomeActivePluginData initialData = getInitialData();
        return new SomeActivePluginView(this, initialData.getForm());
    }

    @Override
    public SomeActivePlugin createNew() {
        return new SomeActivePlugin();
    }

    public void update(Form form) {
        ((SomeActivePluginView) getView()).update(form);
        SomeActivePluginData initialData = getInitialData();
        initialData.setForm(form);
    }

    @Override
    public Form getCurrentState() {
        Form initialForm = this.<SomeActivePluginData>getInitialData().getForm();
        SomeActivePluginView view = (SomeActivePluginView) getView();
        Map<String, WidgetData> widgetData = view.getWidgetData();

        return new Form(initialForm.getName(), null, widgetData, initialForm.getFormData(), initialForm.getDebug());
    }

    public DomainObject getRootDomainObject() {
        return this.<SomeActivePluginData>getInitialData().getForm().getRootObject();
    }
}
