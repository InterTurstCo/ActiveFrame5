package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.event.ExtendedSearchCompleteEvent;
import ru.intertrust.cm.core.gui.impl.client.event.ExtendedSearchShowDialogBoxEvent;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.ExtendedSearchDomainObjectSurfacePluginData;
import ru.intertrust.cm.core.gui.model.plugin.collection.ExtendedSearchCollectionPluginData;

/**
 * @author Vitaliy.Orlov
 */
@ComponentName("reopen.extended.search.panel.action")
public class ReopenExtendedSearchFormAction extends Action {

    @Override
    protected void execute() {
        ExtendedSearchDomainObjectSurfacePluginData pData = (ExtendedSearchDomainObjectSurfacePluginData) getPlugin().getInitialData();
        Application.getInstance().getEventBus().fireEvent(new ExtendedSearchShowDialogBoxEvent(false,pData.getExtendedSearchConfiguration(), pData.getSearchAreas(), pData.getSearchDomainObjectType()));
    }

    @Override
    public Component createNew() {
        return new ReopenExtendedSearchFormAction();
    }

}
