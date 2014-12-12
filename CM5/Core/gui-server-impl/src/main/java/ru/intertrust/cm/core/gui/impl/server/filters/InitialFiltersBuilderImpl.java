package ru.intertrust.cm.core.gui.impl.server.filters;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.form.widget.filter.InitialParamConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.ParamConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFilterConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFiltersConfig;
import ru.intertrust.cm.core.gui.api.server.filters.InitialFiltersBuilder;
import ru.intertrust.cm.core.gui.impl.server.util.FilterBuilderUtil;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.filters.InitialFiltersParams;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 08.11.2014
 *         Time: 13:22
 */

public class InitialFiltersBuilderImpl extends AbstractFiltersBuilder implements InitialFiltersBuilder {

    @Override
    public boolean prepareInitialFilters(InitialFiltersConfig config, InitialFiltersParams params, List<Filter> filters) {
        boolean result = false;
        if (config != null && WidgetUtil.isNotEmpty(config.getFilterConfigs())) {
            List<InitialFilterConfig> filterConfigs = config.getFilterConfigs();
            for (InitialFilterConfig filterConfig : filterConfigs) {
                Filter filter = prepareInitialFilter(filterConfig, params);
                if (filter != null) {
                    filters.add(filter);
                    result = true;
                }
            }
        }
        return result;
    }

    private Filter prepareInitialFilter(InitialFilterConfig filterConfig, InitialFiltersParams params) {

        String filterName = filterConfig.getName();
        Map<String, CollectionColumnProperties> filterNameColumnPropertiesMap = params.getFilterNameColumnPropertiesMap();
        CollectionColumnProperties columnProperties = filterNameColumnPropertiesMap.get(filterName);
        Filter filter = tryPrepareNotTextFilter(filterConfig, params.getRootId(), columnProperties);
        filter = FilterBuilderUtil.prepareFilter(filterConfig, params, filter);

        return filter;
    }


    private Filter tryPrepareNotTextFilter(InitialFilterConfig filterConfig, Id rootId,
                                           CollectionColumnProperties columnProperties) {
        List<InitialParamConfig> paramConfigs = filterConfig.getParamConfigs();
        Filter filter = null;
        for (InitialParamConfig paramConfig : paramConfigs) {
            Value value = tryToGetValueByConfig(paramConfig, rootId, columnProperties);
            if (value != null) {
                if (filter == null) {
                    filter = new Filter();
                    filter.setFilter(filterConfig.getName());
                }
                filter.addCriterion(paramConfig.getName(), value);
            }
        }
        return filter;

    }

    private Value tryToGetValueByConfig(ParamConfig paramConfig, Id rootId, CollectionColumnProperties columnProperties) {
        Value result = null;
        if (paramConfig.isSetCurrentMoment()) {
            String filedType = (String) columnProperties.getProperty(CollectionColumnProperties.TYPE_KEY);
            String timeZoneId = (String) columnProperties.getProperty(CollectionColumnProperties.TYPE_KEY);
            result = literalFieldValueParser.getCurrentTimeValue(filedType, timeZoneId, true);
        } else if (paramConfig.isSetCurrentUser()) {
            Id currentUserId = currentUserAccessor.getCurrentUserId();
            result = new ReferenceValue(currentUserId);
        } else if (paramConfig.isSetBaseObject() && rootId != null) {
            result = new ReferenceValue(rootId);
        }
        return result;
    }
}
