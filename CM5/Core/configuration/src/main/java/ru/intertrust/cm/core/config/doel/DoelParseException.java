package ru.intertrust.cm.core.config.doel;

import ru.intertrust.cm.core.model.DoelException;

public class DoelParseException extends DoelException {

    private String expression;
    private int position;

    public DoelParseException(String expression, int position) {
        super("DOEL parse error [" + expression + "]: unexpected '" + expression.charAt(position)
                + "' at position " + position);
        this.expression = expression;
        this.position = position;
    }

    public String getExpression() {
        return expression;
    }

    public int getPosition() {
        return position;
    }
}
