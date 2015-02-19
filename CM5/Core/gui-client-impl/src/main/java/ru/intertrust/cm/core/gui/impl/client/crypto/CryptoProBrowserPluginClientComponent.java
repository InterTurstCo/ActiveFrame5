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
        JsArrayString allCerts = nativeInit(this.extendedConfig.getTsAddress());
        
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
    }
    private native void nativeSetCertificateIndex(int certNo)
    /*-{
        var cryptoTool = this.@ru.intertrust.cm.core.gui.impl.client.crypto.CryptoProBrowserPluginClientComponent::cryptoTool;
        return cryptoTool.setCertificate(certNo+1);
    }-*/;
    
    private native JsArrayString nativeInit(String tsAddress)
    /*-{
        var cryptoTool = {
            CADESCOM_CADES_X_LONG_TYPE_1: 0x5d,
            CAPICOM_CURRENT_USER_STORE: 2,
            CAPICOM_MY_STORE: "My",
            CAPICOM_STORE_OPEN_MAXIMUM_ALLOWED: 2,
            CAPICOM_CERTIFICATE_FIND_SUBJECT_NAME: 1,
            CAPICOM_CERTIFICATE_FIND_SHA1_HASH: 0,
            CAPICOM_CERTIFICATE_FIND_TIME_VALID: 9,
            
            init: function (tsAddress) {
                this.tsAddress = tsAddress;
                var oStore = this.objCreator("CAPICOM.store");
                oStore.Open(this.CAPICOM_CURRENT_USER_STORE, this.CAPICOM_MY_STORE, this.CAPICOM_STORE_OPEN_MAXIMUM_ALLOWED);
        
                this.oCertificates = oStore.Certificates.Find(this.CAPICOM_CERTIFICATE_FIND_TIME_VALID);
                if (this.oCertificates.Count == 0) {
                    throw "Certificates not found";
                }
                
                oStore.Close();                
            },
            
            getCertificates: function () {
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
        
                var oSignedData = this.objCreator("CAdESCOM.CadesSignedData");
                oSignedData.Content = base64Content;
                oSignedData.ContentEncoding = 1; //CADESCOM_BASE64_TO_BINARY=1, Данные будут перекодированы из Base64 в бинарный массив
        
                try {
                    var sSignedMessage = oSignedData.SignCades(oSigner, this.CADESCOM_CADES_X_LONG_TYPE_1, true);
                } catch (err) {
                    throw "Failed to create signature. Error: " + err;
                }

                return sSignedMessage;
            }
        };    
        
        cryptoTool.init(tsAddress);
        this.@ru.intertrust.cm.core.gui.impl.client.crypto.CryptoProBrowserPluginClientComponent::cryptoTool = cryptoTool;
        return cryptoTool.getCertificates();
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
}
