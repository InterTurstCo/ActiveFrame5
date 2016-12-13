package ru.intertrust.cm.core.dao.impl;


import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.dao.api.CurrentDataSourceContext;

/**
 * Реализация итерфейса для получения/установления контекста используемого источника данных
 */
public class CurrentDataSourceContextImpl implements CurrentDataSourceContext {

    @Autowired
    private DefaultDataSourcesConfiguration defaultDataSourcesConfiguration;

    private static ThreadLocal<String> currentDataSourceJndiName = new ThreadLocal<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public String get() {
        return currentDataSourceJndiName.get();
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
