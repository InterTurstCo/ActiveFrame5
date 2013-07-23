package ru.intertrust.cm.core.gui.api.client;

import com.google.gwt.core.client.GWT;

/**
 * Реестр компонентов GUI
 * @author Denis Mitavskiy
 *         Date: 22.07.13
 *         Time: 16:01
 */
public interface ComponentRegistry {
    /**
     * Экземпляр реестра
     */
    public static ComponentRegistry instance = GWT.create(ComponentRegistry.class);

    /**
     * Возвращает новый экземпляр компонента GUI по его названию
     *
     * @param name имя компонента (например "login.window")
     * @param <T> тип получаемого компонента
     * @return новый экземпляр компонента
     */
    <T extends Component> T get(String name);
}
