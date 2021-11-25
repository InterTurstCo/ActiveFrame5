package ru.intertrust.cm.core.dao.impl.sqlparser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.atomic.AtomicReference;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
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

    private static final Map<String, SelectBody> aclSelectBodyCache = new ConcurrentHashMap<>();
    private static final AtomicReference<WithItem> aclWithItemGroups = new AtomicReference<>();
    protected static final AtomicReference<WithItem> aclWithItemStamps = new AtomicReference<>();

    protected static final WithItem DUMMY = new DummyWithItem();
    static {
        DUMMY.setName("DUMMY");
    }

    /**
     * Очистка кэша
     */
    public static void clearCache() {
        aclSelectBodyCache.clear();
        aclWithItemGroups.set(null);
        aclWithItemStamps.set(null);
    }

    private final ConfigurationExplorer configurationExplorer;
    private final UserGroupGlobalCache userGroupCache;

    private final CurrentUserAccessor currentUserAccessor;
    protected final DomainObjectQueryHelper domainObjectQueryHelper;

    protected Select select = null;

    private boolean aclWithItemAdded = false;
    private boolean stampWithItemAdded = false;

    private final HashMap<PlainSelect, List<List<FromItemAccessor>>> tableGroups = new HashMap<PlainSelect, List<List<FromItemAccessor>>>();

    private final Stack<PlainSelect> selectStack = new Stack<>();

    private final SharedPermissionsChecker sharedPermissionsChecker;

    // При чтении данных из кэша, сохраняем полученные значения в локальные переменные
    protected SelectBody selectBodyLocalCache;
    protected WithItem aclWithItemLocalCache;
    protected WithItem stampWithItemLocalCache;

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

        addAclWithItem();

        addStampWithItem();

        return aclSubSelect;
    }

    protected void addStampWithItem() {
        // stampWithItemLocalCache не должен быть NULL, он будет DUMMY
        if (!stampWithItemAdded && stampWithItemLocalCache != DUMMY){
            addWithItem(stampWithItemLocalCache);
            stampWithItemAdded = true;
        }
    }

    /**
     * Этот метод изменяет withItem, который сюда передан.
     * Важно, чтобы withItem не был взят из кэша. В этом случае необходимо сделать его копию!
     */
    protected void addWithItem(WithItem withItem) {
        List<WithItem> list = select.getWithItemsList();
        list.add(0, withItem);
        if (list.size() > 1 && list.get(1).isRecursive()) {
            list.get(0).setRecursive(true);
            list.get(1).setRecursive(false);
        }
    }

    protected void addAclWithItem() {
        if (!aclWithItemAdded) {
            addWithItem(aclWithItemLocalCache);
            aclWithItemAdded = true;
        }
    }

    protected WithItem copyWithItem(WithItem withItem) {
        if (withItem == null) {
            return null;
        }

        WithItem tmp = new WithItem();
        tmp.setSelectBody(withItem.getSelectBody());
        tmp.setWithItemList(withItem.getWithItemList());
        tmp.setName(withItem.getName());
        tmp.setRecursive(withItem.isRecursive());
        return tmp;
    }

    private SelectBody getAclSelectBody(Table table) {
        String tableName = DaoUtils.unwrap(table.getName());

        readCacheToLocal(tableName);

        if (isCacheFilled()) {
            return getSelectBodyFromCache();
        } else {
            StringBuilder aclQuery = new StringBuilder();
            aclQuery.append("select ").append(tableName).append(".* from ").
                    append(DaoUtils.wrap(tableName)).append(" ").append(tableName).
                    append(" where 1=1");

            domainObjectQueryHelper.appendAccessControlLogicToQuery(aclQuery, tableName);

            SqlQueryParser aclSqlParser = new SqlQueryParser(aclQuery.toString());
            Select aclSelect = aclSqlParser.getSelectStatement();

            cacheAclSelect(tableName, aclSelect);

            return aclSelect.getSelectBody();
        }
    }

    protected SelectBody getSelectBodyFromCache() {
        return selectBodyLocalCache;
    }

    protected boolean isCacheFilled() {
        return selectBodyLocalCache != null && aclWithItemLocalCache != null && stampWithItemLocalCache != null;
    }

    protected void readCacheToLocal(String tableName) {
        // Тут копия, судя по всему, не нужна
        selectBodyLocalCache = aclSelectBodyCache.get(tableName);

        aclWithItemLocalCache = copyWithItem(aclWithItemGroups.get());

        stampWithItemLocalCache = AddAclVisitor.aclWithItemStamps.get();
        if (stampWithItemLocalCache != null) {
            // DUMMY нельзя копировать, он должен всегда оставаться тем же объектом
            if (stampWithItemLocalCache != DUMMY) {
                stampWithItemLocalCache = copyWithItem(stampWithItemLocalCache);
            }
        }
    }

    protected void cacheAclSelect(String tableName, Select aclSelect) {
        aclWithItemLocalCache = domainObjectQueryHelper.getAclWithItem(aclSelect);
        aclWithItemGroups.compareAndSet(null, copyWithItem(aclWithItemLocalCache));

        selectBodyLocalCache = aclSelect.getSelectBody();
        aclSelectBodyCache.putIfAbsent(tableName, selectBodyLocalCache);

        addStampCache(aclSelect);
    }

    protected void addStampCache(Select aclSelect) {
        final Optional<WithItem> stampsWithItem = domainObjectQueryHelper.getStampWithItem(aclSelect);
        stampWithItemLocalCache = stampsWithItem.map(this::copyWithItem).orElse(DUMMY);

        if (stampWithItemLocalCache != null && stampWithItemLocalCache != DUMMY) {
            // Перезапись из нескольких потоков не приведет к проблемам, т.к. в этой реализации
            // будет всегда одно и то же значение. Чтобы не делать какую-то логику на 2 CAS
            AddAclVisitor.aclWithItemStamps.set(stampWithItemLocalCache);
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
        List<FromItemAccessor> newGroup = new ArrayList<>();
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
            tableGroups.put(plainSelect, new ArrayList<>());
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
            select.setWithItemsList(new ArrayList<>());
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

    private static class DummyWithItem extends WithItem {
        @Override
        public String getName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setName(String name) {
            if ("DUMMY".equals(name)) {
                super.setName(name);
                return;
            }

            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isRecursive() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setRecursive(boolean recursive) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SelectBody getSelectBody() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSelectBody(SelectBody selectBody) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<SelectItem> getWithItemList() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setWithItemList(List<SelectItem> withItemList) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void accept(SelectVisitor visitor) {
            throw new UnsupportedOperationException();
        }

        @Override
        public WithItem withName(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public WithItem withWithItemList(List<SelectItem> withItemList) {
            throw new UnsupportedOperationException();
        }

        @Override
        public WithItem withSelectBody(SelectBody selectBody) {
            throw new UnsupportedOperationException();
        }

        @Override
        public WithItem withRecursive(boolean recursive) {
            throw new UnsupportedOperationException();
        }

        @Override
        public WithItem addWithItemList(SelectItem... withItemList) {
            throw new UnsupportedOperationException();
        }

        @Override
        public WithItem addWithItemList(Collection<? extends SelectItem> withItemList) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <E extends SelectBody> E getSelectBody(Class<E> type) {
            throw new UnsupportedOperationException();
        }
    }
}
