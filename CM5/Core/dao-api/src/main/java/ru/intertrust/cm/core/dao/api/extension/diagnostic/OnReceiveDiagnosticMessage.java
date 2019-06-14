package ru.intertrust.cm.core.dao.api.extension.diagnostic;

import ru.intertrust.cm.core.business.api.dto.globalcache.DiagnosticData;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPointHandler;

/**
 * Точка расширения получения диагностических сообщений
 */
public interface OnReceiveDiagnosticMessage extends ExtensionPointHandler{
    
    void onMessage(DiagnosticData diagnosticData);
}
