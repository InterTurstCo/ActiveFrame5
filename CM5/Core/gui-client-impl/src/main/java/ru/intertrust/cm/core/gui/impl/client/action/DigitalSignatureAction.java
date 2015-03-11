package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.crypto.SignedResult;
import ru.intertrust.cm.core.config.crypto.SignedResultItem;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.DigitalSignatureClientComponent;
import ru.intertrust.cm.core.gui.api.client.DigitalSignatureComponentInitHandler;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.crypto.ProgressDialog;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.crypto.DigitalSignatureConfig;
import ru.intertrust.cm.core.gui.model.crypto.GuiSignedData;
import ru.intertrust.cm.core.gui.model.crypto.GuiSignedDataItem;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

@ComponentName("digital.signature.action")
public class DigitalSignatureAction extends Action {
    //private DigitalSignatureServiceAsync digitalSignatureService;
    private ProgressDialog progress;

    @Override
    public Component createNew() {
        return new DigitalSignatureAction();
    }

    @Override
    protected void execute() {
        progress = new ProgressDialog();
        progress.setMessage("Получение конфигурации");
        progress.setModal(true);
        progress.show();

        final Command command = new Command("getConfig", "digital.signature", null);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                DigitalSignatureConfig digitalSignatureConfig = (DigitalSignatureConfig)result;
                signData(digitalSignatureConfig);
            }

            @Override
            public void onFailure(Throwable caught) {
                progress.hide();
                ApplicationWindow.errorAlert("Ошибка при выполнении действия: " + caught.getMessage());
            }
        });
    }

    private void signData(final DigitalSignatureConfig digitalSignatureConfig) {
        final DigitalSignatureClientComponent component =
                ComponentRegistry.instance.get(digitalSignatureConfig.getCryptoSettingsConfig().getGuiComponentName());

        //Получаем ID ДО
        final IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        final FormState formState = editor.getFormState();
        Id id = formState.getObjects().getRootNode().getDomainObject().getId();

        progress.setMessage("Получение подписываемого контента");
        final Command command = new Command("getSignedData", "digital.signature", id);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                progress.hide();
                ApplicationWindow.errorAlert("Ошибка при выполнении действия: " + caught.getMessage());
            }

            @Override
            public void onSuccess(Dto guiSignedData) {
                final GuiSignedData result = (GuiSignedData)guiSignedData;
                try {
                    final SignedResult signResult = new SignedResult();
                    signResult.setRootId(result.getRootId());
                    progress.setMaxValue(result.getSignedDataItems().size());
                    component.init(digitalSignatureConfig, new DigitalSignatureComponentInitHandler() {

                        @Override
                        public void onInit() {
                            try {
                                int progressValue = 0;
                                for (GuiSignedDataItem signedData : result.getSignedDataItems()) {
                                    progress.setMessage("Подпись: " + signedData.getName());
                                    String signature = component.sign(signedData.getContent());
                                    SignedResultItem signResultItem = new SignedResultItem();
                                    signResultItem.setId(signedData.getId());
                                    signResultItem.setSignature(signature);
                                    signResult.getSignedResultItems().add(signResultItem);
                                    progress.setValue(progressValue++);
                                    if (progress.isCancel()) {
                                        break;
                                    }
                                }

                                if (!progress.isCancel()) {
                                    saveSignResults(signResult);
                                }
                            } catch (Exception ex) {
                                progress.hide();
                                //TODO Сюда попадаем когда пользователь отменил ввод пароля, непонятно как при этом скрыть сообщение об ошибке
                                ApplicationWindow.errorAlert("Ошибка при выполнении действия: " + ex.getMessage());
                            }
                        }

                        @Override
                        public void onCancel() {
                            progress.hide();
                        }

                        @Override
                        public void onError(String message) {
                            ApplicationWindow.errorAlert("Ошибка при выполнении действия: " + message);
                        }
                    });
                } catch (Throwable ex) {
                    progress.hide();
                    ApplicationWindow.errorAlert("Ошибка при выполнении действия: " + ex.getMessage());
                }
            }
        });
    }

    private void saveSignResults(SignedResult signResult) {
        final Command command = new Command("saveSignResult", "digital.signature", signResult);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                progress.hide();
                ApplicationWindow.errorAlert("Ошибка при выполнении действия: " + caught.getMessage());
            }

            @Override
            public void onSuccess(Dto result) {
                progress.showSuccess();
            }
        });

    }

}
