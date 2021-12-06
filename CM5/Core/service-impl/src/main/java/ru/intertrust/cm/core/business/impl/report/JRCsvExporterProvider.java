package ru.intertrust.cm.core.business.impl.report;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;
import javax.annotation.Nonnull;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.export.CsvExporterConfiguration;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.ExporterConfiguration;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.ExporterOutput;
import net.sf.jasperreports.export.ReportExportConfiguration;
import net.sf.jasperreports.export.SimpleCsvExporterConfiguration;
import net.sf.jasperreports.export.WriterExporterOutput;
import org.springframework.stereotype.Service;

@Service
public class JRCsvExporterProvider implements ExporterProvider {
    private static final String DEFAULT_DELIMITER = ";";
    private static final String DELIMITER_PARAMETER_NAME = "delimiter";

    @Override
    public Exporter<ExporterInput, ReportExportConfiguration, ExporterConfiguration, ExporterOutput> getExporter(ExporterConfiguration exporterConfiguration) {
        JRCsvExporter jrCsvExporter = new JRCsvExporter();
        jrCsvExporter.setConfiguration((CsvExporterConfiguration) exporterConfiguration);
        return (Exporter) jrCsvExporter;
    }

    @Override
    public ExporterConfiguration getConfiguration(@Nonnull Map<String, Object> params) {
        String delimiter = (String) params.getOrDefault(DELIMITER_PARAMETER_NAME, DEFAULT_DELIMITER);

        SimpleCsvExporterConfiguration simpleCsvExporterConfiguration = new SimpleCsvExporterConfiguration();
        simpleCsvExporterConfiguration.setFieldDelimiter(delimiter);
        return simpleCsvExporterConfiguration;
    }

    @Override
    public String getType() {
        return ReportBuilderFormats.CSV_FORMAT.getFormat();
    }

    @Override
    public void setExporterOutput(Exporter<ExporterInput, ReportExportConfiguration, ExporterConfiguration, ExporterOutput> exporter, FileOutputStream fos) {
        CsvExporterOutput output = new CsvExporterOutput(fos);
        exporter.setExporterOutput(output);
    }

    @Override
    public String getExtension() {
        return ReportBuilderFormats.CSV_FORMAT.getFormat();
    }

    private static class CsvExporterOutput implements WriterExporterOutput {
        private static final String charsetName = "windows-1251";
        private final OutputStreamWriter osw;

        private CsvExporterOutput(FileOutputStream fos) {
            Charset cs = Charset.isSupported(charsetName) ? Charset.forName(charsetName) : Charset.defaultCharset();
            this.osw = new OutputStreamWriter(fos, cs);
        }

        @Override
        public String getEncoding() {
            return charsetName;
        }

        @Override
        public Writer getWriter() {
            return osw;
        }

        @Override
        public void close() {
            try {
                osw.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
