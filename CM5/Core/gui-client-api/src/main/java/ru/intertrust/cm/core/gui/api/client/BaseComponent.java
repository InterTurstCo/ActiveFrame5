package ru.intertrust.cm.core.gui.api.client;

/**
 * Базовая реализация компонентов пользовательского интерфейса
 * @author Denis Mitavskiy
 *         Date: 21.08.13
 *         Time: 14:38
 */
public abstract class BaseComponent implements Component {
    private String name;

    /**
     * Устанавливает имя компонента. Используется системой, разработчики не должны использовать этот метод
     * @param name имя компонента
     * @return
     */
    Component setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Возвращает имя компонента
     * @return имя компонента
     */
    public String getName() {
        return name;
    }
}
