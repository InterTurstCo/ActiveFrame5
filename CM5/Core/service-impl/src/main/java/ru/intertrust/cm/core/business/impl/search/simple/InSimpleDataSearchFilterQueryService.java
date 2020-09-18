package ru.intertrust.cm.core.business.impl.search.simple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.business.api.simpledata.InSimpleDataSearchFilter;
import ru.intertrust.cm.core.business.api.simpledata.SimpleDataSearchFilter;
import ru.intertrust.cm.core.config.SimpleDataConfig;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class InSimpleDataSearchFilterQueryService implements SimpleDataSearchFilterQueryService {

    private final SimpleSearchUtils utils;

    @Autowired
    public InSimpleDataSearchFilterQueryService(SimpleSearchUtils utils) {
        this.utils = utils;
    }

    @Override
    public Class<?> getType() {
        return InSimpleDataSearchFilter.class;
    }

    @Override
    public String prepareQuery(SimpleDataConfig config, SimpleDataSearchFilter filter) {
        InSimpleDataSearchFilter searchFilter = (InSimpleDataSearchFilter) filter;
        String solrFieldName = utils.getSolrFieldName(config, searchFilter.getFieldName());
        List<String> values = new ArrayList<>();
        collectValues(values, searchFilter.getFieldValue());
        String result = "";
        if (!values.isEmpty()) {
            for (String value : values) {
                if (value != null && !value.isEmpty()) {
                    result += (result.isEmpty() ? "" : " OR ") + value;
                }
            }
        } else {
            result += "\"\"";
        }
        return solrFieldName + ":(" + result + ")";
    }

    private void collectValues(List<String> values, Value<?> value) {
        Object tmp = value !=null ? value.get() : null;
        if (tmp != null) {
            if (value instanceof StringValue) {
                values.add("\"" + tmp + "\"");
            } else if (value instanceof LongValue ||
                    value instanceof BooleanValue) {
                values.add("" + tmp);
            } else if (value instanceof DateTimeValue) {
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                values.add("\"" + dateTimeFormat.format(tmp) + "\"");
            } else if (value instanceof TimelessDateValue) {
                values.add("\"" + tmp.toString() + "\"");
            } else if (value instanceof ListValue) {
                List<Value<?>> valueList = ((ListValue)value).getUnmodifiableValuesList();
                for (Value v : valueList) {
                    collectValues(values, v);
                }
            }
        }
    }
}
