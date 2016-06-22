package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 22.06.2016
 * Time: 16:14
 * To change this template use File | Settings | File and Code Templates.
 */
public class CustomDeleteData implements Dto {
    Id objectToDelete;
    String result;

    public Id getObjectToDelete() {
        return objectToDelete;
    }

    public void setObjectToDelete(Id objectToDelete) {
        this.objectToDelete = objectToDelete;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
