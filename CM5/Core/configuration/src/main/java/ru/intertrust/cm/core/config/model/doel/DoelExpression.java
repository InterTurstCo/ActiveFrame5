package ru.intertrust.cm.core.config.model.doel;

import java.util.Arrays;

/**
 * Класс, обеспечивающий разбор и хранение выражений на языке DOEL (Domain Object Expression Language),
 * который используется в различных местах конфигурации. Объекты класса являются неизменяемыми (immutable).
 * 
 * <p>Выражения на DOEL применяются к доменным объектам и позволяют получить поля из них или связанных с ними
 * доменных объектов, а также сами связанные объекты. Простейшим DOEL-выражением является просто имя поля,
 * в таком случае значением выражения становится значение поля исходного доменного объекта. Однако, если это
 * поле является связью (reference, см. {@link ru.intertrust.cm.core.config.model.ReferenceFieldConfig}),
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
 * Планируется также добавление возможности добавлять условия выборки доменных объектов по связям (не реализовано):
 * 
 * <p><code>Commission^Document(onControl=true).Job^Commission(status='executing').Assignee</code>
 * 
 * Корректность DOEL-выражения (существование указанных типов объектов и полей в соответстующих типах) проверяется
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
            String part = parts[i];
            if (part.matches(".+\\^.*")) {
                String[] names = part.split("\\^");
                doel.elements[i] = new Children(names[0], names[1]);
            } else {
                doel.elements[i] = new Field(part);
            }
        }
        return doel;
    }

    private DoelExpression() { }

    public enum ElementType {
        FIELD,
        CHILDREN
    }

    /**
     * Базовый класс для хранения частей DOEL-выражения.
     */
    public abstract static class Element {
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
            if (obj == null || !Field.class.equals(obj.getClass())) {
                return false;
            }
            return name.equals(((Field) obj).name);
        }

        @Override
        public String toString() {
            return name;
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
            if (obj == null || !Children.class.equals(obj.getClass())) {
                return false;
            }
            Children other = (Children) obj;
            return childType.equals(other.childType) && parentLink.equals(other.parentLink);
        }

        @Override
        public String toString() {
            return new StringBuilder().append(childType).append("^").append(parentLink).toString();
        }

        @Override
        public int hashCode() {
            return childType.hashCode() ^ parentLink.hashCode();
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
        if (elements.length != other.elements.length) {
            return false;
        }
        for (int i = 0; i < elements.length; i++) {
            if (!elements[i].equals(other.elements[i])) {
                return false;
            }
        }
        return true;
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
