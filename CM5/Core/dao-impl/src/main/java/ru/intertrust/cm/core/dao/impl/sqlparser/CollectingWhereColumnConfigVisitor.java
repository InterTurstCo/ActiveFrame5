package ru.intertrust.cm.core.dao.impl.sqlparser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SubSelect;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.impl.CollectionsDaoImpl;

/**
 * Визитор для поиска конфигурации полей в Where части SQL запроса и для заполнения фильтров для ссылочных
 * параметров.
 * @author atsvetkov
 */

public class CollectingWhereColumnConfigVisitor implements ExpressionVisitor {

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

    @Override
    public void visit(NullValue nullValue) {
    }

    @Override
    public void visit(Function function) {
    }

    @Override
    public void visit(InverseExpression inverseExpression) {
        if (inverseExpression.getExpression() != null) {
            inverseExpression.getExpression().accept(this);
        }
    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {
    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
    }

    @Override
    public void visit(DoubleValue doubleValue) {
    }

    @Override
    public void visit(LongValue longValue) {
    }

    @Override
    public void visit(DateValue dateValue) {
    }

    @Override
    public void visit(TimeValue timeValue) {
    }

    @Override
    public void visit(TimestampValue timestampValue) {
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        if (parenthesis.getExpression() != null) {
            parenthesis.getExpression().accept(this);
        }
    }

    @Override
    public void visit(StringValue stringValue) {
    }

    @Override
    public void visit(Addition addition) {
        visitBinaryExpression(addition);
    }

    @Override
    public void visit(Division division) {
        visitBinaryExpression(division);
    }

    @Override
    public void visit(Multiplication multiplication) {
        visitBinaryExpression(multiplication);
    }

    @Override
    public void visit(Subtraction subtraction) {
        visitBinaryExpression(subtraction);
    }

    @Override
    public void visit(AndExpression andExpression) {
        visitBinaryExpression(andExpression);
    }

    @Override
    public void visit(OrExpression orExpression) {
        visitBinaryExpression(orExpression);
    }

    @Override
    public void visit(Between between) {
        if (between.getLeftExpression() != null) {
            between.getLeftExpression().accept(this);
        }

        if (between.getBetweenExpressionStart() != null) {
            between.getBetweenExpressionStart().accept(this);
        }

        if (between.getBetweenExpressionEnd() != null) {
            between.getBetweenExpressionEnd().accept(this);
        }
    }

    @Override
    public void visit(EqualsTo equalsTo) {

        visitBinaryExpression(equalsTo);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        visitBinaryExpression(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        visitBinaryExpression(greaterThanEquals);
    }

    @Override
    public void visit(InExpression inExpression) {
        if (inExpression.getLeftExpression() != null) {
            inExpression.getLeftExpression().accept(this);
        }

        // TODO Provide Visitor for Sub Select
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        if (isNullExpression.getLeftExpression() != null) {
            isNullExpression.getLeftExpression().accept(this);
        }
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        visitBinaryExpression(likeExpression);
    }

    @Override
    public void visit(MinorThan minorThan) {
        visitBinaryExpression(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        visitBinaryExpression(minorThanEquals);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        visitBinaryExpression(notEqualsTo);
    }

    @Override
    public void visit(Column column) {
        collectWhereColumnConfigurations(column);
    }

    private void collectWhereColumnConfigurations(Column column) {
        FieldConfig fieldConfig = configurationExplorer.getFieldConfig(SqlQueryModifier.getDOTypeName(plainSelect, column, false),
                column.getColumnName());
        whereColumnToConfigMapping.put(column.getColumnName().toLowerCase(), fieldConfig);
    }

    @Override
    public void visit(SubSelect subSelect) {
        visitSubSelect(subSelect);
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        if (caseExpression.getElseExpression() != null) {
            caseExpression.getElseExpression().accept(this);
        }

        if (caseExpression.getSwitchExpression() != null) {
            caseExpression.getSwitchExpression().accept(this);
        }

        if (caseExpression.getWhenClauses() != null) {
            for (Expression expression : caseExpression.getWhenClauses()) {
                expression.accept(this);
            }
        }
    }

    @Override
    public void visit(WhenClause whenClause) {
        if (whenClause.getThenExpression() != null) {
            whenClause.getThenExpression().accept(this);
        }

        if (whenClause.getWhenExpression() != null) {
            whenClause.getWhenExpression().accept(this);
        }
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        if (existsExpression.getRightExpression() != null) {
            existsExpression.getRightExpression().accept(this);
        }
    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {
        if (allComparisonExpression.getSubSelect() != null) {
            visitSubSelect(allComparisonExpression.getSubSelect());
        }
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        if (anyComparisonExpression.getSubSelect() != null) {
            visitSubSelect(anyComparisonExpression.getSubSelect());
        }
    }

    @Override
    public void visit(Concat concat) {
        visitBinaryExpression(concat);
    }

    @Override
    public void visit(Matches matches) {
        visitBinaryExpression(matches);
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        visitBinaryExpression(bitwiseAnd);
    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
        visitBinaryExpression(bitwiseOr);
    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
        visitBinaryExpression(bitwiseXor);
    }

    @Override
    public void visit(CastExpression castExpression) {
        if (castExpression.getLeftExpression() != null) {
            castExpression.getLeftExpression().accept(this);
        }
    }

    @Override
    public void visit(Modulo modulo) {
        visitBinaryExpression(modulo);
    }

    @Override
    public void visit(AnalyticExpression analyticExpression) {
        if (analyticExpression.getDefaultValue() != null) {
            analyticExpression.getDefaultValue().accept(this);
        }

        if (analyticExpression.getExpression() != null) {
            analyticExpression.getDefaultValue().accept(this);
        }

        if (analyticExpression.getOffset() != null) {
            analyticExpression.getOffset().accept(this);
        }

        if (analyticExpression.getOrderByElements() != null) {
            for (OrderByElement orderByElement : analyticExpression.getOrderByElements()) {
                if (orderByElement.getExpression() != null) {
                    orderByElement.getExpression().accept(this);
                }
            }
        }

        if (analyticExpression.getPartitionByColumns() != null) {
            for (Column column : analyticExpression.getPartitionByColumns()) {
                column.accept(this);
            }
        }
    }

    @Override
    public void visit(ExtractExpression extractExpression) {
        if (extractExpression.getExpression() != null) {
            extractExpression.getExpression().accept(this);
        }
    }

    @Override
    public void visit(IntervalExpression intervalExpression) {
    }

    protected void visitBinaryExpression(BinaryExpression binaryExpression) {
        if (binaryExpression.getLeftExpression() != null) {
            binaryExpression.getLeftExpression().accept(this);
        }

        if (binaryExpression.getRightExpression() != null) {
            binaryExpression.getRightExpression().accept(this);
        }
    }

    protected void visitSubSelect(SubSelect subSelect) {
        if (subSelect.getPivot() != null) {
            subSelect.getPivot().accept(new WrapAndLowerCasePivotVisitor());
        }

        if (subSelect.getSelectBody() != null) {
            subSelect.getSelectBody().accept(new WrapAndLowerCaseSelectVisitor());
        }
    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {
        if (oexpr != null) {
            oexpr.accept(this);
        }

    }

}
