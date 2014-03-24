package ru.intertrust.cm.core.dao.impl.sqlparser;

import java.util.HashMap;
import java.util.Map;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SubSelect;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FieldConfig;

/**
 * Визитор для поиска конфигурации полей в Where части SQL запроса и для заполнения фильтров для ссылочных
 * параметров.
 * @author atsvetkov
 */

public class CollectingWhereColumnConfigVisitor extends BaseExpressionVisitor implements ExpressionVisitor {

    protected ConfigurationExplorer configurationExplorer;
    protected PlainSelect plainSelect;

    protected String plainSelectQuery;

    protected Map<String, FieldConfig> whereColumnToConfigMapping = new HashMap<>();

    public Map<String, FieldConfig> getWhereColumnToConfigMapping() {
        return whereColumnToConfigMapping;
    }

    public String getModifiedQuery() {
        return plainSelectQuery;
    }

    public CollectingWhereColumnConfigVisitor(ConfigurationExplorer configurationExplorer, PlainSelect plainSelect) {
        this.configurationExplorer = configurationExplorer;
        this.plainSelect = plainSelect;
        this.plainSelectQuery = plainSelect.toString();
    }

    protected void visitSubSelect(SubSelect subSelect) {
        // TODO Provide Visitor for Sub Select
    }

    @Override
    public void visit(Column column) {
        collectWhereColumnConfigurations(column);
    }

    @Override
    public void visit(RegExpMatchOperator regExpMatchOperator) {

    }

    private void collectWhereColumnConfigurations(Column column) {
        FieldConfig fieldConfig = configurationExplorer.getFieldConfig(SqlQueryModifier.getDOTypeName(plainSelect, column, false),
                column.getColumnName());
        whereColumnToConfigMapping.put(column.getColumnName().toLowerCase(), fieldConfig);
    }

    @Override
    public void visit(InExpression inExpression) {
        if (inExpression.getLeftExpression() != null) {
            inExpression.getLeftExpression().accept(this);
        }
        // TODO Provide Visitor for Sub Select
    }

    @Override
    public void visit(Function function) {
    }

    @Override
    public void visit(SignedExpression signedExpression) {

    }

}
