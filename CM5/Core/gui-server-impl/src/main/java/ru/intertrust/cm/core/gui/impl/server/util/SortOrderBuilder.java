package ru.intertrust.cm.core.gui.impl.server.util;

import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.SortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.SortCriterionConfig;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 27.01.14
 *         Time: 13:15
 */
public class SortOrderBuilder {
    public static SortOrder getDefaultSortOrder(DefaultSortCriteriaConfig sortCriteriaConfig) {
        if (sortCriteriaConfig == null){
            return null;
        }
        SortOrder sortOrder = new SortOrder();
        SortCriterion.Order order = sortCriteriaConfig.getOrder();
        String field = sortCriteriaConfig.getColumnField();
        SortCriterion defaultSortCriterion = new SortCriterion(field, order);
        sortOrder.add(defaultSortCriterion);
        return sortOrder;
    }

     public static SortOrder getComplexSortOrder(SortCriteriaConfig sortCriteriaConfig){
         if (sortCriteriaConfig == null){
             return null;
         }
         SortOrder sortOrder = new SortOrder();
         List<SortCriterionConfig> sortCriterionList = sortCriteriaConfig.getSortCriterionConfigs();
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
