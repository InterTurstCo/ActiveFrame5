package ru.intertrust.cm.core.gui.impl.server;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.form.widget.filter.AbstractFilterConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.AbstractFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.ParamConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFiltersConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.FilterBuilder;
import ru.intertrust.cm.core.gui.api.server.plugin.LiteralFieldValueParser;
import ru.intertrust.cm.core.gui.model.ComponentName;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.07.2014
 *         Time: 9:51
 */
@ComponentName("filter-builder")
public class FilterBuilderImpl implements FilterBuilder {
    @Autowired
    private LiteralFieldValueParser literalFieldValueParser;

    public boolean prepareInitialFilters(InitialFiltersConfig initialFiltersConfig, List<String> excludedInitialFilterNames,
                                         List<Filter> filters) {
        return prepareInitialOrSelectionFilters(initialFiltersConfig, excludedInitialFilterNames, filters);
    }

    public boolean prepareSelectionFilters(SelectionFiltersConfig selectionFiltersConfig, List<String> excludedInitialFilterNames,
                                           List<Filter> filters) {
        return prepareInitialOrSelectionFilters(selectionFiltersConfig, excludedInitialFilterNames, filters);
    }

    private boolean prepareInitialOrSelectionFilters(AbstractFiltersConfig abstractFiltersConfig, List<String> excludedInitialFilterNames,
                                                     List<Filter> filters) {
        if (abstractFiltersConfig == null) {
            return false;
        }
        List<AbstractFilterConfig> abstractFilterConfigs = abstractFiltersConfig.getAbstractFilterConfigs();
        if (abstractFilterConfigs != null && !abstractFilterConfigs.isEmpty()) {
            for (AbstractFilterConfig abstractFilterConfig : abstractFilterConfigs) {
                String filterName = abstractFilterConfig.getName();
                if (excludedInitialFilterNames == null || !excludedInitialFilterNames.contains(filterName)) {
                    Filter initialFilter = prepareInitialOrSelectionFilter(abstractFilterConfig);
                    filters.add(initialFilter);
                }
            }
            return true;
        }
        return false;
    }

    private Filter prepareInitialOrSelectionFilter(AbstractFilterConfig abstractFilterConfig) {
        String filterName = abstractFilterConfig.getName();
        Filter initFilter = new Filter();
        initFilter.setFilter(filterName);
        List<ParamConfig> paramConfigs = abstractFilterConfig.getParamConfigs();
        if (paramConfigs != null && !paramConfigs.isEmpty()) {
            for (ParamConfig paramConfig : paramConfigs) {

                String valueStr = paramConfig.getValue();
                String type = paramConfig.getType();
                Value value = literalFieldValueParser.textToValue(valueStr, type);
                Integer name = paramConfig.getName();
                initFilter.addCriterion(name, value);
            }

        }
        return initFilter;
    }
}
