package ru.intertrust.cm.core.gui.model.form.widget.hierarchybrowser;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.DisplayValuesAsLinksConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserWidgetState;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 17.09.2014
 *         Time: 0:15
 */
public class HierarchyBrowserUtil {

    public static int getSizeFromString(String size, int defaultSize) {
        if (size == null) {
            return defaultSize;
        }
        String temp = size.replaceAll("\\D", "");
        return Integer.parseInt(temp);
    }

    public static boolean isDisplayingHyperlinks(HierarchyBrowserWidgetState state) {
            DisplayValuesAsLinksConfig displayValuesAsLinksConfig = state.getHierarchyBrowserConfig()
                    .getDisplayValuesAsLinksConfig();
            return displayValuesAsLinksConfig != null && displayValuesAsLinksConfig.isValue();
    }

    public static ArrayList<HierarchyBrowserItem> getCopyOfChosenItems(ArrayList<HierarchyBrowserItem> itemsToCopy) {
        ArrayList<HierarchyBrowserItem> copyOfItems = new ArrayList<HierarchyBrowserItem>(itemsToCopy.size());
        for (HierarchyBrowserItem item : itemsToCopy) {
            copyOfItems.add(item.getCopy());
        }
        return copyOfItems;
    }

    public static boolean shouldInitializeTooltip(HierarchyBrowserWidgetState state, int delta) {
        if (state.getTooltipChosenItems() != null) {
            return false;
        }
        return state.isTooltipAvailable(delta);

    }

    public static void preHandleAddingItemToTempState(HierarchyBrowserItem item, HierarchyBrowserWidgetState state) {
        if (!state.isTooltipAvailable(1)) {
            return;
        }
        List<HierarchyBrowserItem> temporaryTooltipItems = state.getTooltipChosenItems();
        List<HierarchyBrowserItem> temporaryChosenItems = state.getTemporaryChosenItems();
        String collectionName = item.getNodeCollectionName();
        SelectionFiltersConfig selectionFiltersConfig = state.getCollectionNameNodeMap()
                .get(collectionName).getSelectionFiltersConfig();
        Integer expected = WidgetUtil.getLimit(selectionFiltersConfig);
        List<HierarchyBrowserItem> listOfParticularCollection = getListOfParticularCollection(temporaryChosenItems,
                collectionName);
        int actual = listOfParticularCollection.size();
        if (expected == null || expected < actual) {
            return;
        }
        HierarchyBrowserItem itemForTooltip = removeSameCollectionItem(item, temporaryChosenItems);
        if (itemForTooltip != null) {
            temporaryTooltipItems.add(itemForTooltip);

        }

    }

    private static HierarchyBrowserItem removeSameCollectionItem(HierarchyBrowserItem item, Collection<HierarchyBrowserItem> items) {
        Iterator<HierarchyBrowserItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            HierarchyBrowserItem currentItem = iterator.next();
            if (currentItem.getNodeCollectionName().equalsIgnoreCase(item.getNodeCollectionName())) {
                iterator.remove();
                return currentItem;
            }
        }
        return null;
    }

    public static void preHandleRemovingItem(HierarchyBrowserItem item, HierarchyBrowserWidgetState state) {
        if (!state.isTooltipAvailable()) {
            return;
        }
        List<HierarchyBrowserItem> content = state.getCurrentItems();
        if (content.contains(item)) {
            List<HierarchyBrowserItem> tooltipItems = state.getTooltipChosenItems();
            HierarchyBrowserItem itemToContent = tooltipItems.remove(0);

            content.add(itemToContent);
        }

    }


    private static List<HierarchyBrowserItem> getListOfParticularCollection(List<HierarchyBrowserItem> items, String collectionName) {
        List<HierarchyBrowserItem> result = new ArrayList<HierarchyBrowserItem>();
        for (HierarchyBrowserItem item : items) {
            if (collectionName.equalsIgnoreCase(item.getNodeCollectionName())) {
                result.add(item);
            }
        }
        return result;
    }

    public static boolean handleUpdateChosenItem(HierarchyBrowserItem updatedItem, List<HierarchyBrowserItem> chosenItems) {
        if (chosenItems == null || chosenItems.isEmpty()) {
            return false;
        }
        HierarchyBrowserItem itemToRefresh = findHierarchyBrowserItem(updatedItem.getId(), chosenItems);
        if (itemToRefresh == null) {
            return false;
        }
        itemToRefresh.setStringRepresentation(updatedItem.getStringRepresentation());
        return true;
    }

    private static HierarchyBrowserItem findHierarchyBrowserItem(Id id, List<HierarchyBrowserItem> chosenItems) {
        for (HierarchyBrowserItem item : chosenItems) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    public static Map<String, Integer> createTemporaryCountOfType(Map<String, NodeCollectionDefConfig> defConfigMap) {
        Map<String, Integer> result = new HashMap<String, Integer>();
        Set<String> collectionNames = defConfigMap.keySet();
        for (String collectionName : collectionNames) {
            NodeCollectionDefConfig config = defConfigMap.get(collectionName);
            result.put(collectionName, config.getElementsCount());
        }
        return result;
    }

    public static void updateCountOfType(Map<String, Integer> countMap, Map<String, NodeCollectionDefConfig> defConfigMap) {
        Set<String> collectionNames = defConfigMap.keySet();
        for (String collectionName : collectionNames) {
            NodeCollectionDefConfig config = defConfigMap.get(collectionName);
            Integer count = countMap.get(collectionName);
            config.setElementsCount(count);
        }
    }

    @Deprecated
    public static boolean isOtherItemSingleChoice(HierarchyBrowserItem item, HierarchyBrowserItem compareWith,
                                                  boolean commonSingleChoice) {
        return item.getId() != compareWith.getId()
                && ((item.getNodeCollectionName().equalsIgnoreCase(compareWith.getNodeCollectionName())
                && (item.getParentId() == null || item.getParentId().equals(compareWith.getParentId())))
                || (commonSingleChoice && (item.isSingleChoice() == null || item.isSingleChoice())));
    }

    public static boolean isOtherItemSingleChoice(HierarchyBrowserItem item, HierarchyBrowserItem compareWith) {
        return item.getId() != compareWith.getId()
                && ((item.getNodeCollectionName().equalsIgnoreCase(compareWith.getNodeCollectionName())
                && (item.getParentId() == null || item.getParentId().equals(compareWith.getParentId()))));

    }

    public static boolean isModalWindow(DisplayValuesAsLinksConfig commonDisplayValuesAsLinks,
                                        DisplayValuesAsLinksConfig nodeDisplayValuesAsLinks) {
        return nodeDisplayValuesAsLinks == null
                ? (commonDisplayValuesAsLinks == null || commonDisplayValuesAsLinks.isModalWindow())
                : nodeDisplayValuesAsLinks.isModalWindow();

    }

}
