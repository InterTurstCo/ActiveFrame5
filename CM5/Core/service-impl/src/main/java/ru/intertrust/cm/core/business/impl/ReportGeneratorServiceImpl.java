package ru.intertrust.cm.core.business.impl;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.business.api.ReportGeneratorService;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Stateless(name = "ReportGeneratorService")
@Local(ReportGeneratorService.class)
@Remote(ReportGeneratorService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ReportGeneratorServiceImpl implements ReportGeneratorService {
    private static final Logger logger = LoggerFactory.getLogger(ReportGeneratorServiceImpl.class);

    @Override
    public InputStream generateXLS(String title, Map<String, String> columns, List<Map<String, Object>> data) throws Exception {
        logger.debug("generateXLS: start");
        try {
            Workbook workBook = new HSSFWorkbook();
            CellStyle titleStyle = createTitleStyle(workBook);
            CellStyle headerStyle = createHeaderStyle(workBook);
            CellStyle rowStyle = createRowStyle(workBook);
            int rowIdx = 0;

            // Заголовок всей таблицы
            Sheet sheet = workBook.createSheet("Печать представления");
            Row row = sheet.createRow(rowIdx);
            createCell(workBook, row, 0, title != null ? title : "", titleStyle, false);
            int colCntx = columns != null ? columns.size() : 0;

            if (colCntx > 0) {
                logger.debug("generateXLS: generate header");
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, colCntx - 1));
                int colIdx = 0;
                row = sheet.createRow(++ rowIdx);
                for (Map.Entry<String, String> colEntry : columns.entrySet()) {
                    createCell(workBook, row, colIdx ++, colEntry.getValue(), headerStyle, false);
                }
                // Таблица
                if (data != null && !data.isEmpty()) {
                    logger.debug("generateXLS: generate data rows");
                    for (Map<String, Object> rowData : data) {
                        createDataRow(++ rowIdx, workBook, sheet, columns, rowData, rowStyle);
                    }
                }
                // Авторесайз
                for (colIdx = 0; colIdx < colCntx; colIdx ++) {
                    sheet.autoSizeColumn(colIdx);
                }
            }
            logger.debug("generateXLS: create tmp-file");
            File file = new File(System.getProperty("java.io.tmpdir").toString(), "print-view-" + System.currentTimeMillis() + ".xls");
            logger.debug("generateXLS: create workbook");
            workBook.write(new FileOutputStream(file));
            logger.debug("generateXLS: return inputStream");
            return new FileInputStream(file);
        } catch (Exception e) {
            logger.error("generateXLS error: ", e);
            throw new Exception("Error generate XLS report", e);
        }
    }

    private CellStyle createTitleStyle(Workbook workBook) {
        CellStyle titleStyle = workBook.createCellStyle();
        titleStyle.setAlignment(CellStyle.ALIGN_CENTER);
        Font fontTitle = workBook.createFont();
        fontTitle.setBoldweight(Font.BOLDWEIGHT_BOLD);
        titleStyle.setFont(fontTitle);
        return titleStyle;
    }

    private CellStyle createHeaderStyle(Workbook workBook) {
        CellStyle headerStyle = workBook.createCellStyle();
        headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
        headerStyle.setWrapText(true);
        headerStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
        headerStyle.setBorderTop(CellStyle.BORDER_MEDIUM);
        headerStyle.setBorderLeft(CellStyle.BORDER_MEDIUM);
        headerStyle.setBorderRight(CellStyle.BORDER_MEDIUM);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        Font fontHeader = workBook.createFont();
        fontHeader.setBoldweight(Font.BOLDWEIGHT_BOLD);
        headerStyle.setFont(fontHeader);
        return headerStyle;
    }

    private CellStyle createRowStyle(Workbook workBook) {
        CellStyle rowStyle = workBook.createCellStyle();
        rowStyle.setAlignment(CellStyle.ALIGN_LEFT);
        return rowStyle;
    }

    private Row createDataRow(int rowIdx, Workbook workBook, Sheet sheet, Map<String, String> columns,
                              Map<String, Object> rowData, CellStyle cellStyle) {
        Row row = sheet.createRow(rowIdx);
        int colIdx = 0;
        for (String colId : columns.keySet()) {
            createCell(workBook, row, colIdx ++, rowData != null ? rowData.get(colId) : "", cellStyle, true);
        }
        return row;
    }

    private Cell createCell(Workbook workBook, Row row, int column, Object value, CellStyle style, boolean formatByData){
        short dateFormatIdx = 15;
        CellStyle dataStyle = workBook.createCellStyle();
        if (formatByData) {
            DataFormat dataformat = workBook.createDataFormat();
            dateFormatIdx = dataformat.getFormat("dd-mmm-yyyy");
            dataStyle.cloneStyleFrom(style);
        }
        Cell cell = row.createCell(column);
        if (value instanceof Boolean) {
            cell.setCellValue(((Boolean)value).booleanValue());
            dataStyle.setAlignment(CellStyle.ALIGN_CENTER);
        } else if (value instanceof Integer) {
            cell.setCellValue(((Integer)value).intValue());
            dataStyle.setAlignment(CellStyle.ALIGN_RIGHT);
        } else if (value instanceof Long) {
            cell.setCellValue(((Long)value).longValue());
            dataStyle.setAlignment(CellStyle.ALIGN_RIGHT);
        } else if (value instanceof Float) {
            cell.setCellValue(((Float)value).doubleValue());
            dataStyle.setAlignment(CellStyle.ALIGN_RIGHT);
        } else if (value instanceof Double) {
            cell.setCellValue(((Double)value).doubleValue());
            dataStyle.setAlignment(CellStyle.ALIGN_RIGHT);
        } else if (value instanceof BigDecimal) {
            cell.setCellValue(((BigDecimal)value).doubleValue());
            dataStyle.setAlignment(CellStyle.ALIGN_RIGHT);
        } else if (value instanceof Date) {
            cell.setCellValue(((Date)value));
            dataStyle.setAlignment(CellStyle.ALIGN_CENTER);
            dataStyle.setDataFormat(dateFormatIdx);
        } else if (value instanceof Calendar) {
            cell.setCellValue(((Calendar) value));
            dataStyle.setAlignment(CellStyle.ALIGN_CENTER);
            dataStyle.setDataFormat(dateFormatIdx);
        } else {
            cell.setCellValue(value != null ? value.toString() : "");
        }
        cell.setCellStyle(formatByData ? dataStyle : style);
        return cell;
    }

}
