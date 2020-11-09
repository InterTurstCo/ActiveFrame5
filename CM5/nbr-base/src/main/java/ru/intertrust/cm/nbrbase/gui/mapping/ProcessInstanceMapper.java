package ru.intertrust.cm.nbrbase.gui.mapping;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.workflow.ProcessInstanceInfo;
import ru.intertrust.cm.core.gui.api.server.DomainObjectMapper;
import ru.intertrust.cm.core.gui.model.DomainObjectMappingId;
import ru.intertrust.cm.core.model.FatalException;

public class ProcessInstanceMapper implements DomainObjectMapper {
    @Autowired
    private ProcessService processService;

    @Override
    public DomainObject toDomainObject(Object convertedObject) {
        if (convertedObject instanceof ProcessInstanceInfo){
            ProcessInstanceInfo processInstanceInfo = (ProcessInstanceInfo)convertedObject;

            GenericDomainObject result = new GenericDomainObject(getTypeName());
            return result;
        }else{
            throw new FatalException("Object need ProcessInstanceInfo instance");
        }
    }

    @Override
    public Object toObject(DomainObject convertedObject) {
        return new ProcessInstanceInfo();
    }

    @Override
    public String getTypeName() {
        return "process_instance";
    }

    @Override
    public Object getObject(Id id) {

        DomainObjectMappingId processId = (DomainObjectMappingId)id;

        return processService.getProcessInstanceInfo(processId.getId());
    }
}
