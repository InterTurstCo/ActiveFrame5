package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.impl.client.form.FormPanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.LabelWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.LinkedDomainObjectHyperlinkWidget;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;

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
    // локальная шина событий
    protected EventBus eventBus;

    // установка локальной шины событий плагину
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    // получение локальной шины событий плагина
    public EventBus getEventBus() {
        return eventBus;
    }

    private static IWidgetStateFilter defaultWidgetStateFilter = new DefaultWidgetStateFilter();

    protected FormPluginView(FormPlugin plugin, FormDisplayData formDisplayData) {
        super(plugin);
        // установка локальной шины событий
        this.eventBus = plugin.getLocalEventBus();
        //   int formHeight = plugin.getOwner().asWidget().getElement().getClientHeight();
        final FormPluginState pluginState = plugin.getFormPluginState();

        formPanel = new FormPanel(formDisplayData, pluginState, eventBus);
        formPanel.setPlugin(plugin);
        Application.getInstance().getHistoryManager()
                .setMode(HistoryManager.Mode.APPLY, FormPlugin.class.getSimpleName());
    }

    @Override
    public IsWidget getViewWidget() {
        return formPanel;
    }

    public Map<String, WidgetState> getWidgetsState(IWidgetStateFilter widgetStateFilter, boolean deepClone) {
        List<BaseWidget> widgets = formPanel.getWidgets();
        HashMap<String, WidgetState> result = new HashMap<String, WidgetState>(widgets.size());
        IWidgetStateFilter filter = defaultWidgetStateFilter;
        if (widgetStateFilter != null) {
            filter = widgetStateFilter;
        }
        for (BaseWidget widget : widgets) {
            if (filter.exclude(widget)) {
                continue;
            }
            String id = widget.getDisplayConfig().getId();
            if (widget.isEditable()) {

                try {
                    WidgetState state = deepClone ? widget.getFullClientStateCopy() : widget.getCurrentState();
                    result.put(id, state);
                } catch (GuiException e) {
                    ApplicationWindow.errorAlert(e.getMessage()); // todo something more interesting
                }
            }
        }

        return result;
    }

    public boolean isDirty() {
        return formPanel.isDirty();
    }

    public void update(FormState formState) {
        formPanel.update(formState);
    }

    public void updateViewFromHistory() {
        formPanel.updateViewFromHistory();
    }

    private static class DefaultWidgetStateFilter implements IWidgetStateFilter {
        @Override
        public boolean exclude(BaseWidget widget) {
            return widget instanceof LabelWidget || widget instanceof LinkedDomainObjectHyperlinkWidget;
        }
    }
}
