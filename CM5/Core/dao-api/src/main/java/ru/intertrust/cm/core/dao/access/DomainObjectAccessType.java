package ru.intertrust.cm.core.dao.access;

/**
 * Простой тип доступа к доменному объекту.
 * 
 * @author apirozhkov
 */
public class DomainObjectAccessType implements AccessType {

    /**
     * Доступ на чтение объекта
     */
    public static final DomainObjectAccessType READ = new DomainObjectAccessType(1);
    /**
     * Доступ на изменение (сохранение) объекта
     */
    public static final DomainObjectAccessType WRITE = new DomainObjectAccessType(2);
    /**
     * Доступ на удаление объекта
     */
    public static final DomainObjectAccessType DELETE = new DomainObjectAccessType(3);
    /**
     * Доступ на чтение содержимого вложения. Сейчас права с таким доступом хранятся только для вложений и только в собственной таблице ACL
     * (даже если есть наследование прав). При этом вложение считается общедоступным, когда ни для одного пользователя права не указаны.
     */
    public static final DomainObjectAccessType READ_ATTACH = new DomainObjectAccessType(4);

    private int type;

    /**
     * Прямое создание объектов AccessType невозможно.
     * Используйте предопределённые экземпляры {@link #READ}, {@link #WRITE}, {@link #DELETE} и {@link #READ_ATTACH}.
     * 
     * @param type Внутренний идентификатор типа доступа
     */
    protected DomainObjectAccessType(int type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        return new Integer(type).hashCode();
    }

    @Override
    public String toString() {
        switch(type) {
            case 1:  return "READ";
            case 2:  return "WRITE";
            case 3:  return "DELETE";
            case 4:  return "READ_ATTACHMENT";
            default: throw new IllegalArgumentException("Access type is not defined");
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !DomainObjectAccessType.class.equals(obj.getClass())) {
            return false;
        }
        DomainObjectAccessType other = (DomainObjectAccessType) obj;
        return type == other.type;
    }
    
    
}
