package ru.intertrust.cm.core.gui.impl.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.server.DomainObjectMapper;
import ru.intertrust.cm.core.gui.api.server.DomainObjectMapping;
import ru.intertrust.cm.core.gui.model.DomainObjectMappingId;


public class DomainObjectMappingImpl implements DomainObjectMapping {

    private List<DomainObjectMapper> mapperObjects = new ArrayList<>();

    private HashMap<String, DomainObjectMapper> mapperReestr = new HashMap<>();

    @PostConstruct
    private void init(){
        for (DomainObjectMapper mapperObject : mapperObjects) {
            mapperReestr.put(mapperObject.getTypeName().toLowerCase(), mapperObject);
        }
    }

    @Override
    public String getTypeName(Id objectId) {
        if (objectId instanceof DomainObjectMappingId){
            return ((DomainObjectMappingId)objectId).getType();
        }else{
            return null;
        }
    }

    @Override
    public boolean isSupportedType(String typeName) {
        return mapperReestr.containsKey(typeName.toLowerCase());
    }

    @Override
    public DomainObject toDomainObject(String typeName, Object convertedObject) {
        return mapperReestr.get(typeName.toLowerCase()).toDomainObject(convertedObject);
    }

    @Override
    public Object toObject(DomainObject convertedObject) {
        return mapperReestr.get(convertedObject.getTypeName().toLowerCase()).toObject(convertedObject);
    }

    @Override
    public Object getObject(Id id) {
        String typeName = getTypeName(id);
        return mapperReestr.get(typeName.toLowerCase()).getObject(id);
    }
}
