package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;

/**
 * Клиентский обработчик экшена "Сохранить и Закрыть".<br/>
 * Экшен сохранения берется оригинальный, для этой цели данный класс отнаследован от {@link SaveAction}.<br/>
 * После сохранения, форма закрывается по аналогии с работой кнопки "Закрыть".
 * <p>
 * <p>
 * Created by Myskin Sergey on 06.04.2018.
 */

@ComponentName("save.and.close.action")
public class SaveAndCloseAction extends SaveAction {

    @Override
    public Component createNew() {
        return new SaveAndCloseAction();
    }

    @Override
    protected void onSuccess(ActionData result) {
        super.onSuccess(result);

        final CloseInCentralPanelAction action = ComponentRegistry.instance.get("close.in.central.panel.action");

        final ActionContext initialContext = super.getInitialContext();
        action.setInitialContext(initialContext);

        final Plugin plugin = super.getPlugin();
        action.setPlugin(plugin);

        action.perform();
    }

}
