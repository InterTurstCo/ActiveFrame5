package ru.intertrust.cm.core.gui.api.client;

/**
 * Defines recovery point for all plugins with support full screen mode
 *
 * @author Sergey.Okolot
 *         Created on 23.01.14 9:38.
 */
public class CompactModeState {

    private int left;
    private int top;
    private boolean expanded;

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
