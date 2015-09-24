package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.Event;
import ru.intertrust.cm.core.gui.api.client.History;
import com.google.gwt.user.client.ui.Hyperlink;

/**
 * Created by tbilyi on 12.09.2014.
 */
public class HyperLinkWithHistorySupport extends Hyperlink{

    public HyperLinkWithHistorySupport() {
        super();
    }

    public HyperLinkWithHistorySupport(String text, String targetHistoryToken) {
        super(text, targetHistoryToken);
    }

    @Override
    public void onBrowserEvent(Event event) {
        setTargetHistoryToken(History.getToken());
        super.onBrowserEvent(event);
    }


}
