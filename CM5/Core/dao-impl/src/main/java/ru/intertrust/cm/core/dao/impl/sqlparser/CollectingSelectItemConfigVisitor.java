package ru.intertrust.cm.core.dao.impl.sqlparser;

import java.util.Iterator;
import java.util.List;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;

/**
 * 
 * @author atsvetkov
 * 
 */
public class CollectingSelectItemConfigVisitor extends CollectingColumnConfigVisitor {

    public CollectingSelectItemConfigVisitor(ConfigurationExplorer configurationExplorer, PlainSelect plainSelect) {
        super(configurationExplorer, plainSelect);
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        //Если с данным именем уже нашли колонку в подзапросе, то ничего не делаем, конфигурация останется та, что получена в подзапросе
        if (selectExpressionItem.getExpression() instanceof Column
                && columnToConfigMapping.containsKey(((Column) selectExpressionItem.getExpression()).getColumnName())) {
            replaceColumnNameToAliasNameInConfigMap(selectExpressionItem);
            return;
        }

        selectExpressionItem.getExpression().accept(this);

        // Add column to config mapping for column alias
        if (selectExpressionItem.getAlias() != null && selectExpressionItem.getAlias().getName() != null &&
                selectExpressionItem.getExpression() instanceof Column) {
            replaceColumnNameToAliasNameInConfigMap(selectExpressionItem);
        } else if (selectExpressionItem.getAlias() != null && selectExpressionItem.getAlias().getName() != null) {
            String aliasName = DaoUtils.unwrap(Case.toLower(selectExpressionItem.getAlias().getName()));
            columnToConfigMapping.remove(aliasName);
        }
    }

    /**
     * Замена имени конфигурации колонки с имени колонки на алиас
     * @param selectExpressionItem
     */
    private void replaceColumnNameToAliasNameInConfigMap(SelectExpressionItem selectExpressionItem) {
        if (selectExpressionItem.getAlias() != null && selectExpressionItem.getAlias().getName() != null) {
            Column column = (Column) selectExpressionItem.getExpression();
            String aliasName = DaoUtils.unwrap(Case.toLower(selectExpressionItem.getAlias().getName()));
            //Выполняем замену только если алиас отличается от имени колонки
            final String columnName = getColumnName(column);
            if (!aliasName.equalsIgnoreCase(columnName)) {
                final List<FieldData> fieldDataList = columnToConfigMapping.get(columnName);
                if (fieldDataList == null) {
                    return;
                }

                if (fieldDataList.isEmpty()) {
                    changeKey(aliasName, columnName, fieldDataList);
                } else if (fieldDataList.size() == 1) {
                    fieldDataList.get(0).setColumnName(aliasName);
                    changeKey(aliasName, columnName, fieldDataList);
                } else {
                    final String doTypeName = SqlQueryModifier.getDOTypeName(plainSelect, column, false);
                    final Iterator<FieldData> iterator = fieldDataList.iterator();
                    FieldData fieldDataExt = null;
                    while (iterator.hasNext()) {
                        FieldData fieldData = iterator.next();
                        if (fieldData.getDoTypeName().equals(doTypeName)) {
                            iterator.remove();
                            fieldDataExt = fieldData;
                            break;
                        }
                    }

                    if (fieldDataExt != null) {
                        fieldDataExt.setColumnName(aliasName);
                        FieldDataHelper.addFieldData(columnToConfigMapping, fieldDataExt);
                    }
                }
            }
        }
    }

    private void changeKey(String aliasName, String columnName, List<FieldData> fieldDataList) {
        columnToConfigMapping.put(aliasName, fieldDataList);
        columnToConfigMapping.remove(columnName);
    }

}
