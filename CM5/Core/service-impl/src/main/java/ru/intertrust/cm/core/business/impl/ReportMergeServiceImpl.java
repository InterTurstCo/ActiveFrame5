package ru.intertrust.cm.core.business.impl;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import java.util.*;

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
            } else if ("XLSX".equalsIgnoreCase(formats.get(0))) {
                return mergeXlsx(reportFiles, outFile);
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

    private InputStream mergeXlsx(List<File> reportFiles, File outFile) throws Exception {
        FileOutputStream faos = new FileOutputStream(outFile);
        XLSXMerge xm = new XLSXMerge(faos);
        for (File docx : reportFiles) {
            xm.add(new FileInputStream(docx));
        }
        xm.doMerge();
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

    public class XLSXMerge {
        private final List<Workbook> eDocs;
        private final OutputStream result;

        public XLSXMerge(OutputStream result) {
            this.result = result;
            eDocs = new ArrayList<>();
        }

        public void add(String fileName) throws Exception {
            add(new FileInputStream(fileName));
        }

        public void add(InputStream stream) throws Exception {
            XSSFWorkbook srcBook = new XSSFWorkbook(stream);
            stream.close();
            eDocs.add(srcBook);
        }

        public void doMerge() throws Exception {
            if (!eDocs.isEmpty()) {
                Workbook mainBook = eDocs.remove(0);
                int cnt = 0;
                for (Workbook srcBook : eDocs) {
                    int sheetCnt = srcBook.getNumberOfSheets();
                    for (int i = 0; i < sheetCnt; i++) {
                        Sheet newSheet = mainBook.createSheet(srcBook.getSheetName(i) + (++cnt));
                        XlsxUtil.copySheets((XSSFSheet) newSheet, (XSSFSheet) srcBook.getSheetAt(i), true);
                    }
                }
                mainBook.write(result);
                result.flush();
                result.close();
            }
        }
    }

    private static class XlsxUtil {
/*
        private static final int NUMERIC = 0;
        private static final int STRING = 1;
        private static final int FORMULA = 2;
        private static final int BLANK = 3;
        private static final int BOOLEAN = 4;
        private static final int ERROR = 5;
*/
        public static void copySheets(XSSFSheet newSheet, XSSFSheet sheet) {
            copySheets(newSheet, sheet, true);
        }

        public static void copySheets(XSSFSheet newSheet, XSSFSheet sheet, boolean copyStyle) {
            int maxColumnNum = 0;
            Set<CellRangeAddress> mergedRegions = new HashSet<CellRangeAddress>();
            Map<Integer, CellStyle> styleMap = (copyStyle) ? new HashMap<Integer, CellStyle>() : null;
            for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
                XSSFRow srcRow = sheet.getRow(i);
                XSSFRow destRow = newSheet.createRow(i);
                if (srcRow != null) {
                    copyRow(sheet, newSheet, srcRow, destRow, styleMap, mergedRegions);
                    if (srcRow.getLastCellNum() > maxColumnNum) {
                        maxColumnNum = srcRow.getLastCellNum();
                    }
                }
            }
            for (int i = 0; i <= maxColumnNum; i++) {
                newSheet.setColumnWidth(i, sheet.getColumnWidth(i));
            }
        }

        public static void copyRow(XSSFSheet srcSheet, XSSFSheet destSheet, XSSFRow srcRow, XSSFRow destRow,
                                   Map<Integer, CellStyle> styleMap, Set<CellRangeAddress> mergedRegions) {
            destRow.setHeight(srcRow.getHeight());
            for (int j = srcRow.getFirstCellNum(); j <= srcRow.getLastCellNum(); j++) {
                XSSFCell oldCell = srcRow.getCell(j);
                XSSFCell newCell = destRow.getCell(j);
                if (oldCell != null) {
                    if (newCell == null) {
                        newCell = destRow.createCell(j);
                    }
                    copyCell(oldCell, newCell, styleMap);
                    CellRangeAddress mergedRegion = getMergedRegion(srcSheet, oldCell);
                    if (mergedRegion != null) {
                        CellRangeAddress newMergedRegion = new CellRangeAddress(
                                mergedRegion.getFirstRow(), mergedRegion.getLastRow(),
                                mergedRegion.getFirstColumn(), mergedRegion.getLastColumn());
                        if (isNewMergedRegion(newMergedRegion, mergedRegions)) {
                            mergedRegions.add(newMergedRegion);
                            destSheet.addMergedRegion(newMergedRegion);
                        }
                    }
                }
            }

        }

        public static void copyCell(XSSFCell oldCell, XSSFCell newCell, Map<Integer, CellStyle> styleMap) {
            if (styleMap != null) {
                if (oldCell.getSheet().getWorkbook() == newCell.getSheet().getWorkbook()) {
                    newCell.setCellStyle(oldCell.getCellStyle());
                } else {
                    int stHashCode = oldCell.getCellStyle().hashCode();
                    CellStyle newCellStyle = styleMap.get(stHashCode);
                    if (newCellStyle == null) {
                        newCellStyle = newCell.getSheet().getWorkbook().createCellStyle();
                        newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
                        styleMap.put(stHashCode, newCellStyle);
                    }
                    newCell.setCellStyle(newCellStyle);
                }
            }
            switch(oldCell.getCellType()) {
                case STRING:
                    newCell.setCellValue(oldCell.getStringCellValue());
                    break;
                case NUMERIC:
                    newCell.setCellValue(oldCell.getNumericCellValue());
                    break;
                case BLANK:
                    newCell.setCellType(CellType.BLANK);
                    break;
                case BOOLEAN:
                    newCell.setCellValue(oldCell.getBooleanCellValue());
                    break;
                case ERROR:
                    newCell.setCellErrorValue(oldCell.getErrorCellValue());
                    break;
                case FORMULA:
                    newCell.setCellFormula(oldCell.getCellFormula());
                    break;
                default:
                    break;
            }
        }

        public static CellRangeAddress getMergedRegion(XSSFSheet sheet, Cell cell) {
            for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
                CellRangeAddress merged = sheet.getMergedRegion(i);
                if (merged.isInRange(cell)) {
                    return merged;
                }
            }
            return null;
        }

        private static boolean isNewMergedRegion(CellRangeAddress newMergedRegion, Collection<CellRangeAddress> mergedRegions) {
            return !mergedRegions.contains(newMergedRegion);
        }

    }
}
