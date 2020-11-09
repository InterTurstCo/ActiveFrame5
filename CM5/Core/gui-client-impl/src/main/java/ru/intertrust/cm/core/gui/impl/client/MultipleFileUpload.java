package ru.intertrust.cm.core.gui.impl.client;

import java.util.ArrayList;
import java.util.List;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.ui.FileUpload;

public class MultipleFileUpload extends FileUpload {

    public MultipleFileUpload() {
        super();
    }

    public List<String> getFilenames() {
        ArrayList<String> result = new ArrayList<String>();

        JavaScriptObject rawFileList = getElement().getPropertyJSO("files");
        if (rawFileList == null) {
            result.add(InputElement.as(getElement()).getValue()); // IE does not support multiple-select
        } else {
            FileList fileList = rawFileList.cast();
            for (int i = 0; i < fileList.getLength(); ++i) {
                result.add(fileList.item(i).getName());
            }
        }

        return result;
    }

    public void enableMultiple(Boolean multiple) {
        String attr = "multiple";
        if (multiple != null && multiple) {
            getElement().setAttribute(attr, attr);
        } else {
            getElement().removeAttribute(attr);
        }
    }

    public void setAccept(String accept) {
        if (accept != null) {
            getElement().setAttribute("accept", accept);
        }
    }
}
