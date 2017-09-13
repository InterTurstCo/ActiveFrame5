package ru.intertrust.cm.core.gui.impl.client.crypto;

import java.util.ArrayList;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.*;

import ru.intertrust.cm.core.config.crypto.CAdESCryptoSettingsConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.DigitalSignatureClientComponent;
import ru.intertrust.cm.core.gui.api.client.DigitalSignatureComponentInitHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.crypto.DigitalSignatureConfig;

@ComponentName("cryptopro.browser.plugin.client.component")
public class CryptoProBrowserPluginClientComponent extends DigitalSignatureClientComponent {
    private DigitalSignatureConfig config;
    private CAdESCryptoSettingsConfig extendedConfig;
    private JavaScriptObject cryptoTool;

    @Override
    public Component createNew() {
        return new CryptoProBrowserPluginClientComponent();
    }

    @Override
    public void init(DigitalSignatureConfig config, final DigitalSignatureComponentInitHandler handler) {
        this.config = config;
        this.extendedConfig = (CAdESCryptoSettingsConfig) config.getCryptoSettingsConfig().getSettings();
        nativeInit(this.extendedConfig.getTsAddress(),
                (this.config.getCryptoSettingsConfig().getHashOnServer() != null && this.config.getCryptoSettingsConfig().getHashOnServer()),
                new Callback<String, String>() {

                    @Override
                    public void onFailure(String reason) {
                        handler.onCancel();
                        InstallPluginDialog installPluginDialog = new InstallPluginDialog();
                        installPluginDialog.show();            
                    }

                    @Override
                    public void onSuccess(String result) {
                        if (nativeCheckInstall()){
                            nativeGetCertificates(new Callback<JsArrayString, String>(){

                                @Override
                                public void onFailure(String reason) {
                                    handler.onCancel();                                    
                                }

                                @Override
                                public void onSuccess(JsArrayString allCerts) {
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
                                        nativeSetCertificateIndex(0);
                                        handler.onInit();
                                    }
                                }
                            });
                        }else{
                            handler.onCancel();
                            InstallPluginDialog installPluginDialog = new InstallPluginDialog();
                            installPluginDialog.show();            
                        }

                    }
                });

    }

    private native void nativeSetCertificateIndex(int certNo)
    /*-{
        $wnd.cryptoTool.setCertificate(certNo+1);
    }-*/;

    private native boolean nativeCheckInstall()
    /*-{
        return $wnd.cryptoTool.checkInstall();
    }-*/;

    private native void nativeInit(String tsAddress, boolean hashOnServer, Callback<String, String> callback)
    /*-{
         $wnd.cryptoTool.init(tsAddress, hashOnServer, function(isInit, error){
            try{
                if (isInit){
                    callback.@com.google.gwt.core.client.Callback::onSuccess(Ljava/lang/Object;)("");
                }else{
                    callback.@com.google.gwt.core.client.Callback::onFailure(Ljava/lang/Object;)(error);
                }
            } catch (err) {
                callback.@com.google.gwt.core.client.Callback::onFailure(Ljava/lang/Object;)(err.message);
            }
         });
    }-*/;

    @Override
    public String sign(String base64Content, Callback<String, String> calback) {
        return nativeSign(base64Content, calback);
    }

    private native String nativeSign(String base64Content, Callback<String, String> callback)
    /*-{
        var cerNo = $wnd.cryptoTool.getCertificate();
        $wnd.cryptoTool.sign(cerNo, base64Content, function(signData, error){
            if (signData != null){
                callback.@com.google.gwt.core.client.Callback::onSuccess(Ljava/lang/Object;)(signData);
            }else{
                callback.@com.google.gwt.core.client.Callback::onFailure(Ljava/lang/Object;)(error);
            }
        });
    }-*/;

    private native void nativeGetCertificates(Callback<JsArrayString, String> callback)
    /*-{
        $wnd.cryptoTool.getCertificates(function(certsInfo, error){
            if(certsInfo != null){
                callback.@com.google.gwt.core.client.Callback::onSuccess(Ljava/lang/Object;)(certsInfo);
            }else{
                callback.@com.google.gwt.core.client.Callback::onFailure(Ljava/lang/Object;)(error);
            }
        });
    }-*/;

}
