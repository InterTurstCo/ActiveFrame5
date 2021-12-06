package ru.intertrust.cm.core.business.impl.report;

import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.ExporterConfiguration;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.ExporterOutput;
import net.sf.jasperreports.export.ReportExportConfiguration;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import java.io.FileOutputStream;

@DefaultProvider
public class JRPdfExporterProvider implements ExporterProvider {

    @Override
    public Exporter<ExporterInput, ReportExportConfiguration, ExporterConfiguration, ExporterOutput> getExporter(ExporterConfiguration configuration) {
        return (Exporter) new JRPdfExporter();
    }

    @Override
    public String getType() {
        return ReportBuilderFormats.PDF_FORMAT.getFormat();
    }

    @Override
    public void setExporterOutput(
            Exporter<ExporterInput, ReportExportConfiguration, ExporterConfiguration, ExporterOutput> exporter, FileOutputStream fos) {
        SimpleOutputStreamExporterOutput output = new SimpleOutputStreamExporterOutput(fos);
        exporter.setExporterOutput(output);
    }

    @Override
    public String getExtension() {
        return ReportBuilderFormats.PDF_FORMAT.getFormat();
    }
}
