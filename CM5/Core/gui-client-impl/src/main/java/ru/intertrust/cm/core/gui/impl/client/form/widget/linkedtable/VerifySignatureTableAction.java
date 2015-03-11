package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;

/**
 * @author Lesia Puhova
 *         Date: 11.03.2015
 *         Time: 18:35
 */

@ComponentName("verify.signature.table.action")
public class VerifySignatureTableAction extends LinkedTableAction {

    @Override
    protected void execute(Id id, int rowIndex) {
        if (id == null) {
            ApplicationWindow.errorAlert(LocalizeUtil.get(LocalizationKeys.OBJECT_NOT_SAVED_KEY,
                    BusinessUniverseConstants.OBJECT_NOT_SAVED));
            return;
        }
        ActionContext context = new ActionContext();
        context.setRootObjectId(id);

        final Action action = ComponentRegistry.instance.get("verify.digital.signature.action");
        action.setInitialContext(context);
        action.perform();
    }

    @Override
    protected String getServerComponentName() {
        return null;
    }

    @Override
    public String getName() {
        return "verify.signature.table.action";
    }

    @Override
    public Component createNew() {
        return new VerifySignatureTableAction();
    }
}
