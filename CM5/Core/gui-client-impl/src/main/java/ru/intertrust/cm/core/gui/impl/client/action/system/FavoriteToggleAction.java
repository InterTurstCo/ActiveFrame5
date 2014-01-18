package ru.intertrust.cm.core.gui.impl.client.action.system;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.action.ToggleAction;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Sergey.Okolot
 */
@ComponentName("favorite.toggle.action")
public class FavoriteToggleAction extends ToggleAction {

    @Override
    public void execute() {
        final String imageUrl = getInitialContext().getActionConfig().getImageUrl();
        if (getImage() != null && imageUrl != null) {
            if (getImage().getUrl().endsWith(OFF_SUFFIX)) {
                getImage().setUrl(imageUrl.replace(IMAGE_SUFFIX, ON_SUFFIX));
            } else {
                getImage().setUrl(imageUrl.replace(IMAGE_SUFFIX, OFF_SUFFIX));
            }
        }
    }

    @Override
    public Component createNew() {
        return new FavoriteToggleAction();
    }
}
