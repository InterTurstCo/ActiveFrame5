package ru.intertrust.cm.crypto;

import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;

import ru.intertrust.cm.core.model.FatalException;

public class CAdESSignature {
    private byte[] signature;
    private byte[] document;
    private CMSSignedData signedData;
    private List<CAdESSigner> signers = new ArrayList<CAdESSigner>();

    public CAdESSignature(byte[] signature, byte[] document) {
        this.document = document;
        this.signature = signature;
        parse();
    }

    public CAdESSignature(byte[] signature) {
        this.signature = signature;
        parse();
    }

    public void parse() {
        try {
            signedData = new CMSSignedData(new CMSProcessableByteArray(document), signature);

            SignerInformationStore signers = signedData.getSignerInfos();
            for (Object signerObj : signers.getSigners()) {
                SignerInformation signer = (SignerInformation) signerObj;
                CAdESSigner cadesSigner = new CAdESSigner(signedData.getCertificates(), signer);
                this.signers.add(cadesSigner);

                //printAttributes(signer);
            }
        } catch (Exception ex) {
            throw new FatalException("Error parcing signature", ex);
        }
    }

    public List<CAdESSigner> getSigners() {
        return signers;
    }

    /*private void printAttributes(SignerInformation signer) {
        System.out.println("Signed");
        for (Object key : signer.getSignedAttributes().toHashtable().keySet()) {
            Attribute value = (Attribute) signer.getSignedAttributes().toHashtable().get(key);
            System.out.println(" Attribute" +
                    "\n\ttype : " + value.getAttrType().getId() +
                    "\n\tvalue: " + value.getAttrValues());
        }

        System.out.println("Unsigned");
        for (Object key : signer.getUnsignedAttributes().toHashtable().keySet()) {
            Attribute value = (Attribute) signer.getUnsignedAttributes().toHashtable().get(key);
            System.out.println(" Attribute" +
                    "\n\ttype : " + value.getAttrType().getId() +
                    "\n\tvalue: " + value.getAttrValues());
        }
    }*/
}
