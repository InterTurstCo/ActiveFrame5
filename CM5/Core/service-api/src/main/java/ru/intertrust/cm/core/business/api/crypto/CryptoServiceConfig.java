package ru.intertrust.cm.core.business.api.crypto;

import ru.intertrust.cm.core.config.crypto.CryptoSettingsConfig;

public interface CryptoServiceConfig {
    /**
     * Получение конфигураций из глобальных настроек крипто модуля и глобальных настроек приложения
     * @return
     */
    CryptoSettingsConfig getCryptoSettingsConfig();
}
