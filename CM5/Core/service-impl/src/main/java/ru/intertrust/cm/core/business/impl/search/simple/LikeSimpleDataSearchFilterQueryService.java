package ru.intertrust.cm.core.business.impl.search.simple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.simpledata.LikeSimpleDataSearchFilter;
import ru.intertrust.cm.core.business.api.simpledata.SimpleDataSearchFilter;
import ru.intertrust.cm.core.config.SimpleDataConfig;
import ru.intertrust.cm.core.model.FatalException;

@Service
public class LikeSimpleDataSearchFilterQueryService implements SimpleDataSearchFilterQueryService {

    private final SimpleSearchUtils utils;

    @Autowired
    public LikeSimpleDataSearchFilterQueryService(SimpleSearchUtils utils) {
        this.utils = utils;
    }

    @Override
    public Class<?> getType() {
        return LikeSimpleDataSearchFilter.class;
    }

    @Override
    public String prepareQuery(SimpleDataConfig config, SimpleDataSearchFilter filter) {
        final LikeSimpleDataSearchFilter searchFilter = (LikeSimpleDataSearchFilter) filter;

        String solrFieldName = utils.getSolrFieldName(config, searchFilter.getFieldName());
        Value<?> value = searchFilter.getFieldValue();
        String result;
        if (value instanceof StringValue) {
            String val = ((StringValue)value).get();
            // предполагаем стандартный токенайзер - solr.StandardTokenizerFactory
            result = solrFieldName + ": (\"" + (val != null ? val.replaceAll("\"","\\\\\"") : "") + "\")";
        } else {
            throw new FatalException("Like filter can be use only with String fields");
        }
        return result;
    }
}
