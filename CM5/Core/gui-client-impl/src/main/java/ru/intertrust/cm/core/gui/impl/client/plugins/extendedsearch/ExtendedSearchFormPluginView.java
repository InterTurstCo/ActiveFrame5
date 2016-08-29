package ru.intertrust.cm.core.gui.impl.client.plugins.extendedsearch;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.form.FormPanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.LabelWidget;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;

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
        final FormPluginState state = new FormPluginState();
        extendedSearchFormPanel = new FormPanel(formDisplayData, state, Application.getInstance().getEventBus());
        extendedSearchFormPanel.setClassForPluginPanel("ext-search-form-style");
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
                    WidgetState state = widget.getFullClientStateCopy();
                    result.put(id, state);
                } catch (GuiException e) {
                    ApplicationWindow.errorAlert(LocalizeUtil.get(LocalizationKeys.EXTENDED_SEARCH_ERROR_MESSAGE_KEY,
                            BusinessUniverseConstants.EXTENDED_SEARCH_ERROR_MESSAGE) + e.getMessage());
                }
            }
        }
        return result;
    }

    @Override
    public IsWidget getViewWidget() {
        return extendedSearchFormPanel;
    }
}
