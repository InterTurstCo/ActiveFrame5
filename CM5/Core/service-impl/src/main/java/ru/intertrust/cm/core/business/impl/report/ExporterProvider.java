package ru.intertrust.cm.core.business.impl.report;

import java.util.Map;
import javax.annotation.Nonnull;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.ExporterConfiguration;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.ExporterOutput;
import net.sf.jasperreports.export.ReportExportConfiguration;

import java.io.FileOutputStream;

public interface ExporterProvider extends ExtensionProvider {

    ExporterConfiguration DUMMY_CONFIGURATION = new DummyExporterConfiguration();

    Exporter<ExporterInput, ReportExportConfiguration, ExporterConfiguration, ExporterOutput> getExporter(ExporterConfiguration exporterConfiguration);

    default ExporterConfiguration getConfiguration(@Nonnull Map<String, Object> params) {
        return DUMMY_CONFIGURATION;
    }

    String getType();

    void setExporterOutput(
            Exporter<ExporterInput, ReportExportConfiguration, ExporterConfiguration, ExporterOutput> exporter,
            FileOutputStream fos);
}
