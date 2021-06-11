package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.dao.api.CurrentDataSourceContext;

/**
 * Реализация интерфейса для получения/установления контекста используемого источника данных
 */
public class CurrentDataSourceContextImpl implements CurrentDataSourceContext {

    @Autowired
    private DefaultDataSourcesConfiguration defaultDataSourcesConfiguration;

    private static final ThreadLocal<String> currentDataSourceJndiName = new ThreadLocal<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public String get() {
        return currentDataSourceJndiName.get();
    }

    @Override
    public String getDescription() {
        final String jndiName = get();
        if (jndiName == null || jndiName.equals(defaultDataSourcesConfiguration.getMasterDataSourceJndiName())) {
            return "MASTER";
        }
        if (jndiName.equals(defaultDataSourcesConfiguration.getCollectionsDataSourceJndiName())) {
            return "COLLECTIONS";
        }
        if (jndiName.equals(defaultDataSourcesConfiguration.getReportsDataSourceJndiName())) {
            return "REPORT";
        }
        return "UNKNOWN " + jndiName;
    }

    @Override
    public void set(String context) {
        currentDataSourceJndiName.set(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setToMaster() {
        currentDataSourceJndiName.set(defaultDataSourcesConfiguration.getMasterDataSourceJndiName());
    }

    @Override
    public boolean isMaster() {
        final String currentDsJndi = currentDataSourceJndiName.get();
        return currentDsJndi == null || currentDsJndi.equals(defaultDataSourcesConfiguration.getMasterDataSourceJndiName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setToCollections() {
        currentDataSourceJndiName.set(defaultDataSourcesConfiguration.getCollectionsDataSourceJndiName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setToReports() {
        currentDataSourceJndiName.set(defaultDataSourcesConfiguration.getReportsDataSourceJndiName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        currentDataSourceJndiName.set(null);
    }
}
