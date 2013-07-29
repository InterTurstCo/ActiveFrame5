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

    private int type;

    /**
     * Прямое создание объектов AccessType невозможно.
     * Используйте предопределённые экземпляры {@link #READ}, {@link #WRITE} и {@link #DELETE}.
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
        switch(type){
            case 1:  return "READ";
            case 2:  return "WRITE";
            case 3:  return "DELETE";
            default: throw new IllegalArgumentException("Access tyepe is not defined");
        
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
