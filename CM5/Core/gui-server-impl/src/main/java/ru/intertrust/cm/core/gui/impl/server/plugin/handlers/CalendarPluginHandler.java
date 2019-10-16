package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.navigation.SortCriterionConfig;
import ru.intertrust.cm.core.config.gui.navigation.calendar.CalendarConfig;
import ru.intertrust.cm.core.config.gui.navigation.calendar.CalendarItemConfig;
import ru.intertrust.cm.core.config.gui.navigation.calendar.CalendarViewConfig;
import ru.intertrust.cm.core.config.gui.navigation.calendar.ImageFieldConfig;
import ru.intertrust.cm.core.gui.api.server.ActionService;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.api.server.widget.CalendarItemRenderer;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.impl.server.action.calendar.CalendarHandlerStatusData;
import ru.intertrust.cm.core.gui.impl.server.util.ActionConfigBuilder;
import ru.intertrust.cm.core.gui.impl.server.widget.DateTimeValueConverter;
import ru.intertrust.cm.core.gui.impl.server.widget.DateTimeWithTimezoneValueConverter;
import ru.intertrust.cm.core.gui.impl.server.widget.DateValueConverter;
import ru.intertrust.cm.core.gui.impl.server.widget.TimelessDateValueConverter;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.plugin.calendar.*;
import ru.intertrust.cm.core.gui.model.util.UserSettingsHelper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lvov on 03.04.14.
 */
@ComponentName(CalendarConfig.COMPONENT_NAME)
public class CalendarPluginHandler extends ActivePluginHandler {

    @Autowired private ActionConfigBuilder actionConfigBuilder;
    @Autowired private ApplicationContext applicationContext;
    @Autowired private CollectionsService collectionsService;
    @Autowired private FormatHandler formatHandler;

    private DateValueConverter dateValueConverter;

    @Override
    public CalendarPluginData initialize(Dto param) {
        final CalendarConfig pluginConfig = (CalendarConfig) param;
        final CalendarPluginData result = new CalendarPluginData();
        final Calendar calendar = GregorianCalendar.getInstance(GuiContext.getUserTimeZone());
        final Date pluginSelectedDate = pluginConfig.getHistoryValue(UserSettingsHelper.CALENDAR_SELECTED_DATE);
        if (pluginSelectedDate != null) {
            calendar.setTime(pluginSelectedDate);
        }
        result.setSelectedDate(calendar.getTime());
        final boolean monthMode = CalendarConfig.MONTH_MODE.equals(pluginConfig.getStartMode());
        calendar.add(Calendar.MONTH, monthMode ? 2 : 1);
        final Date toDate = setToDayStart(calendar).getTime();
        result.setToDate(toDate);
        calendar.add(Calendar.MONTH, monthMode ? -3 : -2);
        final Date fromDate = calendar.getTime();
        //GuiDateUtil.setStartOfDay(fromDate);
        result.setFromDate(fromDate);
        final Map<Date, List<CalendarItemsData>> values =
                getValues(pluginConfig.getCalendarViewConfig(), fromDate, toDate);
        result.setValues(values);
        result.setToolbarContext(getToolbarContext(pluginConfig));
        return result;
    }

    public CalendarRowsResponse requestRows(Dto dto) {
        final CalendarRowsRequest request = (CalendarRowsRequest) dto;
        final Map<Date, List<CalendarItemsData>> values = getValues(
                request.getCalendarConfig().getCalendarViewConfig(), request.getFromDate(), request.getToDate());
        return new CalendarRowsResponse(values, request.getFromDate(), request.getToDate());
    }

    private Map<Date, List<CalendarItemsData>> getValues(final CalendarViewConfig viewConfig,
                                                        final Date from, final Date to) {
        final Calendar calendar = GregorianCalendar.getInstance(GuiContext.getUserTimeZone());
        final String collectionName = viewConfig.getCollectionRefConfig().getName();
        final List<Filter> filters = getRangeFilters(viewConfig, from, to);
        final SortOrder sortOrder = new SortOrder();
        if (viewConfig.getSortCriteriaConfig() != null) {
            for (SortCriterionConfig criterionConfig : viewConfig.getSortCriteriaConfig().getSortCriterionConfigs()) {
                sortOrder.add(new SortCriterion(criterionConfig.getField(), criterionConfig.getOrder()));
            }
        }
        final IdentifiableObjectCollection identifiableObjects =
                collectionsService.findCollection(collectionName, sortOrder, filters);
        final Map<Date, List<CalendarItemsData>> values = new HashMap<>();
        for (Iterator<IdentifiableObject> it = identifiableObjects.iterator(); it.hasNext();) {
            final IdentifiableObject identifiableObject = it.next();
            final Value dateValue =
                    identifiableObject.getValue(viewConfig.getDateFieldPath().getValue());
            final DateValueConverter converter = getDateValueConverter(dateValue.getFieldType());
            Date date = converter.valueToDate(dateValue, GuiContext.getUserTimeZoneId());
            if (date == null) {
                continue;
            }
            calendar.setTime(date);
            date = setToDayStart(calendar).getTime();
            List<CalendarItemsData> valueList = values.get(date);
            if (valueList == null) {
                valueList = new ArrayList<>();
                values.put(date, valueList);
            }
            boolean hasRenderer = viewConfig.getRendererConfig() != null
                    && viewConfig.getRendererConfig().getValue() != null;
            if (hasRenderer) {
                final CalendarItemRenderer renderer = (CalendarItemRenderer) applicationContext.getBean(
                        viewConfig.getRendererConfig().getValue());
                valueList.add(renderer.renderItem(identifiableObject, viewConfig));
            } else {
                final String valuePattern = viewConfig.getMonthItemConfig().getPattern().getValue();
                final String monthItemPresentation = getPresentation(identifiableObject, valuePattern);
                final CalendarItemsData itemsData = new CalendarItemsData(identifiableObject.getId())
                        .setMonthItem(new CalendarItemData(viewConfig.getMonthItemConfig().isLink(),
                                monthItemPresentation));
                if (viewConfig.getDayItemsConfig() != null) {
                    for (CalendarItemConfig calendarItemConfig : viewConfig.getDayItemsConfig()) {
                        final String dayValuePattern = calendarItemConfig.getPattern().getValue();
                        final String dayItemPresentation = getPresentation(identifiableObject, dayValuePattern);
                        itemsData.addDayItem(new CalendarItemData(calendarItemConfig.isLink(), dayItemPresentation));
                    }
                }
                final ImageFieldConfig imageFieldConfig = viewConfig.getImageFieldConfig();
                if (imageFieldConfig != null) {
                    final Value value = identifiableObject.getValue(imageFieldConfig.getName());
                    final String imageKey = value == null || value.get() == null
                            ? ""
                            : value.get().toString();
                    final String image = imageFieldConfig.getImageMappingsConfig().getImage(imageKey);
                    if (image != null) {
                        itemsData.setImage(image)
                                .setImageWidth(imageFieldConfig.getImageMappingsConfig().getImageWidth())
                                .setImageHeight(imageFieldConfig.getImageMappingsConfig().getImageHeight());
                    }
                }
                valueList.add(itemsData);
            }
        }
        return values;
    }

    private Calendar setToDayStart(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    private String getPresentation(final IdentifiableObject identifiableObject, final String valuePattern) {
        Pattern pattern = Pattern.compile("\\{[\\w.]+\\}");
        Matcher matcher = pattern.matcher(valuePattern);
        final String itemPresentation = formatHandler.format(identifiableObject, matcher, null);
        return itemPresentation;
    }

    private ToolbarContext getToolbarContext(final CalendarConfig pluginConfig) {
        final ToolBarConfig toolbarConfig = pluginConfig.getToolBarConfig() == null
                ? new ToolBarConfig()
                : pluginConfig.getToolBarConfig();
        ToolBarConfig defaultToolbarConfig = null;
        if (toolbarConfig.isRendered() && toolbarConfig.isUseDefault()) {
            defaultToolbarConfig = actionService.getDefaultToolbarConfig(CalendarConfig.COMPONENT_NAME,
                    GuiContext.getUserLocale());
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

    private DateValueConverter getDateValueConverter(FieldType fieldType) {
        if (dateValueConverter == null) {
            if (FieldType.TIMELESSDATE == fieldType) {
                dateValueConverter = new TimelessDateValueConverter();
            } else if (FieldType.DATETIMEWITHTIMEZONE == fieldType) {
                dateValueConverter = new DateTimeWithTimezoneValueConverter();
            } else {
                dateValueConverter = new DateTimeValueConverter();
            }
        }
        return dateValueConverter;
    }

    private List<Filter> getRangeFilters(final CalendarViewConfig viewConfig, final Date from, final Date to) {
        final List<Filter> result = new ArrayList<>();
        final FieldType fieldType = FieldType.forTypeName(viewConfig.getDateFieldFilterConfig().getType());
        final DateValueConverter converter = getDateValueConverter(fieldType);
        final String timeZoneRaw = GuiContext.get().getUserInfo().getTimeZoneId();
        final Filter rangeFilter = new Filter();
        rangeFilter.setFilter(viewConfig.getDateFieldFilterConfig().getName());
        rangeFilter.addCriterion(0, converter.dateToValue(from, timeZoneRaw));
        rangeFilter.addCriterion(1, converter.dateToValue(to, timeZoneRaw));
        result.add(rangeFilter);
        return result;
    }
}
