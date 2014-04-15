package ru.intertrust.cm.remoteclient.profile.test;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.ScheduleService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationTaskMode;
import ru.intertrust.cm.core.business.api.notification.NotificationTaskConfig;
import ru.intertrust.cm.core.config.FindObjectsConfig;
import ru.intertrust.cm.core.config.FindObjectsQueryConfig;
import ru.intertrust.cm.remoteclient.ClientBase;

import java.util.Calendar;
import java.util.Date;

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

        profileService.setProfile(systemProfile1);

        /*
        ProfileObject systemProfile2 = new ProfileObject();
        systemProfile2.setName("dinCreated");
        systemProfile2.setParent(systemProfile1.getParent());
        systemProfile2.setValue("key1", new ProfileStringValue("key1_val"));
        systemProfile2.setValue("key2", new ProfileLongValue(100500));
        systemProfile2.setValue("key3", new ProfileDateTimeValue(new Date()));
        profileService.setProfile(systemProfile2);
        */

        PersonProfile personProfile2 = profileService.getPersonProfile();

    }
}
