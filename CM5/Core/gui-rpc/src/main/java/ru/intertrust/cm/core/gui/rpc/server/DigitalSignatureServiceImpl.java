package ru.intertrust.cm.core.gui.rpc.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.ejb.EJB;
import javax.servlet.annotation.WebServlet;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.crypto.CryptoService;
import ru.intertrust.cm.core.business.api.crypto.CryptoServiceConfig;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.crypto.DocumentVerifyResult;
import ru.intertrust.cm.core.config.crypto.SignedData;
import ru.intertrust.cm.core.config.crypto.SignedDataItem;
import ru.intertrust.cm.core.config.crypto.SignedResult;
import ru.intertrust.cm.core.config.crypto.SignedResultItem;
import ru.intertrust.cm.core.gui.model.crypto.DigitalSignatureConfig;
import ru.intertrust.cm.core.gui.model.crypto.GuiSignedData;
import ru.intertrust.cm.core.gui.model.crypto.GuiSignedDataItem;
import ru.intertrust.cm.core.gui.rpc.api.DigitalSignatureService;
import ru.intertrust.cm.core.model.FatalException;

@WebServlet(name = "DigitalSignatureService", urlPatterns = "/remote/DigitalSignatureService")
public class DigitalSignatureServiceImpl extends BaseService implements DigitalSignatureService {
    private static final long serialVersionUID = 95640497000848408L;

    @EJB
    private CryptoService cryptoService;
    @Autowired
    private CryptoServiceConfig cryptoServiceConfig;
    
    @Override
    public DigitalSignatureConfig getConfig() {
        DigitalSignatureConfig config = new DigitalSignatureConfig();
        config.setCanSigned(true);
        config.setCryptoSettingsConfig(cryptoServiceConfig.getCryptoSettingsConfig());
        return config;
    }

    @Override
    public GuiSignedData getSignedData(Id rootId) {
        InputStream contentStream = null;
        try {
            GuiSignedData result = new GuiSignedData();
            
            List<Id> dataForSign = cryptoService.getBatchForSignature(rootId);
            result.setRootId(rootId);
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
        }finally{
            if (contentStream != null){
                try {
                    contentStream.close();
                } catch (IOException ignoreEx) {
                }                
            }
        }
    }

    @Override
    public void saveSignResult(SignedResult result) {
        for (SignedResultItem signedResultItem : result.getSignedResultItems()) {
            cryptoService.saveSignedResult(signedResultItem);
        }
    }

    @Override
    public List<DocumentVerifyResult> verify(Id documentId) {        
        return cryptoService.verify(documentId);
    }

}
