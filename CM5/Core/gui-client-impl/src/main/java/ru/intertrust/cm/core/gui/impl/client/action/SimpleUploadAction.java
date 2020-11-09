package ru.intertrust.cm.core.gui.impl.client.action;

import java.util.List;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FormPanel;
import ru.intertrust.cm.core.config.gui.action.SimpleUploadActionConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.MultipleFileUpload;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;

@ComponentName("simple.upload.action")
public class SimpleUploadAction extends SimpleServerAction{

    private static final String METHOD = "POST";
    private static final String URL = "af5-services/simple-upload";
    private FormPanel fPanel;

    @Override
    protected SimpleActionContext appendCurrentContext(ActionContext initialContext) {
        SimpleActionContext context = (SimpleActionContext) initialContext;
        return context;
    }

    @Override
    protected void execute() {

        ActionContext initialContext = getInitialContext();
        SimpleUploadActionConfig uploadConfig = initialContext.getActionConfig();

        fPanel = new FormPanel();
        fPanel.setAction(GWT.getHostPageBaseURL() + URL + "/" + uploadConfig.getActionHandler() + "/execute");
        fPanel.setMethod(METHOD);
        fPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
        final MultipleFileUpload fileUpload = new MultipleFileUpload();
        fileUpload.setName("fileselect[]");
        fileUpload.enableMultiple(uploadConfig.getMultipleFile());
        fileUpload.setAccept(uploadConfig.getFileExtensions());

        fileUpload.addChangeHandler(new ChangeHandler() {
                                        @Override
                                        public void onChange(ChangeEvent event) {
                                            List<String> fileNames = ((MultipleFileUpload) event.getSource()).getFilenames();
                                            if (fileNames== null || fileNames.size() < 1) {
                                                System.out.println("No files");
                                            } else {
                                                fPanel.submit();
                                            }
                                        }
                                    }
        );

        fPanel.add(fileUpload);

        getPlugin().getOwner().getCurrentPlugin().getView().asWidget().add(fPanel);
        fileUpload.click();

        fPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent submitCompleteEvent) {
                //Window.alert(submitCompleteEvent.getResults());
                if (submitCompleteEvent.getResults().startsWith("Success")) {
                    showOnSuccessMessage(null, "Загрузка выполнена успешно");
                    plugin.refresh();
                }else{
                    throw new GuiException(submitCompleteEvent.getResults());
                }
            }
        });

    }

    @Override
    public Component createNew() {
        return new SimpleUploadAction();
    }

}
