package ru.intertrust.cm.core.business.impl.report;

import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.export.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;

@Service
public class JRCsvExporterProvider implements ExporterProvider {
    private static final String DEFAULT_DELIMETR = ";";

    @Override
    public Exporter<ExporterInput, ReportExportConfiguration, ExporterConfiguration, ExporterOutput> getExporter() {
        SimpleCsvExporterConfiguration configuration = new SimpleCsvExporterConfiguration();
        configuration.setFieldDelimiter(DEFAULT_DELIMETR);

        JRCsvExporter exporter = new JRCsvExporter();
        exporter.setConfiguration(configuration);
        return (Exporter) exporter;
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
        private static String charsetName = "windows-1251";
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
