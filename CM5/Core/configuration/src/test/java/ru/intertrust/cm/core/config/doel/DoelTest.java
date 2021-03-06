package ru.intertrust.cm.core.config.doel;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

public class DoelTest {

    @Test
    public void parseValidExpression() throws Exception {
        DoelExpression expr = DoelExpression.parse("Document.Commission^Document:Status(Assigned).Assignee");
        assertNotNull(expr);
    }

    @Test
    public void parseEqualExpressions() throws Exception {
        DoelExpression expr1 = DoelExpression.parse("Document.Commission^Document:Status(Assigned).Assignee");
        DoelExpression expr2 = DoelExpression.parse("  Document . Commission^Document :Status( 'Assigned' ) . Assignee   ");
        assertEquals(expr1, expr2);
    }

    @Test(expected = DoelParseException.class)
    public void parseInvalidExpression_ExcessCaret() {
        try {
            /*DoelExpression expr = */DoelExpression.parse("Job^Commission^Document");
        } catch (DoelParseException e) {
            assertEquals(14, e.getPosition());
            throw e;
        }
    }

    @Test(expected = DoelParseException.class)
    public void parseInvalidExpression_WrongBracket() {
        try {
            /*DoelExpression expr = */DoelExpression.parse("Job^Commission(Document)");
        } catch (DoelParseException e) {
            assertEquals(14, e.getPosition());
            throw e;
        }
    }

    @Test(expected = DoelParseException.class)
    public void parseInvalidExpression_UnclosedBracket() {
        try {
            /*DoelExpression expr = */DoelExpression.parse("Commission:Func(Document");
        } catch (DoelParseException e) {
            assertEquals(24, e.getPosition());
            throw e;
        }
    }

    @Test(expected = DoelParseException.class)
    public void parseInvalidExpression_UnclosedQuote() {
        try {
            /*DoelExpression expr = */DoelExpression.parse("Name:join(', )");
        } catch (DoelParseException e) {
            assertEquals(14, e.getPosition());
            throw e;
        }
    }

    @Test
    public void makeExpressionBack() throws Exception {
        final String expression = "Document.Commission^Document:Status(Assigned).Assignee";
        DoelExpression expr = DoelExpression.parse(expression);
        String result = expr.toString();
        assertEquals(expression, result);
    }

    @Test
    public void cutExpression() throws Exception {
        final String expressionCut = "Document.Commission^Document";
        final String expressionFull = expressionCut + ".Assignee";
        DoelExpression exprCut = DoelExpression.parse(expressionCut);
        DoelExpression exprTest = DoelExpression.parse(expressionFull).cutByCount(2);
        assertEquals(exprTest, exprCut);
    }

    @Test
    public void findCommonBeginning() throws Exception {
        final String common = "Document.Commission^Document";
        DoelExpression expr1 = DoelExpression.parse(common + ".Assignee");
        DoelExpression expr2 = DoelExpression.parse(common + ".Job^Commission.Assignee");
        DoelExpression result = expr1.findCommonBeginning(expr2);
        assertEquals(common, result.toString());
    }

    @Test
    public void excludeCommonBeginning() throws Exception {
        final String common = "Document.Commission^Document";
        String diff = "Assignee";
        DoelExpression expr1 = DoelExpression.parse(common + "." + diff);
        DoelExpression expr2 = DoelExpression.parse(common + ".Job^Commission.Assignee");
        DoelExpression result = expr1.excludeCommonBeginning(expr2);
        assertEquals(diff, result.toString());
    }
}
