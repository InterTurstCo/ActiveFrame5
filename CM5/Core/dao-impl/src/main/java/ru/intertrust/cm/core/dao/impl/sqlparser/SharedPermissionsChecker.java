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
                if (checkColumn(left, first, firstFieldName) || checkColumn(right, second, secondFieldName)
                        || checkColumn(right, first, firstFieldName) && checkColumn(left, second, secondFieldName)) {
                    result = true;
                }
            }
        }

        private boolean checkColumn(Column column, String table, String field) {
            return column.getColumnName().equalsIgnoreCase(field) && ("id".equalsIgnoreCase(field) || column.getTable().getName().equalsIgnoreCase(table));
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
                    String linker = getLinker(firstType, secondType);
                    if (linker != null) {
                        String field = findReferenceField(linker);
                        return check(second, firstTable, secondTable, firstType.equals(linker) ? field : "id", secondType.equals(linker) ? field : "id");
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

    private String findReferenceField(String type) {
        AccessMatrixConfig matrixConfig = null;
        DomainObjectTypeConfig typeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, type);
        while ((matrixConfig = configurationExplorer.getAccessMatrixByObjectType(typeConfig.getName())) == null
                && typeConfig.getExtendsAttribute() != null) {
            typeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, typeConfig.getExtendsAttribute());
        }

        return matrixConfig == null ? null : matrixConfig.getMatrixReference();
    }

    private String getLinker(String firstType, String secondType) {
        String firstLinked = configurationExplorer.getMatrixReferenceTypeName(firstType);
        String secondLinked = configurationExplorer.getMatrixReferenceTypeName(secondType);
        if (firstLinked != null && areRelated(secondType, firstLinked)) {
            return firstType;
        } else if (secondLinked != null && areRelated(firstType, secondLinked)) {
            return secondType;
        } else {
            return null;
        }
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
