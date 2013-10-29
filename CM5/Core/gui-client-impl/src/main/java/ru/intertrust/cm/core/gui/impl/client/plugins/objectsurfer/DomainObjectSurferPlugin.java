package ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEventListener;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@ComponentName("domain.object.surfer.plugin")
public class DomainObjectSurferPlugin extends Plugin implements
        IsActive, CollectionRowSelectedEventHandler, IsDomainObjectEditor, IsIdentifiableObjectList {

    private Plugin collectionPlugin;
    private Plugin formPlugin;

    static Logger log = Logger.getLogger("domain.object.surfer.plugin");


    @Override
    public PluginView createView() {
        return new DomainObjectSurferPluginView(this);
    }

    @Override
    protected GwtEvent.Type[] getEventTypesToHandle() {
        return new GwtEvent.Type[]{CollectionRowSelectedEvent.TYPE};
    }

    @Override
    public Component createNew() {
        return new DomainObjectSurferPlugin();
    }

    public Plugin getCollectionPlugin() {
        return collectionPlugin;
    }

    public void setCollectionPlugin(Plugin collectionPlugin) {
        this.collectionPlugin = collectionPlugin;
    }

    public Plugin getFormPlugin() {
        return formPlugin;
    }

    public void setFormPlugin(Plugin formPlugin) {
        this.formPlugin = formPlugin;
    }

    @Override
    public void onCollectionRowSelect(CollectionRowSelectedEvent event) {
        PluginPanel formPluginPanel = formPlugin.getOwner();
        formPluginPanel.closeCurrentPlugin();
        final FormPlugin newFormPlugin = ComponentRegistry.instance.get("form.plugin");
        newFormPlugin.setConfig(new FormPluginConfig(event.getId()));
        newFormPlugin.addViewCreatedListener(new PluginViewCreatedEventListener() {
            @Override
            public void onViewCreation(PluginViewCreatedEvent source) {
                List<ActionContext> actions = ((FormPluginData) newFormPlugin.getInitialData()).getActionContexts();
                DomainObjectSurferPlugin.this.setActionContexts(actions);
            }
        });
        formPluginPanel.open(newFormPlugin);
        this.formPlugin = newFormPlugin;
    }

    @Override
    public FormState getFormState() {
        return ((IsDomainObjectEditor) getFormPlugin()).getFormState();
    }

    @Override
    public void setFormState(FormState formState) {
        ((IsDomainObjectEditor) getFormPlugin()).setFormState(formState);
    }

    @Override
    public DomainObject getRootDomainObject() {
        return ((IsDomainObjectEditor) getFormPlugin()).getRootDomainObject();
    }

    @Override
    public void replaceForm(FormPluginConfig formPluginConfig) {
        FormPlugin newPlugin = ComponentRegistry.instance.get("form.plugin");
        newPlugin.setConfig(formPluginConfig);
        ((Plugin) formPlugin).getOwner().open(newPlugin);
        formPlugin = newPlugin;
    }

    @Override
    public List<Id> getSelectedIds() {
        // todo: quick impl, which is not correct in general
        ArrayList<Id> result = new ArrayList<Id>(1);
        result.add(((IsDomainObjectEditor) getFormPlugin()).getFormState().getObjects().getRootNode().getDomainObject().getId());
        return result;
    }

    @Override
    public void setInitialData(PluginData inData) {
        super.setInitialData(inData);
        DomainObjectSurferPluginData initialData = (DomainObjectSurferPluginData) inData;

        if (this.collectionPlugin == null) {
            this.collectionPlugin = ComponentRegistry.instance.get("collection.plugin");
        }
        this.collectionPlugin.setInitialData(initialData.getCollectionPluginData());

        if (this.formPlugin == null) {
            this.formPlugin = ComponentRegistry.instance.get("form.plugin");
            this.formPlugin.setDisplayActionToolBar(false);
        }
        this.formPlugin.setInitialData(initialData.getFormPluginData());
    }
}
