package ru.intertrust.cm.core.business.diagnostic;

import ru.intertrust.cm.core.business.api.dto.globalcache.DiagnosticData;

public interface DiagnosticMessageProcessor {
    void processDiagnosticData(DiagnosticData diagnosticData);
}
