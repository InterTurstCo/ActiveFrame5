package ru.intertrust.cm.core.business.impl.report;

public interface ExporterProviderFactory {
    ExporterProvider createExporterProvider(String type);
}
