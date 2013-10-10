package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.config.model.gui.form.*;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfigurationConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetDisplayConfig;
import ru.intertrust.cm.core.util.ConfigurationUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 07/10/13
 *         Time: 13:05 PM
 */
public class FormLogicalValidator {

    private final static String ALIGN_TOP = "top";
    private final static String ALIGN_BOTTOM = "bottom";
    private final static String ALIGN_CENTER = "center";
    private final static String ALIGN_LEFT = "left";
    private final static String ALIGN_RIGHT = "right";

    @Autowired
    ApplicationContext context;
    private final static Logger logger = LoggerFactory.getLogger(FormLogicalValidator.class);

    private ConfigurationExplorer configurationExplorer;
    private List<LogicalErrors> validationLogicalErrors;

    public FormLogicalValidator() {

    }
    public FormLogicalValidator(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
        validationLogicalErrors = new ArrayList<LogicalErrors>();
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Выполняет логическую валидацию конфигурации форм
     */
    public void validate() {
        Collection<FormConfig> formConfigList = configurationExplorer.getConfigs(FormConfig.class);

        if (formConfigList.isEmpty()) {
            logger.error("Form config couldn't be resolved");
            return;
        }
        for (FormConfig formConfig : formConfigList) {
            String formName = formConfig.getName();
            LogicalErrors logicalErrors = LogicalErrors.getInstance(formName, "form");
            validateFormConfig(formConfig, logicalErrors);
            validationLogicalErrors.add(logicalErrors);
        }
        StringBuilder errorLogBuilder = new StringBuilder();
        for (LogicalErrors errors : validationLogicalErrors) {
            if(errors.getErrorCount() != 0) {
                errorLogBuilder.append(errors.toString());
                errorLogBuilder.append("\n");
            }
        }
        String errorLog = errorLogBuilder.toString();
        if (!errorLog.equalsIgnoreCase("")) {
            throw new ConfigurationException(errorLog);

        }
        logger.info("Form's configuration has passed logical validation without errors");

    }
    private void validateFormConfig(FormConfig formConfig, LogicalErrors logicalErrors) {
       String formName = formConfig.getName();
       logger.info("Validating '{}' form", formName);
       MarkupConfig markup = formConfig.getMarkup();
        if (markup == null )  {
            return;
        }
        WidgetConfigurationConfig widgetConfiguration = formConfig.getWidgetConfigurationConfig();
        if(widgetConfiguration == null )  {
            return;
        }
        validateWidgetsHandlers(widgetConfiguration, logicalErrors);
        validateHeader(markup, widgetConfiguration, logicalErrors);
        validateBody(markup, widgetConfiguration, logicalErrors);
        logger.info("Form '{}' is validated", formName);
    }

    private void validateHeader(MarkupConfig markup, WidgetConfigurationConfig widgetConfiguration,
                                LogicalErrors logicalErrors) {
       HeaderConfig header = markup.getHeader();
        if (header == null )  {
            return;
        }
        TableLayoutConfig table = header.getTableLayout();
        validateWidgetsInTable(table, widgetConfiguration, logicalErrors);
        validateTableAlignAndDimensions(table, logicalErrors);
    }

    private void validateBody(MarkupConfig markup, WidgetConfigurationConfig widgetConfiguration,
                              LogicalErrors logicalErrors) {
        BodyConfig body = markup.getBody();
        if (body == null )  {
            return;
        }
        List<TabConfig> tabs = body.getTabs();

        for (TabConfig tab : tabs) {
            TabGroupListConfig tabGroupListConfig = tab.getGroupList();

            if (tabGroupListConfig instanceof SingleEntryGroupListConfig) {
                SingleEntryGroupListConfig singleEntryGroupListConfig = (SingleEntryGroupListConfig) tabGroupListConfig;
                TabGroupConfig tabGroupConfig = singleEntryGroupListConfig.getTabGroupConfig();
                TableLayoutConfig table = tabGroupConfig.getLayout();

                validateWidgetsInTable(table, widgetConfiguration, logicalErrors);
                validateTableAlignAndDimensions(table, logicalErrors);

            } else if (tabGroupListConfig instanceof HidingGroupListConfig){
                HidingGroupListConfig hidingGroupListConfig = (HidingGroupListConfig)tabGroupListConfig;
                List<TabGroupConfig> tabGroupConfigList = hidingGroupListConfig.getTabGroupConfigs();
                for (TabGroupConfig tabGroupConfig : tabGroupConfigList) {
                    TableLayoutConfig table = tabGroupConfig.getLayout();

                    validateWidgetsInTable(table, widgetConfiguration, logicalErrors);
                    validateTableAlignAndDimensions(table, logicalErrors);
                }

            } else if (tabGroupListConfig instanceof BookmarkListConfig){
                BookmarkListConfig hidingGroupListConfig = (BookmarkListConfig)tabGroupListConfig;
                List<TabGroupConfig> tabGroupConfigList = hidingGroupListConfig.getTabGroupConfigs();
                for (TabGroupConfig tabGroupConfig : tabGroupConfigList) {
                    TableLayoutConfig table = tabGroupConfig.getLayout();

                    validateWidgetsInTable(table, widgetConfiguration, logicalErrors);
                    validateTableAlignAndDimensions(table, logicalErrors);
                }

            }

            }
                    }

     private void validateWidgetsInTable(TableLayoutConfig table,
                                         WidgetConfigurationConfig widgetConfiguration, LogicalErrors logicalErrors) {
         if (table == null) {
             return;
         }
         List<RowConfig> rows = table.getRows();
         List<String> widgetsIds = findWidgetsInRows(rows);
         for (String widgetId: widgetsIds) {
             findWidgetById(widgetConfiguration, widgetId, logicalErrors);
         }
     }

     private List<String> findWidgetsInRows(List<RowConfig> rows) {
         List<String> widgetsIds = new ArrayList<String>();
         for(RowConfig row : rows)   {
             List<CellConfig> cells = row.getCells();
             findWidgetsInCell(cells, widgetsIds);
         }
         return  widgetsIds;
     }
    private void findWidgetsInCell(List<CellConfig> cells,  List<String> widgetsIds)  {
        for (CellConfig cell : cells) {
            WidgetDisplayConfig widget = cell.getWidgetDisplayConfig();
            String id = widget.getId();
                    widgetsIds.add(id);
        }

    }
    private void findWidgetById(WidgetConfigurationConfig widgetConfiguration,
                                String widgetId, LogicalErrors logicalErrors) {
        List<WidgetConfig> widgetConfigs = widgetConfiguration.getWidgetConfigList();
        if (widgetConfigs.isEmpty()) {
            String error = "Widget configuration is empty";
            logger.error(error);
            logicalErrors.addError(error);
            return;
        }
        for (WidgetConfig widgetConfig : widgetConfigs) {
            if (widgetConfig == null )  {
                continue;
            }
            String id = widgetConfig.getId();
            if (widgetId.equalsIgnoreCase(id)) {
                return;
            }
        }
        logger.error("Couldn't find widget with id '{}'",widgetId);
        logicalErrors.addError(String.format("Couldn't find widget with id '%s'", widgetId));
    }

    private void validateTableAlignAndDimensions(TableLayoutConfig table, LogicalErrors logicalErrors) {
        if (table == null) {
            return;
        }
        String tableHeight = table.getHeight();
        if (tableHeight != null) {
            validateWidthAndHeight(tableHeight, logicalErrors);

        }
        String tableWidth = table.getHeight();
        if (tableWidth != null)  {
            validateWidthAndHeight(tableWidth, logicalErrors);
        }
        String rowHeight = table.getRowHeight();
        if (rowHeight != null)  {
            validateWidthAndHeight(rowHeight, logicalErrors);
        }
        String hAlign = table.getHAlign();
        if (hAlign != null)  {
            validateHAlign(hAlign, logicalErrors);
        }
        String  vAlign= table.getVAlign();
        if (vAlign != null)  {
            validateVAlign(vAlign, logicalErrors);
        }
        List<RowConfig> rows = table.getRows();

        for (RowConfig row: rows) {
            validateRowAlignAndDimensions(row, logicalErrors);
            List<CellConfig> cells = row.getCells();
            for (CellConfig cell : cells) {
                validateCellAlignAndDimensions(cell, logicalErrors);
            }
        }
    }

    private void validateWidthAndHeight(String sizeValue, LogicalErrors logicalErrors) {
        if (!sizeValue.matches("\\d{1,4}(px|em|%)")) {
            logger.error("Dimension '{}' is incorrect", sizeValue);
            logicalErrors.addError(String.format("Dimension '%s' is incorrect", sizeValue));

        }
    }
    private void validateVAlign(String align, LogicalErrors logicalErrors) {
        if (!ALIGN_BOTTOM.equalsIgnoreCase(align) && !ALIGN_CENTER.equalsIgnoreCase(align)
                && !ALIGN_TOP.equalsIgnoreCase(align)) {
            logger.error("v-align '{}' is incorrect", align);
            logicalErrors.addError(String.format("v-align '%s' is incorrect", align));
        }
    }

    private void validateHAlign(String align, LogicalErrors logicalErrors) {
        if (!ALIGN_LEFT.equalsIgnoreCase(align) && !ALIGN_CENTER.equalsIgnoreCase(align)
                && !ALIGN_RIGHT.equalsIgnoreCase(align)) {
            logger.error("h-align '{}' is incorrect" , align);
            logicalErrors.addError(String.format("h-align '%s' is incorrect", align));

        }
    }

    private void validateRowAlignAndDimensions(RowConfig row, LogicalErrors logicalErrors) {
        String height = row.getHeight();
        if (height != null)  {
            validateWidthAndHeight(height, logicalErrors);
        }
        String vAlign = row.getDefaultVerticalAlignment();
        if (vAlign != null)  {
            validateVAlign(vAlign, logicalErrors);
        }
    }

    private void validateCellAlignAndDimensions(CellConfig cell, LogicalErrors logicalErrors) {
        String width = cell.getWidth();
        if (width != null)  {
            validateWidthAndHeight(width, logicalErrors);
        }
        String vAlign = cell.getVerticalAlignment();
        if (vAlign != null)  {
            validateVAlign(vAlign, logicalErrors);
        }
        String hAlign = cell.getHorizontalAlignment();
        if (hAlign != null)  {
            validateHAlign(hAlign, logicalErrors);
        }
    }
    private void validateWidgetsHandlers(WidgetConfigurationConfig widgetConfiguration, LogicalErrors logicalErrors) {
        List<WidgetConfig> widgetConfigs = widgetConfiguration.getWidgetConfigList();
        if (widgetConfigs.isEmpty()) {
            String error = "Widget configuration is empty";
            logger.error(error);
            logicalErrors.addError(error);
            return;
        }
        for (WidgetConfig widgetConfig : widgetConfigs) {
            if (widgetConfig == null )  {
                continue;
            }
           String componentName = ConfigurationUtil.getWidgetTag(widgetConfig);
            Object bean = context.getBean(componentName);

            if (bean == null) {
                String error = String.format("Could not find widget handler for widget with name '%s'", componentName);
                logger.error(error);
                logicalErrors.addError(error);
            }


        }
    }
}
