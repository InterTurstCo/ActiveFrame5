package ru.intertrust.cm.core.business.impl.report;

import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.ExporterConfiguration;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.ExporterOutput;
import net.sf.jasperreports.export.ReportExportConfiguration;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;

@Service
public class HtmlExporterProvider implements ExporterProvider {

    @Override
    public Exporter<ExporterInput, ReportExportConfiguration, ExporterConfiguration, ExporterOutput> getExporter() {
        return (Exporter) new HtmlExporter();
    }

    @Override
    public String getType() {
        return ReportBuilderFormats.HTML_FORMAT.getFormat();
    }

    @Override
    public void setExporterOutput(
            Exporter<ExporterInput, ReportExportConfiguration, ExporterConfiguration, ExporterOutput> exporter, FileOutputStream fos) {
        SimpleHtmlExporterOutput exporterOutput = new SimpleHtmlExporterOutput(fos);
        exporter.setExporterOutput(exporterOutput);
    }

    @Override
    public String getExtension() {
        return ReportBuilderFormats.HTML_FORMAT.getFormat();
    }
}
