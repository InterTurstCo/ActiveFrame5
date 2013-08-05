package ru.intertrust.cm.core.dao.impl.access;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.dao.impl.PostgreSqlQueryHelper;

/**
 * Утилитный класс системы контроля доступа.
 * @author atsvetkov
 *
 */
public class AccessControlUtility {

    public static String getAclTableName(RdbmsId id) {
        String domainObjectTable = id.getTypeName();
        return getAclTableNameFor(domainObjectTable);
    }

    public static String getAclTableNameFor(String domainObjectTable) {
        String domainObjectAclTable = domainObjectTable + PostgreSqlQueryHelper.ACL_TABLE_SUFFIX;
        return domainObjectAclTable;
    }

    public static String getAclReadTableName(RdbmsId id) {
        String domainObjectTable = id.getTypeName();
        return getAclReadTableNameFor(domainObjectTable);
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
