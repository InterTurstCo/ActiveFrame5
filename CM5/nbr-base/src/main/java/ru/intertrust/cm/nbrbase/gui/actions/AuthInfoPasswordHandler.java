package ru.intertrust.cm.nbrbase.gui.actions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.form.widget.TextBoxConfig;
import ru.intertrust.cm.core.gui.api.server.widget.SelfManagingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.widget.TextBoxHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.TextState;
import ru.intertrust.cm.core.model.ObjectNotFoundException;

@ComponentName("auth_info.password.handler")
public class AuthInfoPasswordHandler  extends TextBoxHandler implements SelfManagingWidgetHandler    {
    @Autowired
    CrudService crudService;

    private TextState state;

    @Override
    public TextState getInitialState(WidgetContext context) {
        String currentValue = readCurrentValue(context);// код по считыванию пароля из базы
        state = new TextState(currentValue, true);
        setPaswordIds(context);
        return state;
    }

    private void setPaswordIds(WidgetContext context) {
        TextBoxConfig currentTextBoxConfig = context.getWidgetConfig();

        if (currentTextBoxConfig.getConfirmationFor() != null) {
            state.setConfirmationWidgetId(currentTextBoxConfig.getConfirmationFor().getWidgetId());
            state.setPrimaryWidgetId(currentTextBoxConfig.getId());
        }
        if (currentTextBoxConfig.getConfirmation() != null) {
            state.setPrimaryWidgetId(currentTextBoxConfig.getConfirmation().getWidgetId());
            state.setConfirmationWidgetId(currentTextBoxConfig.getId());
        }
    }

    private String readCurrentValue(WidgetContext context) {
        String result = null;
        DomainObject do1=null;
        DomainObject rootDomainObject = context.getFormObjects().getRootDomainObject();

        Id personId = null;
        if(rootDomainObject.getTypeName().equalsIgnoreCase("Person")) {
            personId = rootDomainObject.getId();
        } else if(rootDomainObject.getTypeName().equalsIgnoreCase("Employee")) {
            personId = rootDomainObject.getId();
        }else {
            personId = rootDomainObject.getReference("PlatformPerson");
        }

        if (personId != null) {
            DomainObject person = crudService.find(personId);
            //Authentication_Info.User_Uid = person.Login
            String Login = person.getString("Login");
            if (Login ==null) return result;
            try {
                Map<String, Value> paramsSimpleKey = new HashMap<>();
                paramsSimpleKey.put("user_uid", new StringValue(Login));
                do1 = crudService.findByUniqueKey("Authentication_Info", paramsSimpleKey);
            } catch (ObjectNotFoundException e){
            }
            if (do1 != null) {
                result = do1.getString("Password");
            }
        }

        return result;
    }

}
