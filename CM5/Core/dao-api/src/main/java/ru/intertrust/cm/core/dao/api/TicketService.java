package ru.intertrust.cm.core.dao.api;

/**
 * Сервис авторизации с помощью билетов
 * @author larin
 *
 */
public interface TicketService {
    /**
     * Создание билета
     * @return
     */
    String createTicket();
    
    /**
     * Проверка билета. Билет одноразовый, после проверки билет становится недействительным
     * Возвращает имя пользователя, для кого был создан билет или формирует исключение если билет невалиден
     * @param ticket
     * @return имя пользователя, для кого сформирован билет
     */
    String checkTicket(String ticket);
}
