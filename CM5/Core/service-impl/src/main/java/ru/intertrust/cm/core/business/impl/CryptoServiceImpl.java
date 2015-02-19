package ru.intertrust.cm.core.business.impl;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.crypto.CryptoBean;
import ru.intertrust.cm.core.business.api.crypto.CryptoService;
import ru.intertrust.cm.core.business.api.crypto.SignatureDataService;
import ru.intertrust.cm.core.business.api.crypto.SignatureResultService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.crypto.VerifyResult;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.crypto.CryptoSettingsConfig;
import ru.intertrust.cm.core.config.crypto.SignedData;
import ru.intertrust.cm.core.config.crypto.SignedResult;
import ru.intertrust.cm.core.config.crypto.TypeCryptoSettingsConfig;
import ru.intertrust.cm.core.config.event.ConfigurationUpdateEvent;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.model.FatalException;

@Stateless
@Local(CryptoService.class)
@Remote(CryptoService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class CryptoServiceImpl implements CryptoService, ApplicationListener<ConfigurationUpdateEvent> {

    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private ApplicationContext context;
    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    private CryptoBean cryptoBean;
    private Map<String, TypeCryptoSettingsConfig> typeCryptoConfigs = new Hashtable<String, TypeCryptoSettingsConfig>();

    @PostConstruct
    private void init() {
        if (configurationExplorer.getGlobalSettings() != null) {
            String serverSignatureVerifierBeanName = configurationExplorer.getGlobalSettings().getCryptoSettingsConfig().getServerSignatureVerifierBeanName();
            cryptoBean = (CryptoBean) context.getBean(serverSignatureVerifierBeanName);
        }
    }

    @Override
    public VerifyResult verify(InputStream document) {
        return cryptoBean.verify(document);
    }

    @Override
    public VerifyResult verify(InputStream document, byte[] signature) {
        return cryptoBean.verify(document, signature);
    }

    @Override
    public VerifyResult verify(InputStream document, byte[] signature, byte[] signerSertificate) {
        return cryptoBean.verify(document, signature, signerSertificate);
    }

    @Override
    public VerifyResult verify(InputStream document, byte[] signature, Id personId) {
        return cryptoBean.verify(document, signature, personId);
    }

    @Override
    public VerifyResult verify(Id documentId) {
        return cryptoBean.verify(documentId);
    }

    @Override
    public CryptoSettingsConfig getCryptoSettingsConfig() {
        return configurationExplorer.getGlobalSettings().getCryptoSettingsConfig();
    }

    @Override
    public SignedData getSignedData(Id rootId) {
        String signedType = domainObjectTypeIdCache.getName(rootId);
        TypeCryptoSettingsConfig config = getTypeCryptoSettingsConfig(signedType);
        SignedData result = new SignedData();
        if (config != null) {
            String getContentBeanName = config.getGetContentBeanName();
            SignatureDataService signatureDataService = (SignatureDataService) context.getBean(getContentBeanName);
            result = signatureDataService.getSignedData(config.getGetContentBeanSettings(), rootId);
        } else {
            throw new FatalException("Type crypto settings not found for type " + signedType);
        }
        return result;
    }

    private TypeCryptoSettingsConfig getTypeCryptoSettingsConfig(String rootTypeName) {
        //Ищем сначала в кэше
        TypeCryptoSettingsConfig typeSignatureConfig = typeCryptoConfigs.get(rootTypeName);

        if (typeSignatureConfig == null) {
            //Ищем непосредственно для типа
            typeSignatureConfig = configurationExplorer.getConfig(TypeCryptoSettingsConfig.class, rootTypeName);

            //Ищем для родительских типов
            if (typeSignatureConfig == null) {
                String parentType = rootTypeName;
                while (parentType != null && typeSignatureConfig == null) {
                    parentType = configurationExplorer.getDomainObjectParentType(parentType);
                    if (parentType != null) {
                        typeSignatureConfig = configurationExplorer.getConfig(TypeCryptoSettingsConfig.class, parentType);
                    }
                }
            }

            //Ищем конфигурацию по умолчанию с name="*"
            if (typeSignatureConfig == null) {
                typeSignatureConfig = configurationExplorer.getConfig(TypeCryptoSettingsConfig.class, "*");
            }

            //Записываем в кэш
            if (typeSignatureConfig != null) {
                typeCryptoConfigs.put(rootTypeName, typeSignatureConfig);
            }
        }

        return typeSignatureConfig;
    }

    @Override
    public void saveSignedResult(SignedResult signedResult) {
        String signedType = domainObjectTypeIdCache.getName(signedResult.getRootId());
        TypeCryptoSettingsConfig config = getTypeCryptoSettingsConfig(signedType);

        if (config != null) {
            String saveSignatureBeanName = config.getSaveSignatureBeanName();
            SignatureResultService signatureResultService = (SignatureResultService) context.getBean(saveSignatureBeanName);
            signatureResultService.saveSignatureresult(config.getSaveSignatureBeanSettings(), signedResult);
        } else {
            throw new FatalException("Type crypto settings not found for type " + signedType);
        }
    }

    @Override
    public void onApplicationEvent(ConfigurationUpdateEvent event) {
        //Очищаем кэш
        typeCryptoConfigs.clear();

    }

}
