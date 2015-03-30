
package ru.intertrust.cm.core.dao.impl.sqlparser;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getALTableSqlName;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.jsqlparser.expression.Alias;
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
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.SignedExpression;
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
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.impl.access.AccessControlUtility;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;

/**
 * Добавляет проверки прав доступа (ACL проверки) в SQL запросы коллекций. Заменяет названия таблиц (доменных объектов)
 * на подзапрос с проверкой прав доступа. 
 * Например, employee -> (select * from employee where exists(...))
 * @author atsvetkov
 */
public class AddAclVisitor implements SelectVisitor, FromItemVisitor, ExpressionVisitor, ItemsListVisitor, SelectItemVisitor {

    private static Map<String, String> aclSubQueryCache = new HashMap<>();

    private ConfigurationExplorer configurationExplorer;

    private UserGroupGlobalCache userGroupCache;
    private CurrentUserAccessor currentUserAccessor;

    public AddAclVisitor(ConfigurationExplorer configurationExplorer, UserGroupGlobalCache userGroupCache, CurrentUserAccessor currentUserAccessor) {
        this.configurationExplorer = configurationExplorer;
        this.userGroupCache = userGroupCache;
        this.currentUserAccessor = currentUserAccessor;
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        processFromItem(plainSelect);

        processJoins(plainSelect);

        processWhereClause(plainSelect);

        processSelectItems(plainSelect);
    }

    private void processSelectItems(PlainSelect plainSelect) {
        for (Object item : plainSelect.getSelectItems()) {
            if (item instanceof SelectExpressionItem) {
                SelectExpressionItem selectExpressionItem = (SelectExpressionItem) item;
                selectExpressionItem.accept(this);
            }
        }
    }

    private void processWhereClause(PlainSelect plainSelect) {
        if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(this);
        }
    }

    private void processJoins(PlainSelect plainSelect) {
        if (plainSelect.getJoins() != null) {
            for (Iterator joinsIt = plainSelect.getJoins().iterator(); joinsIt.hasNext();) {
                Join join = (Join) joinsIt.next();
                FromItem joinItem = join.getRightItem();
                if (joinItem instanceof Table) {
                    Table table = (Table) joinItem;
                    //добавляем подзапрос на права в случае если не стоит флаг read-everybody 
                    if (needToAddAclSubQuery(table)) {
                        SubSelect replace = createAclSubQuery(table.getName());
                        if (table.getAlias() == null) {
                            replace.setAlias(new Alias(table.getName(), false));
                        } else {
                            replace.setAlias(table.getAlias());
                        }
                        join.setRightItem(replace);
                    }
                } else {
                    join.getRightItem().accept(this);
                }
            }
        }
    }

    private boolean needToAddAclSubQuery(Table table) {
        Id personId = currentUserAccessor.getCurrentUserId();
        boolean isAdministratorWithAllPermissions = isAdministratorWithAllPermissions(personId, table.getName());
        if (isAdministratorWithAllPermissions) {
            return false;
        }
        // если ДО нет в конфигурации, значит это системный ДО и для него проверка ACL не нужна.
        boolean isDomainObject = configurationExplorer.getConfig(DomainObjectTypeConfig.class, DaoUtils.unwrap(table.getName())) != null;
        return !configurationExplorer.isReadPermittedToEverybody(DaoUtils.unwrap(table.getName())) && isDomainObject;
    }

    private boolean isAdministratorWithAllPermissions(Id personId, String domainObjectType) {
        return AccessControlUtility.isAdministratorWithAllPermissions(personId, domainObjectType, userGroupCache, configurationExplorer);
    }

    private void processFromItem(PlainSelect plainSelect) {
        FromItem from = plainSelect.getFromItem();
        if (from instanceof Table) {
            Table table = (Table) from;
            //добавляем подзапрос на права в случае если не стоит флаг read-everybody 
            if (needToAddAclSubQuery(table)) {
                SubSelect replace = createAclSubQuery(table.getName());
                if (table.getAlias() == null) {
                    replace.setAlias(new Alias(table.getName(), false));
                } else {
                    replace.setAlias(table.getAlias());
                }
                plainSelect.setFromItem(replace);
            }
        } else {
            plainSelect.getFromItem().accept(this);
        }
    }

/*    private boolean isDomainObjectType(String tableName) {
        return configurationExplorer.getConfig(DomainObjectTypeConfig.class, tableName) != null;
    }
*/
    /**
     * Выполняет замену названия таблицы на ACL подзапрос.
     * @param domainObjectType
     * @return
     */
    private SubSelect createAclSubQuery(String domainObjectType) {
        domainObjectType = DaoUtils.unwrap(domainObjectType);        

        boolean isAuditLog = configurationExplorer.isAuditLogType(domainObjectType);
        String originalDomainObjectType = domainObjectType;

        String aclQueryString = null;
        if (aclSubQueryCache.get(originalDomainObjectType) != null) {
            aclQueryString = aclSubQueryCache.get(originalDomainObjectType);
        } else {
            
            // Проверка прав для аудит лог объектов выполняются от имени родительского объекта.
            domainObjectType = AccessControlUtility.getRelevantType(domainObjectType, configurationExplorer);
            
            //В случае заимствованных прав формируем запрос с "чужой" таблицей xxx_read
            String matrixReferenceTypeName = configurationExplorer.getMatrixReferenceTypeName(domainObjectType);
            String aclReadTable = null;
            if (matrixReferenceTypeName != null){
                aclReadTable = AccessControlUtility.getAclReadTableNameFor(configurationExplorer, matrixReferenceTypeName);
            }else{
                aclReadTable = AccessControlUtility.getAclReadTableNameFor(configurationExplorer, domainObjectType);
            }
                        
            String topLevelParentType = configurationExplorer.getDomainObjectRootType(domainObjectType).toLowerCase();
            String topLevelAuditTable = getALTableSqlName(topLevelParentType);
           
            StringBuilder aclQuery = new StringBuilder();
            
            aclQuery.append("Select ").append(originalDomainObjectType).append(".* from ").
                    append(DaoUtils.wrap(originalDomainObjectType)).append(" ").append(originalDomainObjectType);

            if (isAuditLog) {
                aclQuery.append(" inner join ").append(wrap(topLevelAuditTable)).append(" pal on ").append(originalDomainObjectType).append(".")
                        .append(wrap(Configuration.ID_COLUMN)).append(" = pal.").append(wrap(Configuration.ID_COLUMN));
            }

            if (!topLevelParentType.equalsIgnoreCase(originalDomainObjectType)) {
                aclQuery.append(" inner join ").append(DaoUtils.wrap(topLevelParentType)).append(" rt on rt.").
                        append(wrap(Configuration.ID_COLUMN)).append(" = ");
                if (isAuditLog) {
                    aclQuery.append("pal.").append(DaoUtils.wrap(Configuration.DOMAIN_OBJECT_ID_COLUMN));
                } else {
                    aclQuery.append(originalDomainObjectType).append(".").append(wrap(Configuration.ID_COLUMN));
                }
            }

            aclQuery.append(" where exists (select r.").append(DaoUtils.wrap("object_id"))
                    .append(" from ")
                    .append(DaoUtils.wrap(aclReadTable)).append(" r ");
            aclQuery.append(" inner join ").append(DaoUtils.wrap("group_group")).append(" gg on r.")
                    .append(DaoUtils.wrap("group_id") + " = gg.").append(DaoUtils.wrap("parent_group_id"));
            aclQuery.append(" inner join ").append(DaoUtils.wrap("group_member")).append(" gm on gg.")
                    .append(DaoUtils.wrap("child_group_id"))
                    .append(" = gm.").append(DaoUtils.wrap("usergroup"));

            aclQuery.append(" where gm.\"person_id\" = ").append(SqlQueryModifier.USER_ID_PARAM).
                     append(" and r.").append(wrap("object_id")).append(" = ");

            if (topLevelParentType.equalsIgnoreCase(originalDomainObjectType)) {
                aclQuery.append(originalDomainObjectType).append(".").append(wrap("access_object_id"));
            } else {
                aclQuery.append("rt.").append(wrap("access_object_id"));
            }

            aclQuery.append(")");

            aclQueryString = aclQuery.toString();
            aclSubQueryCache.put(originalDomainObjectType, aclQueryString);
        }

        SubSelect subSelectWithAcl = new SubSelect();
        SqlQueryParser aclSqlParser = new SqlQueryParser(aclQueryString);
        PlainSelect aclEnforcedExpression = (PlainSelect) aclSqlParser.getSelectBody();
        subSelectWithAcl.setSelectBody(aclEnforcedExpression);
        return subSelectWithAcl;
    }

    public void visitBinaryExpression(BinaryExpression binaryExpression) {
        binaryExpression.getLeftExpression().accept(this);
        binaryExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(AllColumns allColumns) {

    }

    @Override
    public void visit(AllTableColumns allTableColumns) {

    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        selectExpressionItem.getExpression().accept(this);

    }

    @Override
    public void visit(ExpressionList expressionList) {
        for (Expression expression : expressionList.getExpressions()) {
            expression.accept(this);
        }
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        for (ExpressionList expressionList : multiExprList.getExprList()) {
            visit(expressionList);
        }

    }

    @Override
    public void visit(NullValue nullValue) {

    }

    @Override
    public void visit(Function function) {

    }

    @Override
    public void visit(SignedExpression signedExpression) {

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
        between.getLeftExpression().accept(this);
        between.getBetweenExpressionStart().accept(this);
        between.getBetweenExpressionEnd().accept(this);
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
        inExpression.getLeftExpression().accept(this);
        inExpression.getRightItemsList().accept(this);

    }

    @Override
    public void visit(IsNullExpression isNullExpression) {

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
    public void visit(Column tableColumn) {

    }

    @Override
    public void visit(CaseExpression caseExpression) {

    }

    @Override
    public void visit(WhenClause whenClause) {

    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        existsExpression.getRightExpression().accept(this);

    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {
        allComparisonExpression.getSubSelect().getSelectBody().accept(this);

    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        anyComparisonExpression.getSubSelect().getSelectBody().accept(this);

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
    public void visit(CastExpression cast) {
        if(cast.getLeftExpression() != null){
            cast.getLeftExpression().accept(this);
            
        }
    }

    @Override
    public void visit(Modulo modulo) {
    }

    @Override
    public void visit(AnalyticExpression aexpr) {
        if (aexpr.getExpression() != null) {
            aexpr.getExpression().accept(this);
        }

    }

    @Override
    public void visit(ExtractExpression eexpr) {
        if (eexpr.getExpression() != null) {
            eexpr.getExpression().accept(this);
        }
    }

    @Override
    public void visit(IntervalExpression iexpr) {
        //Skip, as interval can not contain any sub-query
    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {

    }

    @Override
    public void visit(RegExpMatchOperator regExpMatchOperator) {

    }

    @Override
    public void visit(Table tableName) {

    }

    @Override
    public void visit(SubSelect subSelect) {
        subSelect.getSelectBody().accept(this);
    }

    @Override
    public void visit(SubJoin subjoin) {
        subjoin.getLeft().accept(this);
        subjoin.getJoin().getRightItem().accept(this);
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        lateralSubSelect.accept(this);

    }

    @Override
    public void visit(ValuesList valuesList) {
        if (valuesList.getMultiExpressionList() != null) {
            visit(valuesList.getMultiExpressionList());
        }

    }

    @Override
    public void visit(SetOperationList setOperationList) {
        if (setOperationList.getPlainSelects() != null) {
            for (PlainSelect plainSelect : setOperationList.getPlainSelects()) {
                visit(plainSelect);
            }
        }
    }

    @Override
    public void visit(WithItem withItem) {
    }

}
