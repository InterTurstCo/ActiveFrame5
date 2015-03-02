package ru.intertrust.cm.core.gui.impl.client.crypto;

import java.util.ArrayList;

import ru.intertrust.cm.core.config.crypto.ExtendedCryptoSettingsConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.DigitalSignatureClientComponent;
import ru.intertrust.cm.core.gui.api.client.DigitalSignatureComponentInitHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.crypto.DigitalSignatureConfig;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.PopupPanel;

@ComponentName("cryptopro.browser.plugin.client.component")
public class CryptoProBrowserPluginClientComponent extends DigitalSignatureClientComponent{
    private DigitalSignatureConfig config;
    private ExtendedCryptoSettingsConfig extendedConfig;
    private JavaScriptObject cryptoTool;

    @Override
    public Component createNew() {
        return new CryptoProBrowserPluginClientComponent();
    }

    @Override
    public void init(DigitalSignatureConfig config, final DigitalSignatureComponentInitHandler handler) {
        this.config = config;
        this.extendedConfig = (ExtendedCryptoSettingsConfig)config.getCryptoSettingsConfig().getSettings();
        nativeInit(this.extendedConfig.getTsAddress(), 
                (this.extendedConfig.getHashOnServer() != null && this.extendedConfig.getHashOnServer()));
        
        if (nativeCheckInstall()){
            JsArrayString allCerts = nativeGetCertificates();
            
            if (allCerts.length() > 1){
                ArrayList<String> validCertificates = new ArrayList<String>();
                for (int i=0; i<allCerts.length(); i++) {
                    validCertificates.add(allCerts.get(i));
                }
                
                final SelectCertificateDialog dialog = new SelectCertificateDialog(validCertificates);
                dialog.addCloseHandler(new CloseHandler<PopupPanel>() {
                    
                    @Override
                    public void onClose(CloseEvent<PopupPanel> event) {
                        if (dialog.getResult() > -1){
                            nativeSetCertificateIndex(dialog.getResult());
                            handler.onInit();
                        }else{
                            handler.onCancel();
                        }
                    }
                });
                dialog.show();
            }else{
                handler.onInit();
            }
        }else{
            handler.onCancel();
            InstallPluginDialog installPluginDialog = new InstallPluginDialog();
            installPluginDialog.show();            
        }
    }
    
    private native void nativeSetCertificateIndex(int certNo)
    /*-{
        var cryptoTool = this.@ru.intertrust.cm.core.gui.impl.client.crypto.CryptoProBrowserPluginClientComponent::cryptoTool;
        return cryptoTool.setCertificate(certNo+1);
    }-*/;

    private native boolean nativeCheckInstall()
    /*-{
        var cryptoTool = this.@ru.intertrust.cm.core.gui.impl.client.crypto.CryptoProBrowserPluginClientComponent::cryptoTool;
        return cryptoTool.checkInstall();
    }-*/;
    
    
    private native void nativeInit(String tsAddress, boolean hashOnServer)
    /*-{
        var cryptoTool = {
            CADESCOM_CADES_X_LONG_TYPE_1: 0x5d,
            CAPICOM_CURRENT_USER_STORE: 2,
            CAPICOM_MY_STORE: "My",
            CAPICOM_STORE_OPEN_MAXIMUM_ALLOWED: 2,
            CAPICOM_CERTIFICATE_FIND_SUBJECT_NAME: 1,
            CAPICOM_CERTIFICATE_FIND_SHA1_HASH: 0,
            CAPICOM_CERTIFICATE_FIND_TIME_VALID: 9,
            CAPICOM_CERTIFICATE_FIND_EXTENDED_PROPERTY: 6,
            CAPICOM_PROPID_KEY_PROV_INFO: 2,
            CADESCOM_BASE64_TO_BINARY: 1,
            CADESCOM_HASH_ALGORITHM_CP_GOST_3411: 100,
            
            checkInstall: function(){
                try{
                    var oAbout = this.objCreator("CAdESCOM.About");
                    var Version = oAbout.Version;

                    return true;
                }catch(err){
                    return false;
                }            
            },
            
            init: function (tsAddress, hashOnServer) {
                this.tsAddress = tsAddress;
                this.hashOnServer = hashOnServer;
            },
            
            getCertificates: function () {
            
                var oStore = this.objCreator("CAPICOM.store");
                oStore.Open(this.CAPICOM_CURRENT_USER_STORE, this.CAPICOM_MY_STORE, this.CAPICOM_STORE_OPEN_MAXIMUM_ALLOWED);
        
                //Получаем сертификаты
                this.oCertificates = oStore.Certificates;
                
                // Из них не рассматриваются сертификаты, в которых отсутствует закрытый ключ.
                this.oCertificates = this.oCertificates.Find(this.CAPICOM_CERTIFICATE_FIND_EXTENDED_PROPERTY, this.CAPICOM_PROPID_KEY_PROV_INFO);
                
                // Из них выбираются только сертификаты, действительные в настоящее время.
                this.oCertificates = this.oCertificates.Find(this.CAPICOM_CERTIFICATE_FIND_TIME_VALID);
                
                if (this.oCertificates.Count == 0) {
                    throw "Actual valid certificates not found";
                }
                oStore.Close();                
            
                var result = [];
                for (var i=0; i<this.oCertificates.Count; i++){
                    result.push(this.oCertificates.Item(i+1).SubjectName + " действителен до " + this.oCertificates.Item(i+1).ValidToDate);
                }
                return result;
            },

            setCertificate: function (certNo) {
                this.oCertificate = this.oCertificates(certNo);
            },

            objCreator: function (name) {
                switch (navigator.appName) {
                    case 'Microsoft Internet Explorer':
                    return new ActiveXObject(name);
                default:
                    var userAgent = navigator.userAgent;
                    if (userAgent.match(/Trident\/./i)) { // IE10, 11
                          return new ActiveXObject(name);
                    }
                    if (userAgent.match(/ipod/i) || userAgent.match(/ipad/i) || userAgent.match(/iphone/i)) {
                        return call_ru_cryptopro_npcades_10_native_bridge("CreateObject", [name]);
                    }
                    var cadesobject = document.getElementById('cadesplugin');
                    if (cadesobject == null){
                        cadesobject = document.createElement("object");
                        cadesobject.setAttribute("id", "cadesplugin");
                        cadesobject.setAttribute("type", "application/x-cades");
                        document.body.appendChild(cadesobject);
                    }
                    return cadesobject.CreateObject(name);
                }
            },
            
            sign: function(base64Content){
                var oSigner = this.objCreator("CAdESCOM.CPSigner");
                oSigner.Certificate = this.oCertificate;
                oSigner.TSAAddress = this.tsAddress;
        
                try {
                    var oSignedData = this.objCreator("CAdESCOM.CadesSignedData");
                    if (this.hashOnServer){
                        // Создаем объект CAdESCOM.HashedData
                        var oHashedData = this.objCreator("CAdESCOM.HashedData");

                        // Инициализируем объект заранее вычисленным хэш-значением
                        // Алгоритм хэширования нужно указать до того, как будет передано хэш-значение
                        oHashedData.Algorithm = this.CADESCOM_HASH_ALGORITHM_CP_GOST_3411;
                        oHashedData.SetHashValue(base64Content);

                        var sSignedMessage = oSignedData.SignHash(oSigner, this.CADESCOM_CADES_X_LONG_TYPE_1);
                    }else{
                        oSignedData.ContentEncoding = this.CADESCOM_BASE64_TO_BINARY;
                        oSignedData.Content = base64Content;
                        var sSignedMessage = oSignedData.SignCades(oSigner, this.CADESCOM_CADES_X_LONG_TYPE_1, true);
                    }
        
                } catch (err) {
                    throw "Failed to create signature. Error: " + err;
                }

                return sSignedMessage;
            }
        };    
        
        cryptoTool.init(tsAddress, hashOnServer);
        this.@ru.intertrust.cm.core.gui.impl.client.crypto.CryptoProBrowserPluginClientComponent::cryptoTool = cryptoTool;
    }-*/; 
    
    @Override
    public String sign(String base64Content) {        
        return nativeSign(base64Content);
    }    
    
    private native String nativeSign(String base64Content) 
    /*-{
        var cryptoTool = this.@ru.intertrust.cm.core.gui.impl.client.crypto.CryptoProBrowserPluginClientComponent::cryptoTool;
        return cryptoTool.sign(base64Content);
    }-*/;
    
    private native JsArrayString nativeGetCertificates() 
    /*-{
        var cryptoTool = this.@ru.intertrust.cm.core.gui.impl.client.crypto.CryptoProBrowserPluginClientComponent::cryptoTool;
        return cryptoTool.getCertificates();
    }-*/;
    
}
