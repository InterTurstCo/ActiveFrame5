package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.impl.client.event.FormSavedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.FormSavedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.FormPanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.LabelWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.TextBoxWidget;
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
public class FormPluginView extends PluginView implements FormSavedEventHandler {


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

        formPanel = new FormPanel(formDisplayData, pluginState, eventBus, plugin);
        Application.getInstance().getHistoryManager()
                .setMode(HistoryManager.Mode.APPLY, FormPlugin.class.getSimpleName());
        eventBus.addHandler(FormSavedEvent.TYPE, this);


        if (formDisplayData.getScriptFileConfig() != null) {
            ScriptInjector.fromUrl(GWT.getHostPageBaseURL()+formDisplayData.getScriptFileConfig().
                getUrl()).setWindow(ScriptInjector.TOP_WINDOW).inject();
        }
    }

    @Override
    public IsWidget getViewWidget() {
        /*List<BaseWidget> widgets = formPanel.getWidgets();
        for (BaseWidget widget : widgets) {
            widget.asWidget().ensureDebugId("AF5Platform");
        } */
        return formPanel;
    }

    public Map<String, WidgetState> getWidgetsState(IWidgetStateFilter widgetStateFilter, boolean deepClone) { //deepClone indicates LinkedTable request
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
            putWidgetStateInMap(widget, deepClone, result);
        }

        return result;
    }

    private void putWidgetStateInMap(BaseWidget widget, boolean deepClone, HashMap<String, WidgetState> stateMap) {
        String id = widget.getDisplayConfig().getId();
        try {
            WidgetState state = null;
            if (deepClone) {
                state = widget.getFullClientStateCopy();
                stateMap.put(id, state);
            } else if (widget.isEditable()) {
                state = widget.getCurrentState();
                stateMap.put(id, state);
            }
        } catch (GuiException e) {
            ApplicationWindow.errorAlert("Ошибка при получении состояния виджетов: " + e.getMessage()); // todo something more interesting
        }

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


    @Override
    public void afterFormSaved(FormSavedEvent event) {
        if (event.getViewHashcode() == hashCode()) {
           formPanel.setReadOnly();
        }
    }

    private static class DefaultWidgetStateFilter implements IWidgetStateFilter {
        @Override
        public boolean exclude(BaseWidget widget) {
            return widget instanceof LabelWidget || widget instanceof LinkedDomainObjectHyperlinkWidget;
        }
    }
}
