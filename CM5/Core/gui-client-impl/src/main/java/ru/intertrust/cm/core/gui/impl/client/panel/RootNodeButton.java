package ru.intertrust.cm.core.gui.impl.client.panel;

import com.google.gwt.user.client.ui.HTML;

public class RootNodeButton extends HTML {

    private HTML html;
    private String nameName;

    public RootNodeButton() {
        this.html = new HTML();

    }

    public HTML getHtml() {
        return html;
    }

    public void setHtml(HTML html) {
        this.html = html;
    }

    public String getName() {
        return nameName;
    }

    public void setName(String nameName) {
        this.nameName = nameName;
    }

}
