package ru.intertrust.cm.core.config;

/**
 * Указывает динамическую группу, которой разрешено выполнение действия.
 * @author atsvetkov
 */
public class PermitGroup extends BasePermit {
    public PermitGroup(){
    }

    public PermitGroup(String name){
        super(name);
    }
}
