package ru.intertrust.cm.core.gui.impl.client.panel;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.HTML;

public class RootNodeButton extends HTML {

    private HTML html;
    private String nameName;
    private String name;
    private String image;
    private String displayText;

    public RootNodeButton(long collectionCount, String name, String image, String displayText) {
        this.name = name;
        this.image = image;
        this.displayText = displayText;
        this.html = new HTML();
        writeHtml(collectionCount, name, image, displayText);
    }

    public void writeHtml(long collectionCount, String name, String image, String displayText) {

        String s;
        if (collectionCount > 0) {
            s = "<small>" + collectionCount + "</small>";
        } else {
            s = "";
        }
        this.setHTML("<li><a><img width=\"60\" height=\"50\" border=\"0\" alt=\"\" src=\"" + image + "\"><span>"
                + displayText + "</span>" + s + "</a></li>");

        if (name != null) {
            this.setTitle(name);
        }

        this.getElement().getStyle().setCursor(Style.Cursor.POINTER);
    }

    public HTML getHtml() {
        return html;
    }

    public void setHtml(HTML html) {
        this.html = html;
    }

    public String getNameName() {
        return nameName;
    }

    public void setNameName(String nameName) {
        this.nameName = nameName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }
}
