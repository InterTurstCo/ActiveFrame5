package ru.intertrust.cm.core.gui.impl.server.util;

import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionDisplayConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.SelectionSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.SortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.SortCriterionConfig;
import ru.intertrust.cm.core.gui.model.GuiException;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 27.01.14
 *         Time: 13:15
 */
public class SortOrderBuilder {
    private static final String DEFAULT_SORT_FIELD = "id";

    public static SortOrder getInitSortOrder(DefaultSortCriteriaConfig defaultSortCriteriaConfig,
                                             CollectionDisplayConfig collectionDisplayConfig) {
        SortOrder result = null;
        if (defaultSortCriteriaConfig.isEmpty()) {
            result = getSortOderByDefaultField();
        } else {
            SortCriteriaConfig sortCriteriaConfig = getSortCriteriaIfExists(defaultSortCriteriaConfig, collectionDisplayConfig);
            if (sortCriteriaConfig == null) {
                result = getSimpleSortOrder(defaultSortCriteriaConfig);
            } else {
                result = getComplexSortOrder(sortCriteriaConfig);
            }
        }
        return result;
    }

    public static SortOrder getSimpleSortOrder(DefaultSortCriteriaConfig defaultSortCriteriaConfig) {
        if (defaultSortCriteriaConfig == null) {
            return null;
        }
        SortOrder sortOrder = new SortOrder();
        SortCriterion.Order order = defaultSortCriteriaConfig.getOrder();
        String field = defaultSortCriteriaConfig.getColumnField();
        SortCriterion defaultSortCriterion = new SortCriterion(field, order);
        sortOrder.add(defaultSortCriterion);
        return sortOrder;
    }

    public static SortOrder getNotNullSimpleSortOrder(DefaultSortCriteriaConfig defaultSortCriteriaConfig) {
        SortOrder result = getSimpleSortOrder(defaultSortCriteriaConfig);
        if (result == null) {
            result = getSortOderByDefaultField();
        }
        return result;
    }

    public static SortOrder getComplexSortOrder(SortCriteriaConfig sortCriteriaConfig) {
        if (sortCriteriaConfig == null) {
            return null;
        }
        SortOrder sortOrder = new SortOrder();
        List<SortCriterionConfig> sortCriterionList = sortCriteriaConfig.getSortCriterionConfigs();
        for (SortCriterionConfig sortCriterionConfig : sortCriterionList) {
            SortCriterion sortCriterion = getSortCriterion(sortCriterionConfig);
            sortOrder.add(sortCriterion);

        }
        return sortOrder;
    }

    public static SortOrder getSelectionSortOrder(SelectionSortCriteriaConfig sortCriteriaConfig) {
        SortOrder result = null;
        if (sortCriteriaConfig == null) {
            result = getSortOderByDefaultField();
        } else {
            result = new SortOrder();
            List<SortCriterionConfig> sortCriterionList = sortCriteriaConfig.getSortCriterionConfigs();
            for (SortCriterionConfig sortCriterionConfig : sortCriterionList) {
                SortCriterion sortCriterion = getSortCriterion(sortCriterionConfig);
                result.add(sortCriterion);

            }
        }
        return result;
    }

    private static SortCriterion getSortCriterion(SortCriterionConfig sortCriterionConfig) {
        SortCriterion.Order order = sortCriterionConfig.getOrder();
        String field = sortCriterionConfig.getField();
        SortCriterion sortCriterion = new SortCriterion(field, order);
        return sortCriterion;
    }

    private static SortCriteriaConfig getSortCriteriaIfExists(DefaultSortCriteriaConfig defaultSortCriteriaConfig,
                                                              CollectionDisplayConfig collectionDisplayConfig) {
        SortCriterion.Order order = defaultSortCriteriaConfig.getOrder();
        String field = defaultSortCriteriaConfig.getColumnField();
        CollectionColumnConfig columnConfig = getColumnConfig(field, collectionDisplayConfig);
        SortCriteriaConfig sortCriteriaConfig = null;
        if (order.equals(SortCriterion.Order.ASCENDING)) {
            sortCriteriaConfig = columnConfig.getAscSortCriteriaConfig();
        } else {
            sortCriteriaConfig = columnConfig.getDescSortCriteriaConfig();
        }
        return sortCriteriaConfig;

    }

    private static CollectionColumnConfig getColumnConfig(String field, CollectionDisplayConfig collectionDisplayConfig) {
        List<CollectionColumnConfig> columnConfigLists = collectionDisplayConfig.getColumnConfig();
        for (CollectionColumnConfig columnConfig : columnConfigLists) {
            if (field.equalsIgnoreCase(columnConfig.getField())) {
                return columnConfig;
            }
        }
        throw new GuiException("Couldn't find sorting " + field + "'");
    }

    public static SortOrder getSortOrder(SortCriteriaConfig sortCriteriaConfig, String fieldName, boolean ascend) {
        SortOrder sortOrder = getComplexSortOrder(sortCriteriaConfig);
        if (sortOrder == null) {
            sortOrder = new SortOrder();
            if (ascend) {
                sortOrder.add(new SortCriterion(fieldName, SortCriterion.Order.ASCENDING));
            } else {
                sortOrder.add(new SortCriterion(fieldName, SortCriterion.Order.DESCENDING));
            }
        }
        return sortOrder;
    }

    private static SortOrder getSortOderByDefaultField() {
        SortOrder result = new SortOrder();
        SortCriterion defaultSortCriterion = new SortCriterion(DEFAULT_SORT_FIELD, SortCriterion.Order.ASCENDING);
        result.add(defaultSortCriterion);
        return result;
    }
}
