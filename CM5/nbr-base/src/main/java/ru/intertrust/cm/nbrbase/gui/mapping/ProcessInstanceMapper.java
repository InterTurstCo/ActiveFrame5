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
        GenericDomainObject result = new GenericDomainObject(getTypeName());

        if (convertedObject == null) {
            result.setString("name", "");
        } else {
            if (convertedObject instanceof ProcessInstanceInfo) {
                ProcessInstanceInfo processInstanceInfo = (ProcessInstanceInfo) convertedObject;

                result.setString("name", processInstanceInfo.getName());
                result.setTimestamp("start_date", processInstanceInfo.getStart());
                result.setTimestamp("finish_date", processInstanceInfo.getFinish());
                result.setBoolean("suspended", processInstanceInfo.isSuspended());
                result.setId(new DomainObjectMappingId(getTypeName(), processInstanceInfo.getId()));

            }else{
                throw new FatalException("Object need ProcessInstanceInfo instance");
            }
        }
        return result;
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
