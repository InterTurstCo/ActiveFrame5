package ru.intertrust.cm.core.business.impl.search.simple;

import java.text.SimpleDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.simpledata.EqualSimpleDataSearchFilter;
import ru.intertrust.cm.core.business.api.simpledata.SimpleDataSearchFilter;
import ru.intertrust.cm.core.config.SimpleDataConfig;

@Service
public class EqualsSimpleDataSearchFilterQueryService implements SimpleDataSearchFilterQueryService {

    private final SimpleSearchUtils utils;

    @Autowired
    public EqualsSimpleDataSearchFilterQueryService(SimpleSearchUtils utils) {
        this.utils = utils;
    }

    @Override
    public Class<?> getType() {
        return EqualSimpleDataSearchFilter.class;
    }

    @Override
    public String prepareQuery(SimpleDataConfig config, SimpleDataSearchFilter filter) {
        EqualSimpleDataSearchFilter searchFilter = (EqualSimpleDataSearchFilter) filter;

        String solrFieldName = utils.getSolrFieldName(config, searchFilter.getFieldName());
        Value<?> value = searchFilter.getFieldValue();
        String result = null;
        if (value instanceof StringValue) {
            result = solrFieldName + ": \"" + (value.get() != null ? value.get().toString().replaceAll("\"","\\\\\"") : "") + "\"";
        } else if (value instanceof LongValue) {
            result = solrFieldName + ": " + value.get() + "";
        } else if (value instanceof BooleanValue) {
            result = solrFieldName + ": " + value.get() + "";
        } else if (value instanceof DateTimeValue) {
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            result = solrFieldName + ": \"" + dateTimeFormat.format(value.get()) + "\"";
        } else if (value instanceof TimelessDateValue) {
            TimelessDate timelessDate = ((TimelessDateValue) value).get();
            result = solrFieldName + ": \"" + timelessDate.toString() + "\"";
        }
        return result;
    }
}
