package ru.intertrust.cm.core.config;

/**
 * Указывает контекстную роль, которой разрешено выполнение действия.
 * @author atsvetkov
 */
public class PermitRole extends BasePermit {
    public PermitRole(){
    }

    public PermitRole(String name){
        super(name);
    }

}
