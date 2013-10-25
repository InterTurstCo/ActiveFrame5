package ru.intertrust.cm.core.gui.impl.markup.DialogBoxWidget;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 24.10.13
 * Time: 14:49
 * To change this template use File | Settings | File Templates.
 */
public class Entity {

    boolean aBoolean;
    String string;
    String pop;

    public Entity(boolean aBoolean, String string, String pop) {
        this.aBoolean = aBoolean;
        this.string = string;
        this.pop = pop;
    }

    public String getPop() {
        return pop;
    }

    public boolean isaBoolean() {
        return aBoolean;
    }

    public String getString() {
        return string;
    }

    public void setaBoolean(boolean aBoolean) {
        this.aBoolean = aBoolean;
    }

    public void setString(String string) {
        this.string = string;
    }

    public void setPop(String pop) {
        this.pop = pop;
    }
}
