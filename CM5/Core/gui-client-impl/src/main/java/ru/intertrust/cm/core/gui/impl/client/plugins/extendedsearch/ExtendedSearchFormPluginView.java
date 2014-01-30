package ru.intertrust.cm.core.gui.impl.client.plugins.extendedsearch;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.form.FormPanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.LabelWidget;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: IPetrov
 * Date: 08.01.14
 * Time: 13:09
 * Визуализация формы расширенного поиска в составе плагина расширенного поиска
 */
public class ExtendedSearchFormPluginView extends PluginView {

    private FormPanel extendedSearchFormPanel;
    private EventBus eventBus;

    public FormPanel getExtendedSearchFormPanel() {
        return extendedSearchFormPanel;
    }

    public ExtendedSearchFormPluginView(ExtendedSearchFormPlugin extendedSearchFormPlugin, FormDisplayData formDisplayData) {
        super(extendedSearchFormPlugin);
        extendedSearchFormPanel = new FormPanel(formDisplayData, true, Application.getInstance().getEventBus());
    }

    public Map<String, WidgetState> getWidgetsState() {
        List<BaseWidget> widgets = extendedSearchFormPanel.getWidgets();
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
                    Window.alert(e.getMessage());
                }
            }
        }
        return result;
    }

    @Override
    protected IsWidget getViewWidget() {
        return extendedSearchFormPanel;
    }
}
