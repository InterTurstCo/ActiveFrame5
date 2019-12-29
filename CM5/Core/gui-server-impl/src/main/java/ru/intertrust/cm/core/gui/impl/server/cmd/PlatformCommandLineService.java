package ru.intertrust.cm.core.gui.impl.server.cmd;

import org.apache.commons.fileupload.FileItem;
import ru.intertrust.cm.core.gui.impl.server.cmd.model.PlatformWebServiceResult;

import java.util.List;
import java.util.Map;

/**
 * EJB Обертка над спринг бинами PlatformWebService.
 * Необходима чтобы можно было управлять транзакциями и работать аутентифицированным пользователем.
 */
public interface PlatformCommandLineService {
    PlatformWebServiceResult execute(String beanName, List<FileItem> files, Map<String, String[]> params);
}
