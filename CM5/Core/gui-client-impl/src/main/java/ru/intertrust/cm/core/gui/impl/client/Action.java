package ru.intertrust.cm.core.gui.impl.client;

import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.model.ActionConfig;

/**
 * Действие, которое можно выполнить из плагина при помощи соответствующего элемент пользовательского интерфейса
 * (например, кнопкой или гиперссылкой).
 *
 * @author Denis Mitavskiy
 *         Date: 27.08.13
 *         Time: 16:13
 */
public abstract class Action extends BaseComponent {
    private ActionConfig config;
    private Plugin plugin;


}
