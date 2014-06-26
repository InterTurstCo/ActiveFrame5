package ru.intertrust.cm.core.dao.impl.doel;

import java.util.Collections;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.doel.DoelFunction;
import ru.intertrust.cm.core.dao.access.AccessToken;

/**
 * Функция DOEL, объединяющая несколько строковых значений в одну строку.
 * Обычно применяется на последнем шаге выражения, позволяя получить единое значение, которое может быть использовано
 * для сохранения в поле доменного объекта, подстановки в шаблон уведомления и т.п.
 * Функция принимает 1 обязательный параметр, который используется как разделитель исходных значений.
 * Функция может применяться только к строковым значениям ({@link FieldType#STRING}, {@link FieldType#TEXT})
 * и возвращает значение того же типа.
 * <p><b>Пример использования:</b><br>
 * <code>Commission^parent.Job^parent.Assignee.Name:join(", ")</code> - перечисляет имена всех исполнителей 
 * резолюций через запятую.
 * 
 * @author apirozhkov
 */
@DoelFunction(name = "join",
        requiredParams = 1, contextTypes = { FieldType.STRING, FieldType.TEXT }, resultMultiple = false)
public class TextJoinFunction implements DoelFunctionImplementation {

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T extends Value, S extends Value> List<T> process(List<? super S> context, String[] params,
            AccessToken accessToken) {
        String divider = params[0];
        StringBuilder result = new StringBuilder();
        for (Object value : context) {
            if (result.length() > 0) {
                result.append(divider);
            }
            result.append(((StringValue) value).get());
        }
        return Collections.singletonList((T) new StringValue(result.toString()));
    }

}
