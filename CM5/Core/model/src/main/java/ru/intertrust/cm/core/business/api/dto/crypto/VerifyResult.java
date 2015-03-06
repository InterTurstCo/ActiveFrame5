package ru.intertrust.cm.core.business.api.dto.crypto;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Структура для передачи информации о валидности ЭЦП
 * @author larin
 * 
 */
public class VerifyResult implements Dto {
    private static final long serialVersionUID = -7995499379143873587L;
    private List<SignerInfo> signerInfos = new ArrayList<SignerInfo>();

    public List<SignerInfo> getSignerInfos() {
        return signerInfos;
    }

    public void setSignerInfos(List<SignerInfo> signerInfos) {
        this.signerInfos = signerInfos;
    }

    @Override
    public String toString() {
        String result = "*****************************************************************************\n";
        for (SignerInfo signerInfo : signerInfos) {
            result += "*****************************************************************************\n";
            result += "* Certificate ID: " + signerInfo.getCertificateId() + "\n";
            result += "* Valid from: " + signerInfo.getCertificateValidFrom() + " to " + signerInfo.getCertificateValidTo() + "\n";
            result += "* Signer: " + signerInfo.getName() + "\n";
            result += "* Signature is: " + (signerInfo.isValid() ? "VALID" : "INVALID") + "\n";
            result += "* Signature date is: " + signerInfo.getSignDate() + "\n";
            if (!signerInfo.isValid()) {
                result += "* Error: " + signerInfo.getError() + "\n";
            }
            result += "*****************************************************************************" + "\n";
        }
        result += "*****************************************************************************" + "\n";
        return result;
    }

}
