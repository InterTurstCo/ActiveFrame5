package ru.intertrust.cm.nbrbase.gui.actions;

import java.io.File;
import java.io.FileOutputStream;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.workflow.ProcessInstanceInfo;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.gui.api.server.DomainObjectMapping;
import ru.intertrust.cm.core.gui.impl.server.action.SimpleActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.DomainObjectMappingId;
import ru.intertrust.cm.core.gui.model.action.ShowFileActionData;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;
import ru.intertrust.cm.core.model.FatalException;

@ComponentName("show.model.action")
public class ShowModelActionHandler extends SimpleActionHandler {

    @Autowired
    private ProcessService processService;

    @Autowired
    private DomainObjectMapping domainObjectMapping;

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired
    private CrudService crudService;

    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {
        try {
                ShowFileActionData actionData = new ShowFileActionData();

            Id objectId = context.getRootObjectId();
            String typeName = domainObjectMapping.getTypeName(objectId);
            if (typeName == null){
                typeName = domainObjectTypeIdCache.getName(objectId);
            }

            byte[] model = null;
            String fileName = null;
            if (typeName.equalsIgnoreCase("process_instance")){
                ProcessInstanceInfo instanceInfo = (ProcessInstanceInfo)domainObjectMapping.getObject(objectId);
                fileName = instanceInfo.getId();
                model = processService.getProcessInstanceModel(((DomainObjectMappingId)objectId).getId());
            }else{
                DomainObject domainObject = crudService.find(objectId);
                fileName = domainObject.getString("process_id");
                model = processService.getProcessTemplateModel(objectId);
            }

            File tempFile = File.createTempFile("tmp-", "-view");

            try (FileOutputStream fileStream = new FileOutputStream(tempFile)){
                StreamUtils.copy(model, fileStream);
            }
            actionData.setFileUnid(tempFile.getName());
            actionData.setFileName(fileName + ".jpg");
            actionData.setInline(true);
            return actionData;
        }catch(Exception ex){
            throw new FatalException("Error view file", ex);
        }
    }
}
