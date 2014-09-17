package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.expression.EvaluationException;
import org.springframework.expression.spel.support.StandardTypeComparator;

import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.util.ValueUtil;

/**
 * @author Sergey.Okolot
 *         Created on 16.09.2014 17:07.
 */
public class DomainObjectTypeComparator extends StandardTypeComparator {

    @Override
    public int compare(Object left, Object right) throws EvaluationException {
        if (left == null) {
            return right == null ? 0 : -1;
        } else if (right == null) {
            return 1;
        }
        if (left instanceof Id || right instanceof Id) {
            return compareId(left, right);
        }
        if (left instanceof Value || right instanceof Value) {
            final Value leftValue =
                    left instanceof Value ? (Value) left : getAsValue(((Value) right).getFieldType(), left);
            final Value rightValue =
                    right instanceof Value ? (Value) right : getAsValue(((Value) left).getFieldType(), right);
            return leftValue.compareTo(rightValue);
        }
        return super.compare(left, right);
    }

    private Value getAsValue(final FieldType fieldType, Object source) {
        return ValueUtil.stringValueToObject(source.toString(), fieldType);
    }

    private int compareId(Object left, Object right) {
        final String leftId = left instanceof Id ? ((Id) left).toStringRepresentation() : left.toString();
        final String rightId = right instanceof Id ? ((Id) right).toStringRepresentation() : right.toString();
        return leftId.compareTo(rightId);
    }
}
