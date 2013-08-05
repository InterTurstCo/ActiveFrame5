package ru.intertrust.cm.core.dao.impl.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.intertrust.cm.core.business.api.dto.RdbmsId;

/**
 * Группирует идентификаторы доменных объектов по типу. Это нужно для оптимизации поиска списка ДО по списку разнотипных Id, проверки прав доступа к списку ДО разных типов и т.д.
 * @author atsvetkov
 */

public class IdSorterByType {

    private Set<String> domainObjectTypes = new HashSet<String>();

    private Map<String, List<RdbmsId>> groupedByTypeObjectIds = new HashMap<String, List<RdbmsId>>();

    public IdSorterByType(RdbmsId[] ids) {
        collectDomainObjectTypes(ids);

        groupIdsByType(ids);
    }

    private void groupIdsByType(RdbmsId[] objectIds) {
        for (String domainObjectType : domainObjectTypes) {
            List<RdbmsId> singleTypeIds = null;
            for (RdbmsId id : objectIds) {
                if (domainObjectType.equals(id.getTypeName())) {
                    if (groupedByTypeObjectIds.get(domainObjectType) != null) {
                        singleTypeIds = groupedByTypeObjectIds.get(domainObjectType);
                    } else {
                        singleTypeIds = new ArrayList<RdbmsId>();
                        groupedByTypeObjectIds.put(domainObjectType, singleTypeIds);

                    }
                    singleTypeIds.add(id);
                }
            }
        }
    }

    private void collectDomainObjectTypes(RdbmsId[] objectIds) {
        for (RdbmsId id : objectIds) {
            String typeName = id.getTypeName();
            domainObjectTypes.add(typeName);
        }
    }

    /**
     * Возвращает идентификаторы ДО заданного типа
     * @param domainObjectType тип ДО
     * @return список идентификаторов заданного типа
     */
    public List<RdbmsId> getIdsOfType(String domainObjectType) {
        return groupedByTypeObjectIds.get(domainObjectType);
    }

    /**
     * Возвращает список типов доменных объектов.
     * @return список типов ДО
     */
    public Set<String> getDomainObjectTypes() {
        return domainObjectTypes;
    }
}
