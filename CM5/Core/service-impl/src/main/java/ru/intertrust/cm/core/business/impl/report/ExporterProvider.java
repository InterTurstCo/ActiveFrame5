package ru.intertrust.cm.core.business.impl.report;

import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.ExporterConfiguration;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.ExporterOutput;
import net.sf.jasperreports.export.ReportExportConfiguration;

import java.io.FileOutputStream;

public interface ExporterProvider extends ExtensionProvider {

    Exporter<ExporterInput, ReportExportConfiguration, ExporterConfiguration, ExporterOutput> getExporter();

    String getType();

    void setExporterOutput(
            Exporter<ExporterInput, ReportExportConfiguration, ExporterConfiguration, ExporterOutput> exporter,
            FileOutputStream fos);
}
