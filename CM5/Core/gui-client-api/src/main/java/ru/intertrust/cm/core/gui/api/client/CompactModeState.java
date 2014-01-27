package ru.intertrust.cm.core.gui.api.client;

/**
 * Defines full screen state and recovery point for all plugins which support full screen mode
 *
 * @author Sergey.Okolot
 *         Created on 23.01.14 9:38.
 */
public class CompactModeState {

    private int left;
    private int leftOffset;
    private int topOffset;
    private boolean expanded;

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getLeftOffset() {
        return leftOffset;
    }

    public void setLeftOffset(int leftOffset) {
        this.leftOffset = leftOffset;
    }

    public int getTopOffset() {
        return topOffset;
    }

    public void setTopOffset(int topOffset) {
        this.topOffset = topOffset;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
