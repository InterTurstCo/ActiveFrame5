package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * @author Yaroslav Bondarchuk
 *         Date: 04.01.14
 *         Time: 16:15
 */
public class CollectionCsvController {
    private FormPanel submitForm;
    private TextBox param;

    public CollectionCsvController(Panel root) {
        init(root);
    }
    private void init(Panel root){
        submitForm = new FormPanel();
        root.add(submitForm);
        submitForm.setAction(GWT.getHostPageBaseURL() + "json-export-to-csv");
        submitForm.setMethod(FormPanel.METHOD_POST);
        param = new TextBox();
        param.setName("json");
        VerticalPanel panel = new VerticalPanel();
        panel.add(param);
        submitForm.setWidget(panel);
    }

    public void doPostRequest(String json) {
        param.setText(json);
        submitForm.setAction(GWT.getHostPageBaseURL() + "json-export-to-csv");
        submitForm.submit();
    }

    public void doExtendedSearchPostRequest(String json) {
        param.setText(json);
        submitForm.setAction(GWT.getHostPageBaseURL() + "json-extended-search-export-to-csv");
        submitForm.submit();
    }

}
