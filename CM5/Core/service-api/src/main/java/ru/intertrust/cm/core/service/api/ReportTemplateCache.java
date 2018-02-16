package ru.intertrust.cm.core.service.api;

import java.io.File;
import java.io.IOException;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

/**
 * Сервис для работы с директорией шаблонов отчета
 * @author larin
 *
 */
public interface ReportTemplateCache {

    File getTemplateFolder(DomainObject reportTemplateDo) throws IOException;
}
