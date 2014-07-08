package ru.intertrust.cm.core.dao.impl.sqlparser;

import java.util.HashMap;
import java.util.Map;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FieldConfig;

/**
 * Визитор для поиска конфигурации полей в Where части SQL запроса и для заполнения фильтров для ссылочных
 * параметров.
 * @author atsvetkov
 */

public class CollectingColumnConfigVisitor extends BaseParamProcessingVisitor implements ExpressionVisitor, FromItemVisitor, SelectVisitor {

    protected ConfigurationExplorer configurationExplorer;
    protected PlainSelect plainSelect;

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
        subSelect.getSelectBody().accept(this);
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        selectExpressionItem.getExpression().accept(this);

        // Add column to config mapping for column alias
        if (selectExpressionItem.getAlias() != null && selectExpressionItem.getAlias().getName() != null &&
                selectExpressionItem.getExpression() instanceof Column) {
            Column column = (Column) selectExpressionItem.getExpression();
            String aliasName = selectExpressionItem.getAlias().getName().toLowerCase();
            if (columnToConfigMapping.get(aliasName) == null) {
                columnToConfigMapping.put(aliasName, columnToConfigMapping.get(getColumnName(column)));
            }
        }
    }

    @Override
    public void visit(Column column) {
        collectWhereColumnConfigurations(column);
    }

    @Override
    public void visit(RegExpMatchOperator regExpMatchOperator) {

    }

    private void collectWhereColumnConfigurations(Column column) {
        FieldConfig fieldConfig =
                configurationExplorer.getFieldConfig(SqlQueryModifier.getDOTypeName(plainSelect, column, false),
                        column.getColumnName());
        if (fieldConfig != null) {
            columnToConfigMapping.put(getColumnName(column), fieldConfig);
        }
    }

    private String getColumnName(Column column) {
        return column.getColumnName().toLowerCase();
    }
}
