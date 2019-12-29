package ru.intertrust.cm.core.business.api;

import javax.ejb.EJBContext;

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
}
