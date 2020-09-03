package ru.intertrust.cm.core.business.impl.search.simple;

import ru.intertrust.cm.core.config.SimpleDataConfig;
import ru.intertrust.cm.core.config.SimpleDataFieldConfig;

public interface SimpleSearchUtils {
    String getSolrFieldName(SimpleDataConfig config, String fieldName);

    SimpleDataFieldConfig getFieldConfig(SimpleDataConfig config, String name);
}
