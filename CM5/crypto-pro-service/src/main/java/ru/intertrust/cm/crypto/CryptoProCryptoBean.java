package ru.intertrust.cm.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ru.intertrust.cm.core.business.api.crypto.CryptoBean;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.crypto.VerifyResult;

public class CryptoProCryptoBean implements CryptoBean {
    private static final String TAG = "***";

    @Override
    public VerifyResult verify(InputStream document) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VerifyResult verify(InputStream document, byte[] signature) {
        /*try {
            byte[] documentAsByteArray = readStream(document);
            CAdESSignature cAdESSignature = new CAdESSignature(signature, documentAsByteArray, CAdESType.CAdES_X_Long_Type_1);
            
            int signerIndex=0;
            for (CAdESSigner signer : cAdESSignature.getCAdESSignerInfos()) {
                printSignerInfo(signer, signerIndex++, "");
            }
            
            cAdESSignature.verify(null);
            return null;
        } catch (Exception ex) {
            throw new FatalException("Error verify signature", ex);
        }*/
        return null;
    }

    @Override
    public VerifyResult verify(InputStream document, byte[] signature, byte[] signerSertificate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VerifyResult verify(InputStream document, byte[] signature, Id personId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VerifyResult verify(Id documrntId) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Получение файла в виде массива байт
     * @param file
     * @return
     * @throws IOException
     */
    protected byte[] readStream(InputStream document) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read = 0;
        byte[] buffer = new byte[1024];
        while ((read = document.read(buffer)) > 0) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }
    
    /**
     * Вывод информации об отдельной подписи.
     * 
     * @param signer Подпись.
     * @param index Индекс подписи.
     * @param tab Отступ для удобства печати.
     */
    /*private void printSignerInfo(CAdESSigner signer, int index, String tab) {
        
        X509Certificate signerCert = signer.getSignerCertificate();
        
        System.out.println(tab + " Signature #" + index + " (" + 
            CAdESType.getSignatureTypeName(signer.getSignatureType()) + ")" + 
            (signerCert != null ? (" verified by " + signerCert.getSubjectDN()) : "" ));

        if ( signer.getSignatureType().equals(CAdESType.CAdES_X_Long_Type_1) ) {
                            
            TimeStampToken signatureTimeStamp = signer.getSignatureTimestampToken();
            TimeStampToken cadesCTimeStamp = signer.getCAdESCTimestampToken();
            
            if (signatureTimeStamp != null) {
                System.out.println(tab + TAG + " Signature timestamp set: " + 
                    signatureTimeStamp.getTimeStampInfo().getGenTime());
            } // if
            
            if (cadesCTimeStamp != null) {
                System.out.println(tab + TAG + " CAdES-C timestamp set: " + 
                    cadesCTimeStamp.getTimeStampInfo().getGenTime());
            } // if

        } // if

        printSignerAttributeTableInfo(index,
            signer.getSignerSignedAttributes(), "signed");

        printSignerAttributeTableInfo(index,
            signer.getSignerUnsignedAttributes(), "unsigned");

        printCountersignerInfos(signer.getCAdESCountersignerInfos());
    }*/
    
    /**
     * Вывод содержимого таблицы аттрибутов.
     *
     * @param i Номер подписанта.
     * @param table Таблица с аттрибутами.
     * @param type Тип таблицы: "signed" или "unsigned".
     */
    /*public  void printSignerAttributeTableInfo(int i, AttributeTable table,
        String type) {

        if (table == null) {
            return;
        } // if

        System.out.println("Signer #" + i + " has " + table.size() + " " +
            type + " attributes.");

        Hashtable attributes = table.toHashtable();
        Enumeration attributesEnum = attributes.elements();

        while (attributesEnum.hasMoreElements()) {

            Attribute attribute = Attribute.getInstance(attributesEnum.nextElement());
            System.out.println(" Attribute" +
                "\n\ttype : " + attribute.getAttrType().getId() +
                "\n\tvalue: " + attribute.getAttrValues());

        } // while
    }*/
    
    /**
     * Вывод информации о заверителях отдельной подписи.
     * 
     * @param countersigners Список заверителей.
     */
    /*private void printCountersignerInfos(CAdESSigner[] countersigners) {

        System.out.println("$$$ Print counter signature information $$$");

        // Заверяющие подписи.
        int countersignerIndex = 1;
        for (CAdESSigner countersigner : countersigners) {
            printSignerInfo(countersigner, countersignerIndex++, TAG);
        }
    }*/

}
