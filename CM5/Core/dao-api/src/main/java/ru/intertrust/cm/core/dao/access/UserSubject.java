package ru.intertrust.cm.core.dao.access;

/**
 * Субъект доступа &mdash; пользователь
 * 
 * @author apirozhkov
 */
public class UserSubject implements Subject {

    private final int userId;

    /**
     * Создаёт экземпляр объекта
     * 
     * @param userId объект пользователя
     */
    public UserSubject(int userId) {
        this.userId = userId;
    }

    /**
     * @return объект пользователя
     */
    public int getUserId() {
        return userId;
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }

    @Override
    public int hashCode() {
        return userId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !UserSubject.class.equals(obj.getClass())) {
            return false;
        }
        UserSubject other = (UserSubject) obj;
        return this.userId == other.userId;
    }


}
