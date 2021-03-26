package ru.intertrust.cm.core.business.impl.report;

import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.ExporterConfiguration;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.ExporterOutput;
import net.sf.jasperreports.export.ReportExportConfiguration;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;

@Service
public class JRXlsExporterProvider implements ExporterProvider {
    @Override
    public Exporter<ExporterInput, ReportExportConfiguration, ExporterConfiguration, ExporterOutput> getExporter() {
        return (Exporter) new JRXlsExporter();
    }

    @Override
    public String getType() {
        return ReportBuilderFormats.XLS_FORMAT.getFormat();
    }

    @Override
    public void setExporterOutput(Exporter<ExporterInput, ReportExportConfiguration, ExporterConfiguration, ExporterOutput> exporter, FileOutputStream fos) {
        SimpleOutputStreamExporterOutput output = new SimpleOutputStreamExporterOutput(fos);
        exporter.setExporterOutput(output);
    }

    @Override
    public String getExtension() {
        return ReportBuilderFormats.XLS_FORMAT.getFormat();
    }
}
