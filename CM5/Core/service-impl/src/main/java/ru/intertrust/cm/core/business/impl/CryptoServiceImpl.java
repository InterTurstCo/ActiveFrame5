package ru.intertrust.cm.core.business.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.crypto.CryptoBean;
import ru.intertrust.cm.core.business.api.crypto.CryptoService;
import ru.intertrust.cm.core.business.api.crypto.CryptoServiceConfig;
import ru.intertrust.cm.core.business.api.crypto.SignatureDataService;
import ru.intertrust.cm.core.business.api.crypto.SignatureStorageService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.crypto.DocumentVerifyResult;
import ru.intertrust.cm.core.business.api.dto.crypto.VerifyResult;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.crypto.SignedDataItem;
import ru.intertrust.cm.core.config.crypto.SignedResultItem;
import ru.intertrust.cm.core.config.crypto.TypeCryptoSettingsConfig;
import ru.intertrust.cm.core.config.event.ConfigurationUpdateEvent;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.model.RemoteSuitableException;
import ru.intertrust.cm.core.model.SystemException;

@Stateless(name = "CryptoService")
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
    @Autowired
    private CrudService crudService;
    @Autowired
    private CryptoServiceConfig cryptoServiceConfig;

    private Map<String, TypeCryptoSettingsConfig> typeCryptoConfigs = new Hashtable<String, TypeCryptoSettingsConfig>();

    private CryptoBean getCryptoBean() {
        String cryptoBeanComponent = cryptoServiceConfig.getCryptoSettingsConfig().getServerComponentName();
        return (CryptoBean) context.getBean(cryptoBeanComponent);
    }

    @Override
    public VerifyResult verify(InputStream document) {
        try {
            return getCryptoBean().verify(document);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public VerifyResult verify(InputStream document, byte[] signature) {
        try {
            return getCryptoBean().verify(document, signature);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public VerifyResult verify(InputStream document, byte[] signature, byte[] signerSertificate) {
        try {
            return getCryptoBean().verify(document, signature, signerSertificate);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public List<DocumentVerifyResult> verify(Id documentId) {
        String rootType = domainObjectTypeIdCache.getName(documentId);
        TypeCryptoSettingsConfig rootConfig = getTypeCryptoSettingsConfig(rootType);
        InputStream contentStream = null;
        try {
            if (rootConfig != null) {
                List<DocumentVerifyResult> result = new ArrayList<DocumentVerifyResult>();

                List<Id> signedDocuments = getBatchForSignature(documentId);
                //Цикл по подписанным документам в пачке
                for (Id id : signedDocuments) {
                    DocumentVerifyResult verifyResult = new DocumentVerifyResult();
                    verifyResult.setDocumentId(id);
                    DomainObject signedDo = crudService.find(id);

                    //Получение всех подписей
                    String signedType = domainObjectTypeIdCache.getName(id);
                    TypeCryptoSettingsConfig config = getTypeCryptoSettingsConfig(signedType);
                    if (config != null) {
                        String signatureStorageBeanName = config.getSignatureStorageBeanName();
                        SignatureStorageService signatureStorageService = (SignatureStorageService) context
                            .getBean(signatureStorageBeanName);
                        List<SignedResultItem> signes = signatureStorageService
                            .loadSignature(config.getSignatureStorageBeanSettings(), id);

                        //Цикл по всем подписям
                        for (SignedResultItem signedResultItem : signes) {
                            if (signedResultItem.getId().equals(id)) {
                                SignedDataItem signedDataItem = getContentForSignature(id);
                                contentStream = signedDataItem.getContent();
                                verifyResult.setDocumentName(signedDataItem.getName());

                                VerifyResult oneResult = getCryptoBean().verify(contentStream,
                                    Base64.decodeBase64(signedResultItem.getSignature()));
                                verifyResult.getSignerInfos().addAll(oneResult.getSignerInfos());
                                contentStream.close();
                                contentStream = null;
                            }
                        }
                    }
                    result.add(verifyResult);
                }

                return result;
            } else {
                throw new FatalException("Type crypto settings not found for type " + rootType);
            }
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new FatalException("Error verify attachments signature", ex);
        } finally {
            try {
                if (contentStream != null) {
                    contentStream.close();
                }
            } catch (Exception ignoreEx) {
            }
        }
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
    public void saveSignedResult(SignedResultItem signedResult) {
        try {
            String signedType = domainObjectTypeIdCache.getName(signedResult.getId());
            TypeCryptoSettingsConfig config = getTypeCryptoSettingsConfig(signedType);

            if (config != null) {
                String signatureStorageBeanName = config.getSignatureStorageBeanName();
                SignatureStorageService signatureResultService = (SignatureStorageService) context.getBean(signatureStorageBeanName);
                signatureResultService.saveSignature(config.getSignatureStorageBeanSettings(), signedResult);
            } else {
                throw new FatalException("Type crypto settings not found for type " + signedType);
            }
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public void onApplicationEvent(ConfigurationUpdateEvent event) {
        try {
            //Очищаем кэш
            typeCryptoConfigs.clear();
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public byte[] hash(InputStream document) {
        try {
            return getCryptoBean().hash(document);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public SignedDataItem getContentForSignature(Id id) {
        try {
            String signedType = domainObjectTypeIdCache.getName(id);
            TypeCryptoSettingsConfig config = getTypeCryptoSettingsConfig(signedType);
            SignedDataItem result = null;
            if (config != null) {
                String getContentBeanName = config.getGetContentBeanName();
                SignatureDataService signatureDataService = (SignatureDataService) context.getBean(getContentBeanName);
                result = signatureDataService.getContentForSignature(config.getGetContentBeanSettings(), id);
            } else {
                throw new FatalException("Type crypto settings not found for type " + signedType);
            }
            return result;
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public List<Id> getBatchForSignature(Id rootId) {
        try {
            String signedType = domainObjectTypeIdCache.getName(rootId);
            TypeCryptoSettingsConfig config = getTypeCryptoSettingsConfig(signedType);
            List<Id> result = null;
            if (config != null) {
                String getContentBeanName = config.getGetContentBeanName();
                SignatureDataService signatureDataService = (SignatureDataService) context.getBean(getContentBeanName);
                result = signatureDataService.getBatchForSignature(config.getGetContentBeanSettings(), rootId);
            } else {
                throw new FatalException("Type crypto settings not found for type " + signedType);
            }
            return result;
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

}
