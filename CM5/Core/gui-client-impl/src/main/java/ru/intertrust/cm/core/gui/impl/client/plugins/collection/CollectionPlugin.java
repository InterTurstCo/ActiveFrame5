package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEvent;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowsRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@ComponentName("collection.plugin")
public class CollectionPlugin extends Plugin {

    // поле для локальной шины событий
    protected EventBus eventBus;

    private List<LinkConfig> hierarchicalLinks = new ArrayList<>();

    // установка локальной шины событий плагину
    public void setLocalEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * получение локальной шины событий плагину.
     *
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

    public CollectionRowsRequest getCollectionRowRequest() {
        return ((CollectionPluginView) getView()).createRequest();
    }

    public void refreshCollection(List<CollectionRowItem> collectionRowItems) {
        Application.getInstance().showLoadingIndicator();
        CollectionPluginView view = ((CollectionPluginView) getView());
        view.clearScrollHandler();
        view.handleCollectionRowsResponse(collectionRowItems, true);
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                Application.getInstance().hideLoadingIndicator();

            }
        });

    }

    @Override
    public CollectionPlugin createNew() {
        return new CollectionPlugin();
    }

    @Override
    public boolean restoreHistory() {
        final CollectionPluginView view = (CollectionPluginView) getView();
        return view.restoreHistory();
    }

    @Override
    protected GwtEvent.Type[] getEventTypesToHandle() {
        return new GwtEvent.Type[]{CollectionRowSelectedEvent.TYPE};
    }

    public List<LinkConfig> getHierarchicalLinks() {
        return hierarchicalLinks;
    }

    public void setHierarchicalLinks(List<LinkConfig> hierarchicalLinks) {
        this.hierarchicalLinks = hierarchicalLinks;
    }
}