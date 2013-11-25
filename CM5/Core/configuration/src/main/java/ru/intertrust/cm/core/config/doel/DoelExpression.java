package ru.intertrust.cm.core.config.doel;

import java.util.Arrays;

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
     * @throws ParseException если строка не является корректным выражением на DOEL
     */
    public static DoelExpression parse(String expression) {
        DoelExpression doel = new DoelExpression();
        String[] parts = expression.trim().split("\\.");
        doel.elements = new Element[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            boolean repeated = part.matches(".+\\*$");
            if (repeated) {
                part = part.split("\\*")[0].trim();
            }
            if (part.matches(".+\\^.*")) {
                String[] names = part.split("\\^");
                doel.elements[i] = new Children(names[0].trim(), names[1].trim(), repeated);
            } else {
                doel.elements[i] = new Field(part, repeated);
            }
        }
        return doel;
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
        boolean repeated = false;

        public boolean isRepeated() {
            return repeated;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || !getClass().equals(obj.getClass())) {
                return false;
            }
            return ((Element) obj).repeated == repeated;
        }

        public abstract ElementType getElementType();
    }

    /**
     * Класс, хранящий часть DOEL-выражения - простое имя поля.
     */
    public static class Field extends Element {
        String name;

        Field(String name, boolean repeated) {
            this.name = name;
            this.repeated = repeated;
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
            StringBuilder expr = new StringBuilder(name);
            if (repeated) {
                expr.append("*");
            }
            return expr.toString();
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

        Children(String childType, String parentLink, boolean repeated) {
            this.childType = childType;
            this.parentLink = parentLink;
            this.repeated = repeated;
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
            if (repeated) {
                expr.append("*");
            }
            return expr.toString();
        }

        @Override
        public int hashCode() {
            return childType.hashCode() ^ parentLink.hashCode();
        }
    }

    public static class Subexpression extends Element {
        DoelExpression subExpression;

        public Subexpression(DoelExpression subExpr, boolean repeated) {
            this.subExpression = subExpr;
            this.repeated = repeated;
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
            if (repeated) {
                expr.append("*");
            }
            return expr.toString();
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
        /*if (elements.length != other.elements.length) {
            return false;
        }
        for (int i = 0; i < elements.length; i++) {
            if (!elements[i].equals(other.elements[i])) {
                return false;
            }
        }*/
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
        for (int i = 0; i < elements.length; i++) {
            if (!elements[i].equals(other.elements[i])){
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
