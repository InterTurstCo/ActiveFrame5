package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.SubSelect;

import static ru.intertrust.cm.core.dao.impl.PostgreSqlQueryHelper.wrap;

/**
 * Реализация ExpressionVisitor для транформации sql-запросов: приведение к нижнему регистру и заключение в кавычки
 * имен таблиц и колонок
 * User: vmatsukevich
 * Date: 12/9/13
 * Time: 4:23 PM
 */
public class WrapAndLowerCaseExpressionVisitor implements ExpressionVisitor {

    @Override
    public void visit(NullValue nullValue) {
    }

    @Override
    public void visit(Function function) {
        if (function.getParameters() != null) {
            function.getParameters().accept(new WrapAndLowerCaseItemListVisitor());
        }
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

        if (inExpression.getLeftItemsList() != null) {
            inExpression.getLeftItemsList().accept(new WrapAndLowerCaseItemListVisitor());
        }

        if (inExpression.getRightItemsList() != null) {
            inExpression.getRightItemsList().accept(new WrapAndLowerCaseItemListVisitor());
        }
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
        if (column.getColumnName() != null) {
            column.setColumnName(wrap(column.getColumnName().toLowerCase()));
        }
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

    @Override
    public void visit(OracleHierarchicalExpression oracleHierarchicalExpression) {
        if (oracleHierarchicalExpression.getConnectExpression() != null) {
            oracleHierarchicalExpression.getConnectExpression().accept(this);
        }

        if (oracleHierarchicalExpression.getStartExpression() != null) {
            oracleHierarchicalExpression.getStartExpression().accept(this);
        }
    }

    private void visitBinaryExpression(BinaryExpression binaryExpression) {
        if (binaryExpression.getLeftExpression() != null) {
            binaryExpression.getLeftExpression().accept(this);
        }

        if (binaryExpression.getRightExpression() != null) {
            binaryExpression.getRightExpression().accept(this);
        }
    }

    private void visitSubSelect(SubSelect subSelect) {
        if (subSelect.getPivot() != null) {
            subSelect.getPivot().accept(new WrapAndLowerCasePivotVisitor());
        }

        if (subSelect.getSelectBody() != null) {
            subSelect.getSelectBody().accept(new WrapAndLowerCaseSelectVisitor());
        }
    }

}
