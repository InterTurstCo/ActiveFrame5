package ru.intertrust.cm.core.dao.access;

/**
 * Тип доступа &mdash; создание дочернего доменного объекта определённого типа.
 * 
 * @author apirozhkov
 */
public class CreateChildAccessType implements AccessType {

    private String childType;

    /**
     * Создаёт экземпляр типа доступа
     * 
     * @param childType тип дочернего доменного объекта
     */
    public CreateChildAccessType(String childType) {
        childType.getClass();    // Just to throw NullPointerException
        this.childType = childType;
    }

    /**
     * @return тип дочернего доменного объекта
     */
    public String getChildType() {
        return childType;
    }

    @Override
    public int hashCode() {
        return childType.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !CreateChildAccessType.class.equals(obj.getClass())) {
            return false;
        }
        CreateChildAccessType other = (CreateChildAccessType) obj;
        return childType.equals(other.childType);
    }

    @Override
    public String toString() {
        return "CreateChildAccessType [childType=" + childType + "]";
    }
    
}
