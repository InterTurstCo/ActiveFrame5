package ru.intertrust.cm.core.gui.impl.server.action;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.ImportDataService;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.ImportDataActionContext;
import ru.intertrust.cm.core.gui.model.form.FormState;

/**
 * 
 * @author lyakin
 *
 */
@ComponentName("import.data.action")
public class ImportDataActionHandler extends ActionHandler<ImportDataActionContext, ActionData> {

    @Autowired
    private ImportDataService importDataService;
    
    @Override
    public ActionData executeAction(ImportDataActionContext context) {
        ActionData data = new ActionData();
        FormState formState = context.getFormState();
        String path = ""; //TODO Сделать получение пути из формы
        try {
            File file = new File(path+"/.index");
            BufferedReader br = new BufferedReader(new FileReader(file));
            for(String line; (line = br.readLine()) != null; ) {
                File fileCsv = new File(line);
                importDataService.importData(readFile(fileCsv));
            }
        } /*catch (FileNotFoundException e) {
            data.setOnSuccessMessage("Файл не найден " + e.getMessage());
        } */catch(Exception e){
            e.printStackTrace();// TODO data.setOnSuccessMessage(e.getMessage());
        }
        return data;
    }
    
    @Override
    public ImportDataActionContext getActionContext(ActionConfig actionConfig) {
        return new ImportDataActionContext(actionConfig);
    }
    
    private static byte[] readFile(File file) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            int read = 0;
            byte[] buffer = new byte[1024];
            while ((read = input.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        } finally {
            input.close();
        }
    }
    
}
