package ru.intertrust.cm.core.dao.impl.attach;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.config.DeleteFileConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

import java.util.Calendar;
import java.util.Date;

public class DelayedFileDeleteStrategy implements FileDeleteStrategy{
    private DeleteFileConfig config;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;

    @Override
    public void setConfiguration(DeleteFileConfig config) {
        this.config = config;
    }

    @Override
    public void deleteFile(String path) {
        // Удаление производится периодической задачей DelayedStrategyAttachmentCleanerScheduleTask которая зачитывает delete_attachment_journal
        GenericDomainObject domainObject = new GenericDomainObject("delete_attachment_journal");
        domainObject.setString("path", path);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        // По умолчанию физическое удаление производится через сутки
        calendar.add(Calendar.DAY_OF_MONTH, config.getDelay() == null ? 1 : config.getDelay());
        domainObject.setTimestamp("delete_after", calendar.getTime());

        AccessToken token = accessControlService.createSystemAccessToken(DelayedFileDeleteStrategy.class.toString());
        domainObjectDao.save(domainObject, token);
    }
}
