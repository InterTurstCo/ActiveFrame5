package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.Predicate;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserChangeSelectionEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserChangeSelectionEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserCheckBoxUpdateEvent;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;
import ru.intertrust.cm.core.gui.model.form.widget.hierarchybrowser.HierarchyBrowserUtil;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.12.2014
 *         Time: 7:22
 */
public class HierarchyCheckBoxesManager {
    private List<HierarchyCheckBoxesWrapper> hierarchyCheckBoxesWrappers;
    private EventBus eventBus;

    public HierarchyCheckBoxesManager(List<HierarchyCheckBoxesWrapper> hierarchyCheckBoxesWrappers, EventBus eventBus) {
        this.hierarchyCheckBoxesWrappers = hierarchyCheckBoxesWrappers;
        this.eventBus = eventBus;
    }

    public void activate() {
        eventBus.addHandler(HierarchyBrowserChangeSelectionEvent.TYPE, new HierarchyBrowserChangeSelectionEventHandler() {
            @Override
            public void onChangeSelectionEvent(HierarchyBrowserChangeSelectionEvent event) {
                HierarchyBrowserItem handledItem = event.getItem();
                boolean handledItemIsChosen = handledItem.isChosen();
                if (handledItemIsChosen) {
                   handleAdding(handledItem, event.isCommonSingleChoice());
                } else {
                    handleRemoving(handledItem);
                }

            }
        });
    }

    private void handleAdding(HierarchyBrowserItem handledItem, boolean commonSingleChoice){
        if (commonSingleChoice) {
            handleCommonSingleChoice(handledItem);
        } else if(handledItem.isSingleChoice() != null && handledItem.isSingleChoice()) {
            deselectSingleChoiceItem(handledItem);

        }

    }

    private void handleRemoving(HierarchyBrowserItem handledItem){
        findAndDeselectItem(handledItem);
    }

    private void findAndDeselectItem(HierarchyBrowserItem item){
        HierarchyCheckBoxesWrapper checkBoxesWrapper = findByItem(item);
        if(checkBoxesWrapper != null) {
        checkBoxesWrapper.getCheckBox().setValue(false);
        }
        item.setChosen(false);
    }

    private void deselectSingleChoiceItem(HierarchyBrowserItem compareWith){
        HierarchyBrowserItem itemToRemoveFromState = null;
        for (HierarchyCheckBoxesWrapper hierarchyCheckBoxesWrapper : hierarchyCheckBoxesWrappers) {
            CheckBox checkBox= hierarchyCheckBoxesWrapper.getCheckBox();
            if(checkBox.getValue()){
            HierarchyBrowserItem item = hierarchyCheckBoxesWrapper.getItem();
                if(HierarchyBrowserUtil.isOtherItemSingleChoice(item, compareWith)) {
                    checkBox.setValue(false);
                    item.setChosen(false);
                    itemToRemoveFromState = item;
                }
            }
        }
        eventBus.fireEvent(new HierarchyBrowserCheckBoxUpdateEvent(itemToRemoveFromState, true));
    }

    private void handleCommonSingleChoice(HierarchyBrowserItem handledItem) {
        for (HierarchyCheckBoxesWrapper hierarchyCheckBoxesWrapper : hierarchyCheckBoxesWrappers) {
            HierarchyBrowserItem item = hierarchyCheckBoxesWrapper.getItem();
            CheckBox checkBox = hierarchyCheckBoxesWrapper.getCheckBox();
            if (!item.equals(handledItem)) {
                item.setChosen(false);
                checkBox.setValue(false);
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
