package ru.intertrust.cm.core.business.api;

import javax.ejb.EJBContext;
import java.util.Date;

/**
 * Сервис очищает файловую систему хранилища от не удаленных и измененных вложений, которые больше не используются
 */
public interface FileSystemAttachmentCleaner {
    /**
     * Выполнить очистку хранилища вложений
     * @param ejbContext
     * @return
     */
    String clean(EJBContext ejbContext);

    /**
     * Выполнить очистку вложений, созданных в период между датами, переданными в параметрах from и to
     * @param ejbContext
     * @param from
     * @param to
     * @return
     */
    String clean(EJBContext ejbContext, Date from, Date to);
}
