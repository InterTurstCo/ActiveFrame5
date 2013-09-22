package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.user.client.ui.IsWidget;
import ru.intertrust.cm.core.gui.impl.client.form.FormPanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.LabelWidget;
import ru.intertrust.cm.core.gui.model.form.Form;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 15:29
 */
public class SomeActivePluginView extends PluginView {
    private final Form form;
    private FormPanel formPanel;

    protected SomeActivePluginView(Plugin plugin, Form form) {
        super(plugin);
        this.form = form;
    }

    @Override
    public IsWidget getViewWidget() {
        formPanel = new FormPanel(form);
        return formPanel;
    }

    public Map<String, WidgetData> getWidgetData() {
        List<BaseWidget> widgets = formPanel.getWidgets();
        HashMap<String, WidgetData> result = new HashMap<String, WidgetData>(widgets.size());
        for (BaseWidget widget : widgets) {
            if (widget instanceof LabelWidget) {
                continue;
            }
            String id = widget.getDisplayConfig().getId();
            WidgetData state = widget.getState();
            result.put(id, state);
        }
        return result;
    }
}
