package ru.intertrust.cm.core.business.impl;

import java.util.Hashtable;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.Environment;

import ru.intertrust.cm.core.business.api.ScriptContext;
import ru.intertrust.cm.core.business.api.ScriptService;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.tools.Session;

/**
 * 
 * @author atsvetkov
 * 
 */
public class JavaScriptServiceImpl implements ScriptService {

    private static final String SESSION_OBJECT = "session";

    private static final String CONTEXT_OBJECT = "ctx";

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected Environment environment;
    
    private ScriptEngine engine;
    private Map<String, CompiledScript> compiledScripts = new Hashtable<String, CompiledScript>();
    
    private Map<String, Object> injectBeans;

    @PostConstruct
    private void init() {
        engine = new ScriptEngineManager().getEngineByName("js");
    }

    /**
     * Вовращает результат вычисления Java Script выражения. Если результат
     * вычисления не устанавливаетя явно в скрипте (через вызов
     * ScriptContext.setResult()), то возвращается результат вычисления самого
     * скриптового выражения (результат вычисления последнего выполненного
     * выражения).
     */
    @Override
    public Object eval(String script, ScriptContext context) {
        try {
            //Ищется откомпилированный скрипт
            CompiledScript compiledScript = compiledScripts.get(script);
            if (compiledScript == null) {
                //Если не находится то компилится скрипт и сохраняется в кэше откомпилированных скриптов
                compiledScript = ((Compilable) engine).compile(script);
                compiledScripts.put(script, compiledScript);
            }

            Bindings bindings = new SimpleBindings();
            bindings.put(SESSION_OBJECT, new Session());
            bindings.put(CONTEXT_OBJECT, context);
            injectBeans(bindings);

            Object evaluateResult = compiledScript.eval(bindings);
            if (context.getResult() == null) {
                return evaluateResult;
            }
            return context.getResult();
        } catch (ScriptException e) {
            throw new FatalException("Error executing script " + script, e);
        }
    }

    protected void injectBeans(Bindings bindings) {
        synchronized (JavaScriptServiceImpl.class) {
            if (injectBeans == null){
                injectBeans = new Hashtable<String, Object>();
                ApplicationContext context = getInjectedApplicationContext();
                String[] beanDefinitionNames = context.getBeanDefinitionNames();
    
                for (String beanDefinitionName : beanDefinitionNames) {
                    if (context.isSingleton(beanDefinitionName) && 
                            !((AbstractApplicationContext)context).getBeanFactory().getBeanDefinition(beanDefinitionName).isAbstract() ) {
                        Object bean = context.getBean(beanDefinitionName);
                        injectBeans.put(beanDefinitionName, bean);
                    }
                }
                //Добавляем переменные из server.properties
                injectBeans.put("environment", environment);
            }
        }
        
        for (String beanName : injectBeans.keySet()) {
            bindings.put(beanName, injectBeans.get(beanName));
        }
    }
    
    protected ApplicationContext getInjectedApplicationContext(){
        return applicationContext;
    }    
}
