package ru.intertrust.cm.core.gui.impl.server.filters;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.form.widget.filter.ComplicatedParamConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.NullFilterConfig;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.impl.server.form.FieldValueConfigToValueResolver;
import ru.intertrust.cm.core.gui.impl.server.util.FilterBuilderUtil;
import ru.intertrust.cm.core.gui.model.filters.ComplicatedFiltersParams;
import ru.intertrust.cm.core.gui.model.filters.WidgetIdComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.LinkEditingWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 08.11.2014
 *         Time: 13:22
 */
public abstract class ComplicatedFiltersBuilder<T> extends AbstractFiltersBuilder {

    abstract boolean prepareComplicatedFilters(T config, ComplicatedFiltersParams params, List<Filter> filters);

    protected Filter prepareComplicatedFilter(NullFilterConfig<? extends ComplicatedParamConfig> filterConfig,
                                              ComplicatedFiltersParams complicatedFiltersParams) {
        Filter filter = null;
        if (isNullFilter(filterConfig, complicatedFiltersParams)) {
            filter = initNullFilter(filterConfig);
            if(filter == null){
                return filter;
            }
        }
        filter = tryPrepareNotTextFilter(filterConfig, complicatedFiltersParams, filter);
        filter = prepareComplicatedFilter(filterConfig, filter);

        return filter;
    }

    private Filter initNullFilter(NullFilterConfig nullFilterConfig) {
        String nullFilterName = nullFilterConfig.getNullValueFilterName();
        Filter filter = null;
        if (nullFilterName != null) {
            filter = new Filter();
            filter.setFilter(nullFilterName);
        }
        return filter;
    }

    private Filter tryPrepareNotTextFilter(NullFilterConfig<? extends ComplicatedParamConfig> filterConfig,
                                           ComplicatedFiltersParams complicatedFiltersParams, Filter filter) {
        List<? extends ComplicatedParamConfig> paramConfigs = filterConfig.getParamConfigs();
        for (ComplicatedParamConfig paramConfig : paramConfigs) {
            List<? extends Value> values = tryToGetValueByConfig(paramConfig, complicatedFiltersParams);
            if (WidgetUtil.isNotEmpty(values)) {
                filter = FilterBuilderUtil.initFilter(filterConfig.getName(), filter);
                filter.addMultiCriterion(paramConfig.getName(), (List<Value>) values);

            }
        }
        return filter;
    }

    private List<? extends Value> tryToGetValueByConfig(ComplicatedParamConfig paramConfig, ComplicatedFiltersParams complicatedFiltersParams) {
        List<? extends Value> result = null;
        if (paramConfig.isSetCurrentMoment()) {
            String filedType = paramConfig.getType();
            String timeZoneId = paramConfig.getTimeZoneId();
            result = Arrays.asList(literalFieldValueParser.getCurrentTimeValue(filedType, timeZoneId, true));
        } else if (paramConfig.isSetCurrentUser()) {
            Id currentUserId = currentUserAccessor.getCurrentUserId();
            result = Arrays.asList(new ReferenceValue(currentUserId));
        } else if (paramConfig.isSetBaseObject() && complicatedFiltersParams.getRootId() != null) {
            result = Arrays.asList(new ReferenceValue(complicatedFiltersParams.getRootId()));
        } else if (paramConfig.getUniqueKeyValueConfig() != null) {
            FieldValueConfigToValueResolver resolver = (FieldValueConfigToValueResolver)
                    applicationContext.getBean("fieldValueConfigToValueResolver");
            DomainObject domainObject = resolver.findDomainObjectByUniqueKey(paramConfig.getUniqueKeyValueConfig());
            if(domainObject != null && domainObject.getId() != null){
            result = Arrays.asList(new ReferenceValue(domainObject.getId()));
            }
        } else if(paramConfig.getWidgetId() != null){
            result = getWidgetValues(paramConfig.getWidgetId(), complicatedFiltersParams);
        }
        return result;
    }

    private Filter prepareComplicatedFilter(NullFilterConfig<? extends ComplicatedParamConfig> filterConfig, Filter filter) {
        Filter result = FilterBuilderUtil.initFilter(filterConfig.getName(), filter);
        List<? extends ComplicatedParamConfig> paramConfigs = filterConfig.getParamConfigs();
        if (paramConfigs != null && !paramConfigs.isEmpty()) {
            for (ComplicatedParamConfig paramConfig : paramConfigs) {
                String valueStr = paramConfig.getValue();
                String type = paramConfig.getType();
                if(valueStr != null && type != null){
                Value value = literalFieldValueParser.textToValue(valueStr, type);
                Integer name = paramConfig.getName();
                result.addCriterion(name, value);
                }
            }

        }
        return result;
    }

    public boolean isNullFilter(NullFilterConfig<? extends ComplicatedParamConfig> filterConfig, ComplicatedFiltersParams complicatedFiltersParams) {
        List<? extends ComplicatedParamConfig> params = filterConfig.getParamConfigs();
        for (ComplicatedParamConfig param : params) {
            if (paramHasNullOrEmptyValue(param, complicatedFiltersParams)) {
                return true;
            }
        }
        return false;
    }

    private boolean paramHasNullOrEmptyValue(ComplicatedParamConfig param, ComplicatedFiltersParams complicatedFiltersParams) {
        boolean result = false;
        String widgetId = param.getWidgetId();
        if (widgetId != null) {
            List<Value> values = getWidgetValues(widgetId, complicatedFiltersParams);
            result = WidgetUtil.isEmpty(values);

        }
        result = result || isEmptyDueRoot(param, complicatedFiltersParams);
        return result;
    }

    private boolean isEmptyDueRoot(ComplicatedParamConfig param, ComplicatedFiltersParams complicatedFiltersParams){
        return param.isSetBaseObject() && complicatedFiltersParams.getRootId() == null;
    }


    private List<Value> getWidgetValues(String widgetId, ComplicatedFiltersParams complicatedFiltersParams) {
        List<Value> result = null;
        Map.Entry<WidgetIdComponentName, WidgetState> widgetValue = complicatedFiltersParams.getWidgetValue(widgetId);
        if (widgetValue != null) {
            WidgetState widgetState = widgetValue.getValue();
            if (widgetState instanceof LinkEditingWidgetState) {
                result = getLinkedReferences((LinkEditingWidgetState) widgetState);
            } else {
                result = getValuesByHandler(widgetValue.getKey().getComponentName(), widgetState);
            }
        }
        return result;
    }

    private List<Value> getLinkedReferences(LinkEditingWidgetState state) {
        List<Id> ids = state.getIds();
        if (WidgetUtil.isEmpty(ids)) {
            return null;
        }
        List<Value> result = new ArrayList<>(ids.size());
        for (Id id : ids) {
            result.add(new ReferenceValue(id));
        }
        return result;
    }

    private List<Value> getValuesByHandler(String componentName, WidgetState widgetState) {
        WidgetHandler widgetHandler = (WidgetHandler) applicationContext.getBean(componentName);
        Value value = widgetHandler.getValue(widgetState);
        if (value != null && !value.isEmpty()) {
            return Arrays.asList(value);
        }
        return null;
    }
}
