package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.Predicate;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserChangeSelectionEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserChangeSelectionEventHandler;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.12.2014
 *         Time: 7:22
 */
public class HierarchyCheckBoxesManager {
    private List<HierarchyCheckBoxesWrapper> hierarchyCheckBoxesWrappers;
    private Map<String, HierarchyBrowserItem> previousChosenItems;
    private EventBus eventBus;

    public HierarchyCheckBoxesManager(List<HierarchyCheckBoxesWrapper> hierarchyCheckBoxesWrappers, EventBus eventBus) {
        this.hierarchyCheckBoxesWrappers = hierarchyCheckBoxesWrappers;
        this.eventBus = eventBus;
    }

    public void activate() {
        previousChosenItems = new HashMap<String, HierarchyBrowserItem>();
        eventBus.addHandler(HierarchyBrowserChangeSelectionEvent.TYPE, new HierarchyBrowserChangeSelectionEventHandler() {
            @Override
            public void onChangeSelectionEvent(HierarchyBrowserChangeSelectionEvent event) {
                HierarchyBrowserItem handledItem = event.getItem();
                String handledItemCollectionName = handledItem.getNodeCollectionName();
                boolean handledItemIsChosen = handledItem.isChosen();
                if (handledItemIsChosen) {
                    if (event.isHandleOnlyNode()) {
                        HierarchyBrowserItem itemFromPreviousMap = previousChosenItems.get(handledItemCollectionName);
                        if (itemFromPreviousMap != null) {
                            findAndDeselectItem(itemFromPreviousMap);
                        }
                    } else {
                        handleCommonSingleChoice(handledItem);

                    }
                    previousChosenItems.put(handledItemCollectionName, handledItem);
                } else {
                    previousChosenItems.remove(handledItemCollectionName);
                    findAndDeselectItem(handledItem);
                }

            }
        });
    }

    private void findAndDeselectItem(HierarchyBrowserItem item){
        HierarchyCheckBoxesWrapper checkBoxesWrapper = findByItem(item);
        if(checkBoxesWrapper != null) {
        checkBoxesWrapper.getCheckBox().setValue(false);
        }
        item.setChosen(false);
    }

    private void handleCommonSingleChoice(HierarchyBrowserItem handledItem) {

        for (HierarchyCheckBoxesWrapper hierarchyCheckBoxesWrapper : hierarchyCheckBoxesWrappers) {
            HierarchyBrowserItem item = hierarchyCheckBoxesWrapper.getItem();
            if (!item.equals(handledItem)) {
                item.setChosen(false);
                hierarchyCheckBoxesWrapper.getCheckBox().setValue(false);
            }
        }
    }

    private HierarchyCheckBoxesWrapper findByItem(final HierarchyBrowserItem item) {
        return GuiUtil.find(hierarchyCheckBoxesWrappers, new Predicate<HierarchyCheckBoxesWrapper>() {
            @Override
            public boolean evaluate(HierarchyCheckBoxesWrapper input) {
                return input.getItem().equals(item);
            }
        });
    }

}
