package ru.intertrust.cm.core.business.api.crypto;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.CollectorSettings;
import ru.intertrust.cm.core.config.crypto.SignedData;

public interface SignatureDataService {
    SignedData getSignedData(CollectorSettings settings, Id rootId);
}
