package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 30.12.13
 * Time: 12:32
 * To change this template use File | Settings | File Templates.
 */
public class SimpleSearchEvent extends GwtEvent<SimpleSearchEventHandler> {
    public static final Type<SimpleSearchEventHandler> TYPE = new Type<SimpleSearchEventHandler>();
    private String text;
    private boolean typeButton;


    public SimpleSearchEvent(String text, boolean typeButton) {
        this.text = text;
        this.typeButton = typeButton;
    }

    @Override
    public Type<SimpleSearchEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SimpleSearchEventHandler handler) {
        handler.collectionSimpleSearch(this);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isTypeButton() {
        return typeButton;
    }

    public void setTypeButton(boolean typeButton) {
        this.typeButton = typeButton;
    }
}
