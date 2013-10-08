package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.config.model.gui.form.*;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfigurationConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetDisplayConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 07/10/13
 *         Time: 13:05 PM
 */
public class FormLogicalValidator {

    private final static String DIMENSIONS_PX = "px";
    private final static String DIMENSIONS_PERCENTAGE = "%";
    private final static String DIMENSIONS_EM = "em";

    private final static String ALIGN_TOP = "top";
    private final static String ALIGN_BOTTOM = "bottom";
    private final static String ALIGN_CENTER = "center";
    private final static String ALIGN_LEFT = "left";
    private final static String ALIGN_RIGHT = "right";

    private final static Logger logger = LoggerFactory.getLogger(NavigationPanelLogicalValidator.class);

    private ConfigurationExplorer configurationExplorer;

    public FormLogicalValidator(ConfigurationExplorer configurationExplorer) {
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
            validateFormConfig(formConfig);
        }
        logger.info("Form config has passed logical validation");
    }
    private void validateFormConfig(FormConfig formConfig) {
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
        validateHeader(markup, widgetConfiguration);
        validateBody(markup, widgetConfiguration);
    }

    private void validateHeader(MarkupConfig markup, WidgetConfigurationConfig widgetConfiguration) {
       HeaderConfig header = markup.getHeader();
        if (header == null )  {
            return;
        }
        TableLayoutConfig table = header.getTableLayout();
        validateWidgetsInTable(table, widgetConfiguration);
        validateTableAlignAndDimensions(table);
    }

    private void validateBody(MarkupConfig markup, WidgetConfigurationConfig widgetConfiguration) {
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
                if (table == null) {
                    continue;
                }
                validateWidgetsInTable(table, widgetConfiguration);
                validateTableAlignAndDimensions(table);

            } else if (tabGroupListConfig instanceof HidingGroupListConfig){
                HidingGroupListConfig hidingGroupListConfig = (HidingGroupListConfig)tabGroupListConfig;
                List<TabGroupConfig> tabGroupConfigList = hidingGroupListConfig.getTabGroupConfigs();
                for (TabGroupConfig tabGroupConfig : tabGroupConfigList) {
                    TableLayoutConfig table = tabGroupConfig.getLayout();
                    if (table == null) {
                        continue;
                    }
                    validateWidgetsInTable(table, widgetConfiguration);
                    validateTableAlignAndDimensions(table);
                }

            } else if (tabGroupListConfig instanceof BookmarkListConfig){
                BookmarkListConfig hidingGroupListConfig = (BookmarkListConfig)tabGroupListConfig;
                List<TabGroupConfig> tabGroupConfigList = hidingGroupListConfig.getTabGroupConfigs();
                for (TabGroupConfig tabGroupConfig : tabGroupConfigList) {
                    TableLayoutConfig table = tabGroupConfig.getLayout();

                    validateWidgetsInTable(table, widgetConfiguration);
                    validateTableAlignAndDimensions(table);
                }

            }

            }
                    }

     private void validateWidgetsInTable(TableLayoutConfig table, WidgetConfigurationConfig widgetConfiguration) {
         if (table == null) {
             return;
         }
         List<RowConfig> rows = table.getRows();
         List<String> widgetsIds = findWidgetsInRows(rows);
         for (String widgetId: widgetsIds) {
             findWidgetById(widgetConfiguration, widgetId);
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
    private void findWidgetById(WidgetConfigurationConfig widgetConfiguration, String widgetId) {
        List<WidgetConfig> widgetConfigs = widgetConfiguration.getWidgetConfigList();
        if (widgetConfigs.isEmpty()) {
            logger.error("Widget configuration is empty");
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
        logger.error("Couldn't find widget with id '" + widgetId + "'");
    }

    private void validateTableAlignAndDimensions(TableLayoutConfig table) {
        if (table == null) {
            return;
        }
        String tableHeight = table.getHeight();
        if (tableHeight != null) {
            validateWidthAndHeight(tableHeight);

        }
        String tableWidth = table.getHeight();
        if (tableWidth != null)  {
            validateWidthAndHeight(tableWidth);
        }
        String rowHeight = table.getRowHeight();
        if (rowHeight != null)  {
            validateWidthAndHeight(rowHeight);
        }
        String hAlign = table.getHAlign();
        if (hAlign != null)  {
            validateHAlign(hAlign);
        }
        String  vAlign= table.getVAlign();
        if (vAlign != null)  {
            validateVAlign(vAlign);
        }
        List<RowConfig> rows = table.getRows();

        for (RowConfig row: rows) {
            validateRowAlignAndDimensions(row);
            List<CellConfig> cells = row.getCells();
            for (CellConfig cell : cells) {
                validateCellAlignAndDimensions(cell);
            }

        }
    }

    private void validateWidthAndHeight(String sizeValue) {
        if (!sizeValue.matches("\\d{1,4}(px|em|%)")) {
             logger.error("Dimensions are incorrect");
        }

    }
    private void validateVAlign(String align) {
        if (!ALIGN_BOTTOM.equalsIgnoreCase(align) && !ALIGN_CENTER.equalsIgnoreCase(align)
                && !ALIGN_TOP.equalsIgnoreCase(align)) {
            logger.error("v-align is incorrect");
        }
    }

    private void validateHAlign(String align) {
        if (!ALIGN_LEFT.equalsIgnoreCase(align) && !ALIGN_CENTER.equalsIgnoreCase(align)
                && !ALIGN_RIGHT.equalsIgnoreCase(align)) {
            logger.error("h-align is incorrect");
        }
    }
    private void validateRowAlignAndDimensions(RowConfig row) {
        String height = row.getHeight();
        if (height != null)  {
            validateWidthAndHeight(height);
        }
        String vAlign = row.getDefaultVerticalAlignment();
        if (vAlign != null)  {
            validateVAlign(vAlign);
        }
    }
    private void validateCellAlignAndDimensions(CellConfig cell) {
        String width = cell.getWidth();
        if (width != null)  {
            validateWidthAndHeight(width);
        }
        String vAlign = cell.getVerticalAlignment();
        if (vAlign != null)  {
            validateVAlign(vAlign);
        }
        String hAlign = cell.getHorizontalAlignment();
        if (hAlign != null)  {
            validateHAlign(hAlign);
        }
    }
}
