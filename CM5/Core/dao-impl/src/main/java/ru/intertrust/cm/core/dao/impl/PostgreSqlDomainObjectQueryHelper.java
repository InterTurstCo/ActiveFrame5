package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.impl.access.AccessControlUtility;
import ru.intertrust.cm.core.dao.impl.utils.ConfigurationExplorerUtils;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;

import static ru.intertrust.cm.core.dao.api.DomainObjectDao.ID_COLUMN;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getALTableSqlName;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlAlias;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

/**
 * PostgreSql-специфичный класс для генерации sql-запросов для работы с доменными объектами
 *
 */
public class PostgreSqlDomainObjectQueryHelper extends DomainObjectQueryHelper {

    @Override
    public void appendAccessControlLogicToQuery(StringBuilder query, String linkedType) {
        boolean isAuditLog = configurationExplorer.isAuditLogType(linkedType);
        String originalLinkedType = DataStructureNamingHelper.getSqlName(linkedType);

        // Проверка прав для аудит лог объектов выполняются от имени родительского объекта.
        linkedType = getRelevantType(linkedType);

        Id personId = currentUserAccessor.getCurrentUserId();
        boolean isAdministratorWithAllPermissions = isAdministratorWithAllPermissions(personId, linkedType);

        //Добавляем учет ReadPermittedToEverybody
        if (!(configurationExplorer.isReadPermittedToEverybody(linkedType) || isAdministratorWithAllPermissions)) {
            // Проверка прав для аудит лог объектов выполняются от имени родительского объекта.
            linkedType = getRelevantType(linkedType);
            //В случае заимствованных прав формируем запрос с "чужой" таблицей xxx_read
            String matrixReferenceTypeName = configurationExplorer.getMatrixReferenceTypeName(linkedType);
            String childAclReadTable = null;
            if (matrixReferenceTypeName != null){
                childAclReadTable = AccessControlUtility.getAclReadTableNameFor(configurationExplorer, matrixReferenceTypeName);
            }else{
                childAclReadTable = AccessControlUtility.getAclReadTableNameFor(configurationExplorer, linkedType);
            }
            String topLevelParentType = ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, linkedType);
            String topLevelAuditTable = getALTableSqlName(topLevelParentType);

            String rootType = configurationExplorer.getDomainObjectRootType(linkedType).toLowerCase();

            query.append(" and exists (select r." + wrap("object_id") + " from ").append(wrap(childAclReadTable)).append(" r ");

            query.append(" inner join ").append(DaoUtils.wrap("group_group")).append(" gg on r.").append(DaoUtils.wrap("group_id"))
                    .append(" = gg.").append(DaoUtils.wrap("parent_group_id"));
            query.append(" inner join ").append(DaoUtils.wrap("group_member")).append(" gm on gg.")
                    .append(DaoUtils.wrap("child_group_id")).append(" = gm.").append(DaoUtils.wrap("usergroup"));
            query.append("inner join ").append(DaoUtils.wrap(rootType)).append(" rt on r.")
                    .append(DaoUtils.wrap("object_id"))
                    .append(" = rt.").append(DaoUtils.wrap("access_object_id"));
            if (isAuditLog) {
                query.append(" inner join ").append(wrap(topLevelAuditTable)).append(" pal on ").append(originalLinkedType).append(".")
                        .append(wrap(Configuration.ID_COLUMN)).append(" = pal.").append(wrap(Configuration.ID_COLUMN));
            }

            query.append("where gm.").append(wrap("person_id")).append(" = :user_id and rt.").append(wrap("id")).append(" = ");
            if (!isAuditLog) {
                query.append(originalLinkedType).append(".").append(DaoUtils.wrap(ID_COLUMN));
            } else {
                query.append(topLevelAuditTable).append(".").append(DaoUtils.wrap(Configuration.DOMAIN_OBJECT_ID_COLUMN));
            }

            query.append(" limit 1)");
        }
    }

    @Override
    protected void appendAccessRightsPart(String typeName, AccessToken accessToken, String tableAlias, StringBuilder query, boolean isSingleDomainObject) {
        /* IN CASE OF SINGLE DOMAIN OBJECT
         * and exists (
         *      select a."object_id" from "country_read" a
         *      inner join "group_group" gg on a."group_id" = gg."parent_group_id"
         *      inner join "group_member" gm on gg."child_group_id" = gm."usergroup"
         *      inner join "country" o on (o."access_object_id" = a."object_id")
         *      where
         *      gm."person_id" = 4
         *      and o."id" = 29
         *  )
         *
         * IN CASE OF MULTIPLE DOMAIN OBJECTS
         * and exists (
         *      select a."object_id" from "country_read" a
         *      inner join "group_group" gg on a."group_id" = gg."parent_group_id"
         *      inner join "group_member" gm on gg."child_group_id" = gm."usergroup"
         *      where
         *      gm."person_id" = 4
         *      and country1."access_object_id" = a."object_id"
         *      )
         */
        boolean isDomainObject = configurationExplorer.getConfig(DomainObjectTypeConfig.class, DaoUtils.unwrap(typeName)) != null;
        if (accessToken.isDeferred() && isDomainObject) {
            boolean isAuditLog = configurationExplorer.isAuditLogType(typeName);

            // Проверка прав для аудит лог объектов выполняются от имени родительского объекта.
            typeName = getRelevantType(typeName);
            String permissionType = typeName;
            String matrixRefType = configurationExplorer.getMatrixReferenceTypeName(typeName);
            if (matrixRefType != null) {
                permissionType = matrixRefType;
            }

            Id personId = currentUserAccessor.getCurrentUserId();
            boolean isAdministratorWithAllPermissions = AccessControlUtility.isAdministratorWithAllPermissions(personId, typeName, userGroupCache, configurationExplorer);

            //Получаем матрицу для permissionType
            //В полученной матрице получаем флаг read-evrybody и если его нет то добавляем подзапрос с правами
            if (!isReadEveryBody(permissionType) && !isAdministratorWithAllPermissions) {

                //Таблица с правами на read получается с учетом наследования типов
                String aclReadTable = AccessControlUtility
                        .getAclReadTableName(configurationExplorer, permissionType);
                String toplevelParentType = ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, typeName);
                String topLevelAuditTable = getALTableSqlName(toplevelParentType);
                String domainObjectBaseTable = DataStructureNamingHelper.getSqlName(toplevelParentType);

                query.append(" and exists (select a.\"object_id\" from ").append(wrap(aclReadTable)).append(" a ");
                query.append(" inner join ").append(wrap("group_group")).append(" gg on a.")
                        .append(wrap("group_id")).append(" = gg.").append(wrap("parent_group_id"));
                query.append(" inner join ").append(wrap("group_member")).append(" gm on gg.")
                        .append(wrap("child_group_id")).append(" = gm.").append(wrap("usergroup"));
                //обавляем в связи с появлением функциональности замещения прав
                if (isSingleDomainObject) {
                    query.append(" inner join ").append(DaoUtils.wrap(domainObjectBaseTable)).append(" o on (o.");
                    query.append(DaoUtils.wrap("access_object_id")).append(" = a.").append(DaoUtils.wrap("object_id")).append(")");
                }
                if (isAuditLog) {
                    query.append(" inner join ").append(wrap(topLevelAuditTable)).append(" pal on ").append(tableAlias).append(".") // todo check usage of tableAlias
                            .append(wrap(Configuration.ID_COLUMN)).append(" = pal.").append(wrap(Configuration.ID_COLUMN));
                }

                query.append(" where gm.").append(wrap("person_id")).append(" = :user_id and ");

                if (isAuditLog) {
                    query.append("o.").append(wrap("id")).append(" = ").append(topLevelAuditTable).append(".").append(DaoUtils.wrap(Configuration.DOMAIN_OBJECT_ID_COLUMN));

                } else {
                    if (isSingleDomainObject) {
                        query.append("o.").append(wrap("id")).append(" = :id");
                    } else {
                        query.append(getSqlAlias(domainObjectBaseTable)).append(".\"access_object_id\" = a.\"object_id\"");
                    }
                }
                query.append(" limit 1)");
            }
        }
    }
}
