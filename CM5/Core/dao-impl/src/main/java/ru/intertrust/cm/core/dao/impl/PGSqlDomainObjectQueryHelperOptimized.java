package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.impl.sqlparser.AddAclVisitor;
import ru.intertrust.cm.core.dao.impl.sqlparser.AddingAclOptimizedVisitor;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;

import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

public class PGSqlDomainObjectQueryHelperOptimized extends PostgreSqlDomainObjectQueryHelper {

    @Override
    protected void appendAccessControlLogic(StringBuilder query, String typeName, String originalLinkedTypeOrAlias, String baseTypeName, String childAclReadTable, String topLevelParentType, String domainObjectBaseTable) {
        appendWithPart(query, childAclReadTable, typeName);
        appendQueryPart(query, typeName, originalLinkedTypeOrAlias, baseTypeName, childAclReadTable, topLevelParentType, domainObjectBaseTable);
    }

    private void appendWithPart(StringBuilder query, String childAclReadTable, String typeName) {
        StringBuilder withSubQuery = new StringBuilder();

        boolean hasWithKeyword = isHasWithKeyword(query);
        if (!hasWithKeyword) {
            withSubQuery.append("with ");
        }

        withSubQuery.append(childAclReadTable).append("_tmp").append(" as ")
                .append("(select distinct gg.").append(wrap("parent_group_id")).append(" ")
                .append("from ").append(wrap("group_member")).append(" gm ")
                .append("inner join ").append(wrap("group_group")).append(" gg on gg.")
                .append(wrap("child_group_id")).append(" = gm.").append(wrap("usergroup"))
                .append("inner join ").append(wrap(childAclReadTable)).append(" r on r.")
                .append(wrap("group_id")).append(" = gg.").append(wrap("parent_group_id"))
                .append(" where gm.").append(wrap("person_id")).append(" = :user_id)");

        appendWithPartForSecurityStampIfNeeded(typeName, withSubQuery);

        if (hasWithKeyword) {
            withSubQuery.append(", ");
            query.insert(5, withSubQuery);
        } else {
            withSubQuery.append(" ");
            query.insert(0, withSubQuery);
        }
    }

    @Override
    protected void appendAccessControlPart(StringBuilder query, String originalLinkedTypeOrAlias, String childAclReadTable) {
        query.append(" and exists (select 1 from ").append(wrap(childAclReadTable + "_tmp")).append(" r");
        query.append(" where r.").append(OBJECT_ID_COL).append(" = ").append(originalLinkedTypeOrAlias).append(".").append(ACCESS_OBJECT_ID_COL);
        query.append(")");
    }

    @Override
    protected void appendInheritedAccessPart(StringBuilder query, String originalLinkedTypeOrAlias, String aclReadTable, String domainObjectBaseTable) {
        query.append(" and exists (select 1 from ").append(wrap(aclReadTable + "_tmp")).append(" r");
        query.append(" inner join ").append(DaoUtils.wrap(domainObjectBaseTable)).append(" rt " )
                .append("on r.").append(OBJECT_ID_COL).append(" = rt.").append(ACCESS_OBJECT_ID_COL);
        query.append(" where rt.").append(ID_COL).append(" = ").append(originalLinkedTypeOrAlias).append(".").append(ID_COL);
        query.append(")");
    }

    @Override
    public AddAclVisitor createVisitor(ConfigurationExplorer configurationExplorer, UserGroupGlobalCache userGroupCache, CurrentUserAccessor currentUserAccessor) {
        return new AddingAclOptimizedVisitor(configurationExplorer, userGroupCache, currentUserAccessor, this);
    }
}
