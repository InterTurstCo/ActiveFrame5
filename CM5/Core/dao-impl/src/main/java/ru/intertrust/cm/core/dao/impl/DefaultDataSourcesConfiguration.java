package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Value;

/**
 * Класс предоставляет доступ к конфигурации источников данных
 */
public class DefaultDataSourcesConfiguration {

    @Value("${datasource.master:java:jboss/datasources/CM5}")
    private String masterDataSourceJndiName;

    @Value("${datasource.collections:java:jboss/datasources/CM5}")
    private String collectionsDataSourceJndiName;

    @Value("${datasource.reports:java:jboss/datasources/CM5}")
    private String reportsDataSourceJndiName;

    String getMasterDataSourceJndiName() {
        return masterDataSourceJndiName;
    }

    String getCollectionsDataSourceJndiName() {
        return collectionsDataSourceJndiName;
    }

    String getReportsDataSourceJndiName() {
        return reportsDataSourceJndiName;
    }
}
