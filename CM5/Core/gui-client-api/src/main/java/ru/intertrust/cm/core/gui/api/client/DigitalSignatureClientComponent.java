package ru.intertrust.cm.core.gui.api.client;

import com.google.gwt.core.client.Callback;

import ru.intertrust.cm.core.gui.model.crypto.DigitalSignatureConfig;

/**
 * Клиентская компонента ЭП. Получает контент для подписи и формирует ЭП
 * @author larin
 *
 */
public abstract class DigitalSignatureClientComponent extends BaseComponent{
    
    /**
     * Подписать контент. 
     * @param сontent контент может присылаться весь в формате base64 или только его hash в формате base16, в зависимости от настроек сервера 
     * CryptoSettingsConfig.getHashOnServer()
     * @param callback 
     * @return возвращает ЭП в формате base64
     */
    public abstract String sign(String сontent, Callback<String, String> callback);
    
    /**
     * Инициализация компоненты. Например на данном этапе можно вывести диалог со всеми доступными сертификатами для подписи, или выбора крипто контейнера
     * @param config конфигурация криптомодуля
     * @param onInit CallBack объект. После окончания инициализации необходимо вызвать метод onInit.onInit() или onInit.onCancel() или onError() в зависимости от результата инициализации 
     */
    public abstract void init(DigitalSignatureConfig config, DigitalSignatureComponentInitHandler onInit);
}
