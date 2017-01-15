package ru.intertrust.cm.core.business.impl.search;

import ru.intertrust.cm.core.model.SearchException;

public class SolrUtils {

    public static final String SCORE_FIELD = "score";

    private enum State {
        PLAIN,
        QUOTED,
        SCREENED
    }

    public static String protectSearchString(String text) {
        return protectSearchString(text, false);
    }

    public static String protectSearchString(String text, boolean noQuotes) {
        State state = State.PLAIN;
        State lastState = null;
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
}
