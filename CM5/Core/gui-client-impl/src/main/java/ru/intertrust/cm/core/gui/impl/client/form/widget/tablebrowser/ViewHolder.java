package ru.intertrust.cm.core.gui.impl.client.form.widget.tablebrowser;

import com.google.gwt.user.client.ui.Widget;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 20.11.2014
 *         Time: 8:00
 */
public abstract class ViewHolder<W extends Widget,S> {
    protected W widget;
    protected ViewHolder<? extends Widget, S> childViewHolder;

    public ViewHolder(W widget) {
        this.widget = widget;
    }

    public W getWidget() {
        return widget;
    }

    public abstract void setContent(S state);

    public void setChildViewHolder(ViewHolder<? extends Widget, S> childViewHolder) {
        this.childViewHolder = childViewHolder;
    }

    public ViewHolder<? extends Widget, S> getChildViewHolder() {
        return childViewHolder;
    }
}
