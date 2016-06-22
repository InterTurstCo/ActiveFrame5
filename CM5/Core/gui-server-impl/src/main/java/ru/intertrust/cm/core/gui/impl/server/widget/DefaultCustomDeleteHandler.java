package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.CustomDeleteData;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 22.06.2016
 * Time: 16:10
 * To change this template use File | Settings | File and Code Templates.
 */
@ComponentName("default.custom.delete.component")
public class DefaultCustomDeleteHandler implements ComponentHandler {
    @Autowired
    CrudService crudService;

    public Dto delete(Dto data){
        CustomDeleteData cData = (CustomDeleteData)data;
        try {
            crudService.delete(cData.getObjectToDelete());
        }
        catch (Exception e){
            cData.setResult(e.getMessage());
            e.printStackTrace();
        }
        return cData;
    }
}
