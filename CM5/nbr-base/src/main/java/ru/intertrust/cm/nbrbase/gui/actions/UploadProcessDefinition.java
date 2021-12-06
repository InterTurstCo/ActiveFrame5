package ru.intertrust.cm.nbrbase.gui.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.UploadActionContext;
import ru.intertrust.cm.core.gui.model.action.UploadActionData;
import ru.intertrust.cm.core.model.FatalException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@ComponentName("upload.process.model")
public class UploadProcessDefinition extends ActionHandler<UploadActionContext, UploadActionData> {

    @Autowired
    private ProcessService processService;

    @Override
    public UploadActionData executeAction(UploadActionContext context) {

        for (String fileName : context.getUploadedFiles().keySet()) {
            File processDefinitionFile = context.getUploadedFiles().get(fileName);
            try {
                processService.saveProcess(() -> getInputStream(processDefinitionFile), fileName, ProcessService.SaveType.ONLY_SAVE);
            } catch (FatalException | GuiException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new FatalException("Error upload process definition file", ex);
            }
        }

        UploadActionData result = new UploadActionData();
        result.setOnSuccessMessage("Загрузка процессов завершена успешно"); // TODO localization
        return result;
    }

    private InputStream getInputStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            throw new FatalException("Error upload process definition file", ex);
        }
    }
}
