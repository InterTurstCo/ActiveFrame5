package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.gui.api.server.action.ActionVisibilityChecker;
import ru.intertrust.cm.core.gui.api.server.action.ActionVisibilityContext;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 22.11.2016
 * Time: 11:20
 * To change this template use File | Settings | File and Code Templates.
 */
@ComponentName("deploy.action.visibility.checker")
public class DeployActionVisibilityChecker implements ActionVisibilityChecker {
    private static final String QUERY = "select P.id from group_member GM " +
            "join person P ON P.id=GM.person_id " +
            "join user_group UG on UG.id=GM.usergroup AND UG.group_name in ('Superusers','Administrators') " +
            "where UPPER(P.login)=UPPER('%s')";
    @Autowired
    PersonService personService;

    @Autowired
    CollectionsService collectionsService;

    @Override
    public boolean isVisible(ActionVisibilityContext context) {
        IdentifiableObjectCollection result = collectionsService.findCollectionByQuery(
                String.format(QUERY,personService.getCurrentPerson().getString("login")));
        if (result.size()>=1)
            return true;
        else
            return false;
    }
}
