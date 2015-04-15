package ru.intertrust.cm.core.gui.impl.server.businessuniverse;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.businessuniverse.UserExtraInfoBuilder;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.PlainTextUserExtraInfo;

import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 15.04.2015
 *         Time: 15:58
 */
@ComponentName("default.user.extra.info.builder")
public class DefaultUserExtraInfoBuilder implements UserExtraInfoBuilder {
    @Autowired
    private ProfileService profileService;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Autowired
    private PersonManagementService personManagementService;

    @Override
    public PlainTextUserExtraInfo getUserExtraInfo() {
        final List<DomainObject> groups = personManagementService.getPersonGroups(currentUserAccessor.getCurrentUserId());
        if (groups == null || groups.isEmpty()) {
            return new PlainTextUserExtraInfo("");
        }
        String defaultGroup = "";
        for (DomainObject groupDo : groups) {
            final String groupName = groupDo.getString("group_name");
            if (groupName == null || groupName.isEmpty()) {
                continue;
            }
            if (groupName.equals("Superusers")) {
                return new PlainTextUserExtraInfo(MessageResourceProvider.getMessage("Superuser", profileService.getPersonLocale(), null));
            } else if (groupName.equals("Administrators") || groupName.equals("Administrators_Read_Only")) {
                return new PlainTextUserExtraInfo(MessageResourceProvider.getMessage("Administrator", profileService.getPersonLocale(), null));
            }
            if (groupName.equals("AllPersons") || groupName.equals("Person")) { // system groups, ignore them
                continue;
            }
            defaultGroup = groupName;
        }
        return new PlainTextUserExtraInfo(defaultGroup);
    }
}
