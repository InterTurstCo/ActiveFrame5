package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.api.client.Component;
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

    @Override
    public PluginView createView() {
        return new CollectionPluginView(this);

    }
    @Override
    public Component createNew() {
        return new CollectionPlugin();
    }

    @Override
    protected GwtEvent.Type[] getEventTypesToHandle() {
        return new GwtEvent.Type[]{CollectionRowSelectedEvent.TYPE};
    }

}