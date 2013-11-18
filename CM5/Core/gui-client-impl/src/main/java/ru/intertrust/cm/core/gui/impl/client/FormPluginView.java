package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import ru.intertrust.cm.core.gui.impl.client.form.FormPanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.LabelWidget;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 15:29
 */
public class FormPluginView extends PluginView {
    private FormPanel formPanel;

    protected FormPluginView(Plugin plugin, FormDisplayData formDisplayData) {
        super(plugin);
        formPanel = new FormPanel(formDisplayData, plugin.getEventBus());
    }

    @Override
    public IsWidget getViewWidget() {
        return formPanel;
    }

    public Map<String, WidgetState> getWidgetsState() {
        List<BaseWidget> widgets = formPanel.getWidgets();
        HashMap<String, WidgetState> result = new HashMap<String, WidgetState>(widgets.size());
        for (BaseWidget widget : widgets) {
            if (widget instanceof LabelWidget) {
                continue;
            }
            String id = widget.getDisplayConfig().getId();
            if (widget.isEditable()) {
                try {
                    WidgetState state = widget.getCurrentState();
                    result.put(id, state);
                } catch (GuiException e) {
                    Window.alert(e.getMessage()); // todo something more interesting
                }
            }
        }
        return result;
    }

    void update(FormState formState) {
        formPanel.update(formState);
    }
}
