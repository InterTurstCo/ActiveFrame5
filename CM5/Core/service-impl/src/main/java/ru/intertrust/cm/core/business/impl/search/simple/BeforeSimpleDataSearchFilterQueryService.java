package ru.intertrust.cm.core.business.impl.search.simple;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.simpledata.BeforeSimpleDataSearchFilter;
import ru.intertrust.cm.core.business.api.simpledata.SimpleDataSearchFilter;
import ru.intertrust.cm.core.config.SimpleDataConfig;
import ru.intertrust.cm.core.model.FatalException;

@Service
public class BeforeSimpleDataSearchFilterQueryService implements SimpleDataSearchFilterQueryService{

    private final SimpleSearchUtils utils;

    @Autowired
    public BeforeSimpleDataSearchFilterQueryService(SimpleSearchUtils utils) {
        this.utils = utils;
    }

    @Override
    public Class<?> getType() {
        return BeforeSimpleDataSearchFilter.class;
    }

    @Override
    public String prepareQuery(SimpleDataConfig config, SimpleDataSearchFilter filter) {
        final BeforeSimpleDataSearchFilter searchFilter = (BeforeSimpleDataSearchFilter) filter;

        String solrFieldName = utils.getSolrFieldName(config, searchFilter.getFieldName());
        Value<?> value = searchFilter.getFieldValue();

        if (value instanceof StringValue) {
            return prepareQuery(searchFilter, solrFieldName, value.get());
        } else if (value instanceof DateTimeValue) {
            final DateTimeValue dateTimeValue = (DateTimeValue) value;
            String stringDateTime = dateTimeValue.get().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                    .format(DateTimeFormatter.ISO_DATE_TIME) + "Z";

            return prepareQuery(searchFilter, solrFieldName, stringDateTime);
        } else if (value instanceof TimelessDateValue) {
            final TimelessDate date = (TimelessDate) value.get();
            String stringDate = LocalDate.of(date.getYear(), date.getMonth() + 1, date.getDayOfMonth())
                    .format(DateTimeFormatter.ISO_DATE);

            return prepareQuery(searchFilter, solrFieldName, stringDate);
        } else {
            throw new FatalException("Unable to prepare query. Unsupported value type: " + value.getClass());
        }
    }

    private String prepareQuery(BeforeSimpleDataSearchFilter searchFilter, String solrFieldName, Object rawValue) {
        return solrFieldName + ":[* TO " + rawValue + getBracket(searchFilter);
    }

    private String getBracket(BeforeSimpleDataSearchFilter filter) {
        return filter.isExclusive() ? "}" : "]";
    }
}
