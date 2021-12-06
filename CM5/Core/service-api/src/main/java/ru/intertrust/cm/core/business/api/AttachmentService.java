package ru.intertrust.cm.core.business.api;


/**
 * Работа с доменными объектами Вложения. Вынесен в отдельный сервис, так как нужны функции работы с контентом Вложений.
 *
 * @author atsvetkov
 */
public interface AttachmentService extends BaseAttachmentService {

    /**
     * Удаленный интерфейс для EJB
     *
     * @author atsvetkov
     */
    interface Remote extends BaseAttachmentService {
    }
}
