package ru.intertrust.cm.plugins.permission;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.plugin.Plugin;
import ru.intertrust.cm.core.business.api.plugin.PluginHandler;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.plugins.PluginBase;

import javax.ejb.EJBContext;
import javax.transaction.Status;
import java.util.Collections;
import java.util.Map;

@Plugin(name = "DeleteDynamicGroups",
        description = "Удаление динамических групп доступа",
        transactional = false)
public class DeleteDynamicGroups extends PluginBase implements PluginHandler {

    @Autowired
    private CollectionsService collectionsService;

    @Autowired
    private CrudService crudService;

    @Override
    public String execute(EJBContext context, String param) {
        Map<String, String> params = getParametersMap(param);

        String groupName = params.get("group");
        if (groupName == null){
            throw new FatalException("Params need contains group name for delete. Param format: group=group_name");
        }
        info("Delete group " + groupName);

        // Получаем все группы
        IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery(
                "select id from user_group where group_name = {0}",
                Collections.singletonList(new StringValue(groupName)));
        for (IdentifiableObject row : collection) {
            deleteGroup(context, row);
        }

        return getLog();
    }

    private void deleteGroup(EJBContext context, IdentifiableObject row) {
        try{
            context.getUserTransaction().begin();
            info("Delete group with id " + row.getId().toStringRepresentation());
            crudService.delete(row.getId());
            context.getUserTransaction().commit();
        }catch (Exception ex){
            error("Error delete group with id " + row.getId().toStringRepresentation(), ex);
            try {
                if (context.getUserTransaction().getStatus() == Status.STATUS_ACTIVE ||
                        context.getUserTransaction().getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                    context.getUserTransaction().rollback();
                }
            }catch (Exception ignoreEx){
                info("Warning! Error rollback transaction");
            }
        }
    }
}
