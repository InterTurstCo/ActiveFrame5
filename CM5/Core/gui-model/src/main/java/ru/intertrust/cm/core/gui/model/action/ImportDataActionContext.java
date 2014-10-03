package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.model.form.FormState;


/**
 * 
 * @author lyakin
 *
 */
public class ImportDataActionContext extends ActionContext {
    private FormState formState;
    
    public ImportDataActionContext(ActionConfig actionConfig) {
        super(actionConfig);
    }

    public FormState getFormState() {
        return formState;
    }

    public void setFormState(FormState formState) {
        this.formState = formState;
        
    }

}
