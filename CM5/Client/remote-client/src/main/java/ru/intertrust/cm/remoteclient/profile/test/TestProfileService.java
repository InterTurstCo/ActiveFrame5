package ru.intertrust.cm.remoteclient.profile.test;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.ScheduleService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Profile;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationTaskMode;
import ru.intertrust.cm.core.business.api.notification.NotificationTaskConfig;
import ru.intertrust.cm.core.config.FindObjectsConfig;
import ru.intertrust.cm.core.config.FindObjectsQueryConfig;
import ru.intertrust.cm.remoteclient.ClientBase;

import java.util.Calendar;

public class TestProfileService extends ClientBase {

    private ProfileService profileService;

    public static void main(String[] args) {
        try {
            TestProfileService test = new TestProfileService();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        super.execute(args);
        profileService = (ProfileService) getService(
                "ProfileService", ProfileService.Remote.class);

        Profile systemProfile1 = profileService.getProfile("test1");

        Profile personProfile1 = profileService.getPersonProfile(new RdbmsId(5011, 13));

    }
}
