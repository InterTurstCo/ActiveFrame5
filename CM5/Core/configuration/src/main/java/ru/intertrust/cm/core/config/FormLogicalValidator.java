package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.config.gui.form.*;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfigurationConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetDisplayConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 07/10/13
 *         Time: 13:05 PM
 */
public class FormLogicalValidator {

    private static final String ALIGN_TOP = "top";
    private static final String ALIGN_BOTTOM = "bottom";
    private static final String ALIGN_CENTER = "center";
    private static final String ALIGN_LEFT = "left";
    private static final String ALIGN_RIGHT = "right";

    private static final String WIDGET_HANDLER_FULL_QUALIFIED_NAME =
            "ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler";

    @Autowired
    ApplicationContext context;

    private final static Logger logger = LoggerFactory.getLogger(FormLogicalValidator.class);

    private ConfigurationExplorer configurationExplorer;
    private List<LogicalErrors> validationLogicalErrors;

    public FormLogicalValidator() {
        validationLogicalErrors = new ArrayList<LogicalErrors>();
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
            if (errors.getErrorCount() != 0) {
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
        FormToValidate data = new FormToValidate();

        String formName = formConfig.getName();
        String domainObjectType = formConfig.getDomainObjectType();
        data.setFormName(formName);
        data.setDomainObjectType(domainObjectType);
        logger.info("Validating '{}' form", formName);
        MarkupConfig markup = formConfig.getMarkup();

        if (markup == null) {
            return;
        }
        data.setMarkup(markup);
        WidgetConfigurationConfig widgetConfiguration = formConfig.getWidgetConfigurationConfig();
        if (widgetConfigurationIsEmpty(widgetConfiguration)) {
            String error = String.format("Widget configuration for form '%s' is empty", formName);
            logger.error(error);
            logicalErrors.addError(error);
            return;
        }

        data.setWidgetConfigs(widgetConfiguration.getWidgetConfigList());
        validateHeader(data, logicalErrors);
        validateBody(data, logicalErrors);
        validateWidgetsForExtendingHandler(data, logicalErrors);
        if (!FormConfig.TYPE_REPORT.equals(formConfig.getType())) {
            WidgetConfigurationLogicalValidator validator = new WidgetConfigurationLogicalValidator(configurationExplorer);
            validator.validate(data, logicalErrors);

        } else {
            ReportFormLogicalValidator validator = new ReportFormLogicalValidator();
            validator.validate(formConfig, logicalErrors);
        }
        logger.info("Form '{}' is validated", formName);
    }

    private void validateHeader(FormToValidate data,
                                LogicalErrors logicalErrors) {
        HeaderConfig header = data.getMarkup().getHeader();
        if (header == null) {
            return;
        }
        TableLayoutConfig table = header.getTableLayout();

        validateWidgetsExisting(table, data, logicalErrors);
        validateTableAlignAndDimensions(table, logicalErrors);
    }

    private void validateBody(FormToValidate data,
                              LogicalErrors logicalErrors) {
        BodyConfig body = data.getMarkup().getBody();
        if (body == null) {
            return;
        }
        List<TabConfig> tabs = body.getTabs();

        for (TabConfig tab : tabs) {
            TabGroupListConfig tabGroupListConfig = tab.getGroupList();

            if (tabGroupListConfig instanceof SingleEntryGroupListConfig) {
                SingleEntryGroupListConfig singleEntryGroupListConfig = (SingleEntryGroupListConfig) tabGroupListConfig;
                TabGroupConfig tabGroupConfig = singleEntryGroupListConfig.getTabGroupConfig();
                TableLayoutConfig table = tabGroupConfig.getLayout();

                validateWidgetsExisting(table, data, logicalErrors);
                validateTableAlignAndDimensions(table, logicalErrors);

            } else if (tabGroupListConfig instanceof HidingGroupListConfig) {
                HidingGroupListConfig hidingGroupListConfig = (HidingGroupListConfig) tabGroupListConfig;
                List<TabGroupConfig> tabGroupConfigList = hidingGroupListConfig.getTabGroupConfigs();
                for (TabGroupConfig tabGroupConfig : tabGroupConfigList) {
                    TableLayoutConfig table = tabGroupConfig.getLayout();

                    validateWidgetsExisting(table, data, logicalErrors);
                    validateTableAlignAndDimensions(table, logicalErrors);
                }

            } else if (tabGroupListConfig instanceof BookmarkListConfig) {
                BookmarkListConfig hidingGroupListConfig = (BookmarkListConfig) tabGroupListConfig;
                List<TabGroupConfig> tabGroupConfigList = hidingGroupListConfig.getTabGroupConfigs();
                for (TabGroupConfig tabGroupConfig : tabGroupConfigList) {
                    TableLayoutConfig table = tabGroupConfig.getLayout();

                    validateWidgetsExisting(table, data, logicalErrors);
                    validateTableAlignAndDimensions(table, logicalErrors);
                }

            }

        }
    }

    private void validateWidgetsExisting(TableLayoutConfig table, FormToValidate data, LogicalErrors logicalErrors) {

        if (table == null) {
            String error = String.format("There is no table for form '%s'", data.getFormName());
            logger.error(error);
            logicalErrors.addError(error);
            return;
        }
        List<RowConfig> rows = table.getRows();
        List<String> widgetsIds = findWidgetsInRows(rows);
        for (String widgetId : widgetsIds) {
            findWidgetById(data, widgetId, logicalErrors);
        }
    }

    private List<String> findWidgetsInRows(List<RowConfig> rows) {
        List<String> widgetsIds = new ArrayList<String>();
        for (RowConfig row : rows) {
            List<CellConfig> cells = row.getCells();
            findWidgetsInCell(cells, widgetsIds);
        }
        return widgetsIds;
    }

    private void findWidgetsInCell(List<CellConfig> cells, List<String> widgetsIds) {
        for (CellConfig cell : cells) {
            WidgetDisplayConfig widget = cell.getWidgetDisplayConfig();
            String id = widget.getId();
            widgetsIds.add(id);
        }

    }

    private void findWidgetById(FormToValidate data, String widgetId, LogicalErrors logicalErrors) {
        List<WidgetConfig> widgetConfigs = data.getWidgetConfigs();

        for (WidgetConfig widgetConfig : widgetConfigs) {
            if (widgetConfig == null) {
                continue;
            }
            String id = widgetConfig.getId();
            if (widgetId.equalsIgnoreCase(id)) {
                return;
            }
        }
        String error = String.format("Couldn't find widget with id '%s'", widgetId);
        logger.error(error);
        logicalErrors.addError(error);
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
        if (tableWidth != null) {
            validateWidthAndHeight(tableWidth, logicalErrors);
        }
        String rowHeight = table.getRowHeight();
        if (rowHeight != null) {
            validateWidthAndHeight(rowHeight, logicalErrors);
        }
        String hAlign = table.getHAlign();
        if (hAlign != null) {
            validateHAlign(hAlign, logicalErrors);
        }
        String vAlign = table.getVAlign();
        if (vAlign != null) {
            validateVAlign(vAlign, logicalErrors);
        }
        List<RowConfig> rows = table.getRows();

        for (RowConfig row : rows) {
            validateRowAlignAndDimensions(row, logicalErrors);
            List<CellConfig> cells = row.getCells();
            for (CellConfig cell : cells) {
                validateCellAlignAndDimensions(cell, logicalErrors);
            }
        }
    }

    private void validateWidthAndHeight(String sizeValue, LogicalErrors logicalErrors) {
        if (!sizeValue.matches("\\d{1,4}(px|em|%)")) {
            String error = String.format("Dimension '%s' is incorrect", sizeValue);
            logger.error(error);
            logicalErrors.addError(error);

        }
    }

    private void validateVAlign(String align, LogicalErrors logicalErrors) {
        if (!ALIGN_BOTTOM.equalsIgnoreCase(align) && !ALIGN_CENTER.equalsIgnoreCase(align)
                && !ALIGN_TOP.equalsIgnoreCase(align)) {
            String error = String.format("v-align '%s' is incorrect", align);
            logger.error(error);
            logicalErrors.addError(error);
        }
    }

    private void validateHAlign(String align, LogicalErrors logicalErrors) {
        if (!ALIGN_LEFT.equalsIgnoreCase(align) && !ALIGN_CENTER.equalsIgnoreCase(align)
                && !ALIGN_RIGHT.equalsIgnoreCase(align)) {
            String error = String.format("h-align '%s' is incorrect", align);
            logger.error(error);
            logicalErrors.addError(error);

        }
    }

    private void validateRowAlignAndDimensions(RowConfig row, LogicalErrors logicalErrors) {
        String height = row.getHeight();
        if (height != null) {
            validateWidthAndHeight(height, logicalErrors);
        }
        String vAlign = row.getDefaultVerticalAlignment();
        if (vAlign != null) {
            validateVAlign(vAlign, logicalErrors);
        }
    }

    private void validateCellAlignAndDimensions(CellConfig cell, LogicalErrors logicalErrors) {
        String width = cell.getWidth();
        if (width != null) {
            validateWidthAndHeight(width, logicalErrors);
        }
        String vAlign = cell.getVerticalAlignment();
        if (vAlign != null) {
            validateVAlign(vAlign, logicalErrors);
        }
        String hAlign = cell.getHorizontalAlignment();
        if (hAlign != null) {
            validateHAlign(hAlign, logicalErrors);
        }
    }

    private void validateWidgetsForExtendingHandler(FormToValidate data, LogicalErrors logicalErrors) {
        List<WidgetConfig> widgetConfigs = data.getWidgetConfigs();

        for (WidgetConfig widgetConfig : widgetConfigs) {
            if (widgetConfig == null) {
                continue;
            }
            String componentName = widgetConfig.getComponentName();

            Object bean = null;
            try {
                bean = context.getBean(componentName);
            } catch (BeansException exception) {
                String error = String.format("Could not find widget handler for widget with name '%s'", componentName);
                logger.error(error);
                logicalErrors.addError(error);
                continue;
            }

            Class clazz = bean.getClass();
            validateWidgetForExtendingHandler(clazz, componentName, logicalErrors);

        }
    }

    private void validateWidgetForExtendingHandler(Class clazz, String componentName, LogicalErrors logicalErrors) {
        Class parentClass = clazz.getSuperclass();

        if (parentClass == null) {
            String error = String.format("Could not find widget handler for widget with name '%s'", componentName);
            logger.error(error);
            logicalErrors.addError(error);
            return;
        }

        String parentClassFullName = parentClass.getCanonicalName();
        if (thisIsWidgetHandlerClass(parentClassFullName)) {
            return;
        }

        validateWidgetForExtendingHandler(parentClass, componentName, logicalErrors);

    }

    private boolean widgetConfigurationIsEmpty(WidgetConfigurationConfig config) {
        return config == null || config.getWidgetConfigList() == null || config.getWidgetConfigList().isEmpty();
    }

    private boolean thisIsWidgetHandlerClass(String className) {
        return WIDGET_HANDLER_FULL_QUALIFIED_NAME.equalsIgnoreCase(className);
    }

}
