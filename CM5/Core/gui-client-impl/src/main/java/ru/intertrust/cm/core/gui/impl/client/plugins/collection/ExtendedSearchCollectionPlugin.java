package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ExpandableObjectConfig;
import ru.intertrust.cm.core.config.gui.navigation.ChildCollectionConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.RowsSelectionConfig;
import ru.intertrust.cm.core.config.gui.navigation.RowsSelectionDefaultState;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.action.CreateNewObjectAction;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.SideBarResizeEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SideBarResizeEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.collection.*;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.form.widget.CollectionRowsResponse;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRefreshRequest;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowsRequest;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@ComponentName("extended.search.collection.plugin")
public class ExtendedSearchCollectionPlugin extends CollectionPlugin {

    public ExtendedSearchCollectionPlugin() {

    }

    @Override
    public PluginView createView() {
        eventBus.addHandler(CollectionRowStateChangedEvent.TYPE, this);
        eventBus.addHandler(CollectionRowFilteredEvent.TYPE, this);
        eventBus.addHandler(CollectionRowMoreItemsEvent.TYPE, this);
        eventBus.addHandler(CollectionAddGroupEvent.TYPE, this);
        eventBus.addHandler(CollectionAddElementEvent.TYPE, this);
        eventBus.addHandler(CollectionRowSelectedEvent.TYPE, this);
        return new ExtendedSearchCollectionPluginView(this);
    }

    @Override
    public ExtendedSearchCollectionPlugin createNew() {
        return new ExtendedSearchCollectionPlugin();
    }

}