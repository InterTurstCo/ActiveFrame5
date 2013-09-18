package ru.intertrust.cm.core.dao.impl.utils;

import ru.intertrust.cm.core.business.api.dto.RdbmsId;

import java.util.*;

/**
 * Группирует идентификаторы доменных объектов по типу. Это нужно для оптимизации поиска списка ДО по списку разнотипных Id, проверки прав доступа к списку ДО разных типов и т.д.
 * @author atsvetkov
 */

public class IdSorterByType {

    private Set<Integer> domainObjectTypeIds = new HashSet<>();

    private Map<Integer, List<RdbmsId>> groupedByTypeObjectIds = new HashMap<>();

    public IdSorterByType(RdbmsId[] ids) {
        collectDomainObjectTypes(ids);

        groupIdsByType(ids);
    }

    private void groupIdsByType(RdbmsId[] objectIds) {
        for (Integer domainObjectType : domainObjectTypeIds) {
            List<RdbmsId> singleTypeIds = null;
            for (RdbmsId id : objectIds) {
                if (domainObjectType.equals(id.getTypeId())) {
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
            Integer typeName = id.getTypeId();
            domainObjectTypeIds.add(typeName);
        }
    }

    /**
     * Возвращает идентификаторы ДО заданного типа
     * @param domainObjectType тип ДО
     * @return список идентификаторов заданного типа
     */
    public List<RdbmsId> getIdsOfType(Integer domainObjectType) {
        return groupedByTypeObjectIds.get(domainObjectType);
    }

    /**
     * Возвращает список типов доменных объектов.
     * @return список типов ДО
     */
    public Set<Integer> getDomainObjectTypeIds() {
        return domainObjectTypeIds;
    }
}
