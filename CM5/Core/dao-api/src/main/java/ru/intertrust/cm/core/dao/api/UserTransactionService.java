package ru.intertrust.cm.core.dao.api;



/**
 * 
 * Реализация сервиса для работы с пользовательскими транзакциями.
 * Сервис регистрирует слушателя, который должен быть выполнен по завершению транзакции.
 * Если действия выполняются вне контеста контейнерной транзакции,
 * регистрация слушателей будет проигнорированна.
 */
public interface UserTransactionService {
    /**
     * А.П. - метод {@link #addListener(ActionListener)} может и должен использоваться для любых обработчиков
     */
    @Deprecated
    public void addListenerForSaveFile(final String filePath);

    /**
     * Регистрируем событие для фиксации или отката "пользовательской" транзакции.
     * @param actionListener - событие, необходимо переопределить методы onCommit и onRollback.
     */
    public void addListener(ActionListener actionListener);


    /**
     * возвращает ID текущей транзакции
     * @return ID текущей транзакции или null вне контекста
     */
    public String getTransactionId();

    /**
     * Возвращает слушатель задданного типа для текущей транзакции
     * @param tClass класс
     * @param <T> тип слушателя
     * @return слушатель заданного типа или null, если такой не зарегистрирован
     */
    public <T> T getListener(Class<T> tClass);
}
