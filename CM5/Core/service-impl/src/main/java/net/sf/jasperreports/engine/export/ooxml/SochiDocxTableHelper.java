package net.sf.jasperreports.engine.export.ooxml;

import java.io.Writer;

import net.sf.jasperreports.engine.JRPrintElementIndex;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.PrintPageFormat;
import net.sf.jasperreports.engine.export.CutsInfo;

public class SochiDocxTableHelper extends DocxTableHelper {

	/**
	 * @param jasperReportsContext
	 * @param writer
	 * @param xCuts
	 * @param pageBreak
	 * @param pageFormat
	 * @param frameIndex
	 */
	protected SochiDocxTableHelper(JasperReportsContext jasperReportsContext, Writer writer, CutsInfo xCuts, boolean pageBreak, PrintPageFormat pageFormat, JRPrintElementIndex frameIndex) {
		super(jasperReportsContext, writer, xCuts, pageBreak, pageFormat, frameIndex);
	}

	@Override
	public void exportRowHeader(int rowHeight, boolean allowRowResize) 
	{
		write("   <w:tr>\n");
	}

}
