package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZoneValue;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.navigation.calendar.CalendarConfig;
import ru.intertrust.cm.core.gui.api.server.ActionService;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.api.server.widget.CalendarItemRenderer;
import ru.intertrust.cm.core.gui.impl.server.action.calendar.CalendarHandlerStatusData;
import ru.intertrust.cm.core.gui.impl.server.util.ActionConfigBuilder;
import ru.intertrust.cm.core.gui.impl.server.widget.DateTimeValueConverter;
import ru.intertrust.cm.core.gui.impl.server.widget.DateValueConverter;
import ru.intertrust.cm.core.gui.impl.server.widget.TimelessDateValueConverter;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarItemData;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarPluginData;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarRowsRequest;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarRowsResponse;

/**
 * Created by lvov on 03.04.14.
 */
@ComponentName(CalendarConfig.COMPONENT_NAME)
public class CalendarPluginHandler extends ActivePluginHandler {

    @Autowired private ActionService actionService;
    @Autowired private ActionConfigBuilder actionConfigBuilder;
    @Autowired private ApplicationContext applicationContext;
    @Autowired private CollectionsService collectionsService;

    private DateValueConverter dateValueConverter;

    @Override
    public CalendarPluginData initialize(Dto param) {
        final CalendarConfig pluginConfig = (CalendarConfig) param;
        final Map<Date, List<CalendarItemData>> values = getValues(pluginConfig);

        final CalendarPluginData result = new CalendarPluginData();
        final Calendar calendar = GregorianCalendar.getInstance();
        result.setSelectedDate(calendar.getTime());
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
        result.setToDate(calendar.getTime());
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 2);
        result.setFromDate(calendar.getTime());
        result.setValues(values);
        result.setToolbarContext(getToolbarContext(pluginConfig));
        return result;
    }

    public CalendarRowsResponse requestRows(Dto dto) {
        System.out.println("----------------------> CalendarHandler#requestRows");
        final CalendarRowsRequest request = (CalendarRowsRequest) dto;
        final Map<Date, List<CalendarItemData>> values = getValues(request.getCalendarConfig());
        return new CalendarRowsResponse(values);
    }

    private Map<Date, List<CalendarItemData>> getValues(final CalendarConfig pluginConfig) {
        final String collectionName = pluginConfig.getCalendarViewConfig().getCollectionRefConfig().getName();
        final List<Filter> filters = new ArrayList<>();
        final SortOrder sortOrder = null;
        final Map<Date, List<CalendarItemData>> values = new HashMap<>();
        final IdentifiableObjectCollection identifiableObjects =
                collectionsService.findCollection(collectionName, sortOrder, filters);
        for (Iterator<IdentifiableObject> it = identifiableObjects.iterator(); it.hasNext();) {
            final IdentifiableObject identifiableObject = it.next();
            final Value dateValue =
                    identifiableObject.getValue(pluginConfig.getCalendarViewConfig().getDateFieldPath().getValue());
            final DateValueConverter converter = getDateValueConverter(dateValue);
            final Date date = converter.valueToDate(dateValue, GuiContext.get().getUserInfo().getTimeZoneId());
            List<CalendarItemData> valueList = values.get(date);
            if (valueList == null) {
                valueList = new ArrayList<>();
                values.put(date, valueList);
            }
            boolean hasRenderer = pluginConfig.getCalendarViewConfig().getRendererConfig() != null
                    && pluginConfig.getCalendarViewConfig().getRendererConfig().getValue() != null;
            if (hasRenderer) {
                final CalendarItemRenderer renderer = (CalendarItemRenderer) applicationContext.getBean(
                        pluginConfig.getCalendarViewConfig().getRendererConfig().getValue());
                valueList.add(renderer.renderItem(identifiableObject, pluginConfig.getCalendarViewConfig()));
            } else {
                final String pattern = pluginConfig.getCalendarViewConfig().getPattern().getValue();

                // FIXME
                final CalendarItemData itemData = new CalendarItemData(pattern);
                valueList.add(itemData);
            }
        }
        return values;
    }

    private ToolbarContext getToolbarContext(final CalendarConfig pluginConfig) {
        final ToolBarConfig toolbarConfig = pluginConfig.getToolBarConfig() == null
                ? new ToolBarConfig()
                : pluginConfig.getToolBarConfig();
        ToolBarConfig defaultToolbarConfig = null;
        if (toolbarConfig.isRendered() && toolbarConfig.isUseDefault()) {
            defaultToolbarConfig = actionService.getDefaultToolbarConfig(CalendarConfig.COMPONENT_NAME);
        }
        if (defaultToolbarConfig == null) {
            defaultToolbarConfig = new ToolBarConfig();
        }
        final Map<String, Object> params = new HashMap<>();
        params.put(CalendarHandlerStatusData.MODE_KEY, pluginConfig.getStartMode());
        final ToolbarContext result = new ToolbarContext();
        actionConfigBuilder.appendConfigs(defaultToolbarConfig.getActions(), params);
        actionConfigBuilder.appendConfigs(toolbarConfig.getActions(), params);
        result.setContexts(actionConfigBuilder.getActionContexts(), ToolbarContext.FacetName.LEFT);
        actionConfigBuilder.clear();
        actionConfigBuilder.appendConfigs(defaultToolbarConfig.getRightFacetConfig().getActions(), params);
        actionConfigBuilder.appendConfigs(toolbarConfig.getRightFacetConfig().getActions(), params);
        result.setContexts(actionConfigBuilder.getActionContexts(), ToolbarContext.FacetName.RIGHT);
        return result;
    }

    private DateValueConverter getDateValueConverter(Value dateValue) {
        if (dateValueConverter == null) {
            if (dateValue instanceof TimelessDateValue) {
                dateValueConverter = new TimelessDateValueConverter();
            } else if (dateValue instanceof DateTimeWithTimeZoneValue) {
                dateValueConverter = new DateTimeValueConverter();
            } else {
                dateValueConverter = new DateTimeValueConverter();
            }
        }
        return dateValueConverter;
    }
}
