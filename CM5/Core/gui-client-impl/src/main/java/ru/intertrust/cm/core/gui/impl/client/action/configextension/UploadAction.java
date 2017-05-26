package ru.intertrust.cm.core.gui.impl.client.action.configextension;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.action.SimpleServerAction;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;

/**
 * Created by Ravil on 26.05.2017.
 */
@ComponentName("upload.action.handler")
public class UploadAction extends SimpleServerAction {

    private static final String METHOD = "POST";
    private static final String URL = "upload-configuration";
    private FormPanel fPanel;

    @Override
    protected SimpleActionContext appendCurrentContext(ActionContext initialContext) {
        SimpleActionContext context = (SimpleActionContext) initialContext;
        return context;
    }

    @Override
    protected void execute() {
        fPanel = new FormPanel();
        fPanel.setAction(GWT.getHostPageBaseURL() + URL);
        fPanel.setMethod(METHOD);
        fPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
        final FileUpload fileUpload = new FileUpload();
        fileUpload.getElement().setAttribute("multiple", "multiple");
        fileUpload.setName("fileselect[]");

        fileUpload.addChangeHandler(new ChangeHandler() {
                                        @Override
                                        public void onChange(ChangeEvent event) {
                                            String fileName = ((FileUpload) event.getSource()).getFilename();
                                            if (fileName == null || fileName.length() < 1) {
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
                Window.alert(submitCompleteEvent.getResults());
            }
        });
    }

    @Override
    public Component createNew() {
        return new UploadAction();
    }
}
