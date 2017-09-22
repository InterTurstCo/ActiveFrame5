package ru.intertrust.cm.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.GOST3411Digest;
import org.bouncycastle.crypto.digests.GOST3411_2012_256Digest;
import org.bouncycastle.crypto.io.DigestInputStream;
import org.bouncycastle.tsp.TimeStampToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.intertrust.cm.core.business.api.crypto.CryptoBean;
import ru.intertrust.cm.core.business.api.dto.crypto.SignerInfo;
import ru.intertrust.cm.core.business.api.dto.crypto.VerifyResult;
import ru.intertrust.cm.core.model.FatalException;

public class BouncycastleCryptoBean implements CryptoBean {
    private static final Logger logger = LoggerFactory.getLogger(BouncycastleCryptoBean.class);

    @Override
    public VerifyResult verify(InputStream document) {
        VerifyResult result = new VerifyResult();
        try {
            byte[] documentAsByteArray = readStream(document);

            CAdESSignature cadesSignature = new CAdESSignature(documentAsByteArray);

            for (CAdESSigner signer : cadesSignature.getSigners()) {
                SignerInfo signerInfo = getSignerInfo(signer);
                result.getSignerInfos().add(signerInfo);
            }

            return result;
        } catch (Exception ex) {
            throw new FatalException("Error verify signature", ex);
        }
    }

    @Override
    public VerifyResult verify(InputStream document, byte[] signature) {
        VerifyResult result = new VerifyResult();
        try {
            byte[] documentAsByteArray = readStream(document);

            CAdESSignature cadesSignature = new CAdESSignature(signature, documentAsByteArray);

            for (CAdESSigner signer : cadesSignature.getSigners()) {
                SignerInfo signerInfo = getSignerInfo(signer);
                result.getSignerInfos().add(signerInfo);
            }

            return result;
        } catch (Exception ex) {
            throw new FatalException("Error verify signature", ex);
        }
    }

    @Override
    public VerifyResult verify(InputStream document, byte[] signature, byte[] signerSertificate) {
        throw new UnsupportedOperationException();
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

    protected SignerInfo getSignerInfo(CAdESSigner signer) {
        SignerInfo signerInfo = new SignerInfo();
        try {
            signer.verify();
            verifyTimestamp(signer);
            X509Certificate cer = signer.getCertificate();
            //Формируем имя
            signerInfo.setName(getName(cer.getSubjectDN().getName()));
            signerInfo.setCertificateId(cer.getSerialNumber().toString());
            signerInfo.setCertificateValidFrom(cer.getNotBefore());
            signerInfo.setCertificateValidTo(cer.getNotAfter());
            signerInfo.setValid(true);
            signerInfo.setSubject(cer.getSubjectDN().getName());
            signerInfo.setIssuer(cer.getIssuerDN().getName());            
            TimeStampToken tst = signer.getSignatureTimestampToken();
            if (tst != null){
                signerInfo.setSignDate(tst.getTimeStampInfo().getGenTime());
            }else{
                signerInfo.setSignDate(signer.getSignatureDate());
            }
        } catch (Exception ex) {
            signerInfo.setValid(false);
            signerInfo.setError(ex.toString());
            logger.error("Error verify signature", ex);
        }
        return signerInfo;
    }

    private void verifyTimestamp(CAdESSigner signer) {
        if (signer.getSignatureType().equals(CAdESSigner.CAdES_X_Long_Type_1)) {

            TimeStampToken signTimestamp = signer.getSignatureTimestampToken();
            if (signTimestamp == null) {
                throw new FatalException("Signature timestamp is null");
            } // if

            TimeStampToken cdsCTimestamp = signer.getCAdESCTimestampToken();
            if (cdsCTimestamp == null) {
                throw new FatalException("CAdES-C timestamp is null");
            } // if
        }else{
            
        }
    }

    protected String getName(String subjectDN) throws IOException {
        Map<String, String> subjectDnMap = new HashMap<String, String>();
        String[] subjectDNArray = subjectDN.split(",");
        for (String subjectItem : subjectDNArray) {
            String[] subjectItemArray = subjectItem.split("=");
            subjectDnMap.put(subjectItemArray[0].toUpperCase().trim(), subjectItemArray[1].trim());
        }

        String result = "";
        //Пытаемся вытащить фамилию и имя
        if (subjectDnMap.get("SURNAME") != null) {
            result += (String) subjectDnMap.get("SURNAME");
        }
        if (subjectDnMap.get("GIVENNAME") != null) {
            if (!result.isEmpty()) {
                result += " ";
            }
            result += (String) subjectDnMap.get("GIVENNAME");
        }

        //Если нет фасмилии и имени выраскиваем CN
        if (result.isEmpty()) {
            result += (String) subjectDnMap.get("CN");
        }
        return result;
    }

    @Override
    public byte[] hash(InputStream document) {
        try {
            // создание объекта хеширования данных
            //final Digest digest = new GOST3411Digest();
            final Digest digest = new GOST3411_2012_256Digest();

            // обработка хешируемых данных
            final DigestInputStream digestStream = new DigestInputStream(document, digest);
            while (digestStream.available() != 0) {
                digestStream.read();
            }

            // вычисление значения хеша
            byte[]  resBuf = new byte[digest.getDigestSize()];
            digest.doFinal(resBuf, 0);
            return resBuf;
        } catch (Exception ex) {
            throw new FatalException("Error claculate document hash", ex);
        }
        
    }

}
