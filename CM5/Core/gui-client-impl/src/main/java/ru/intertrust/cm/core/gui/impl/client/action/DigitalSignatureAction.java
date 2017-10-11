package ru.intertrust.cm.core.gui.impl.client.action;

import java.util.List;

import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.crypto.SignedResult;
import ru.intertrust.cm.core.config.crypto.SignedResultItem;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.DigitalSignatureClientComponent;
import ru.intertrust.cm.core.gui.api.client.DigitalSignatureComponentInitHandler;
import ru.intertrust.cm.core.gui.api.client.event.CreateCryptoSignature;
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
    private ProgressDialog progress;
    private Id rootId;

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

        //Получаем ID ДО
        final IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        final FormState formState = editor.getFormState();
        rootId = formState.getObjects().getRootNode().getDomainObject().getId();

        final Command command = new Command("getConfig", "digital.signature", null);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                DigitalSignatureConfig digitalSignatureConfig = (DigitalSignatureConfig) result;
                signData(digitalSignatureConfig);
            }

            @Override
            public void onFailure(Throwable caught) {
                progress.hide();
                sendErrorMessage("Ошибка при выполнении действия: " + caught.getMessage());
                ApplicationWindow.errorAlert("Ошибка при выполнении действия: " + caught.getMessage());
            }
        });
    }

    private void signData(final DigitalSignatureConfig digitalSignatureConfig) {
        final DigitalSignatureClientComponent component =
                ComponentRegistry.instance.get(digitalSignatureConfig.getCryptoSettingsConfig().getGuiComponentName());

        progress.setMessage("Получение подписываемого контента");
        final Command command = new Command("getSignedData", "digital.signature", rootId);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                progress.hide();
                sendErrorMessage("Ошибка при выполнении действия: " + caught.getMessage());
                ApplicationWindow.errorAlert("Ошибка при выполнении действия: " + caught.getMessage());
            }

            @Override
            public void onSuccess(Dto guiSignedData) {
                final GuiSignedData result = (GuiSignedData) guiSignedData;
                try {
                    final SignedResult signResult = new SignedResult();
                    signResult.setRootId(result.getRootId());
                    progress.setMaxValue(result.getSignedDataItems().size());
                    component.init(digitalSignatureConfig, new DigitalSignatureComponentInitHandler() {

                        @Override
                        public void onInit() {
                            try {
                                signData(component, result.getSignedDataItems(), signResult, 0);
                            } catch (Exception ex) {
                                progress.hide();
                                sendErrorMessage("Ошибка при выполнении действия: " + ex.getMessage());
                                ApplicationWindow.errorAlert("Ошибка при выполнении действия: " + ex.getMessage());
                            }
                        }

                        @Override
                        public void onCancel() {
                            progress.hide();
                        }

                        @Override
                        public void onError(String message) {
                            sendErrorMessage("Ошибка при выполнении действия: " + message);
                            ApplicationWindow.errorAlert("Ошибка при выполнении действия: " + message);
                        }
                    });
                } catch (Throwable ex) {
                    progress.hide();
                    sendErrorMessage("Ошибка при выполнении действия: " + ex.getMessage());
                    ApplicationWindow.errorAlert("Ошибка при выполнении действия: " + ex.getMessage());
                }
            }
        });
    }

    private void signData(final DigitalSignatureClientComponent component, final List<GuiSignedDataItem> signedDatas, final SignedResult signResult,
            final int number) {
        final GuiSignedDataItem signedData = signedDatas.get(number);

        progress.setMessage("Подпись: " + signedData.getName());
        component.sign(signedData.getContent(), new Callback<String, String>() {

            @Override
            public void onFailure(String reason) {
                progress.hide();
                //Сюда попадаем в том числе когда пользователь отменил ввод пароля, при этом можно ориентироваться только на текст сообщения
                if (reason.contains("0x8010006E") &&
                        (reason.contains("The action was cancelled")
                                || reason.contains("Действие было отменено"))) {
                    //Пока не делается ничего
                }else{
                    sendErrorMessage("Ошибка при выполнении действия: " + reason);
                    ApplicationWindow.errorAlert("Ошибка при выполнении действия: " + reason);
                }
            }

            @Override
            public void onSuccess(String signature) {
                SignedResultItem signResultItem = new SignedResultItem();
                signResultItem.setId(signedData.getId());
                signResultItem.setSignature(signature);
                signResult.getSignedResultItems().add(signResultItem);
                progress.setValue(number);

                if (!progress.isCancel()) {
                    if (signedDatas.size() > number + 1) {
                        signData(component, signedDatas, signResult, number + 1);
                    } else {
                        saveSignResults(signResult);
                    }
                } else {
                    sendErrorMessage("Создание подписи отменено пользователем");
                }
            }

        });
    }

    private void saveSignResults(final SignedResult signResult) {
        final Command command = new Command("saveSignResult", "digital.signature", signResult);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                progress.hide();
                sendErrorMessage("Ошибка при выполнении действия: " + caught.getMessage());
                ApplicationWindow.errorAlert("Ошибка при выполнении действия: " + caught.getMessage());
            }

            @Override
            public void onSuccess(Dto result) {
                EventBus eBus = Application.getInstance().getEventBus();
                eBus.fireEvent(new CreateCryptoSignature(signResult));
                progress.showSuccess();
            }
        });
    }

    private void sendErrorMessage(String message) {
        EventBus eBus = Application.getInstance().getEventBus();
        eBus.fireEvent(new CreateCryptoSignature(rootId, message));
    }

}
