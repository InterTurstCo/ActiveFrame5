package ru.intertrust.cm.core.business.impl.report;

import net.sf.jasperreports.engine.export.ooxml.SochiJRDocxExporter;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.ExporterConfiguration;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.ExporterOutput;
import net.sf.jasperreports.export.ReportExportConfiguration;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;

@Service
public class SochiJRDocxExporterProvider implements ExporterProvider {
    @Override
    public Exporter<ExporterInput, ReportExportConfiguration, ExporterConfiguration, ExporterOutput> getExporter(ExporterConfiguration configuration) {
        return (Exporter) new SochiJRDocxExporter();
    }

    @Override
    public String getType() {
        return ReportBuilderFormats.SOCHI_DOCX_FORMAT.getFormat();
    }

    @Override
    public void setExporterOutput(
            Exporter<ExporterInput, ReportExportConfiguration, ExporterConfiguration, ExporterOutput> exporter, FileOutputStream fos) {
        SimpleOutputStreamExporterOutput output = new SimpleOutputStreamExporterOutput(fos);
        exporter.setExporterOutput(output);
    }

    @Override
    public String getExtension() {
        return ReportBuilderFormats.DOCX_FORMAT.getFormat();
    }
}