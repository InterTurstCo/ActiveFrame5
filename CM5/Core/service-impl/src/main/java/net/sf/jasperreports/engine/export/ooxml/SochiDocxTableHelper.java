package net.sf.jasperreports.engine.export.ooxml;

import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.export.CutsInfo;

import java.io.Writer;

public class SochiDocxTableHelper extends DocxTableHelper {

    protected SochiDocxTableHelper(JasperReportsContext jasperReportsContext,
                                   Writer writer, CutsInfo xCuts, boolean pageBreak) {
        super(jasperReportsContext, writer, xCuts, pageBreak);
    }

    @Override
    public void exportRowHeader(int rowHeight, boolean allowRowResize) {
        write("   <w:tr>\n");
    }

}
