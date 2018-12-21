package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.FormObjectsRemoverConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.api.server.form.FormObjectsRemover;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;
import ru.intertrust.cm.core.gui.model.form.FormState;


/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 21.07.2016
 * Time: 9:33
 * To change this template use File | Settings | File and Code Templates.
 */
@ComponentName("test.simple.action")
public class TestSimpleActionHandler extends ActionHandler<SimpleActionContext, SimpleActionData> {
    @Autowired
    CrudService crudService;

    @Autowired
    CollectionsService collectionsService;


    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {

        SimpleActionData actionData = new SimpleActionData();

        IdentifiableObjectCollection objects = collectionsService.findCollectionByQuery("select id from norefobject");
        if(objects.size()>0){
            for (IdentifiableObject s:
            objects) {
                    crudService.delete(s.getId());
            }

        }
        actionData.setDeleteAction(true);
        actionData.setDeletedObject(context.getRootObjectId());

        return actionData;
    }

    @Override
    public SimpleActionContext getActionContext(ActionConfig actionConfig) {
        return new SimpleActionContext(actionConfig);
    }
}
