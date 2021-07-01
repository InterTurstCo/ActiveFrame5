package ru.intertrust.cm.core.business.impl;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.business.api.ReportMergeService;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Stateless(name = "ReportMergeService")
@Local(ReportMergeService.class)
@Remote(ReportMergeService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ReportMergeServiceImpl implements ReportMergeService {
    private static final Logger logger = LoggerFactory.getLogger(ReportMergeServiceImpl.class);

    @Override
    public InputStream mergeReports(List<String> formats, List<File> reportFiles, File outFile) throws Exception {
        if (formats != null && !formats.isEmpty()) {
            if ("DOCX".equalsIgnoreCase(formats.get(0))) {
                return mergeDocx(reportFiles, outFile);
            }
        }
        return new FileInputStream(outFile);
    }

    private InputStream mergeDocx(List<File> reportFiles, File outFile) throws Exception {
        FileOutputStream faos = new FileOutputStream(outFile);
        DOCXMerge wm = new DOCXMerge(faos);
        for (File docx : reportFiles) {
            wm.add(new FileInputStream(docx));
        }
        wm.doMerge();
        return new FileInputStream(outFile);
    }

    private class DOCXMerge {
        private final List<XWPFDocument> wDocs;
        private final OutputStream result;

        public DOCXMerge(OutputStream result) {
            this.result = result;
            wDocs = new ArrayList<>();
        }

        public void add(String fileName) throws Exception {
            add(new FileInputStream(fileName));
        }

        public void add(InputStream stream) throws Exception {
            OPCPackage srcPackage = OPCPackage.open(stream);
            XWPFDocument srcDocument = new XWPFDocument(srcPackage);
            stream.close();
            wDocs.add(srcDocument);
        }

        public void doMerge() throws Exception {
            if (!wDocs.isEmpty()) {
                // Добавляем раздел (чтобы отступы для разных документов не разъезжались)
                for (int i = 0; i < wDocs.size() - 1; i++) {
                    XWPFDocument srcDocument = wDocs.get(i);
                    XWPFParagraph paragraph = srcDocument.createParagraph();
                    paragraph.getCTP().addNewPPr().setSectPr(srcDocument.getDocument().getBody().getSectPr());
                }
                XWPFDocument mainDocument = wDocs.remove(0);
                for (XWPFDocument srcDocument : wDocs) {
                    mainDocument.getDocument().addNewBody().set(srcDocument.getDocument().getBody());
                }
                mainDocument.write(result);
                result.flush();
                result.close();
            }
        }
    }
}
