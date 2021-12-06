package ru.intertrust.cm.core.business.impl.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExporterProviderFactoryImpl implements ExporterProviderFactory {

    private final Map<String, ExporterProvider> exporterProviderMap = new HashMap<>();

    @DefaultProvider
    private ExporterProvider defaultExporterProvider;

    @Autowired
    public ExporterProviderFactoryImpl(List<ExporterProvider> providerList) {
        providerList.forEach(it -> exporterProviderMap.putIfAbsent(it.getType(), it));
    }

    @Override
    public ExporterProvider createExporterProvider(@Nonnull String type) {
        return exporterProviderMap.getOrDefault(type.toUpperCase(), defaultExporterProvider);
    }
}
