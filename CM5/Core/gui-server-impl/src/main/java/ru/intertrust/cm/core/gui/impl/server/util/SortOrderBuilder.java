package ru.intertrust.cm.core.gui.impl.server.util;

import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.config.gui.navigation.SortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.SortCriterionConfig;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 27.01.14
 *         Time: 13:15
 */
public class SortOrderBuilder {
    public static SortOrder getSortOrder(SortCriteriaConfig sortCriteriaConfig) {
        if (sortCriteriaConfig == null){
            return null;
        }
        List<SortCriterionConfig> sortCriterionList = sortCriteriaConfig.getSortCriterionConfigs();
        if (sortCriterionList == null || sortCriterionList.isEmpty()) {
            return null;
        }
        SortOrder sortOrder = new SortOrder();
        for (SortCriterionConfig sortCriterionConfig : sortCriterionList){
            SortCriterion sortCriterion = getSortCriterion(sortCriterionConfig);
            sortOrder.add(sortCriterion);

        }

        return sortOrder;
    }

    private static SortCriterion getSortCriterion(SortCriterionConfig sortCriterionConfig) {
        SortCriterion.Order order = sortCriterionConfig.getOrder();
        String field = sortCriterionConfig.getField();
        SortCriterion sortCriterion = new SortCriterion(field, order);
        return sortCriterion;
    }
}
