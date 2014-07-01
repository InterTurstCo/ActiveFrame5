package ru.intertrust.cm.core.dao.impl.doel;

import java.util.Collections;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.doel.DoelFunction;
import ru.intertrust.cm.core.dao.access.AccessToken;

/**
 * Функция DOEL, возвращающая количество значений в списке. Не имеет параметров.
 * При подсчёте количества значений учитываются только значения, отличные от NULL (заполненные).
 * Может применяться к значениям любых типов, но не рекомендуется использовать её для строковых типов
 * ({@link FieldType#STRING} и {@link FieldType#TEXT}), т.к. пустые строки, как правило, сохраняются не как
 * NULL-значения, а как строки длины 0, и в таком случае будут учитываться в общем количестве значений.
 * Функция возвращает единственное значение типа {@link FieldType#LONG}.
 * <p><b>Пример использования:</b><br>
 * <code>Commission^parent:Status(Assigned,Executing):Qty</code>
 * - вычисляет количество поручений у документа в статусах "Назначено" и "Исполняется" (используется также функция
 * {@link StatusFunction}).
 * 
 * @author apirozhkov
 */
@DoelFunction(name = "qty", changesType = true, resultType = FieldType.LONG, resultMultiple = false)
public class QuantityFunction implements DoelFunctionImplementation {

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T extends Value, S extends Value> List<T> process(List<? super S> context, String[] params,
            AccessToken accessToken) {
        int qty = 0;
        for (Object value : context) {
            if (((Value) value).get() != null) {
                qty++;
            }
        }
        return Collections.singletonList((T) new LongValue(qty));
    }

}
