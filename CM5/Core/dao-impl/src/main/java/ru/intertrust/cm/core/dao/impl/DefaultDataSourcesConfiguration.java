package ru.intertrust.cm.core.dao.impl;


/**
 * Класс предоставляет доступ к конфигурации источников данных
 */
public class DefaultDataSourcesConfiguration {

    @org.springframework.beans.factory.annotation.Value("${datasource.master:java:jboss/datasources/CM5}")
    private String masterDataSourceJndiName;

    @org.springframework.beans.factory.annotation.Value("${datasource.collections:java:jboss/datasources/CM5}")
    private String collectionsDataSourceJndiName;

    @org.springframework.beans.factory.annotation.Value("${datasource.reports:java:jboss/datasources/CM5}")
    private String reportsDataSourceJndiName;

    public String getMasterDataSourceJndiName() {
        return masterDataSourceJndiName;
    }

    public String getCollectionsDataSourceJndiName() {
        return collectionsDataSourceJndiName;
    }

    public String getReportsDataSourceJndiName() {
        return reportsDataSourceJndiName;
    }
}
