package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEvent;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@ComponentName("collection.plugin")
public class CollectionPlugin extends Plugin {

    // поле для локальной шины событий
    protected EventBus eventBus;

    // установка локальной шины событий плагину
    public void setLocalEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * получение локальной шины событий плагину.
     * @return
     */
    @Override
    public EventBus getLocalEventBus() {
        return eventBus;
    }

    public CollectionPlugin() {
    }

    @Override
    public PluginView createView() {
        return new CollectionPluginView(this);

    }

    @Override
    public CollectionPlugin createNew() {
        return new CollectionPlugin();
    }

    @Override
    protected GwtEvent.Type[] getEventTypesToHandle() {
        return new GwtEvent.Type[]{CollectionRowSelectedEvent.TYPE};
    }

}