package ru.intertrust.cm.core.business.impl;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import ru.intertrust.cm.core.business.api.ScriptContext;
import ru.intertrust.cm.core.business.api.ScriptService;
import ru.intertrust.cm.core.model.EventTriggerException;
import ru.intertrust.cm.core.tools.Session;

/**
 * 
 * @author atsvetkov
 *
 */
public class JavaScriptServiceImpl implements ScriptService {

    private static final String SESSION_OBJECT = "session";

    private static final String CONTEXT_OBJECT = "ctx";

    /**
     * Вовращает результат вычисления Java Script выражения. Если результат вычисления не устанавливаетя явно в скрипте
     * (через вызов ScriptContext.setResult()), то возвращается результат вычисления самого
     * скриптового выражения (результат вычисления последнего выполненного выражения).
     */
    @Override
    public Boolean eval(String script, ScriptContext context) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
        engine.put(SESSION_OBJECT, new Session());
        engine.put(CONTEXT_OBJECT, context);
        try {
            Object evaluateResult = engine.eval(script);
            if (context.getResult() == null) {
                if (evaluateResult instanceof Boolean) {
                    return (Boolean) evaluateResult;
                } else {
                    throw new IllegalArgumentException("Script is not correct: " + script
                            + ". It should either evaluate to boolean result " +
                            " or define the result in ScriptContext");
                }
            }
            return context.getResult();
        } catch (ScriptException e) {
            throw new EventTriggerException("Error executing script " + script);
        }
    }

}
