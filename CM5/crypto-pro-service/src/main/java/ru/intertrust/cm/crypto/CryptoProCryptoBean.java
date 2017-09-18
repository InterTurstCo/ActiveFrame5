package ru.intertrust.cm.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.tsp.TimeStampToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.CAdESSigner;
import ru.CryptoPro.CAdES.CAdESType;
import ru.intertrust.cm.core.business.api.crypto.CryptoBean;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.crypto.SignerInfo;
import ru.intertrust.cm.core.business.api.dto.crypto.VerifyResult;
import ru.intertrust.cm.core.model.FatalException;

public class CryptoProCryptoBean implements CryptoBean {
    private static final Logger logger = LoggerFactory.getLogger(CryptoProCryptoBean.class);
    public static final String DIGEST_ALG_2001 = "GOST3411";

    @Override
    public VerifyResult verify(InputStream document) {
        try {
            VerifyResult result = new VerifyResult();
            byte[] documentAsByteArray = readStream(document);
            CAdESSignature cAdESSignature = new CAdESSignature(documentAsByteArray, null, null);

            for (CAdESSigner signer : cAdESSignature.getCAdESSignerInfos()) {
                SignerInfo signerInfo = getSignerInfo(signer);
                result.getSignerInfos().add(signerInfo);
            }
            return result;

        } catch (Exception ex) {
            throw new FatalException("Error on verify signature", ex);
        }
    }

    @Override
    public VerifyResult verify(InputStream document, byte[] signature) {
        try {
            VerifyResult result = new VerifyResult();
            byte[] documentAsByteArray = readStream(document);
            CAdESSignature cAdESSignature = new CAdESSignature(signature, documentAsByteArray, null);

            for (CAdESSigner signer : cAdESSignature.getCAdESSignerInfos()) {
                SignerInfo signerInfo = getSignerInfo(signer);
                result.getSignerInfos().add(signerInfo);
            }
            return result;

        } catch (Throwable ex) {
            throw new FatalException("Error on verify signature", ex);
        }
    }

    protected SignerInfo getSignerInfo(CAdESSigner signer) {
        SignerInfo signerInfo = new SignerInfo();
        try {
            signer.verify(null, null, null, true);
            verifyTimestamp(signer);
            X509Certificate cer = signer.getSignerCertificate();
            //Формируем имя
            signerInfo.setName(getName(cer.getSubjectDN().getName()));
            signerInfo.setCertificateId(cer.getSerialNumber().toString());
            signerInfo.setCertificateValidFrom(cer.getNotBefore());
            signerInfo.setCertificateValidTo(cer.getNotAfter());
            signerInfo.setValid(true);
            signerInfo.setSubject(cer.getSubjectDN().getName());
            signerInfo.setIssuer(cer.getIssuerDN().getName());
            TimeStampToken tst = signer.getSignatureTimestampToken();
            signerInfo.setSignDate(tst.getTimeStampInfo().getGenTime());
        } catch (Exception ex) {
            signerInfo.setValid(false);
            signerInfo.setError(ex.toString());
            logger.error("Error verify signature", ex);
        }
        return signerInfo;
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

        //Если нет фамилии и имени вытаскиваем CN
        if (result.isEmpty()) {
            result += (String) subjectDnMap.get("CN");
        }
        return result;
    }

    protected void verifyTimestamp(CAdESSigner signer) {
        if (signer.getSignatureType().equals(CAdESType.CAdES_X_Long_Type_1)) {

            TimeStampToken signTimestamp = signer.getSignatureTimestampToken();
            if (signTimestamp == null) {
                throw new FatalException("Signature timestamp is null");
            } // if

            TimeStampToken cdsCTimestamp = signer.getCAdESCTimestampToken();
            if (cdsCTimestamp == null) {
                throw new FatalException("CAdES-C timestamp is null");
            } // if

            Collection<TimeStampToken> signTimestampList = signer.getSignatureTimestampTokenList();
            if (signTimestampList == null) {
                throw new FatalException("Signature timestamp list is null");
            } // if

            int sz = signTimestampList.size();
            if (sz != 1) {
                throw new FatalException("It is weird... Size of signature timestamp " +
                        "list is more than 1 (" + sz + ")");
            } // if

            Collection<TimeStampToken> cdsCTimestampList = signer.getCAdESCTimestampTokenList();
            if (cdsCTimestampList == null) {
                throw new FatalException("CAdES-C timestamp list is null");
            } // if

            sz = cdsCTimestampList.size();
            if (sz != 1) {
                throw new FatalException("It is weird... Size of CAdES-C timestamp " +
                        "list is more than 1 (" + sz + ")");
            } // if

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

    @Override
    public byte[] hash(InputStream document) {
        try {
            // создание объекта хеширования данных
            final MessageDigest digest = MessageDigest.getInstance("GOST3411_2012_256" /*DIGEST_ALG_2001*/);

            // обработка хешируемых данных
            final DigestInputStream digestStream = new DigestInputStream(document, digest);
            while (digestStream.available() != 0) {
                digestStream.read();
            }

            // вычисление значения хеша
            return digest.digest();
        } catch (Exception ex) {
            throw new FatalException("Error claculate document hash", ex);
        }
    }

}
