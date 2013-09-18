package ru.intertrust.cm.core.dao.impl.access;

import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.dao.impl.PostgreSqlQueryHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Утилитный класс системы контроля доступа.
 * @author atsvetkov
 *
 */
public class AccessControlUtility {

    public static String getAclTableName(String doTypeName) {
        return getAclTableNameFor(doTypeName);
    }

    public static String getAclTableNameFor(String domainObjectTable) {
        String domainObjectAclTable = domainObjectTable + PostgreSqlQueryHelper.ACL_TABLE_SUFFIX;
        return domainObjectAclTable;
    }

    public static String getAclReadTableName(String doTypeName) {
        return getAclReadTableNameFor(doTypeName);
    }

    public static String getAclReadTableNameFor(String domainObjectTable) {
        String domainObjectAclTable = domainObjectTable + PostgreSqlQueryHelper.READ_TABLE_SUFFIX;
        return domainObjectAclTable;
    }

    /**
     * Конвертирует список объектов {@see RdbmsId} в список объектов типа {@see Long}
     * @param objectIds
     * @return
     */
    public static List<Long> convertRdbmsIdsToLongIds(List<RdbmsId> objectIds) {
        List<Long> idList = new ArrayList<Long>();
        for (RdbmsId id : objectIds) {
            if (id != null && id.getClass().equals(RdbmsId.class)) {
                idList.add(id.getId());
            }
        }
        return idList;
    }
}
