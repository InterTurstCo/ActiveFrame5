package ru.intertrust.cm.core.gui.model.crypto;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.crypto.DocumentVerifyResult;

import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 11.03.2015
 *         Time: 13:11
 */
public class VerifySignatureResponse implements Dto {

    private List<DocumentVerifyResult> verifyResults;

    public List<DocumentVerifyResult> getVerifyResults() {
        return verifyResults;
    }

    public void setVerifyResults(List<DocumentVerifyResult> verifyResults) {
        this.verifyResults = verifyResults;
    }
}
