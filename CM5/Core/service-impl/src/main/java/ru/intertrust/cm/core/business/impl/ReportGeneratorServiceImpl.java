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
import java.util.*;

@Stateless(name = "ReportGeneratorService")
@Local(ReportGeneratorService.class)
@Remote(ReportGeneratorService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ReportGeneratorServiceImpl implements ReportGeneratorService {
    private static final Logger logger = LoggerFactory.getLogger(ReportGeneratorServiceImpl.class);

    private enum DataType {
        BOOLEAN,
        INTEGER,
        LONG,
        FLOAT,
        DOUBLE,
        BIGDECIMAL,
        DATE,
        CALENDAR,
        OTHER
    }

    @Override
    public InputStream generateXLS(String title, Map<String, String> columns, List<Map<String, Object>> data) throws Exception {
        logger.debug("generateXLS: start");
        try {
            Workbook workBook = new HSSFWorkbook();
            WBStyleCache styleCache = new WBStyleCache(workBook);
            DataFormat dataformat = workBook.createDataFormat();
            short dateFormatIdx = dataformat != null ? dataformat.getFormat("dd-mmm-yyyy") : 15;
            CellStyle titleStyle = createTitleStyle(workBook);
            CellStyle headerStyle = createHeaderStyle(workBook);
            CellStyle rowStyle = createRowStyle(workBook);
            int rowIdx = 0;

            // Заголовок всей таблицы
            Sheet sheet = workBook.createSheet("Печать представления");
            Row row = sheet.createRow(rowIdx);
            createCell(styleCache, row, 0, title != null ? title : "", titleStyle, false, dateFormatIdx);
            int colCntx = columns != null ? columns.size() : 0;

            if (colCntx > 0) {
                logger.debug("generateXLS: generate header");
                if (colCntx > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, colCntx - 1));
                }
                int colIdx = 0;
                row = sheet.createRow(++ rowIdx);
                for (Map.Entry<String, String> colEntry : columns.entrySet()) {
                    createCell(styleCache, row, colIdx ++, colEntry.getValue(), headerStyle, false, dateFormatIdx);
                }
                // Таблица
                if (data != null && !data.isEmpty()) {
                    logger.debug("generateXLS: generate data rows");
                    for (Map<String, Object> rowData : data) {
                        createDataRow(++ rowIdx, styleCache, sheet, columns, rowData, rowStyle, dateFormatIdx);
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
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        Font fontTitle = workBook.createFont();
        fontTitle.setBold(true);
        titleStyle.setFont(fontTitle);
        return titleStyle;
    }

    private CellStyle createHeaderStyle(Workbook workBook) {
        CellStyle headerStyle = workBook.createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setWrapText(true);
        headerStyle.setBorderBottom(BorderStyle.MEDIUM);
        headerStyle.setBorderTop(BorderStyle.MEDIUM);
        headerStyle.setBorderLeft(BorderStyle.MEDIUM);
        headerStyle.setBorderRight(BorderStyle.MEDIUM);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font fontHeader = workBook.createFont();
        fontHeader.setBold(true);
        headerStyle.setFont(fontHeader);
        return headerStyle;
    }

    private CellStyle createRowStyle(Workbook workBook) {
        CellStyle rowStyle = workBook.createCellStyle();
        rowStyle.setAlignment(HorizontalAlignment.LEFT);
        return rowStyle;
    }

    private Row createDataRow(int rowIdx, WBStyleCache styleCache, Sheet sheet, Map<String, String> columns,
                              Map<String, Object> rowData, CellStyle cellStyle, short dateFormatIdx) {
        Row row = sheet.createRow(rowIdx);
        int colIdx = 0;
        for (String colId : columns.keySet()) {
            createCell(styleCache, row, colIdx ++, rowData != null ? rowData.get(colId) : "", cellStyle, true, dateFormatIdx);
        }
        return row;
    }

    private Cell createCell(WBStyleCache styleCache, Row row, int column, Object value,
                            CellStyle style, boolean formatByData, short dateFormatIdx){
        CellStyle dataStyle = null;
        Cell cell = row.createCell(column);
        if (value instanceof Boolean) {
            cell.setCellValue(((Boolean)value).booleanValue());
            dataStyle = getDataStyle(styleCache, DataType.BOOLEAN, style, formatByData, dateFormatIdx);
        } else if (value instanceof Integer) {
            cell.setCellValue(((Integer)value).intValue());
            dataStyle = getDataStyle(styleCache, DataType.INTEGER, style, formatByData, dateFormatIdx);
        } else if (value instanceof Long) {
            cell.setCellValue(((Long)value).longValue());
            dataStyle = getDataStyle(styleCache, DataType.LONG, style, formatByData, dateFormatIdx);
        } else if (value instanceof Float) {
            cell.setCellValue(((Float)value).doubleValue());
            dataStyle = getDataStyle(styleCache, DataType.FLOAT, style, formatByData, dateFormatIdx);
        } else if (value instanceof Double) {
            cell.setCellValue(((Double)value).doubleValue());
            dataStyle = getDataStyle(styleCache, DataType.DOUBLE, style, formatByData, dateFormatIdx);
        } else if (value instanceof BigDecimal) {
            cell.setCellValue(((BigDecimal)value).doubleValue());
            dataStyle = getDataStyle(styleCache, DataType.BIGDECIMAL, style, formatByData, dateFormatIdx);
        } else if (value instanceof Date) {
            cell.setCellValue(((Date)value));
            dataStyle = getDataStyle(styleCache, DataType.DATE, style, formatByData, dateFormatIdx);
        } else if (value instanceof Calendar) {
            cell.setCellValue(((Calendar) value));
            dataStyle = getDataStyle(styleCache, DataType.CALENDAR, style, formatByData, dateFormatIdx);
        } else {
            cell.setCellValue(value != null ? value.toString() : "");
            dataStyle = getDataStyle(styleCache, DataType.OTHER, style, formatByData, dateFormatIdx);
        }
        cell.setCellStyle(dataStyle);
        return cell;
    }

    private CellStyle getDataStyle(WBStyleCache styleCache, DataType dataType, CellStyle style, boolean formatByData, short dateFormatIdx) {
        return styleCache.getCellStyle(dataType, style, formatByData, dateFormatIdx);
    }

    private static class WBStyleCache {
        private final Workbook workBook;
        private final Map<String, CellStyle> styleCache = new HashMap<>();

        WBStyleCache(Workbook workBook) {
            this.workBook = workBook;
        }

        private CellStyle getCellStyle(DataType dataType, CellStyle style, boolean formatByData, short dateFormatIdx) {
            String key = dataType.name() + "_" + style.hashCode() + "_" + formatByData;
            CellStyle dataStyle = styleCache.get(key);
            if (dataStyle == null) {
                dataStyle = workBook.createCellStyle();
                dataStyle.cloneStyleFrom(style);
                styleCache.put(key, dataStyle);
                if (formatByData) {
                    switch (dataType) {
                        case INTEGER:
                        case LONG:
                        case FLOAT:
                        case DOUBLE:
                        case BIGDECIMAL:
                            dataStyle.setAlignment(HorizontalAlignment.RIGHT);
                            break;
                        case DATE:
                        case CALENDAR:
                            dataStyle.setDataFormat(dateFormatIdx);
                        case BOOLEAN:
                            dataStyle.setAlignment(HorizontalAlignment.CENTER);
                            break;
                        default:
                            break;
                    }
                }
            }
            return dataStyle;
        }
    }
}
