package ru.intertrust.cm.core.business.impl.report;

import net.sf.jasperreports.export.ExporterConfiguration;

/**
 * Configuration without any implementation...
 */
public class DummyExporterConfiguration implements ExporterConfiguration {

    @Override
    public Boolean isOverrideHints() {
        return null;
    }
}
