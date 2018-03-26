package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import ru.intertrust.cm.core.config.AccessMatrixConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;

public class SharedPermissionsChecker {

    private class JoinedByIdChecker extends ExpressionVisitorAdapter {

        private String first;
        private String second;
        private String firstFieldName;
        private String secondFieldName;
        private boolean result;

        public JoinedByIdChecker(String first, String second, String firstFieldname, String secondFieldName) {
            this.first = first;
            this.second = second;
            this.firstFieldName = firstFieldname;
            this.secondFieldName = secondFieldName;
        }

        public boolean getResult() {
            return result;
        }

        @Override
        public void visit(EqualsTo expr) {
            if (expr.getLeftExpression() instanceof Column && expr.getRightExpression() instanceof Column) {
                Column left = (Column) expr.getLeftExpression();
                Column right = (Column) expr.getRightExpression();
                if (checkColumn(left, first, firstFieldName) && checkColumn(right, second, secondFieldName)
                        || checkColumn(right, first, firstFieldName) && checkColumn(left, second, secondFieldName)) {
                    result = true;
                }
            }
        }

        private boolean checkColumn(Column column, String table, String field) {
            return column.getColumnName().equalsIgnoreCase(field)
                    && (column.getTable() == null || column.getTable().getName() == null || column.getTable().getName().equalsIgnoreCase(table));
        }

        @Override
        protected void visitBinaryExpression(BinaryExpression expr) {
            if (expr instanceof AndExpression) {
                AndExpression andExpression = (AndExpression) expr;
                JoinedByIdChecker left = new JoinedByIdChecker(first, second, firstFieldName, secondFieldName);
                JoinedByIdChecker right = new JoinedByIdChecker(first, second, firstFieldName, secondFieldName);
                andExpression.getLeftExpression().accept(left);
                andExpression.getRightExpression().accept(right);
                if (left.getResult() || right.getResult()) {
                    result = true;
                }
            }
        }

    }

    private ConfigurationExplorer configurationExplorer;

    public SharedPermissionsChecker(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    public boolean check(FromItemAccessor first, FromItemAccessor second) {
        if (first.getFromItem() instanceof Table && second.getFromItem() instanceof Table) {
            Table firstTable = ((Table) first.getFromItem());
            Table secondTable = (Table) second.getFromItem();
            String firstType = firstTable.getName();
            String secondType = secondTable.getName();
            if (isType(firstType) && isType(secondType)) {
                if (areRelated(firstType, secondType)) {
                    return check(second, firstTable, secondTable, "id", "id");
                } else {
                    if (oneReferencesAnother(firstType, secondType)) {
                        return check(second, firstTable, secondTable, getReferenceField(firstType), "id");
                    } else if (oneReferencesAnother(secondType, firstType)) {
                        return check(second, firstTable, secondTable, "id", getReferenceField(secondType));
                    } else {
                        return false;
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean oneReferencesAnother(String one, String another) {
        String matrixReferenceField = getReferenceField(one);
        if (matrixReferenceField != null) {
            String referencedType = ((ReferenceFieldConfig) configurationExplorer.getFieldConfig(one, matrixReferenceField)).getType();
            return areRelated(referencedType, another);
        } else {
            return false;
        }
    }

    private String getReferenceField(String type) {
        AccessMatrixConfig accessMatrix = configurationExplorer.getAccessMatrixByObjectType(type);
        return accessMatrix == null ? null : accessMatrix.getMatrixReference();
    }

    private boolean check(FromItemAccessor second, Table firstTable, Table secondTable, String firstFieldName, String secondFieldName) {
        return second.isNaturalJoin() || joinedById(firstTable, secondTable, second.getCondition(), firstFieldName, secondFieldName);
    }

    private boolean isType(String name) {
        return configurationExplorer.getConfig(DomainObjectTypeConfig.class, name) != null;
    }

    private boolean joinedById(Table first, Table second, Expression condition, String firstFieldName, String secondFieldName) {
        if (condition != null) {
            JoinedByIdChecker checker = new JoinedByIdChecker(getNameOrAlias(first), getNameOrAlias(second), firstFieldName, secondFieldName);
            condition.accept(checker);
            return checker.getResult();
        } else {
            return false;
        }
    }

    private String getNameOrAlias(Table table) {
        if (table.getAlias() != null) {
            return table.getAlias().getName();
        } else {
            return table.getName();
        }
    }

    private boolean areRelated(String first, String second) {
        String firstRoot = configurationExplorer.getDomainObjectRootType(first);
        String secondRoot = configurationExplorer.getDomainObjectRootType(second);
        return firstRoot.equalsIgnoreCase(secondRoot);
    }

}
