package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.crypto.DocumentVerifyResult;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.crypto.VerifySignatureDialog;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.crypto.VerifySignatureResponse;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.List;

@ComponentName("verify.digital.signature.action")
public class VerifyDigitalSignatureAction extends Action {
  //  private DigitalSignatureServiceAsync digitalSignatureService;

    @Override
    public Component createNew() {
        return new VerifyDigitalSignatureAction();
    }

    @Override
    protected void execute() {
        //Получаем ID ДО
        Id id;
        final IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        if (editor != null) {
            final FormState formState = editor.getFormState();
            id = formState.getObjects().getRootNode().getDomainObject().getId();
        } else {
            id = getInitialContext().getRootObjectId();
        }
        final VerifySignatureDialog dialog = new VerifySignatureDialog();
        dialog.show();

        final Command command = new Command("verify", "digital.signature", id);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            
            @Override
            public void onSuccess(Dto result) {
                VerifySignatureResponse response = (VerifySignatureResponse)result;
                List<DocumentVerifyResult> verifyResults = response.getVerifyResults();
                dialog.setDocumentVerifyResults(verifyResults);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                ApplicationWindow.errorAlert("Ошибка при выполнении действия: " + caught.getMessage());                
            }
        });
        
    }

}
