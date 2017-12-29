package ru.intertrust.cm.core.dao.impl.sqlparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.select.*;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.impl.DomainObjectQueryHelper;
import ru.intertrust.cm.core.dao.impl.access.AccessControlUtility;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;

/**
 * Добавляет проверки прав доступа (ACL проверки) в SQL запросы коллекций.
 * Заменяет названия таблиц (доменных объектов) на подзапрос с проверкой прав
 * доступа. Например, employee -> (select * from employee where exists(...))
 * @author atsvetkov
 */
public class AddAclVisitor extends StatementVisitorAdapter implements StatementVisitor, SelectVisitor, SelectItemVisitor {

    private static Map<String, SelectBody> aclSelectBodyCache = new ConcurrentHashMap<>();

    private static WithItem aclWithItem = null;

    /**
     * Метод для тестов, используется для "стерилизации" контекста.
     */
    protected static void clearStaticFields() {
        aclSelectBodyCache = new ConcurrentHashMap<>();
        aclWithItem = null;
    }

    private ConfigurationExplorer configurationExplorer;
    private UserGroupGlobalCache userGroupCache;

    private CurrentUserAccessor currentUserAccessor;
    private DomainObjectQueryHelper domainObjectQueryHelper;

    private Select select = null;

    private boolean aclWithItemAdded = false;

    private RecursiveExpressionVisitor recursiveExpressionVisitor;

    private HashMap<PlainSelect, List<List<FromItemAccessor>>> tableGroups = new HashMap<PlainSelect, List<List<FromItemAccessor>>>();

    private SharedPermissionsChecker sharedPermissionsChecker;

    public AddAclVisitor(ConfigurationExplorer configurationExplorer, UserGroupGlobalCache userGroupCache,
            CurrentUserAccessor currentUserAccessor, DomainObjectQueryHelper domainObjectQueryHelper) {
        this.configurationExplorer = configurationExplorer;
        this.userGroupCache = userGroupCache;
        this.currentUserAccessor = currentUserAccessor;
        this.domainObjectQueryHelper = domainObjectQueryHelper;
        recursiveExpressionVisitor = new RecursiveExpressionVisitor(this);
        sharedPermissionsChecker = new SharedPermissionsChecker(configurationExplorer);
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
            select.getWithItemsList().add(0, aclWithItem);
            aclWithItemAdded = true;
        }

        return aclSubSelect;
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

    private boolean isAdministratorWithAllPermissions(Id personId, String domainObjectType) {
        return AccessControlUtility.isAdministratorWithAllPermissions(personId, domainObjectType, userGroupCache, configurationExplorer);
    }

    private boolean needToAddAclSubQuery(Table table) {
        Id personId = currentUserAccessor.getCurrentUserId();
        boolean isAdministratorWithAllPermissions = isAdministratorWithAllPermissions(personId, table.getName());
        if (isAdministratorWithAllPermissions) {
            return false;
        }
        // если ДО нет в конфигурации, значит это системный ДО и для него
        // проверка ACL не нужна.
        boolean isDomainObject = configurationExplorer.getConfig(DomainObjectTypeConfig.class, DaoUtils.unwrap(table.getName())) != null;
        return !configurationExplorer.isReadPermittedToEverybody(DaoUtils.unwrap(table.getName())) && isDomainObject;
    }

    private void processFromItem(PlainSelect plainSelect) {
        FromItem from = plainSelect.getFromItem();
        if (from instanceof Table) {
            Table table = (Table) from;
            // добавляем подзапрос на права в случае если не стоит флаг
            // read-everybody
            if (needToAddAclSubQuery(table)) {
                // plainSelect.setFromItem(createAclSubSelect(table));
                addTableToTableGroup(new FromItemAccessor(plainSelect), plainSelect);
            }
        } else {
            if (from != null) {
                from.accept(recursiveExpressionVisitor);
            }
        }
    }

    private void processJoins(PlainSelect plainSelect) {
        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                FromItem joinItem = join.getRightItem();
                if (joinItem instanceof Table) {
                    Table table = (Table) joinItem;
                    // добавляем подзапрос на права в случае если не стоит флаг
                    // read-everybody
                    if (needToAddAclSubQuery(table)) {
                        // join.setRightItem(createAclSubSelect(table));
                        addTableToTableGroup(new FromItemAccessor(join), plainSelect);
                    }
                } else {
                    joinItem.accept(recursiveExpressionVisitor);
                }
            }
        }
    }

    private void addTableToTableGroup(FromItemAccessor accessor, PlainSelect plainSelect) {
        if (!tableGroups.containsKey(plainSelect)) {
            tableGroups.put(plainSelect, new ArrayList<List<FromItemAccessor>>());
        }
        List<List<FromItemAccessor>> contextTableGroups = tableGroups.get(plainSelect);
        for (List<FromItemAccessor> group : contextTableGroups) {
            for (FromItemAccessor a : group) {
                if (sharedPermissionsChecker.check(a, accessor)) {
                    group.add(accessor);
                    return;
                }
            }
        }
        List<FromItemAccessor> newGroup = new ArrayList<FromItemAccessor>();
        newGroup.add(accessor);
        contextTableGroups.add(newGroup);
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
            plainSelect.getWhere().accept(recursiveExpressionVisitor);
        }
    }

    @Override
    public void visit(AllColumns allColumns) {

    }

    @Override
    public void visit(AllTableColumns allTableColumns) {

    }

    @Override
    public void visit(PlainSelect plainSelect) {

        processFromItem(plainSelect);

        processJoins(plainSelect);

        processWhereClause(plainSelect);

        processSelectItems(plainSelect);
    }

    private void substituteTablesWithAclSubQueries() {
        for (List<List<FromItemAccessor>> contextGroup : tableGroups.values()) {
            for (List<FromItemAccessor> group : contextGroup) {
                FromItemAccessor accessor = group.get(0);
                accessor.setFromItem(createAclSubSelect((Table) accessor.getFromItem()));
            }
        }

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

        substituteTablesWithAclSubQueries();
        tableGroups.clear();
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        selectExpressionItem.getExpression().accept(recursiveExpressionVisitor);
    }

    @Override
    public void visit(SetOperationList setOperationList) {
        if (setOperationList.getSelects() != null) {
            for (SelectBody plainSelect : setOperationList.getSelects()) {
                visit((PlainSelect) plainSelect);
            }
        }
    }

    @Override
    public void visit(WithItem withItem) {
        if (withItem.getSelectBody() != null) {
            withItem.getSelectBody().accept(this);
        }
    }
}
