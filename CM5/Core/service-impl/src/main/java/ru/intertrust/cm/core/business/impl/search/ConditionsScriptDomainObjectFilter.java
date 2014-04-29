package ru.intertrust.cm.core.business.impl.search;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.DomainObjectFilter;
import ru.intertrust.cm.core.business.api.ScriptService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.tools.SearchAreaFilterScriptContext;

/**
 * Java Script версия фильтра в области поиска. Если скриптовое выражение не устанавливает результат явно в контексте
 * скрипта, то возвращается результат последнего выполненного выражения в скрипте.
 * @author atsvetkov
 */
public class ConditionsScriptDomainObjectFilter implements DomainObjectFilter {

    @Autowired
    private ScriptService scriptService;

    private String conditionsScript;

    public void setConditionsScript(String conditionsScript) {
        this.conditionsScript = conditionsScript;
    }

    @Override
    public boolean filter(DomainObject object) {
        SearchAreaFilterScriptContext context = new SearchAreaFilterScriptContext(object);
        Object evaluateResult = scriptService.eval(conditionsScript, context);
        if (evaluateResult instanceof Boolean) {
            return (Boolean) evaluateResult;
        } else {
            throw new IllegalArgumentException("Script is not correct: " + conditionsScript
                    + ". It should either evaluate to boolean result " +
                    " or define the result in ScriptContext");
        }

    }

}
