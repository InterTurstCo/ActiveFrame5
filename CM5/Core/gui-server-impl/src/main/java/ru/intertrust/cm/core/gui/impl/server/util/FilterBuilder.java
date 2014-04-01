package ru.intertrust.cm.core.gui.impl.server.util;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.01.14
 *         Time: 13:15
 */
public class FilterBuilder {
    public static final String EXCLUDED_IDS_FILTER = "idsExcluded";
    public static final String INCLUDED_IDS_FILTER = "idsIncluded";
    private static final String TIMELESS_DATE_TYPE = "timelessDate";
    public static Filter prepareFilter(Set<Id> ids, String type) {
        List<ReferenceValue> idsCriterion = new ArrayList<>();
        for (Id id : ids) {
            idsCriterion.add(new ReferenceValue(id));
        }
        Filter filter = createRequiredFilter(idsCriterion, type);
        return filter;
    }

    private static Filter createRequiredFilter(List<ReferenceValue> idsCriterion, String type){
        if (EXCLUDED_IDS_FILTER.equalsIgnoreCase(type)){
            Filter filter = new IdsExcludedFilter(idsCriterion);
            filter.setFilter(EXCLUDED_IDS_FILTER  + (int)(65 * Math.random()));
            return filter;
        }
        if (INCLUDED_IDS_FILTER.equalsIgnoreCase(type)) {
            Filter filter = new IdsIncludedFilter(idsCriterion);
            filter.setFilter(INCLUDED_IDS_FILTER + (int)(65 * Math.random()));
            return filter;
        }
        return null;
    }

    public static Filter prepareSearchFilter(String filterValue, CollectionColumnProperties columnProperties) {
        Filter filter = new Filter();
        String filterName = (String)columnProperties.getProperty(CollectionColumnProperties.SEARCH_FILTER_KEY);
        filter.setFilter(filterName);
        Value value = null;
        String fieldType = (String)columnProperties.getProperty(CollectionColumnProperties.TYPE_KEY);
        if (TIMELESS_DATE_TYPE.equalsIgnoreCase(fieldType)) {
            try {
                String datePattern= (String)columnProperties.getProperty(CollectionColumnProperties.PATTERN_KEY);
                DateFormat format = new SimpleDateFormat(datePattern);
                Date selectedDate = format.parse(filterValue);
                Calendar selectedCalendar = new GregorianCalendar();
                selectedCalendar.setTime(selectedDate);
                TimelessDate timelessDateSelected = new TimelessDate(selectedCalendar.get(Calendar.YEAR), selectedCalendar.get(Calendar.MONTH),
                        selectedCalendar.get(Calendar.DAY_OF_MONTH));
                value = new TimelessDateValue(timelessDateSelected );
                filter.addCriterion(0, value);
                Calendar currentCalendar = new GregorianCalendar();

                TimelessDate currentTimelessDate = new TimelessDate(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH),
                        currentCalendar.get(Calendar.DAY_OF_MONTH)) ;
                Value currentTimeValue = new TimelessDateValue(currentTimelessDate);
                filter.addCriterion(1, currentTimeValue);
            } catch (IllegalArgumentException e) {

            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else {
            value = new StringValue("%" + filterValue + "%");
            filter.addCriterion(0, value);
        }
        return filter;
    }
}
