package net.sf.jasperreports.engine.export.ooxml;

import java.io.Writer;

import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.export.CutsInfo;

public class SochiDocxTableHelper extends DocxTableHelper {

	protected SochiDocxTableHelper(JasperReportsContext jasperReportsContext,
			Writer writer, CutsInfo xCuts, boolean pageBreak) {
		super(jasperReportsContext, writer, xCuts, pageBreak);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void exportRowHeader(int rowHeight, boolean allowRowResize) 
	{
		write("   <w:tr>\n");
	}

}
