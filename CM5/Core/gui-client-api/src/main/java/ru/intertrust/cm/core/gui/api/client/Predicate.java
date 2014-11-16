package ru.intertrust.cm.core.gui.api.client;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.11.2014
 *         Time: 13:11
 */
public interface Predicate<T> {
    boolean evaluate(T input);
}
