package ru.intertrust.cm.core.dao.api;

/**
 * Базовая реализация интерфейса {@link ActionListener}. Все методы не делают ничего.
 * Используйте этот класс как базовый для обработчиков событий транзакций, чтобы не определять все методы интерфейса.
 * 
 * @author apirozhkov
 */
public class BaseActionListener implements ActionListener {

    @Override
    public void onBeforeCommit() { }

    @Override
    public void onRollback() { }

    @Override
    public void onAfterCommit() { }

}
