package ru.intertrust.cm.core.dao.impl.attach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.config.DeleteFileConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

import java.util.Calendar;
import java.util.Date;

@Component("delayedFileDeleteStrategy")
@Scope("prototype")
public class DelayedFileDeleteStrategy implements StatefullFileDeleteStrategy {
    
    private DeleteFileConfig config;
    private String name;
    private int delayFromProps = -1;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private Environment environment;
    
    @Override
    public void setConfiguration(DeleteFileConfig config) {
        this.config = config;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void init() {
        final String delay = environment.getProperty(FileSystemAttachmentStorageImpl.PROP_PREFIX + name + ".delay");
        if (delay != null) {
            this.delayFromProps = Integer.parseInt(delay);
            if (delayFromProps < 1) {
                throw new NumberFormatException("Delay attribute must be more then 1");
            }
        }
    }

    @Override
    public void deleteFile(String path) {
        // Удаление производится периодической задачей DelayedStrategyAttachmentCleanerScheduleTask которая зачитывает delete_attachment_journal
        GenericDomainObject domainObject = new GenericDomainObject("delete_attachment_journal");
        domainObject.setString("path", path);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        // По умолчанию физическое удаление производится через сутки

        calendar.add(Calendar.DAY_OF_MONTH, getDelay());
        domainObject.setTimestamp("delete_after", calendar.getTime());

        AccessToken token = accessControlService.createSystemAccessToken(DelayedFileDeleteStrategy.class.toString());
        domainObjectDao.save(domainObject, token);
    }

    private int getDelay() {
        // Приоритет у настроек server.properties
        if (delayFromProps != -1) {
            return delayFromProps;
        }
        return config.getDelay() == null ? 1 : config.getDelay();
    }
}
