package ru.intertrust.cm.core.business.impl.search;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ru.intertrust.cm.core.model.SearchException;

public class SolrUtils {

    public static final String ID_FIELD = "id";
    public static final String SCORE_FIELD = "score";
    public static final String PARAM_FIELD_PREFIX = "literal.";
    private static final Map<ESCAPE_TYPE, String> symbolsToEscape = new HashMap<>(3);

    private enum State {
        PLAIN,
        QUOTED,
        SCREENED
    }

    public enum ESCAPE_TYPE {
        REG_CNTX_SEARCH,
        SIMPLE_SEARCH_EQUALS,
        SIMPLE_SEARCH_LIKE
    }

    static {
        symbolsToEscape.put(ESCAPE_TYPE.REG_CNTX_SEARCH,
                "(?<!\\\\)\\+|(?<!\\\\)\\-|(?<!\\\\)\\&|(?<!\\\\)\\||(?<!\\\\)\\!" +
                        "|(?<!\\\\)\\(|(?<!\\\\)\\)|(?<!\\\\)\\{|(?<!\\\\)\\}|(?<!\\\\)\\[|(?<!\\\\)\\]" +
                        "|(?<!\\\\)\\^|(?<!\\\\)\"|(?<!\\\\)\\~|(?<!\\\\)\\:" +
                        "|(?<!\\\\)\\/|(?<!\\\\)\\\\$" +
                        "|(?<!\\\\)\\\\[_0-9A-Za-zА-Яа-я]");
        symbolsToEscape.put(ESCAPE_TYPE.SIMPLE_SEARCH_EQUALS, "(?<!\\\\)\"|(?<!\\\\)\\\\$");
        symbolsToEscape.put(ESCAPE_TYPE.SIMPLE_SEARCH_LIKE,
                "(?<!\\\\)\\ |(?<!\\\\)\\+|(?<!\\\\)\\-|(?<!\\\\)\\&|(?<!\\\\)\\||(?<!\\\\)\\!" +
                        "|(?<!\\\\)\\(|(?<!\\\\)\\)|(?<!\\\\)\\{|(?<!\\\\)\\}|(?<!\\\\)\\[|(?<!\\\\)\\]" +
                        "|(?<!\\\\)\\^|(?<!\\\\)\"|(?<!\\\\)\\~|(?<!\\\\)\\*|(?<!\\\\)\\?|(?<!\\\\)\\:" +
                        "|(?<!\\\\)\\/|(?<!\\\\)\\\\$");
    }

    public static String protectSearchString(String text) {
        return protectSearchString(text, false);
    }

    public static String protectSearchString(String text, boolean noQuotes) {
        State state = State.PLAIN;
        State lastState = State.PLAIN;
        StringBuilder converted = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            switch (state) {
            case PLAIN:
            case QUOTED:
                switch (ch) {
                case '"':
                    if (noQuotes) {
                        converted.append('\\');
                    } else {
                        state = state == State.PLAIN ? State.QUOTED : State.PLAIN;
                    }
                    break;
                case '\\':
                    lastState = state;
                    state = State.SCREENED;
                    break;
                case '(':
                case ')':
                case '{':
                case '}':
                case '[':
                case ']':
                case ':':
                    converted.append('\\');
                    break;
                }
                break;
            case SCREENED:
                state = lastState;
                break;
            }
            converted.append(ch);
        }
        switch (state) {
        case PLAIN:
            return converted.toString();
        case QUOTED:
            throw new SearchException("Непарная кавычка в строке поиска");
        case SCREENED:
            // Или вернуть строку, отрезав/экранировав завершающий бекслеш?
            throw new SearchException("Строка поиска не может завершаться символом \"\\\"");
        }
        throw new RuntimeException("Unreachable code!");
        //return text.replaceAll("[\\(\\)\\{\\}\\[\\]:\"\\\\]", "\\\\$0");
    }

    public static String joinStrings(String operation, Collection<String> strings) {
        if (strings.size() == 0) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (String str : strings) {
            if (result.length() > 0) {
                result.append(" ").append(operation).append(" ");
            }
            result.append(str);
        }
        if (strings.size() > 1) {
            result.insert(0, "(").append(")");
        }
        return result.toString();
    }

    public static String escapeString(String srcString, ESCAPE_TYPE escapeType) {
        String resultString = srcString;
        if (resultString != null) {
            resultString = resultString.replaceAll(symbolsToEscape.get(escapeType), "\\\\$0");
        }
        return resultString;
    }
}
