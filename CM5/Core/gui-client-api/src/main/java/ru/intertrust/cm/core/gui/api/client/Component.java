package ru.intertrust.cm.core.gui.api.client;

/**
 * Компонент GUI.
 * Компонент GUI - это элемент управления, который может быть создан по имени.
 *
 * @author Denis Mitavskiy
 *         Date: 22.07.13
 *         Time: 17:19
 */
public interface Component {
    /**
     * Фабричный метод, который создаёт новый экземпляр компонента.
     * @return новый экземпляр компонента
     */
    Component createNew();
}
