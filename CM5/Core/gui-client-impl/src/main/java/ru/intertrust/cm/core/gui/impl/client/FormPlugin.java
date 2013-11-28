package ru.intertrust.cm.core.gui.impl.client;

import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.event.PluginPanelSizeChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SizeChangedEventListener;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.*;

import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 15:28
 */
@ComponentName("form.plugin")
public class FormPlugin extends Plugin implements IsActive, IsDomainObjectEditor {

    // поле для локальной шины событий
    protected EventBus eventBus;

    // установка локальной шины событий плагину
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    // получение локальной шины событий плагину
    public EventBus getEventBus() {
        return eventBus;
    }

    public FormPlugin() {
    }

    @Override
    public PluginView createView() {
        FormPluginData initialData = getInitialData();
        return new FormPluginView(this, initialData.getFormDisplayData());
    }

    @Override
    public FormPlugin createNew() {
        return new FormPlugin();
    }

    @Override
    public FormState getCurrentState() {
        return getFormState();
    }

    @Override
    protected void afterInitialDataChange(PluginData oldData, PluginData newData) {
        super.afterInitialDataChange(oldData, newData);
        ((FormPluginView) getView()).update(((FormPluginData) newData).getFormDisplayData().getFormState());
    }

    public DomainObject getRootDomainObject() {
        return this.<FormPluginData>getInitialData().getFormDisplayData().getFormState().getObjects().getRootNode().getDomainObject();
    }

    @Override
    public void replaceForm(FormPluginConfig formPluginConfig) {
        final FormPlugin newPlugin = ComponentRegistry.instance.get("form.plugin");
        newPlugin.setConfig(formPluginConfig);
        getOwner().open(newPlugin);
        newPlugin.addViewCreatedListener(new SizeChangedEventListener() {
            @Override
            public void onViewCreation(PluginViewCreatedEvent source) {
                eventBus.fireEvent(new PluginPanelSizeChangedEvent());
            }
        }
        );
    }

    @Override
    public FormState getFormState() {
        FormState initialFormState = this.<FormPluginData>getInitialData().getFormDisplayData().getFormState();
        FormPluginView view = (FormPluginView) getView();
        Map<String, WidgetState> widgetsState = view.getWidgetsState();

        return new FormState(initialFormState.getName(), widgetsState, initialFormState.getObjects());
    }

    @Override
    public void setFormState(FormState formState) {
        ((FormPluginView) getView()).update(formState);
        FormPluginData initialData = getInitialData();
        initialData.getFormDisplayData().setFormState(formState);
    }


}
