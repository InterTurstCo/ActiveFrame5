package ru.intertrust.cm.core.dao.impl.doel;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.doel.DoelFunction;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.StatusDao;

import java.util.*;

/**
 * Функция DOEL, фильтрующая доменные объекты по их статусу.
 * Может применяться на любом шаге выражения, вычисляющем ссылки на доменный объект ({@link FieldType#REFERENCE}).
 * Функция принимает 1 и более параметров, каждый из которых считается именем статуса.
 * Ссылки на доменные объекты, статус которых не совпадает ни с одним из перечисленных, удаляются из списка значений.
 * Функция не выполняет преобразование типа значения.
 * <p><b>Пример использования:</b><br>
 * <code>Commission^parent:Status(Assigned,Executing).Job^parent.Assignee</code>
 * - вычисляет всех исполнителей поручений, находящихся в статусе "Назначено" или "Исполняется".
 * 
 * @author apirozhkov
 */
@DoelFunction(name = "Status",
        requiredParams = 1, optionalParams = 999, contextTypes = { FieldType.REFERENCE })
public class StatusFunction implements DoelFunctionImplementation {

    @Autowired private CollectionsDao collectionsDao;
    @Autowired private ConfigurationExplorer configurationExplorer;
    @Autowired private DomainObjectTypeIdCache typeIdCache;
    @Autowired private DomainObjectDao domainObjectDao;
    @Autowired private StatusDao statusDao;
    @Autowired private AccessControlService accessControlService;

    private final static String UNKNOWN_TYPE = "*";

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T extends Value, S extends Value> List<T> process(List<? super S> context, String[] params,
            AccessToken accessToken) {
        Map<String, List<ReferenceValue>> grouped = groupByType(context);
        ArrayList<Value> filtered = new ArrayList<>();
        if (grouped.containsKey(UNKNOWN_TYPE)) {
            filtered.addAll(processSimple(grouped.remove(UNKNOWN_TYPE), params, accessToken));
        }
        if (accessToken.getSubject() instanceof UserSubject) {
            accessToken = accessControlService.createCollectionAccessToken(accessToken.getSubject().getName());
        }
        String statusFilter = buildFilterFromArray("s.name", params);
        for (String type : grouped.keySet()) {
            List<ReferenceValue> refs = grouped.get(type);
            String[] idHolders = new String[refs.size()];
            Arrays.fill(idHolders, "");
            String idFilter = buildFilterFromArray("t.id", idHolders);
            StringBuilder query = new StringBuilder();
            query.append("SELECT t.id FROM \"").append(type).append("\" t JOIN status s ON t.status=s.id WHERE ");
            query.append(statusFilter).append(" AND ").append(idFilter);
            IdentifiableObjectCollection found = collectionsDao.findCollectionByQuery(query.toString(),
                    refs, 0, refs.size(), accessToken);
            for (int row = 0; row < found.size(); row++) {
                filtered.add(found.get(0, row));
            }
        }
        return (List<T>) filtered;
    }

    private Map<String, List<ReferenceValue>> groupByType(List<?> refs) {
        HashMap<String, List<ReferenceValue>> result = new HashMap<>();
        for (Object value : refs) {
            if (!(value instanceof ReferenceValue)) {
                System.out.println("Not a reference value: " + value.getClass().getName() + " [" + value + "]");
                continue;
            }
            ReferenceValue ref = (ReferenceValue) value;
            Id id = ref.get();
            String type;
            if (id instanceof RdbmsId) {
                type = typeIdCache.getName(((RdbmsId) id).getTypeId());
                type = Case.toLower(configurationExplorer.getDomainObjectRootType(type));
            } else {
                type = UNKNOWN_TYPE;
            }
            if (!result.containsKey(type)) {
                result.put(type, new ArrayList<ReferenceValue>());
            }
            result.get(type).add(ref);
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    private List<Value> processSimple(List<ReferenceValue> refs, String[] statuses, AccessToken accessToken) {
        if (accessToken.getSubject() instanceof UserSubject) {
            Id[] ids = new Id[refs.size()];
            int i = 0;
            for (ReferenceValue ref : refs) {
                ids[i++] = ref.get();
            }
            UserSubject subject = (UserSubject) accessToken.getSubject();
            accessToken = accessControlService.createAccessToken(subject.getName(), ids,
                    DomainObjectAccessType.READ, false);
        }
        HashSet<Id> statusIds = new HashSet<>(statuses.length);
        for (String status : statuses) {
            statusIds.add(statusDao.getStatusIdByName(status));
        }
        ArrayList<Id> ids = new ArrayList<>(refs.size());
        for (ReferenceValue ref : refs) {
            ids.add(ref.get());
        }
        ArrayList<Value> result = new ArrayList<>(refs.size());
        List<DomainObject> objects = domainObjectDao.find(ids, accessToken);
        for (DomainObject object : objects) {
            if (statusIds.contains(object.getStatus())) {
                result.add(new ReferenceValue(object.getId()));
            }
        }
        return result;
    }

    private String buildFilterFromArray(String comparedField, String[] values) {
        StringBuilder filter = new StringBuilder();
        for (String value : values) {
            if (filter.length() > 0) {
                filter.append(" OR ");
            }
            filter.append(comparedField).append("=");
            if (value.isEmpty()) {
                filter.append("?");
            } else {
                //TODO screen dangerous characters in value
                filter.append("'").append(value).append("'");
            }
        }
        return filter.insert(0, "(").append(")").toString();
    }
}
