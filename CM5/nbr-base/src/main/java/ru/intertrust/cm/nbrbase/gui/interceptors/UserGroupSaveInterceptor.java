package ru.intertrust.cm.nbrbase.gui.interceptors;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.server.form.FormBeforeSaveInterceptor;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.LinkedDomainObjectsTableState;
import ru.intertrust.cm.core.gui.model.form.widget.SuggestBoxState;

@ComponentName("user.group.save.interceptor")
public class UserGroupSaveInterceptor implements FormBeforeSaveInterceptor {

    protected static Logger log = LoggerFactory.getLogger(UserGroupSaveInterceptor.class);
    
    @Autowired
    private CrudService crudService;
    
	@Override
	public void beforeSave(FormState formState) {
        Map<Id,Id> uniqMembers = new HashMap<Id,Id>();
        try {

            final LinkedDomainObjectsTableState grTable = ((LinkedDomainObjectsTableState)formState.getWidgetState("MemberTableWidget"));
            List<Id> ids = grTable.getIds();
            LinkedHashMap<String, FormState> tableStateNew = grTable.getNewFormStates();

            if ( ids.size() == 0 && tableStateNew.size() == 0) {
                //throw new GuiException("Незаполнена таблица!");
            } else {
                for (Id id : ids) {
                    if (uniqMembers.containsValue(crudService.find(id).getReference("person_id"))) {
                        throw new GuiException("Обнаружены дубли в составе группы");
                    } else uniqMembers.put(id, crudService.find(id).getReference("person_id"));
                }
                SuggestBoxState member = null;
                for (String key : tableStateNew.keySet()) {
                    member = (SuggestBoxState) grTable.getFromNewStates(key).getWidgetState("PersonField");
                    Id id = member.getIds().get(0);
                    if (uniqMembers.containsValue(id)) {
                        throw new GuiException("Обнаружены дубли в составе группы");
                    } else uniqMembers.put(null, id);
                }
            }

        } catch (IndexOutOfBoundsException e) {
            throw new GuiException("Обнаружены пустые значения.");
        }

	}
}
