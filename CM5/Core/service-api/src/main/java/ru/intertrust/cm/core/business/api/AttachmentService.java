package ru.intertrust.cm.core.business.api;

import com.healthmarketscience.rmiio.RemoteInputStream;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.List;

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
    public interface Remote extends BaseAttachmentService {
    }


}
