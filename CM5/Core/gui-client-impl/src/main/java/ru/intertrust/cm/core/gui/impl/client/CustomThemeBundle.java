package ru.intertrust.cm.core.gui.impl.client;

/**
 * Created by User on 18.03.14.
 */
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.MenuBar;

public interface CustomThemeBundle extends ClientBundle {

    public static final CustomThemeBundle INSTANCE = GWT.create(CustomThemeBundle.class);

    @Source("custom-theme.css")
    @CssResource.NotStrict
    CssResource css();
}