package ru.intertrust.cm.core.business.api.crypto;

import ru.intertrust.cm.core.config.CollectorSettings;
import ru.intertrust.cm.core.config.crypto.SignedResult;

public interface SignatureResultService {
    void saveSignatureresult(CollectorSettings settings, SignedResult signedResult);
}
