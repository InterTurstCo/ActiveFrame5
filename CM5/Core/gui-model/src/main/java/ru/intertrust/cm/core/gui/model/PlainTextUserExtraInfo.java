package ru.intertrust.cm.core.gui.model;

/**
 * @author Denis Mitavskiy
 *         Date: 15.04.2015
 *         Time: 16:03
 */
public class PlainTextUserExtraInfo implements UserExtraInfo {
    private String text;

    public PlainTextUserExtraInfo() {
    }

    public PlainTextUserExtraInfo(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
