package ru.intertrust.cm.core.gui.impl.client.action;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.crypto.DocumentVerifyResult;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.crypto.VerifySignatureDialog;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;
import ru.intertrust.cm.core.gui.rpc.api.DigitalSignatureServiceAsync;

@ComponentName("verify.digital.signature.action")
public class VerifyDigitalSignatureAction extends Action{
    private DigitalSignatureServiceAsync digitalSignatureService;

    @Override
    public Component createNew() {
        return new VerifyDigitalSignatureAction();
    }

    @Override
    protected void execute() {
        //Получаем ID ДО
        final IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        final FormState formState = editor.getFormState();
        Id id = formState.getObjects().getRootNode().getDomainObject().getId();
        digitalSignatureService = DigitalSignatureServiceAsync.Impl.getInstance();
        
        final VerifySignatureDialog dialog = new VerifySignatureDialog();
        dialog.show();
        
        digitalSignatureService.verify(id, new AsyncCallback<List<DocumentVerifyResult>>() {
            
            @Override
            public void onSuccess(List<DocumentVerifyResult> result) {
                dialog.setDocumentVerifyResults(result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                ApplicationWindow.errorAlert("Ошибка при выполнении действия: " + caught.getMessage());                
            }
        });
        
    }

}
