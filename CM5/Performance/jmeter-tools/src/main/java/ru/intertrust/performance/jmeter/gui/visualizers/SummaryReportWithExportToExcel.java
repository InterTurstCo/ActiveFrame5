package ru.intertrust.performance.jmeter.gui.visualizers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.gui.NumberRenderer;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.gui.RendererUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.Functor;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ru.intertrust.performance.jmeter.util.Calculator;

/**
 * Класс, формирующий суммарный отчет с возможностью выгрузки в Excel
 * 
 */
public class SummaryReportWithExportToExcel extends AbstractVisualizer implements Clearable, ActionListener {

    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String USE_GROUP_NAME = "useGroupName"; //$NON-NLS-1$

    private static final String SAVE_HEADERS = "saveHeaders"; //$NON-NLS-1$

    private static final String[] COLUMNS = {"sampler_label", //$NON-NLS-1$
            "aggregate_report_count", "3", "4", "5",//$NON-NLS-1$
            "average", //$NON-NLS-1$
            "tps",
            "tps_system",
    };

    private final String TOTAL_ROW_LABEL = JMeterUtils.getResString("aggregate_report_total_label"); //$NON-NLS-1$

    private JTable myJTable;
    // Кнопка Excel
    private JButton excelButton = new JButton("Save to Excel");

    private JScrollPane myScrollPane;

    private final JButton saveTable =
            new JButton(JMeterUtils.getResString("aggregate_graph_save_table")); //$NON-NLS-1$

    private final JCheckBox saveHeaders = // should header be saved with the
                                          // data?
            new JCheckBox(JMeterUtils.getResString("aggregate_graph_save_table_header"), true); //$NON-NLS-1$

    private final JCheckBox useGroupName =
            new JCheckBox(JMeterUtils.getResString("aggregate_graph_use_group_name")); //$NON-NLS-1$

    private transient ObjectTableModel model;

    /**
     * Lock used to protect tableRows update + model update
     */
    private final transient Object lock = new Object();

    private final Map<String, Calculator> tableRows =
            new ConcurrentHashMap<String, Calculator>();

    // Column renderers
    private static final TableCellRenderer[] RENDERERS =
            new TableCellRenderer[] {
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    new NumberRenderer("#0.00"),
                    new NumberRenderer("#0.00"),

            };

    public SummaryReportWithExportToExcel() {
        super();
        model = new ObjectTableModel(COLUMNS,
                Calculator.class,// All rows have this class
                new Functor[] {
                        new Functor("getLabel"),
                        new Functor("getCount"),
                        new Functor("getCount"),
                        new Functor("getCount"),
                        new Functor("getLabel"),
                        new Functor("getMeanAsNumber"),
                        new Functor("getTps"),
                        new Functor("getTpsSystem"),

                },
                new Functor[] {null, null, null, null, null, null, null, null },
                new Class[] {String.class, Long.class, Long.class, Long.class, String.class, Long.class, String.class, String.class });
        clearData();
        init();
    }

    /** @deprecated - only for use in testing */
    @Deprecated
    public static boolean testFunctors() {
        SummaryReportWithExportToExcel instance = new SummaryReportWithExportToExcel();
        return instance.model.checkFunctors(null, instance.getClass());
    }

    @Override
    public String getLabelResource() {
        return "summary_report";
    }

    @Override
    public void add(final SampleResult res) {
        final String sampleLabel = res.getSampleLabel(useGroupName.isSelected());
        JMeterUtils.runSafe(new Runnable() {
            @Override
            public void run() {
                Calculator row = null;
                synchronized (lock) {
                    row = tableRows.get(sampleLabel);
                    if (row == null) {
                        row = new Calculator(sampleLabel);
                        tableRows.put(row.getLabel(), row);
                        model.insertRow(row, model.getRowCount() - 1);
                    }
                }
                /*
                 * Synch is needed because multiple threads can update the
                 * counts.
                 */
                synchronized (row) {
                    row.addSample(res);
                }
                Calculator tot = tableRows.get(TOTAL_ROW_LABEL);
                synchronized (tot) {
                    tot.addSample(res);
                }
                model.fireTableDataChanged();
            }
        });
    }

    /**
     * Clears this visualizer and its model, and forces a repaint of the table.
     */
    @Override
    public void clearData() {
        // Synch is needed because a clear can occur while add occurs
        synchronized (lock) {
            model.clearData();
            tableRows.clear();
            tableRows.put(TOTAL_ROW_LABEL, new Calculator(TOTAL_ROW_LABEL));
            model.addRow(tableRows.get(TOTAL_ROW_LABEL));
        }
    }

    /**
     * Main visualizer setup.
     */
    private void init() {
        this.setLayout(new BorderLayout());

        // MAIN PANEL
        JPanel mainPanel = new JPanel();
        Border margin = new EmptyBorder(10, 10, 5, 10);

        mainPanel.setBorder(margin);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(makeTitlePanel());

        myJTable = new JTable(model);
        myJTable.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        myJTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        RendererUtils.applyRenderers(myJTable, RENDERERS);
        myScrollPane = new JScrollPane(myJTable);
        this.add(mainPanel, BorderLayout.NORTH);
        this.add(myScrollPane, BorderLayout.CENTER);
        saveTable.addActionListener(this);
        excelButton.addActionListener(this);
        JPanel opts = new JPanel();
        opts.add(useGroupName, BorderLayout.WEST);
        opts.add(saveTable, BorderLayout.CENTER);
        opts.add(excelButton, BorderLayout.CENTER);
        opts.add(saveHeaders, BorderLayout.EAST);
        this.add(opts, BorderLayout.SOUTH);
    }

    @Override
    public void modifyTestElement(TestElement c) {
        super.modifyTestElement(c);
        c.setProperty(USE_GROUP_NAME, useGroupName.isSelected(), false);
        c.setProperty(SAVE_HEADERS, saveHeaders.isSelected(), true);
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        useGroupName.setSelected(el.getPropertyAsBoolean(USE_GROUP_NAME, false));
        saveHeaders.setSelected(el.getPropertyAsBoolean(SAVE_HEADERS, true));
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        // Если нажата кнопка Save to Excel
        if (ev.getSource() == excelButton) {
            // Имя сохраняемого файла по умолчанию
            JFileChooser chooser = FileDialoger.promptToSaveFile("report.xlsx");//$NON-NLS-1$
            if (chooser == null) {
                return;
            }
            try {
                // Получаем шаблон
                InputStream templateStream = getClass().getClassLoader().getResourceAsStream("template.xlsx");
                // Получаем книгу
                Workbook wb = new XSSFWorkbook(OPCPackage.open(templateStream));
                // Получаем лист
                Sheet sheet = wb.getSheetAt(0);
                int rowCount = model.getRowCount();
                int colCount = model.getColumnCount();
                for (int i = 0; i < rowCount - 1; i++) {
                    Row row = sheet.createRow(i + 2);
                    for (int j = 0; j < colCount; j++) {
                        Object curValue = model.getValueAt(i, j);
                        Cell cell = row.createCell(j);
                        // Смещение,т.к. в шаблоне есть шапка отчета
                        int rowNum = i + 3;
                        switch (j) {
                            case 0:
                                cell.setCellValue(Double.valueOf(curValue.toString()));
                                break;
                            case 1:
                                cell.setCellValue(Double.valueOf(curValue.toString()));
                                break;
                            case 2:
                                cell.setCellValue(Double.valueOf(curValue.toString()));
                                break;
                            case 3:
                                cell.setCellValue(Double.valueOf(0));
                                break;
                            case 4: {
                                cell.setCellType(Cell.CELL_TYPE_FORMULA);
                                cell.setCellFormula("D" + rowNum + "*100/B" + rowNum);
                                break;
                            }
                            case 5:
                                cell.setCellValue(Double.valueOf(curValue.toString()));
                                break;
                            case 6: {
                                cell.setCellType(Cell.CELL_TYPE_FORMULA);
                                cell.setCellFormula("1/(F" + rowNum + "/1000)");
                                break;
                            }
                            case 7: {
                                cell.setCellType(Cell.CELL_TYPE_FORMULA);
                                cell.setCellFormula("G" + rowNum + "*A" + rowNum);
                                break;
                            }
                        }

                    }
                }
                FileOutputStream fileOut = new FileOutputStream(chooser.getSelectedFile());
                wb.write(fileOut);
                fileOut.close();
            } catch (FileNotFoundException e) {
                log.warn(e.getMessage());
            } catch (IOException e) {
                log.warn(e.getMessage());
            } catch (InvalidFormatException e) {
                log.warn(e.getMessage());
            }
        }
        if (ev.getSource() == saveTable) {

            JFileChooser chooser = FileDialoger.promptToSaveFile("summary.csv");//$NON-NLS-1$
            if (chooser == null) {
                return;
            }
            FileWriter writer = null;
            try {
                writer = new FileWriter(chooser.getSelectedFile());
                CSVSaveService.saveCSVStats(model, writer, saveHeaders.isSelected());
            } catch (FileNotFoundException e) {
                log.warn(e.getMessage());
            } catch (IOException e) {
                log.warn(e.getMessage());
            } finally {
                JOrphanUtils.closeQuietly(writer);
            }
        }
    }

    // Название листнера в меню
    public String getStaticLabel() {
        return "Summary report with export to Excel";
    }
}
