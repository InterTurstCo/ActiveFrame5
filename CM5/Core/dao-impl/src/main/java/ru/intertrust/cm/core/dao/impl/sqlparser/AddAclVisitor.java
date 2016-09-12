
package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.impl.DomainObjectQueryHelper;
import ru.intertrust.cm.core.dao.impl.access.AccessControlUtility;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Добавляет проверки прав доступа (ACL проверки) в SQL запросы коллекций. Заменяет названия таблиц (доменных объектов)
 * на подзапрос с проверкой прав доступа. 
 * Например, employee -> (select * from employee where exists(...))
 * @author atsvetkov
 */
public class AddAclVisitor implements StatementVisitor, SelectVisitor, FromItemVisitor, ExpressionVisitor, ItemsListVisitor, SelectItemVisitor {

    private static Map<String, SelectBody> aclSelectBodyCache = new ConcurrentHashMap<>();
    private static WithItem aclWithItem = null;

    private ConfigurationExplorer configurationExplorer;

    private UserGroupGlobalCache userGroupCache;
    private CurrentUserAccessor currentUserAccessor;
    private DomainObjectQueryHelper domainObjectQueryHelper;

    private Select select = null;
    private boolean aclWithItemAdded = false;

    public AddAclVisitor(ConfigurationExplorer configurationExplorer, UserGroupGlobalCache userGroupCache,
                         CurrentUserAccessor currentUserAccessor, DomainObjectQueryHelper domainObjectQueryHelper) {
        this.configurationExplorer = configurationExplorer;
        this.userGroupCache = userGroupCache;
        this.currentUserAccessor = currentUserAccessor;
        this.domainObjectQueryHelper = domainObjectQueryHelper;
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
            for (Join join : plainSelect.getJoins()) {
                FromItem joinItem = join.getRightItem();
                if (joinItem instanceof Table) {
                    Table table = (Table) joinItem;
                    //добавляем подзапрос на права в случае если не стоит флаг read-everybody 
                    if (needToAddAclSubQuery(table)) {
                        join.setRightItem(createAclSubSelect(table));
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
                plainSelect.setFromItem(createAclSubSelect(table));
            }
        } else {
            plainSelect.getFromItem().accept(this);
        }
    }

    private SelectBody getAclSelectBody(Table table) {
        String tableName = DaoUtils.unwrap(table.getName());

        if (aclSelectBodyCache.get(tableName) != null) {
            return aclSelectBodyCache.get(tableName);
        } else {
            StringBuilder aclQuery = new StringBuilder();
            aclQuery.append("select ").append(tableName).append(".* from ").
                    append(DaoUtils.wrap(tableName)).append(" ").append(tableName).
                    append(" where 1=1");

            domainObjectQueryHelper.appendAccessControlLogicToQuery(aclQuery, tableName);

            SqlQueryParser aclSqlParser = new SqlQueryParser(aclQuery.toString());
            Select aclSelect = aclSqlParser.getSelectStatement();

            if (aclWithItem == null) {
                aclWithItem = aclSelect.getWithItemsList().get(0);
            }

            aclSelectBodyCache.put(tableName, aclSelect.getSelectBody());

            return aclSelect.getSelectBody();
        }
    }

    private SubSelect createAclSubSelect(Table table) {
        SubSelect aclSubSelect = new SubSelect();
        aclSubSelect.setSelectBody(getAclSelectBody(table));

        if (table.getAlias() == null) {
            aclSubSelect.setAlias(new Alias(table.getName(), false));
        } else {
            aclSubSelect.setAlias(table.getAlias());
        }

        if (!aclWithItemAdded) {
            select.getWithItemsList().add(aclWithItem);
            aclWithItemAdded = true;
        }

        return aclSubSelect;
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

    @Override
    public void visit(Select select) {
        this.select = select;

        if (select.getWithItemsList() == null) {
            select.setWithItemsList(new ArrayList<WithItem>());
        }

        if (select.getSelectBody() != null) {
            select.getSelectBody().accept(this);
        }

        for (WithItem withItem : select.getWithItemsList()) {
            withItem.accept(this);
        }
    }

    @Override
    public void visit(Delete delete) {

    }

    @Override
    public void visit(Update update) {

    }

    @Override
    public void visit(Insert insert) {

    }

    @Override
    public void visit(Replace replace) {

    }

    @Override
    public void visit(Drop drop) {

    }

    @Override
    public void visit(Truncate truncate) {

    }

    @Override
    public void visit(CreateIndex createIndex) {

    }

    @Override
    public void visit(CreateTable createTable) {

    }

    @Override
    public void visit(CreateView createView) {

    }

    @Override
    public void visit(Alter alter) {

    }

    @Override
    public void visit(Statements statements) {

    }
}
