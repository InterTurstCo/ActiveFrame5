package ru.intertrust.cm.core.gui.impl.server.util;

import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionDisplayConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.SelectionSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.SortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.SortCriterionConfig;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.util.PlaceholderResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 27.01.14
 *         Time: 13:15
 */
public class SortOrderBuilder {
    private static final String DEFAULT_SORT_FIELD = "id";

    public static SortOrder getInitSortOrder(DefaultSortCriteriaConfig defaultSortCriteriaConfig,
                                             CollectionDisplayConfig collectionDisplayConfig, String locale) {
        SortOrder result = null;
        if (!defaultSortCriteriaConfig.isEmpty()) {
            SortCriteriaConfig sortCriteriaConfig = getSortCriteriaIfExists(defaultSortCriteriaConfig, collectionDisplayConfig, locale);
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

    @Deprecated
    public static SortOrder getNotNullSimpleSortOrder(DefaultSortCriteriaConfig defaultSortCriteriaConfig) {
        SortOrder result = getSimpleSortOrder(defaultSortCriteriaConfig);
        if (result == null) {
            result = getSortOderByDefaultField();
        }
        return result;
    }

    public static SortOrder getComplexSortOrder(SortCriteriaConfig sortCriteriaConfig) { //should be private
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
        if (sortCriteriaConfig != null) {
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
                                                              CollectionDisplayConfig collectionDisplayConfig, String locale) {
        SortCriterion.Order order = defaultSortCriteriaConfig.getOrder();
        String field = defaultSortCriteriaConfig.getColumnField();
        CollectionColumnConfig columnConfig = getColumnConfig(field, collectionDisplayConfig, locale);
        SortCriteriaConfig sortCriteriaConfig = null;
        if (order.equals(SortCriterion.Order.ASCENDING)) {
            sortCriteriaConfig = columnConfig.getAscSortCriteriaConfig();
        } else {
            sortCriteriaConfig = columnConfig.getDescSortCriteriaConfig();
        }
        return sortCriteriaConfig;

    }

    private static CollectionColumnConfig getColumnConfig(String field, CollectionDisplayConfig collectionDisplayConfig, String locale) {
        List<CollectionColumnConfig> columnConfigLists = collectionDisplayConfig.getColumnConfig();
        for (CollectionColumnConfig columnConfig : columnConfigLists) {
            if (field.equalsIgnoreCase(columnConfig.getField())) {
                return columnConfig;
            }
        }

        throw new GuiException(buildMessage(LocalizationKeys.GUI_EXCEPTION_SORT_FIELD_NOT_FOUND,
                "Couldn't find sorting '${field}'", locale, new Pair("field", field)));
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

    public static SortOrder getSortOderByDefaultField() {
        SortOrder result = new SortOrder();
        SortCriterion defaultSortCriterion = new SortCriterion(DEFAULT_SORT_FIELD, SortCriterion.Order.ASCENDING);
        result.add(defaultSortCriterion);
        return result;
    }

    public static SortOrder getSortOderByFieldName(String fieldName) {
        SortOrder result = new SortOrder();
        SortCriterion defaultSortCriterion = new SortCriterion(fieldName, SortCriterion.Order.ASCENDING);
        result.add(defaultSortCriterion);
        return result;
    }

    private static String buildMessage(String message, String defaultValue, String locale) {
        return MessageResourceProvider.getMessage(message, locale, defaultValue);
    }

    private static String buildMessage(String message, String defaultValue, String locale, Pair<String, String>... params) {
        Map<String, String> paramsMap = new HashMap<>();
        for (Pair<String, String> pair  : params) {
            paramsMap.put(pair.getFirst(), pair.getSecond());
        }
        return PlaceholderResolver.substitute(buildMessage(message, defaultValue, locale), paramsMap);
    }
}
