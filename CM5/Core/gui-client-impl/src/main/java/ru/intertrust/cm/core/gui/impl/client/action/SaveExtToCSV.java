package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.event.SaveToCsvEvent;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;
import ru.intertrust.cm.core.gui.impl.client.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Действие выгрузки в сsv отмеченных записей представления или всех с учетом фильтров (если не выбрана ни одна)
 */
@ComponentName("save-ext-csv.action")
public class SaveExtToCSV extends Action {
    EventBus eventBus;

    @Override
    public Component createNew() {
        return new SaveExtToCSV();
    }

    @Override
    protected void execute() {
        eventBus = plugin.getLocalEventBus();
        eventBus.fireEvent(new SaveToCsvEvent(getSelectedIds()));
    }

    private List<Id> getSelectedIds() {
        List<Id> selectedIdsList = null;
        Plugin plugin = getPlugin();
        final IsDomainObjectEditor editor = (plugin instanceof IsDomainObjectEditor) ? (IsDomainObjectEditor) plugin : null;
        if (editor instanceof DomainObjectSurferPlugin) {
            selectedIdsList = new ArrayList<Id>();
            CollectionPlugin collectionPlugin = ((DomainObjectSurferPlugin) editor).getCollectionPlugin();
            if (collectionPlugin.getChangedRowsState() != null) {
                for (Id id : collectionPlugin.getChangedRowsState().keySet()) {
                    final Boolean isChanged = collectionPlugin.getChangedRowsState().get(id);
                    if ((isChanged != null) && isChanged) {
                        selectedIdsList.add(id);
                    }
                }
            }
        }
        return selectedIdsList;
    }

}