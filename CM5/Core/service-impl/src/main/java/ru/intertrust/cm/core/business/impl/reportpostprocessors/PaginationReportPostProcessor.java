package ru.intertrust.cm.core.business.impl.reportpostprocessors;

import java.io.File;
import java.math.BigInteger;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Body;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.P;
import org.docx4j.wml.STPageOrientation;
import org.docx4j.wml.SectPr.PgMar;
import org.docx4j.wml.SectPr.PgSz;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.docx4j.wml.TrPr;

import ru.intertrust.cm.core.business.api.ReportPostProcessor;
import ru.intertrust.cm.core.model.ReportServiceException;


public class PaginationReportPostProcessor implements ReportPostProcessor {

	@Override
	public void format(File reportFile) {
		
		try {
			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(reportFile);
            Body body = wordMLPackage.getMainDocumentPart().getContents().getBody();
            PgSz pgsz = body.getSectPr().getPgSz();
            pgsz.setOrient(STPageOrientation.LANDSCAPE);
            pgsz.setW(BigInteger.valueOf(16839));
            pgsz.setH(BigInteger.valueOf(11907));
            
            PgMar pgmar = body.getSectPr().getPgMar();
            pgmar.setBottom(BigInteger.valueOf(555));
            pgmar.setTop(BigInteger.valueOf(555));
            pgmar.setLeft(BigInteger.valueOf(1110));
            pgmar.setRight(BigInteger.valueOf(555));
            
            Tbl tbl = (Tbl) ((javax.xml.bind.JAXBElement) body.getContent().get(0)).getValue();
            Tr tblRow = (Tr) tbl.getContent().get(0);
            Tc tc = (Tc) ((javax.xml.bind.JAXBElement)tblRow.getContent().get(0)).getValue();
            P p =(P) tc.getContent().get(0);
            tbl.getContent().remove(0);
            body.getContent().add(0,p);
            tblRow = (Tr) tbl.getContent().get(0);
            
            BooleanDefaultTrue bdt = Context.getWmlObjectFactory().createBooleanDefaultTrue();
            
            TrPr trPr = tblRow.getTrPr();
            if (trPr==null){
            	trPr = new TrPr();
            }
            trPr.getCnfStyleOrDivIdOrGridBefore().add(
                  Context.getWmlObjectFactory().createCTTrPrBaseTblHeader(bdt)                   
            );
            tblRow.setTrPr(trPr);

            
            wordMLPackage.save(reportFile); 
                   
        wordMLPackage.save(reportFile); 
		} catch (Exception ex) {
			throw new ReportServiceException("Error post edit jasper report file", ex);
        }
	}

}
