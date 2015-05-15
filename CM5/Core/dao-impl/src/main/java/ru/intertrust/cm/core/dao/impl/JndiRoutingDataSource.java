package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.jdbc.datasource.lookup.DataSourceLookup;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import ru.intertrust.cm.core.dao.api.CurrentDataSourceContext;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Реализация абстрактоного источника данных, использующая {@link CurrentDataSourceContext} для определения реального
 * источника данных, используемого в данный момент
 */
public class JndiRoutingDataSource extends AbstractRoutingDataSource {

    @Autowired
    private CurrentDataSourceContext currentDataSourceContext;

    @Autowired
    private DefaultDataSourcesConfiguration defaultDataSourcesConfiguration;

    private Map<Object, DataSource> resolvedDataSources = new ConcurrentHashMap<>();

    private DataSourceLookup dataSourceLookup = new JndiDataSourceLookup();

    public JndiRoutingDataSource() {
        setTargetDataSources(new HashMap<>());
    }

    @Override
    protected String determineCurrentLookupKey() {
        return currentDataSourceContext.get();
    }

    @Override
    protected DataSource determineTargetDataSource() {
        String lookupKey = determineCurrentLookupKey();
        if (lookupKey == null) {
            lookupKey = defaultDataSourcesConfiguration.getMasterDataSourceJndiName();
        }

        DataSource result = resolvedDataSources.get(lookupKey);

        if (result != null) {
            return result;
        }

        result = this.dataSourceLookup.getDataSource(lookupKey);

        if (result == null) {
            throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + lookupKey + "]");
        }

        resolvedDataSources.put(lookupKey, result);
        return result;
    }
}
