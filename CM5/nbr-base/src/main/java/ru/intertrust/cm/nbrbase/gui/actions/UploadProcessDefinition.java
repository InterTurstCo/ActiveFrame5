package ru.intertrust.cm.nbrbase.gui.actions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import com.healthmarketscience.rmiio.DirectRemoteInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.workflow.ProcessTemplateInfo;
import ru.intertrust.cm.core.dao.api.MD5Service;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.UploadActionContext;
import ru.intertrust.cm.core.gui.model.action.UploadActionData;
import ru.intertrust.cm.core.model.FatalException;

@ComponentName("upload.process.model")
public class UploadProcessDefinition extends ActionHandler<UploadActionContext, UploadActionData> {

    @Autowired
    private CrudService crudService;

    @Autowired
    private ProcessService processService;

    @Override
    public UploadActionData executeAction(UploadActionContext context) {

        for (String fileName : context.getUploadedFiles().keySet()) {
            File processDefinitionFile = context.getUploadedFiles().get(fileName);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            try (FileInputStream processDefinitionStream = new FileInputStream(processDefinitionFile)) {
                StreamUtils.copy(processDefinitionStream, outStream);

                processService.saveProcess(outStream.toByteArray(), fileName, false);
            }catch(FatalException | GuiException ex) {
                throw ex;
            }catch(Exception ex) {
                throw new FatalException("Error upload process definition file", ex);
            }
        }

        UploadActionData result = new UploadActionData();
        result.setOnSuccessMessage("Загрузка процессов завершена успешно");
        return result;
    }
}
