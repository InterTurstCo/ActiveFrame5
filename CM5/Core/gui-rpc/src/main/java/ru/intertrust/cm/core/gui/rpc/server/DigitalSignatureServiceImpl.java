package ru.intertrust.cm.core.gui.rpc.server;

import javax.ejb.EJB;
import javax.servlet.annotation.WebServlet;

import ru.intertrust.cm.core.business.api.crypto.CryptoService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.crypto.SignedData;
import ru.intertrust.cm.core.config.crypto.SignedResult;
import ru.intertrust.cm.core.gui.model.crypto.DigitalSignatureConfig;
import ru.intertrust.cm.core.gui.rpc.api.DigitalSignatureService;

@WebServlet(name = "DigitalSignatureService", urlPatterns = "/remote/DigitalSignatureService")
public class DigitalSignatureServiceImpl extends BaseService implements DigitalSignatureService {
    private static final long serialVersionUID = 95640497000848408L;
    
    @EJB
    private CryptoService cryptoService; 

    @Override
    public DigitalSignatureConfig getConfig() {
        DigitalSignatureConfig config = new DigitalSignatureConfig();
        config.setCanSigned(true);
        config.setCryptoSettingsConfig(cryptoService.getCryptoSettingsConfig());
        return config;
    }

    @Override
    public SignedData getSignedData(Id rootId) {
        return cryptoService.getSignedData(rootId);
    }

    @Override
    public void saveSignResult(SignedResult result) {
        cryptoService.saveSignedResult(result);
    }

}
