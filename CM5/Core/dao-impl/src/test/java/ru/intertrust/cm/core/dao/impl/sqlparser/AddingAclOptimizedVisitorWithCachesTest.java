package ru.intertrust.cm.core.dao.impl.sqlparser;

import java.util.HashMap;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.RequestInfo;
import ru.intertrust.cm.core.dao.api.SecurityStamp;
import ru.intertrust.cm.core.dao.impl.DomainObjectQueryHelper;
import ru.intertrust.cm.core.dao.impl.PGSqlDomainObjectQueryHelperOptimized;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Да, с т.з. unit-тестов, это не правильный класс с тестами, т.к. все методы внутри завязаны друг на друга
 * Но такой тест позволяет выявить достаточно много проблем, по этой причине я решил его сделать.
 *
 * Внимание! Надо быть аккуратным и не допускать разные иерархии для одних и тех же таблиц
 */
public class AddingAclOptimizedVisitorWithCachesTest {

    private static final String STAMP_SUBQUERY = "WITH person_stamp_values AS (SELECT stamp FROM person_stamp WHERE person = :user_id), ";

    private static class User {
        private final Id id;
        private final String login;
        private final boolean isAdmin;
        private final boolean isSuperUser;

        public User(Id id, String login, boolean isAdmin, boolean isSuperUser) {
            this.id = id;
            this.login = login;
            this.isAdmin = isAdmin;
            this.isSuperUser = isSuperUser;
        }

        public Id getId() {
            return id;
        }

        public String getLogin() {
            return login;
        }

        public boolean isAdmin() {
            return isAdmin;
        }

        public boolean isSuperUser() {
            return isSuperUser;
        }

    }

    private static final String GROUPS_SUBQUERY = "%s_read_tmp AS (SELECT DISTINCT gg.\"parent_group_id\" FROM \"group_member\" gm INNER JOIN \"group_group\" gg ON gg.\"child_group_id\" = gm.\"usergroup\" INNER JOIN \"%s_read\" r ON r.\"group_id\" = gg.\"parent_group_id\" WHERE gm.\"person_id\" = :user_id) ";

    private final HashMap<Id, User> users = new HashMap<Id, User>();
    private final HashMap<String, User> usersByLogin = new HashMap<String, User>();

    private final FakeConfigurationExplorer configurationExplorer = new FakeConfigurationExplorer();
    private Id currentUser;
    private final CurrentUserAccessor accessor = new CurrentUserAccessor() {

        @Override
        public Id getCurrentUserId() {
            return currentUser;
        }

        @Override
        public String getCurrentUser() {
            return users.get(currentUser).getLogin();
        }

        @Override
        public void setTicket(String ticket) {
        }

        @Override
        public void cleanTicket() {
        }

        @Override
        public RequestInfo getRequestInfo() {
            return null;
        }

        @Override
        public void setRequestInfo(RequestInfo requestInfo) {
        }
    };

    private final UserGroupGlobalCache userCache = new UserGroupGlobalCache() {

        @Override
        public boolean isPersonSuperUser(Id personId) {
            return users.get(personId).isSuperUser();
        }

        @Override
        public boolean isAdministrator(Id personId) {
            return users.get(personId).isAdmin();
        }

        @Override
        public Id getUserIdByLogin(String login) {
            return usersByLogin.get(login).getId();
        }

        @Override
        public void cleanCache() {

        }

        @Override
        public boolean isInfoSecAuditor(Id personId) {
            return false;
        }
    };

    private final DomainObjectQueryHelper queryHelper = new PGSqlDomainObjectQueryHelperOptimized();
    private SecurityStamp securityStamp;

    @Before
    public void setUp() {
        queryHelper.setConfigurationExplorer(configurationExplorer);
        queryHelper.setCurrentUserAccessor(accessor);
        queryHelper.setUserGroupCache(userCache);
        securityStamp = mock(SecurityStamp.class);
        ReflectionTestUtils.setField(queryHelper, "securityStamp", securityStamp);
        addUser(new User(new RdbmsId(1, 1), "user", false, false));
    }

    @Test
    public void testNoAclForNonTypeTable() {
        AddingAclOptimizedVisitor visitor = new AddingAclOptimizedVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select id from documents2");
        Select select = parser.getSelectStatement();
        String expected = select.toString();
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testSingleType1() {
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("documents1")));
        AddingAclOptimizedVisitor visitor = new AddingAclOptimizedVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select id from documents1");
        Select select = parser.getSelectStatement();
        String expected = groupsSubquery("documents1") + "SELECT id FROM " + aclSubquery("documents1", "documents1", "documents1", null);
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }


    // TODO! Данный кейс показывает проблему при добавлении штампа. Сам тест не упадет, но из-за того, что он запишет значения в кэш, упадет тест выше.
    //  Без кэша (см. дочернюю реализацию) тесты будут проходить корректно. Это влечет только небольшую потерю в скорости исполнения запросов, заведу запрос
    @Ignore
    @Test
    public void testSingleType1_with_stamp() {
        when(securityStamp.isSupportSecurityStamp("documents3")).thenReturn(true);

        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("documents3")));
        AddAclVisitor visitor = new AddAclVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select id from documents3");
        Select select = parser.getSelectStatement();
        String expected = STAMP_SUBQUERY + groupsSubqueryWithoutWith("documents3") + "SELECT id FROM " + aclSubquery("documents3", "documents3", "documents3", null, true);
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testUsageOfParentType() {
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("base_documents")));
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("documents").parent("base_documents")));
        AddingAclOptimizedVisitor visitor = new AddingAclOptimizedVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select id from documents");
        Select select = parser.getSelectStatement();
        String expected = groupsSubquery("base_documents") + "SELECT id FROM " + aclSubquery("documents", "base_documents", "base_documents", null);
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    private String groupsSubquery(String table) {
        return "WITH " + String.format(GROUPS_SUBQUERY, table, table);
    }

    private String groupsSubqueryWithoutWith(String table) {
        return String.format(GROUPS_SUBQUERY, table, table);
    }

    @Test
    public void testUsageOfLinkedType() {
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("base_documents")));
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("linked_attribute").linkedTo("base_documents", "base_document_id")));
        AddingAclOptimizedVisitor visitor = new AddingAclOptimizedVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select string_value from linked_attribute");
        Select select = parser.getSelectStatement();
        String expected = groupsSubquery("base_documents") + "SELECT string_value FROM " + aclSubquery("linked_attribute", "base_documents", "linked_attribute", null);
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testCaseInsensitiveness() {
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("Base_Documents")));
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("Documents", false).parent("Base_Documents")));
        AddingAclOptimizedVisitor visitor = new AddingAclOptimizedVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select id from documents d join base_documents bd on bd.id = d.id");
        Select select = parser.getSelectStatement();
        String expected = groupsSubquery("base_documents")
                + "SELECT id FROM " + aclSubquery("documents", "base_documents", "base_documents", "d")
                + " JOIN base_documents bd ON bd.id = d.id";
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testSubslectWithoutFrom() {
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("Base_Documents")));
        AddingAclOptimizedVisitor visitor = new AddingAclOptimizedVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select id, (select coaleasce(bd.a, bd.b)) ab from base_documents bd");
        Select select = parser.getSelectStatement();
        String expected = groupsSubquery("base_documents")
                + select.toString().replaceFirst("base_documents bd", aclSubquery("base_documents", "base_documents", "base_documents", "bd"));
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testSelectWithWith() {
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("Base_Documents")));
        AddingAclOptimizedVisitor visitor = new AddingAclOptimizedVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("with t as(select x from y where z = 0) select id from base_documents bd where id = t.x");
        Select select = parser.getSelectStatement();

        String expected = groupsSubquery("base_documents").trim() + ", " + select.toString().replace("WITH ", "").trim()
                .replaceFirst("base_documents bd", aclSubquery("base_documents", "base_documents", "base_documents", "bd"));

        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testUsageOfLinkedTypeWithParent() {
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("base_documents")));
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("documents", false).parent("base_documents")));
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("linked_attribute").linkedTo("documents", "document_id")));
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("linked_attribute_child", false).parent("linked_attribute")));
        AddingAclOptimizedVisitor visitor = new AddingAclOptimizedVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select string_value from linked_attribute_child");
        Select select = parser.getSelectStatement();
        String expected = groupsSubquery("base_documents") + "SELECT string_value FROM " + aclSubquery("linked_attribute_child", "base_documents", "linked_attribute", null);
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testJoinOfIndependentTables() {
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("a")));
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("b")));
        AddingAclOptimizedVisitor visitor = new AddingAclOptimizedVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select id from a join b on a.x = b.x");
        Select select = parser.getSelectStatement();
        String expected = groupsSubquery("b").trim() + ", " + groupsSubqueryWithoutWith("a") + "SELECT id FROM "
                + aclSubquery("a", "a", "a", null)
                + " JOIN " + aclSubquery("b", "b", "b", null) + " ON a.x = b.x";
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testSubquery() {
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("a")));
        AddingAclOptimizedVisitor visitor = new AddingAclOptimizedVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select id, created_date, abc from (select id, created_date, s || b as abc from a) t");
        Select select = parser.getSelectStatement();
        String expected = groupsSubquery("a") + "SELECT id, created_date, abc FROM (SELECT id, created_date, s || b AS abc FROM "
                + aclSubquery("a", "a", "a", null)
                + ") t";
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testExists() {
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("a")));
        AddingAclOptimizedVisitor visitor = new AddingAclOptimizedVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select id, created_date, abc from x where exists (select id from a where x.n = a.n)");
        Select select = parser.getSelectStatement();
        String expected = groupsSubquery("a") + "SELECT id, created_date, abc FROM x WHERE EXISTS (SELECT id FROM "
                + aclSubquery("a", "a", "a", null)
                + " WHERE x.n = a.n)";
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testEliminateExcessiveAcl() {
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("base_documents")));
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("documents", false).parent("base_documents")));
        AddingAclOptimizedVisitor visitor = new AddingAclOptimizedVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select id from documents d join base_documents bd on bd.id = d.id");
        Select select = parser.getSelectStatement();
        String expected = groupsSubquery("base_documents") + "SELECT id FROM "
                + aclSubquery("documents", "base_documents", "base_documents", "d")
                + " JOIN base_documents bd ON bd.id = d.id";
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testEliminateExcessiveAclMoreComplex() {
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("base_documents")));
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("documents", false).parent("base_documents")));
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("documents_m").linkedTo("documents", "owner")));
        AddingAclOptimizedVisitor visitor = new AddingAclOptimizedVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser(
                "select id from documents d join base_documents bd on bd.id = d.id left join documents_m dm on dm.owner = d.id");
        Select select = parser.getSelectStatement();
        String expected = groupsSubquery("base_documents") + "SELECT id FROM "
                + aclSubquery("documents", "base_documents", "base_documents", "d")
                + " JOIN base_documents bd ON bd.id = d.id"
                + " LEFT JOIN documents_m dm ON dm.owner = d.id";
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testEliminateExcessiveAclEvenMoreComplex() {
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("base_documents")));
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("documents", false).parent("base_documents")));
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("base_attribute", false)));
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("attribute").parent("base_attribute").linkedTo("base_documents", "root")));
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("attribute_attribute").linkedTo("base_attribute", "root")));
        AddingAclOptimizedVisitor visitor = new AddingAclOptimizedVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser(
                "select count(*) from "
                        + "(select b.id, b.created_date, b.x from attribute a"
                        + " left join document d on d.id = a.root"
                        + " left join base_document bd on bd.id = d.id"
                        + " left join base_attribute ba on ba.id = a.id"
                        + " left join attribute_attribute aa on aa.root = a.id"
                        + " where a.t is not null and ba.x <> '1') t"
        );
        Select select = parser.getSelectStatement();
        String expected = groupsSubquery("base_documents")
                + select.toString().replaceAll("FROM attribute a", "FROM " + aclSubquery("attribute", "base_documents", "base_attribute", "a"));
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testBasicQueryStartsWithWithRecursive() {
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("base_documents")));
        AddingAclOptimizedVisitor visitor = new AddingAclOptimizedVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("with recursive t as (select id from base_documents bd) select id from t");
        Select select = parser.getSelectStatement();
        String expected = groupsSubquery("base_documents").replaceAll("WITH", "WITH RECURSIVE").replaceAll(":user_id\\) ", ":user_id\\)")
                + select.toString().replaceAll("WITH RECURSIVE", ",")
                .replaceAll("FROM base_documents bd", "FROM " + aclSubquery("base_documents", "base_documents", "base_documents", "bd"));
        select.accept(visitor);
        assertEquals(expected, select.toString());

    }

    @Test
    public void testEliminateExcessiveAclInSubquery() {
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("documents1")));
        configurationExplorer.createTypeConfig((new FakeConfigurationExplorer.TypeConfigBuilder("attributes").linkedTo("documents1", "Owner")));
        AddingAclOptimizedVisitor visitor = new AddingAclOptimizedVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select id, (select string_agg(v, ', ') from attributes where owner = d.id) from documents1 d");
        Select select = parser.getSelectStatement();
        String expected = groupsSubquery("documents1")
                + select.toString().replaceAll("FROM documents1 d", "FROM " + aclSubquery("documents1", "documents1", "documents1", "d"));
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    private String aclSubquery(String type, String aclType, String accessObjectIdType, String alias) {
        return aclSubquery(type, aclType, accessObjectIdType, alias, false);
    }

    private String aclSubquery(String type, String aclType, String accessObjectIdType, String alias, boolean useStamps) {
        return "(SELECT "
                + type
                + ".* FROM "
                + quote(type)
                + " "
                + type
                + " WHERE 1 = 1 " +
                // stamp проверки
                (useStamps ?
                        "AND EXISTS (" +
                                "SELECT 1 FROM " + type + " ptf WHERE ptf.id = " + type + ".access_object_id AND (ptf.security_stamp IS NULL OR " +
                                "ptf.security_stamp IN (SELECT stamp FROM person_stamp_values))) " : "")  +
                // read проверки:
                "AND EXISTS ("
                + "SELECT 1 FROM \""
                + aclType
                + "_read_tmp\" r "
                + (type.equals(accessObjectIdType) ? "" : "INNER JOIN \""
                + accessObjectIdType
                + "\" rt ON r.\"object_id\" = rt.\"access_object_id\" ")
                + (type.equals(accessObjectIdType) ? "WHERE r.\"object_id\" = " : "WHERE rt.\"id\" = ")
                + type
                + (type.equals(accessObjectIdType) ? ".\"access_object_id\" LIMIT 1)) " : ".\"id\" LIMIT 1)) ")
                + (alias == null ? type : alias);
    }

    private String quote(String typeName) {
        return "\"" + typeName + "\"";
    }

    private void addUser(User user) {
        users.put(user.getId(), user);
        usersByLogin.put(user.getLogin(), user);
    }
}