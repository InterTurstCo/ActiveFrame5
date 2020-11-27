package ru.intertrust.cm.core.dao.impl.sqlparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;
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
public class AddAclVisitor extends BasicVisitor {

    private static Map<String, SelectBody> aclSelectBodyCache = new ConcurrentHashMap<>();

    private static WithItem aclWithItemGroups = null;
    private static WithItem aclWithItemStamps = null;

    /**
     * Очистка кэша
     */
    public static void clearCache() {
        aclSelectBodyCache = new ConcurrentHashMap<>();
        aclWithItemGroups = null;
        aclWithItemStamps = null;
    }

    private ConfigurationExplorer configurationExplorer;
    private UserGroupGlobalCache userGroupCache;

    private CurrentUserAccessor currentUserAccessor;
    private DomainObjectQueryHelper domainObjectQueryHelper;

    private Select select = null;

    private boolean aclWithItemAdded = false;
    private boolean stampWithItemAdded = false;

    private HashMap<PlainSelect, List<List<FromItemAccessor>>> tableGroups = new HashMap<PlainSelect, List<List<FromItemAccessor>>>();

    private Stack<PlainSelect> selectStack = new Stack<>();

    private SharedPermissionsChecker sharedPermissionsChecker;

    public AddAclVisitor(ConfigurationExplorer configurationExplorer, UserGroupGlobalCache userGroupCache,
            CurrentUserAccessor currentUserAccessor, DomainObjectQueryHelper domainObjectQueryHelper) {
        this.configurationExplorer = configurationExplorer;
        this.userGroupCache = userGroupCache;
        this.currentUserAccessor = currentUserAccessor;
        this.domainObjectQueryHelper = domainObjectQueryHelper;
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
            List<WithItem> list = select.getWithItemsList();
            list.add(0, aclWithItemGroups);
            if (list.size() > 1 && list.get(1).isRecursive()) {
                list.get(0).setRecursive(true);
                list.get(1).setRecursive(false);
            }
            aclWithItemAdded = true;
        }

        if (!stampWithItemAdded && aclWithItemStamps != null){
            List<WithItem> list = select.getWithItemsList();
            list.add(0, aclWithItemStamps);
            if (list.size() > 1 && list.get(1).isRecursive()) {
                list.get(0).setRecursive(true);
                list.get(1).setRecursive(false);
            }
            stampWithItemAdded = true;
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

            if (aclWithItemGroups == null) {
                aclWithItemGroups = aclSelect.getWithItemsList().get(0);
            }
            if (aclWithItemStamps == null && aclSelect.getWithItemsList().size() > 1) {
                aclWithItemStamps = aclSelect.getWithItemsList().get(1);
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
        
        // Проверка что это audit_log
        if(configurationExplorer.isAuditLogType(DaoUtils.unwrap(table.getName()))) {
            // Даем доступ дополнительно роли InfoSecAuditor
            if (userGroupCache.isInfoSecAuditor(personId)) {
                return false;
            }            
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
                addTableToTableGroup(new FromItemAccessor(plainSelect), plainSelect);
            }
        } else {
            if (from != null) {
                from.accept(this);
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
                        addTableToTableGroup(new FromItemAccessor(join), plainSelect);
                    }
                } else {
                    joinItem.accept(this);
                }
            }
        }
    }

    private void addTableToTableGroup(FromItemAccessor accessor, PlainSelect plainSelect) {
        for (PlainSelect ps : selectStack) {
            List<List<FromItemAccessor>> contextTableGroups = tableGroups.get(ps);
            for (List<FromItemAccessor> group : contextTableGroups) {
                for (FromItemAccessor a : group) {
                    if (sharedPermissionsChecker.check(a, accessor)) {
                        group.add(accessor);
                        return;
                    }
                }
            }
        }
        List<List<FromItemAccessor>> contextTableGroups = tableGroups.get(plainSelect);
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
            plainSelect.getWhere().accept(this);
        }
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        if (!tableGroups.containsKey(plainSelect)) {
            tableGroups.put(plainSelect, new ArrayList<List<FromItemAccessor>>());
        }

        processFromItem(plainSelect);

        processWhereClause(plainSelect);

        selectStack.push(plainSelect);

        processJoins(plainSelect);

        processSelectItems(plainSelect);

        selectStack.pop();
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
}
