package ru.intertrust.cm.core.business.impl.report;

import net.sf.jasperreports.export.CsvExporterConfiguration;

/**
 * Configuration without any implementation...
 */
public class DummyCsvExporterConfiguration extends DummyExporterConfiguration implements CsvExporterConfiguration {

    @Override
    public Boolean isOverrideHints() {
        return null;
    }

    @Override
    public String getFieldDelimiter() {
        return null;
    }

    @Override
    public String getFieldEnclosure() {
        return null;
    }

    @Override
    public Boolean getForceFieldEnclosure() {
        return null;
    }

    @Override
    public String getRecordDelimiter() {
        return null;
    }

    @Override
    public Boolean isWriteBOM() {
        return null;
    }
}
