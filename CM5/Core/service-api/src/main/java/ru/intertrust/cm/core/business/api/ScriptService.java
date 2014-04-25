package ru.intertrust.cm.core.business.api;


/**
 * Сервис выполнения скриптовых выражений
 * @author atsvetkov
 *
 */
public interface ScriptService {

    /**
     * Выполняет скипт. Вовзращает результат выполнения скипта. 
     * @param script
     * @param context
     * @return
     */
    Boolean eval(String script, ScriptContext context);
}
