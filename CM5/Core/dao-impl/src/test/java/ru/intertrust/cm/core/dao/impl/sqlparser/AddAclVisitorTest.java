package ru.intertrust.cm.core.dao.impl.sqlparser;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import net.sf.jsqlparser.statement.select.Select;

import org.junit.Before;
import org.junit.Test;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.impl.DomainObjectQueryHelper;
import ru.intertrust.cm.core.dao.impl.sqlparser.FakeConfigurationExplorer.TypeConfigBuilder;

public class AddAclVisitorTest {

    private class User {
        private Id id;
        private String login;
        private boolean isAdmin;
        private boolean isSuperUser;

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

    private static final String GROUPS_SUBQUERY = "WITH cur_user_groups AS "
            + "(SELECT DISTINCT gg.\"parent_group_id\" FROM \"group_member\" gm "
            + "INNER JOIN \"group_group\" gg ON gg.\"child_group_id\" = gm.\"usergroup\" WHERE gm.\"person_id\" = :user_id) ";

    private HashMap<Id, User> users = new HashMap<Id, User>();
    private HashMap<String, User> usersByLogin = new HashMap<String, User>();

    private FakeConfigurationExplorer configurationExplorer = new FakeConfigurationExplorer();
    private Id currentUser;
    private CurrentUserAccessor accessor = new CurrentUserAccessor() {

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
    };

    private UserGroupGlobalCache userCache = new UserGroupGlobalCache() {

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
    };

    private DomainObjectQueryHelper queryHelper = new DomainObjectQueryHelper();

    @Before
    public void setUp() {
        AddAclVisitor.clearStaticFields();
        queryHelper.setConfigurationExplorer(configurationExplorer);
        queryHelper.setCurrentUserAccessor(accessor);
        queryHelper.setUserGroupCache(userCache);
        addUser(new User(new RdbmsId(1, 1), "user", false, false));
    }

    @Test
    public void testNoAclForNonTypeTable() {
        AddAclVisitor visitor = new AddAclVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select id from documents");
        Select select = parser.getSelectStatement();
        String expected = select.toString();
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testSingleType() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("documents")));
        AddAclVisitor visitor = new AddAclVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select id from documents");
        Select select = parser.getSelectStatement();
        String expected = GROUPS_SUBQUERY + "SELECT id FROM " + aclSubquery("documents", "documents", "documents", null);
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testUsageOfParentType() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("base_documents")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("documents").parent("base_documents")));
        AddAclVisitor visitor = new AddAclVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select id from documents");
        Select select = parser.getSelectStatement();
        String expected = GROUPS_SUBQUERY + "SELECT id FROM " + aclSubquery("documents", "base_documents", "base_documents", null);
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testUsageOfLinkedType() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("base_documents")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("linked_attribute").linkedTo("base_documents", "base_document_id")));
        AddAclVisitor visitor = new AddAclVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select string_value from linked_attribute");
        Select select = parser.getSelectStatement();
        String expected = GROUPS_SUBQUERY + "SELECT string_value FROM " + aclSubquery("linked_attribute", "base_documents", "linked_attribute", null);
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testCaseInsensitiveness() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("Base_Documents")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("Documents", false).parent("Base_Documents")));
        AddAclVisitor visitor = new AddAclVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select id from documents d join base_documents bd on bd.id = d.id");
        Select select = parser.getSelectStatement();
        String expected = GROUPS_SUBQUERY
                + "SELECT id FROM " + aclSubquery("documents", "base_documents", "base_documents", "d")
                + " JOIN base_documents bd ON bd.id = d.id";
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testSubslectWithoutFrom() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("Base_Documents")));
        AddAclVisitor visitor = new AddAclVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select id, (select coaleasce(bd.a, bd.b)) ab from base_documents bd");
        Select select = parser.getSelectStatement();
        String expected = GROUPS_SUBQUERY
                + select.toString().replaceFirst("base_documents bd", aclSubquery("base_documents", "base_documents", "base_documents", "bd"));
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testSelectWithWith() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("Base_Documents")));
        AddAclVisitor visitor = new AddAclVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("with t as(select x from y where z = 0) select id from base_documents bd where id = t.x");
        Select select = parser.getSelectStatement();

        String expected = GROUPS_SUBQUERY.trim() + ", " + select.toString().replace("WITH ", "").trim()
                .replaceFirst("base_documents bd", aclSubquery("base_documents", "base_documents", "base_documents", "bd"));

        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testUsageOfLinkedTypeWithParent() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("base_documents")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("documents", false).parent("base_documents")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("linked_attribute").linkedTo("documents", "document_id")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("linked_attribute_child", false).parent("linked_attribute")));
        AddAclVisitor visitor = new AddAclVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select string_value from linked_attribute_child");
        Select select = parser.getSelectStatement();
        String expected = GROUPS_SUBQUERY + "SELECT string_value FROM " + aclSubquery("linked_attribute_child", "base_documents", "linked_attribute", null);
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testJoinOfIndependentTables() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("a")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("b")));
        AddAclVisitor visitor = new AddAclVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select id from a join b on a.x = b.x");
        Select select = parser.getSelectStatement();
        String expected = GROUPS_SUBQUERY + "SELECT id FROM "
                + aclSubquery("a", "a", "a", null)
                + " JOIN " + aclSubquery("b", "b", "b", null) + " ON a.x = b.x";
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testSubquery() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("a")));
        AddAclVisitor visitor = new AddAclVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select id, created_date, abc from (select id, created_date, s || b as abc from a) t");
        Select select = parser.getSelectStatement();
        String expected = GROUPS_SUBQUERY + "SELECT id, created_date, abc FROM (SELECT id, created_date, s || b AS abc FROM "
                + aclSubquery("a", "a", "a", null)
                + ") t";
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testExists() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("a")));
        AddAclVisitor visitor = new AddAclVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select id, created_date, abc from x where exists (select id from a where x.n = a.n)");
        Select select = parser.getSelectStatement();
        String expected = GROUPS_SUBQUERY + "SELECT id, created_date, abc FROM x WHERE EXISTS (SELECT id FROM "
                + aclSubquery("a", "a", "a", null)
                + " WHERE x.n = a.n)";
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testEliminateExcessiveAcl() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("base_documents")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("documents", false).parent("base_documents")));
        AddAclVisitor visitor = new AddAclVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("select id from documents d join base_documents bd on bd.id = d.id");
        Select select = parser.getSelectStatement();
        String expected = GROUPS_SUBQUERY + "SELECT id FROM "
                + aclSubquery("documents", "base_documents", "base_documents", "d")
                + " JOIN base_documents bd ON bd.id = d.id";
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testEliminateExcessiveAclMoreComplex() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("base_documents")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("documents", false).parent("base_documents")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("documents_m").linkedTo("documents", "owner")));
        AddAclVisitor visitor = new AddAclVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser(
                "select id from documents d join base_documents bd on bd.id = d.id left join documents_m dm on dm.owner = d.id");
        Select select = parser.getSelectStatement();
        String expected = GROUPS_SUBQUERY + "SELECT id FROM "
                + aclSubquery("documents", "base_documents", "base_documents", "d")
                + " JOIN base_documents bd ON bd.id = d.id"
                + " LEFT JOIN documents_m dm ON dm.owner = d.id";
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testEliminateExcessiveAclEvenMoreComplex() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("base_documents")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("documents", false).parent("base_documents")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("base_attribute", false)));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("attribute").parent("base_attribute").linkedTo("base_documents", "root")));
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("attribute_attribute").linkedTo("base_attribute", "root")));
        AddAclVisitor visitor = new AddAclVisitor(configurationExplorer, userCache, accessor, queryHelper);
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
        String expected = GROUPS_SUBQUERY
                + select.toString().replaceAll("FROM attribute a", "FROM " + aclSubquery("attribute", "base_documents", "base_attribute", "a"));
        select.accept(visitor);
        assertEquals(expected, select.toString());
    }

    @Test
    public void testBasicQueryStartsWithWithRecursive() {
        configurationExplorer.createTypeConfig((new TypeConfigBuilder("base_documents")));
        AddAclVisitor visitor = new AddAclVisitor(configurationExplorer, userCache, accessor, queryHelper);
        SqlQueryParser parser = new SqlQueryParser("with recursive t as (select id from base_documents bd) select id from t");
        Select select = parser.getSelectStatement();
        String expected = GROUPS_SUBQUERY.replaceAll("WITH", "WITH RECURSIVE").replaceAll(":user_id\\) ", ":user_id\\)")
                + select.toString().replaceAll("WITH RECURSIVE", ",")
                        .replaceAll("FROM base_documents bd", "FROM " + aclSubquery("base_documents", "base_documents", "base_documents", "bd"));
        select.accept(visitor);
        assertEquals(expected, select.toString());

    }

    private String aclSubquery(String type, String aclType, String accessObjectIdType, String alias) {
        String subquery = "(SELECT "
                + type
                + ".* FROM "
                + quote(type)
                + " "
                + type
                + " WHERE 1 = 1 AND EXISTS ("
                + "SELECT 1 FROM \""
                + aclType
                + "_read\" r "
                + (type.equals(accessObjectIdType) ? "" : "INNER JOIN \""
                        + accessObjectIdType
                        + "\" rt ON r.\"object_id\" = rt.\"access_object_id\" ")
                + "WHERE r.\"group_id\" IN (SELECT \"parent_group_id\" FROM cur_user_groups) "
                + (type.equals(accessObjectIdType) ? "AND r.\"object_id\" = " : "AND rt.\"id\" = ")
                + type
                + (type.equals(accessObjectIdType) ? ".\"access_object_id\")) " : ".\"id\")) ")
                + (alias == null ? type : alias);
        return subquery;
    }

    private String quote(String typeName) {
        return "\"" + typeName + "\"";
    }

    private void addUser(User user) {
        users.put(user.getId(), user);
        usersByLogin.put(user.getLogin(), user);
    }
}
