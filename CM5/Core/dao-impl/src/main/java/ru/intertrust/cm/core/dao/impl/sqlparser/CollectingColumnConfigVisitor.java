package ru.intertrust.cm.core.dao.impl.sqlparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubSelect;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;

/**
 * Визитор для поиска конфигурации полей в Where части SQL запроса и для заполнения фильтров для ссылочных
 * параметров.
 * @author atsvetkov
 */

public class CollectingColumnConfigVisitor extends BaseParamProcessingVisitor implements ExpressionVisitor, FromItemVisitor, SelectVisitor {

    protected ConfigurationExplorer configurationExplorer;
    protected PlainSelect plainSelect;

    protected List<PlainSelect> innerSubSelects = new ArrayList<>();

    protected String plainSelectQuery;

    protected Map<String, FieldConfig> columnToConfigMapping = new HashMap<>(); 

    public Map<String, FieldConfig> getColumnToConfigMapping() {
        return columnToConfigMapping;
    }

    public String getModifiedQuery() {
        return plainSelectQuery;
    }

    public CollectingColumnConfigVisitor(ConfigurationExplorer configurationExplorer, PlainSelect plainSelect) {
        this.configurationExplorer = configurationExplorer;
        this.plainSelect = plainSelect;
        this.plainSelectQuery = plainSelect.toString();
    }

    @Override
    protected void visitSubSelect(SubSelect subSelect) {
        if (subSelect.getSelectBody() instanceof PlainSelect) {
            PlainSelect subPlainSelect = (PlainSelect) subSelect.getSelectBody();
            innerSubSelects.add(subPlainSelect);
        }
        subSelect.getSelectBody().accept(this);        
    }

    @Override
    public void visit(SetOperationList setOpList) {

        innerSubSelects.addAll(setOpList.getPlainSelects());
        super.visit(setOpList);
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        selectExpressionItem.getExpression().accept(this);

        // Add column to config mapping for column alias
        if (selectExpressionItem.getAlias() != null && selectExpressionItem.getAlias().getName() != null &&
                selectExpressionItem.getExpression() instanceof Column) {
            Column column = (Column) selectExpressionItem.getExpression();
            String aliasName = DaoUtils.unwrap(selectExpressionItem.getAlias().getName().toLowerCase());
            if (columnToConfigMapping.get(aliasName) == null) {
                columnToConfigMapping.put(aliasName, columnToConfigMapping.get(getColumnName(column)));
            }
        }
    }

    @Override
    public void visit(Column column) {
        collectColumnConfiguration(column);
    }

    @Override
    public void visit(RegExpMatchOperator regExpMatchOperator) {

    }

    private void collectColumnConfiguration(Column column) {
        FieldConfig fieldConfig =
                configurationExplorer.getFieldConfig(SqlQueryModifier.getDOTypeName(plainSelect, column, false),
                        DaoUtils.unwrap(column.getColumnName()));
        //если колонка не объявлена в основном запросе, выполняется поиск по подзапросам
        if (fieldConfig == null) {
            for (PlainSelect innerSubSelect : innerSubSelects) {
                fieldConfig =
                        configurationExplorer.getFieldConfig(SqlQueryModifier.getDOTypeName(innerSubSelect, column, false),
                                DaoUtils.unwrap(column.getColumnName()));
                if (fieldConfig != null) {
                    break;
                }
            }
        }
        if (fieldConfig != null) {
            columnToConfigMapping.put(getColumnName(column), fieldConfig);
        }
    }

    private String getColumnName(Column column) {
        return DaoUtils.unwrap(column.getColumnName().toLowerCase());
    }
}
