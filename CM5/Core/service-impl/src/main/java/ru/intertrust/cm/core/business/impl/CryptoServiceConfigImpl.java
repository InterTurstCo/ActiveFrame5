package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.GlobalServerSettingsService;
import ru.intertrust.cm.core.business.api.crypto.CryptoService;
import ru.intertrust.cm.core.business.api.crypto.CryptoServiceConfig;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.crypto.CAdESCryptoSettingsConfig;
import ru.intertrust.cm.core.config.crypto.CryptoSettingsConfig;
import ru.intertrust.cm.core.model.FatalException;

public class CryptoServiceConfigImpl implements CryptoServiceConfig {
    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private GlobalServerSettingsService globalServerSettingsService;

    @Override
    public CryptoSettingsConfig getCryptoSettingsConfig() {

        try {
            //Получаем глобальные настройки
            CryptoSettingsConfig globalCryptoSettingsConfig = configurationExplorer.getGlobalSettings().getCryptoSettingsConfig();

            if (globalCryptoSettingsConfig == null) {
                throw new FatalException("Crypto Settings not configured in global config");
            }

            CryptoSettingsConfig result = ObjectCloner.getInstance().cloneObject(globalCryptoSettingsConfig);
            //Заменяем параметры из глобальных настроек.

            //Место хэширования, поддерживаются true и false
            Boolean hashOnServer = globalServerSettingsService.getBoolean(CryptoService.HASH_ON_SERVER);
            if (hashOnServer != null) {
                result.setHashOnServer(hashOnServer);
            }

            //Серверная компонента
            String serverComponebt = globalServerSettingsService.getString(CryptoService.SERVER_COMPONENT);
            if (serverComponebt != null) {
                result.setServerComponentName(serverComponebt);
            }            
            
            if (result.getSettings() instanceof CAdESCryptoSettingsConfig) {
                CAdESCryptoSettingsConfig providerConfig = (CAdESCryptoSettingsConfig) result.getSettings();

                //Тип подписи. Поддерживаются CAdES-X b CAdES-BES
                String signatureType = globalServerSettingsService.getString(CryptoService.SIGNATURE_TYPE);
                if (signatureType != null) {
                    providerConfig.setSignatureType(signatureType);
                }
                if (providerConfig.getSignatureType() == null) {
                    providerConfig.setSignatureType(CAdESCryptoSettingsConfig.CADES_BES_SIGNATURE_TYPE);
                }

                //Алгоритм хэшировапния
                String hashAlgorithm = globalServerSettingsService.getString(CryptoService.HASH_ALGORITHM);
                if (hashAlgorithm != null) {
                    providerConfig.setHashAlgorithm(hashAlgorithm);
                }
                if (providerConfig.getHashAlgorithm() == null) {
                    providerConfig.setHashAlgorithm(CryptoService.HASH_ALGORITHM_GOST_3411_2012_256);
                }

                //Сервер штампов времени
                String timeStampServer = globalServerSettingsService.getString(CryptoService.TIME_STAMP_SERVER);
                if (timeStampServer != null) {
                    providerConfig.setTsAddress(timeStampServer);
                    ;
                }
            }

            return result;

        } catch (Exception ex) {
            throw new FatalException("CryptoServiceImpl getCryptoSettingsConfig", ex);
        }
    }

}
