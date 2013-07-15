package ru.intertrust.cm.core.config.doel;

import org.junit.Test;

import ru.intertrust.cm.core.config.model.doel.DoelExpression;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

public class DoelTest {

    @Test
    public void parseValidExpression() throws Exception {
        DoelExpression expr = DoelExpression.parse("Document.Commission^Document.Assignee");
        assertNotNull(expr);
    }

    @Test
    public void parseEqualExpressions() throws Exception {
        DoelExpression expr1 = DoelExpression.parse("Document.Commission^Document.Assignee");
        DoelExpression expr2 = DoelExpression.parse("  Document.Commission^Document.Assignee   ");
        assertEquals(expr1, expr2);
    }

    @Test
    public void makeExpressionBack() throws Exception {
        String expression = "Document.Commission^Document.Assignee";
        DoelExpression expr = DoelExpression.parse(expression);
        String result = expr.toString();
        assertEquals(expression, result);
    }

    @Test
    public void findCommonBeginning() throws Exception {
        String common = "Document.Commission^Document";
        DoelExpression expr1 = DoelExpression.parse(common + ".Assignee");
        DoelExpression expr2 = DoelExpression.parse(common + ".Job^Commission.Assignee");
        DoelExpression result = expr1.findCommonBeginning(expr2);
        assertEquals(common, result.toString());
    }

    @Test
    public void excludeCommonBeginning() throws Exception {
        String common = "Document.Commission^Document";
        String diff = "Assignee";
        DoelExpression expr1 = DoelExpression.parse(common + "." + diff);
        DoelExpression expr2 = DoelExpression.parse(common + ".Job^Commission.Assignee");
        DoelExpression result = expr1.excludeCommonBeginning(expr2);
        assertEquals(diff, result.toString());
    }
}
