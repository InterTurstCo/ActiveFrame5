package ru.intertrust.cm.core.gui.impl.server.widget;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.crypto.CryptoService;
import ru.intertrust.cm.core.business.api.crypto.CryptoServiceConfig;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.crypto.SignedDataItem;
import ru.intertrust.cm.core.config.crypto.SignedResult;
import ru.intertrust.cm.core.config.crypto.SignedResultItem;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.crypto.DigitalSignatureConfig;
import ru.intertrust.cm.core.gui.model.crypto.GuiSignedData;
import ru.intertrust.cm.core.gui.model.crypto.GuiSignedDataItem;
import ru.intertrust.cm.core.gui.model.crypto.VerifySignatureResponse;
import ru.intertrust.cm.core.model.FatalException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 11.03.2015
 *         Time: 12:33
 */

@ComponentName("digital.signature")
public class DigitalSignatureHandler implements ComponentHandler {

    @Autowired
    private CryptoService cryptoService;
    @Autowired
    private CryptoServiceConfig cryptoServiceConfig;
    
    public DigitalSignatureConfig getConfig(Dto dummy) {
        DigitalSignatureConfig config = new DigitalSignatureConfig();
        config.setCanSigned(true);
        config.setCryptoSettingsConfig(cryptoServiceConfig.getCryptoSettingsConfig());
        return config;
    }

    public GuiSignedData getSignedData(Dto rootId) {

        InputStream contentStream = null;
        try {
            GuiSignedData result = new GuiSignedData();

            List<Id> dataForSign = cryptoService.getBatchForSignature((Id)rootId);
            result.setRootId((Id)rootId);
            for (Id id : dataForSign) {
                SignedDataItem signedDataItem = cryptoService.getContentForSignature(id);
                GuiSignedDataItem guiSignedDataItem = new GuiSignedDataItem();
                guiSignedDataItem.setId(signedDataItem.getId());
                guiSignedDataItem.setName(signedDataItem.getName());
                String content = null;
                contentStream = signedDataItem.getContent();
                //В случае хеширования на сервере формируем хаш в base16 кодировке
                //В случае хеширования на клиенте формируем вложение в виде base64 строки
                if (cryptoServiceConfig.getCryptoSettingsConfig().getHashOnServer()) {
                    byte[] hash = cryptoService.hash(contentStream);
                    content = Hex.encodeHexString(hash);
                } else {
                    ByteArrayOutputStream attachmentBytes = new ByteArrayOutputStream();
                    int read = 0;
                    byte[] buffer = new byte[1024];
                    while ((read = contentStream.read(buffer)) > 0) {
                        attachmentBytes.write(buffer, 0, read);
                    }
                    content = Base64.encodeBase64String(attachmentBytes.toByteArray());
                }
                contentStream.close();
                contentStream = null;

                guiSignedDataItem.setContent(content);
                result.getSignedDataItems().add(guiSignedDataItem);
            }
            return result;
        } catch (Exception ex) {
            throw new FatalException("Error get signed data", ex);
        } finally{
            if (contentStream != null){
                try {
                    contentStream.close();
                } catch (IOException ignoreEx) {
                }
            }
        }
    }

    public Dto saveSignResult(Dto signedResult) {
        SignedResult result = (SignedResult)signedResult;
        for (SignedResultItem signedResultItem : result.getSignedResultItems()) {
            cryptoService.saveSignedResult(signedResultItem);
        }
        return null;
    }

    public VerifySignatureResponse verify(Dto documentId) {
        Id id = (Id)documentId;
        VerifySignatureResponse response = new VerifySignatureResponse();
        response.setVerifyResults(cryptoService.verify(id));
        return response;
    }
}
