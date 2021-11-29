package ru.intertrust.cm.core.dao.impl.attach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
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

        calendar.add(Calendar.DAY_OF_MONTH, getDelay());
        domainObject.setTimestamp("delete_after", calendar.getTime());

        AccessToken token = accessControlService.createSystemAccessToken(DelayedFileDeleteStrategy.class.toString());
        domainObjectDao.save(domainObject, token);
    }

    private int getDelay() {
        // Настройки захардкожены, проблем с NPE не будет (по идее это перестраховка, конфиг должен приходить заполненным)
        final int defaultValue = (int) DeleteFileConfig.Mode.DELAYED.getProperties().get(0).getDefault();
        return config.getDelay() == null ? defaultValue : config.getDelay();
    }

    @Override
    public String toString() {
        return "DelayedFileDeleteStrategy{" +
                "config=" + config +
                '}';
    }
}
