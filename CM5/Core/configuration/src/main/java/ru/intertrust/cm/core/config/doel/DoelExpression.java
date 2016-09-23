package ru.intertrust.cm.core.config.doel;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Класс, обеспечивающий разбор и хранение выражений на языке DOEL (Domain Object Expression Language),
 * который используется в различных местах конфигурации. Объекты класса являются неизменяемыми (immutable).
 *
 * <p>Выражения на DOEL применяются к доменным объектам и позволяют получить поля из них или связанных с ними
 * доменных объектов, а также сами связанные объекты. Простейшим DOEL-выражением является просто имя поля,
 * в таком случае значением выражения становится значение поля исходного доменного объекта. Однако, если это
 * поле является связью (reference, см. {@link ru.intertrust.cm.core.config.ReferenceFieldConfig}),
 * то значением становится сам связанный объект, который, в свою очередь, может быть использован для
 * извлечения его полей (через точку). Например, для объекта "Персона" можно получить имя руководителя его
 * подразделения:
 *
 * <p><code>Department.Head.Name</code>
 *
 * <p>Также возможно извлечение дочерних или просто связанных объектов, хранящих ссылки на исходный внутри себя.
 * Для этого используется конструкция <code><i>тип дочернего объекта</i>^<i>имя поля связи</i></code>. Например,
 * так можно получить имена всех исполнителей поручений по документу:
 *
 * <p><code>Commission^Document.Assignee^Commission.Name</code>
 *
 * <p>Планируется также добавление возможности задавать условия выборки доменных объектов по связям (не реализовано):
 *
 * <p><code>Commission^Document[onControl=true].Job^Commission[status='executing'].Assignee</code>
 *
 * <p>Звёздочка, указанная после имени поля, означает повторный переход по связи, причём он может выполняться
 * неограниченное число раз. Остальная часть выражения будет вычисляться для всех объектов, полученных на каждом
 * шаге такого перехода. Например, если у поручения могут быть дочерние поручения, образующие иерархию неизвестной
 * глубины, исполнители всех дочерних поручений могут быть получены таким выражением:
 *
 * <p><code>Commission^Parent*.Assignee^Commission.Name</code>
 *
 * <p>Корректность DOEL-выражения (существование указанных типов объектов и полей в соответстующих типах) проверяется
 * в момент загрузки конфигурации. Его вычисление никогда не может привести к ошибке. Если на каком-либо этапе
 * связанные доменные объекты отсутствуют, то результатом вычисления становится пустой набор объектов.
 *
 * @author apirozhkov
 */
public class DoelExpression {

    /**
     * Разбирает и сохраняет в объекте выражение на DOEL.
     *
     * @param expression Строка, содержащая DOEL-выражение
     * @return Разобранное DOEL-выражение
     * @throws DoelParseException если строка не является корректным выражением на DOEL
     */
    public static DoelExpression parse(String expression) {
        return new Parser().parse(expression);
    }

    private static class Parser {

        private interface CharProcessor {
            boolean processChar(char ch);
            boolean mayBreakNow();
        }

        private enum TokenType {

            NAME {
                @Override
                CharProcessor getCharProcessor(Parser parser) {
                    return new CharProcessor() {
                        @Override
                        public boolean processChar(char ch) {
                            return isNameChar(ch);
                        }
                        @Override
                        public boolean mayBreakNow() {
                            return true;
                        }
                    };
                }
            },

            SYMBOL {
                @Override
                CharProcessor getCharProcessor(Parser parser) {
                    return new CharProcessor() {
                        boolean ready = true;
                        @Override
                        public boolean processChar(char ch) {
                            try {
                                return ready;
                            } finally {
                                ready = !ready;
                            }
                        }
                        @Override
                        public boolean mayBreakNow() {
                            return true;
                        }
                    };
                }
            },

            STRING {
                @Override
                CharProcessor getCharProcessor(Parser parser) {
                    return new CharProcessor() {
                        char startChar = '?';
                        @Override
                        public boolean processChar(char ch) {
                            switch(startChar) {
                            case '?':
                                startChar = ch;
                                return true;
                            case '!':
                                startChar = '?';
                                return false;
                            default:
                                if (startChar == ch) {
                                    startChar = '!';
                                }
                                return true;
                            }
                        }
                        @Override
                        public boolean mayBreakNow() {
                            return startChar == '!';
                        }
                    };
                }
            },

            EMPTY;

            CharProcessor getCharProcessor(Parser parser) {
                return null;
            }
        }

        private interface TokenAcceptor {
            State accept(Parser parser, String token, TokenType type);
        }

        private enum State {
            FIELD_WAIT (new TokenAcceptor() {
                @Override
                public State accept(Parser parser, String token, TokenType type) {
                    if (type == TokenType.NAME) {
                        parser.stack.push(token);
                        return FIELDNAME_GOT;
                    } else if (type == TokenType.SYMBOL && "(".equals(token)) {
                        parser.stack.push(token);
                        return FIELD_WAIT;
                    } else {
                        throw new IllegalStateException();
                    }
                }
            }),

            FIELDNAME_GOT (new TokenAcceptor() {
                @Override
                public State accept(Parser parser, String token, TokenType type) {
                    if (type == TokenType.SYMBOL && "^".equals(token)) {
                        return CHILDLINK_WAIT;
                    } else {
                        parser.stack.push(new Field((String) parser.stack.pop()));
                        return FIELD_GOT.tokenAcceptor.accept(parser, token, type);
                    }
                }
            }),

            CHILDLINK_WAIT (new TokenAcceptor() {
                @Override
                public State accept(Parser parser, String token, TokenType type) {
                    if (type == TokenType.NAME) {
                        parser.stack.push(new Children((String) parser.stack.pop(), token));
                        return FIELD_GOT;
                    } else {
                        throw new IllegalStateException();
                    }
                }
            }),

            FIELD_GOT (new TokenAcceptor() {
                @Override
                public State accept(Parser parser, String token, TokenType type) {
                    if (type == TokenType.SYMBOL && ".".equals(token)) {
                        parser.finishElement();
                        return FIELD_WAIT;
                    } else if (type == TokenType.SYMBOL && ":".equals(token)) {
                        return FUNCTION_WAIT;
                    } else if (type == TokenType.SYMBOL && ")".equals(token)) {
                        parser.finishElement();
                        LinkedList<Element> nested = new LinkedList<>();
                        while (true/*!parser.stack.isEmpty()*/) {
                            Object stored = parser.stack.pop();
                            if (stored instanceof Element) {
                                nested.addFirst((Element) stored);
                            } else if ("(".equals(stored)) {
                                break;
                            } else {
                                throw new IllegalStateException();
                            }
                        }
                        if (nested.isEmpty()) {
                            throw new IllegalStateException();
                        }
                        DoelExpression subExpr = new DoelExpression();
                        subExpr.elements = nested.toArray(new Element[nested.size()]);
                        parser.stack.push(subExpr);
                        return FIELD_GOT;
                    } else if (type == TokenType.SYMBOL && "*".equals(token)) {
                        Element element = (Element) parser.stack.pop();
                        if (element.isRepeated()) {
                            throw new IllegalStateException();
                        }
                        element.setRepeated(true);
                        parser.stack.push(element);
                        return FIELD_GOT;
                    } else if (type == TokenType.EMPTY) {
                        parser.finishElement();
                        return FINISHED;
                    } else {
                        throw new IllegalStateException();
                    }
                }
            }),

            FUNCTION_WAIT (new TokenAcceptor() {
                @Override
                public State accept(Parser parser, String token, TokenType type) {
                    if (type == TokenType.NAME) {
                        parser.stack.push(token);
                        return FUNCNAME_GOT;
                    } else {
                        throw new IllegalStateException();
                    }
                }
            }),

            FUNCNAME_GOT (new TokenAcceptor() {
                @Override
                public State accept(Parser parser, String token, TokenType type) {
                    if (type == TokenType.SYMBOL && "(".equals(token)) {
                        parser.stack.push(token);
                        return ARGUMENT_WAIT;
                    } else {
                        parser.stack.push(new Function((String) parser.stack.pop(), null));
                        return FIELD_GOT.tokenAcceptor.accept(parser, token, type);
                    }
                }
            }),

            ARGUMENT_WAIT (new TokenAcceptor() {
                @Override
                public State accept(Parser parser, String token, TokenType type) {
                    if (type == TokenType.NAME) {
                        parser.stack.push(token);
                        return ARGUMENT_GOT;
                    } else if (type == TokenType.STRING) {
                        parser.stack.push(token.substring(1, token.length() - 1));  // removing quotes
                        return ARGUMENT_GOT;
                    } else if (type == TokenType.SYMBOL && ")".equals(token)) {
                        if (!"(".equals(parser.stack.pop())) {
                            throw new IllegalStateException();
                        }
                        return FIELD_GOT;
                    } else {
                        throw new IllegalStateException();
                    }
                }
            }),

            ARGUMENT_GOT (new TokenAcceptor() {
                @Override
                public State accept(Parser parser, String token, TokenType type) {
                    if (type == TokenType.SYMBOL && ",".equals(token)) {
                        return ARGUMENT_WAIT;
                    } else if (type == TokenType.SYMBOL && ")".equals(token)) {
                        LinkedList<Object> arguments = new LinkedList<>();
                        while(!parser.stack.isEmpty()) {
                            String arg = (String) parser.stack.pop();
                            if ("(".equals(arg)) {
                                break;
                            }
                            arguments.addFirst(arg);
                        }
                        parser.stack.push(new Function((String) parser.stack.pop(),
                                arguments.toArray(new String[arguments.size()])));
                        return FIELD_GOT;
                    } else {
                        throw new IllegalStateException();
                    }
                }
            }),

            FINISHED (null);

            TokenAcceptor tokenAcceptor;

            private State(TokenAcceptor tokenAcceptor) {
                this.tokenAcceptor = tokenAcceptor;
            }
        }

        TokenType tokenType = null;
        CharProcessor charProcessor = null;
        int positionMark = -1;
        private State state = State.FIELD_WAIT;
        private LinkedList<Object> stack = new LinkedList<>();

        DoelExpression parse(String expression) {
            for (int i = 0; i < expression.length(); i++) {
                char ch = expression.charAt(i);
                if (tokenType != null) {
                    if (charProcessor.processChar(ch)) {
                        continue;
                    }
                    String token = expression.substring(positionMark, i);
                    try {
                        addToken(token, tokenType);
                    } catch (Exception e) {
                        throw new DoelParseException(expression, positionMark);
                    }
                    tokenType = null;
                    charProcessor = null;
                }
                if (Character.isWhitespace(ch)) {
                    continue;
                }
                if (ch == '"' || ch == '\'') {
                    tokenType = Parser.TokenType.STRING;
                } else if (isSpecialChar(ch)) {
                    tokenType = Parser.TokenType.SYMBOL;
                } else if (isNameChar(ch)) {
                    tokenType = Parser.TokenType.NAME;
                }
                if (tokenType == null) {
                    throw new DoelParseException(expression, i);
                }
                charProcessor = tokenType.getCharProcessor(this);
                positionMark = i;
                charProcessor.processChar(ch);
            }
            if (tokenType != null) {
                if (!charProcessor.mayBreakNow()) {
                    throw new DoelParseException(expression, expression.length()/*positionMark*/);
                }
                String token = expression.substring(positionMark);
                try {
                    addToken(token, tokenType);
                } catch (Exception e) {
                    throw new DoelParseException(expression, positionMark);
                }
            }
            DoelExpression doel = new DoelExpression();
            try {
                doel.elements = getResult();
            } catch (Exception e) {
                throw new DoelParseException(expression, expression.length());
            }
            return doel;
        }

        private void finishElement() {
            LinkedList<Function> functions = new LinkedList<>();
            while(true/*!stack.isEmpty()*/) {
                Object stored = stack.pop();
                if (stored instanceof Function) {
                    functions.addFirst((Function) stored);
                } else if (stored instanceof Element) {
                    ((Element) stored).setFunctions(functions.toArray(new Function[functions.size()]));
                    stack.push(stored);
                    break;
                } else {
                    throw new IllegalStateException();
                }
            }
        }

        void addToken(String token, TokenType type) {
            //System.out.println(type.name() + ": " + token);
            state = state.tokenAcceptor.accept(this, token, type);
        }

        Element[] getResult() {
            //System.out.println(TokenType.EMPTY.name());
            state = state.tokenAcceptor.accept(this, null, TokenType.EMPTY);
            if (state != State.FINISHED) {
                throw new IllegalStateException();
            }
            Collections.reverse(stack);
            return stack.toArray(new Element[stack.size()]);
        }
    }

    private static boolean isNameChar(char ch) {
        return !isSpecialChar(ch) && !Character.isWhitespace(ch);
    }

    private static char[] specialChars = new char[] { '.', '^', '*', '(', ')', ':', ',', '"', '\'' };
    static {
        Arrays.sort(specialChars);
    }

    private static boolean isSpecialChar(char ch) {
        return Arrays.binarySearch(specialChars, ch) >= 0;
    }

    private DoelExpression() { }

    public enum ElementType {
        FIELD,
        CHILDREN,
        SUBEXPRESSION
    }

    /**
     * Базовый класс для хранения частей DOEL-выражения.
     */
    public abstract static class Element {
        Function[] functions;
        boolean repeated = false;

        void setFunctions(Function[] functions) {
            this.functions = functions;
        }

        void setRepeated(boolean repeated) {
            this.repeated = repeated;
        }

        public boolean isRepeated() {
            return repeated;
        }

        public Function[] getFunctions() {
            return functions;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || !getClass().equals(obj.getClass())) {
                return false;
            }
            Element that = (Element) obj;
            return this.repeated == that.repeated && Arrays.equals(this.functions, that.functions);
        }

        protected String decorate(String element) {
            StringBuilder result = new StringBuilder(element);
            if (isRepeated()) {
                result.append("*");
            }
            if (functions != null) {
                for (Function func : functions) {
                    result.append(":").append(func.toString());
                }
            }
            return result.toString();
        }

        public abstract ElementType getElementType();
    }

    /**
     * Класс, хранящий часть DOEL-выражения - простое имя поля.
     */
    public static class Field extends Element {
        String name;

        Field(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public ElementType getElementType() {
            return ElementType.FIELD;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj) && name.equals(((Field) obj).name);
        }

        @Override
        public String toString() {
            return decorate(name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    /**
     * Класс, хранящий часть DOEL-выражения - ссылку на дочерние (связанные) объекты.
     */
    public static class Children extends Element {
        String childType;
        String parentLink;

        Children(String childType, String parentLink) {
            this.childType = childType;
            this.parentLink = parentLink;
        }

        public String getChildType() {
            return childType;
        }

        public String getParentLink() {
            return parentLink;
        }

        @Override
        public ElementType getElementType() {
            return ElementType.CHILDREN;
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            Children other = (Children) obj;
            return childType.equals(other.childType) && parentLink.equals(other.parentLink);
        }

        @Override
        public String toString() {
            StringBuilder expr = new StringBuilder().append(childType).append("^").append(parentLink);
            return decorate(expr.toString());
        }

        @Override
        public int hashCode() {
            return childType.hashCode() ^ parentLink.hashCode();
        }
    }

    public static class Subexpression extends Element {
        DoelExpression subExpression;

        Subexpression(DoelExpression subExpr) {
            this.subExpression = subExpr;
        }

        @Override
        public ElementType getElementType() {
            return ElementType.SUBEXPRESSION;
        }

        @Override
        public int hashCode() {
            return subExpression.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj) && ((Subexpression) obj).subExpression.equals(subExpression);
        }

        @Override
        public String toString() {
            StringBuilder expr = new StringBuilder().append("(").append(subExpression.toString()).append(")");
            /*if (repeated) {
                expr.append("*");
            }*/
            return decorate(expr.toString());
        }

    }

    public static class Function {
        String name;
        String[] arguments;

        Function(String name, String[] arguments) {
            this.name = name;
            this.arguments = arguments;
        }

        public String getName() {
            return name;
        }

        public String[] getArguments() {
            if (arguments == null) {
                return new String[0];
            }
            return arguments;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || !Function.class.isAssignableFrom(obj.getClass())) {
                return false;
            }
            Function that = (Function) obj;
            return this.name.equals(that.name) && Arrays.equals(this.arguments, that.arguments);
        }

        @Override
        public int hashCode() {
            int hash = name.hashCode();
            if (arguments != null) {
                for (String arg : arguments) {
                    hash = hash * 31 + arg.hashCode();
                }
            }
            return hash;
        }

        @Override
        public String toString() {
            StringBuilder expr = new StringBuilder();
            expr.append(name);
            if (arguments != null) {
                expr.append("(");
                for (int i = 0; i < arguments.length; i++) {
                    String arg = arguments[i];
                    if (i > 0) {
                        expr.append(",");
                    }
                    if (mustBeQuoted(arg)) {
                        expr.append("\"").append(arg.replaceAll("\"", "\"\"")).append("\"");
                    } else {
                        expr.append(arg);
                    }
                }
                expr.append(")");
            }
            return expr.toString();
        }

        private static boolean mustBeQuoted(String arg) {
            return arg.matches(".*[,\"\\(\\)].*");
        }
    }

    private Element[] elements;

    /**
     * Возвращает массив частей DOEL-выражения.
     */
    public Element[] getElements() {
        return elements;
    }

    /**
     * Возвращает DOEL-выражение, содержащее копию первых count элементов данного.
     * Если запрошенное количество элементов совпадает или превышает число элементов в данном выражении,
     * возвращается оно само.
     *
     * @param count Число копируемых элементов
     * @return Другое или то же DOEL-выражение
     * @throws IllegalArgumentException если count не положительно
     */
    public DoelExpression cutByCount(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive number");
        }
        if (count >= elements.length) {
            return this;
        }
        DoelExpression result = new DoelExpression();
        result.elements = Arrays.copyOf(elements, count);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !DoelExpression.class.equals(obj.getClass())) {
            return false;
        }
        DoelExpression other = (DoelExpression) obj;
        return elements.length == other.elements.length && elements.length == countCommonBeginning(other);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Element link : elements) {
            if (result.length() > 0) {
                result.append(".");
            }
            result.append(link.toString());
        }
        return result.toString();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (Element link : elements) {
            hash *= 31;
            hash ^= link.hashCode();
        }
        return hash;
    }

    private int countCommonBeginning(DoelExpression other) {
        int matches = 0;
        for (int i = 0; i < elements.length && i < other.elements.length; i++) {
            if (!elements[i].equals(other.elements[i])) {
                break;
            }
            ++matches;
        }
        return matches;
    }

    /**
     * Формирует DOEL-выражение, являющееся общей частью двух выражений.
     *
     * @see #excludeCommonBeginning(DoelExpression)
     *
     * @param other второе DOEL-выражение
     * @return null, если выражения не имеют общей части
     */
    public DoelExpression findCommonBeginning(DoelExpression other) {
        int matches = countCommonBeginning(other);
        if (matches == 0) {
            return null;
        }
        DoelExpression result = new DoelExpression();
        result.elements = Arrays.copyOf(elements, matches);
        return result;
    }

    /**
     * Формирует DOEL-выражение, не включающее общую часть двух выражений.
     *
     * <p>Важно отметить, что при вычислении сформированного этим методом выражения контекстным объектом для него
     * должен быть не тот объект, который предполагался для исходного выражения, а объект, вычисленный по общей
     * для этих же двух части выражения.
     *
     * <p>Этот метод совместно с {@link #findCommonBeginning(DoelExpression)} может использоваться для
     * более эффективной организации вычислений выражений, использующих множество промежуточных объектов.
     * Сначала необходимо вычислить выражение, сформированное методом findCommonBeginning(), а затем использовать
     * вычисленный объект в качестве начального для выражений, сформированных вычитанием общего выражения из исходных:
     * <pre>
     * DoelExpression expr1, expr2;
     * DomainObject sourceObj;
     * //...
     * DoelExpression common = expr1.findCommonBeginning(expr2);
     * if (common != null) {
     *     sourceObj = calculateDoel(common, sourceObj);
     *     expr1 = expr1.excludeCommonBeginning(common);
     *     expr2 = expr2.excludeCommonBeginning(common);
     * }
     * result1 = calculateDoel(expr1, sourceObj);
     * result2 = calculateDoel(expr2, sourceObj);
     *
     * @param other второе DOEL-выражение. Рекомендуется использовать выражение, сформированное ранее методом
     * {@link #findCommonBeginning(DoelExpression)}
     * @return null, если другое выражение полностью включает в себя данное
     */
    public DoelExpression excludeCommonBeginning(DoelExpression other) {
        int matches = countCommonBeginning(other);
        if (matches == 0) {
            return this;
        }
        if (matches == elements.length) {
            return null;
        }
        DoelExpression result = new DoelExpression();
        result.elements = Arrays.copyOfRange(elements, matches, elements.length);
        return result;
    }
}
