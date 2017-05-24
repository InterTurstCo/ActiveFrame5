package ru.intertrust.cm.core.gui.impl.client.action.configextension;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.action.SimpleServerAction;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

import java.util.*;

/**
 * Created by Ravil on 24.05.2017.
 */
@ComponentName("apply.action.handler")
public class ApplyDraftsAction extends SimpleServerAction {


    @Override
    protected SimpleActionContext appendCurrentContext(ActionContext initialContext) {
        final DomainObjectSurferPlugin dosPlugin = (DomainObjectSurferPlugin) getPlugin();
        Map selected =  dosPlugin.getCollectionPlugin().getChangedRowsState();
        List<Id> idS = new ArrayList<>();
        Iterator I = selected.keySet().iterator();
        while(I.hasNext()){
            Id objectId = (Id)I.next();
            if((Boolean)selected.get(objectId)){
                idS.add(objectId);
            }
        }

        FormState formState = ((IsDomainObjectEditor) getPlugin()).getFormState();
        SimpleActionContext context = (SimpleActionContext) initialContext;
        context.getObjectsIds().clear();
        context.setRootObjectId(formState.getObjects().getRootNode().getDomainObject().getId());
        context.getObjectsIds().addAll(idS);
        return context;
    }
    @Override
    public Component createNew() {
        return new ApplyDraftsAction();
    }
}
