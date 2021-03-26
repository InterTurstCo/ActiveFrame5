package ru.intertrust.cm.core.business.impl.report;

public enum ReportBuilderFormats {
    PDF_FORMAT("PDF"),
    RTF_FORMAT("RTF"),
    XLS_FORMAT("XLS"),
    HTML_FORMAT("HTML"),
    DOCX_FORMAT("DOCX"),
    XLSX_FORMAT("XLSX"),
    SOCHI_DOCX_FORMAT("SOCHIDOCX");

    private final String format;

    ReportBuilderFormats(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }
}
