package ru.intertrust.cm.core.business.diagnostic;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.globalcache.DiagnosticData;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;
import ru.intertrust.cm.core.dao.api.extension.diagnostic.OnReceiveDiagnosticMessage;

@ExtensionPoint
public class ReceiveGlobalCacheDiagnosticMessage implements OnReceiveDiagnosticMessage {

    @Autowired
    private DiagnosticMessageProcessor processor;

    @Override
    public void onMessage(DiagnosticData diagnosticData) {
        // Чтобы открыть транзакцию и инициализировать контекст безопасности используем EJB
        processor.processDiagnosticData(diagnosticData);

    }

}
