package ru.intertrust.cm.core.gui.impl.client.action;

/**
 * @author Sergey.Okolot
 *         Created on 07.04.2014 11:15.
 */
public interface ActionItem {

    public static final String ACTION = "action";
    public static final String TEXT = "text";
    public static final String IMAGE = "image";
    public static final String KEY_STROKE = "keyStroke";
    public static final String IMMEDIATE = "immediate";

    /**
     *
     * @return value of action attribute.
     */
    String getUrlLink();

    String getText();

    String getImage();

    String getKeyStroke();

    boolean isDisabled();

    boolean isImmediate();

    boolean isSupportHistory();
}
