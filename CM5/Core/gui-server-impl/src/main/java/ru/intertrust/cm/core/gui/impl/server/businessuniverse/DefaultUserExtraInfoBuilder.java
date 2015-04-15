package ru.intertrust.cm.core.gui.impl.server.businessuniverse;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.gui.api.server.businessuniverse.UserExtraInfoBuilder;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.PlainTextUserExtraInfo;

/**
 * @author Denis Mitavskiy
 *         Date: 15.04.2015
 *         Time: 15:58
 */
@ComponentName("default.user.extra.info.builder")
public class DefaultUserExtraInfoBuilder implements UserExtraInfoBuilder {
    @Autowired
    private ProfileService profileService;

    @Override
    public PlainTextUserExtraInfo getUserExtraInfo() {
        return new PlainTextUserExtraInfo(MessageResourceProvider.getMessage("Administrator", profileService.getPersonLocale(), null));
    }
}
