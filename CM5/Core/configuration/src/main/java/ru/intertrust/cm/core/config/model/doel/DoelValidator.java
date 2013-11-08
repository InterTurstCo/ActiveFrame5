package ru.intertrust.cm.core.config.model.doel;

import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import java.util.*;

/**
 * Контейнер статических методов, осуществляющих проверку DOEL-выражений.
 * Создание экземпляров данного класса не имеет смысла.
 *
 * @author apirozhkov
 */
public class DoelValidator {

    /**
     * Хранилище информации о типах проверенного DOEL-выражения.
     * Экземпляры этого класса возвращаются методом {@link DoelValidator#validateTypes(DoelExpression, String)}.
     *
     * @author apirozhkov
     */
    public static class DoelTypes {
        private Link typeChain;
        private boolean brokenPaths = false;
        private boolean singleResult = true;
        private Set<FieldType> resultTypes;
        private Set<String> resultObjectTypes;

        /**
         * Элемент цепочки (дерева) типов доменных объектов, используемых при вычислении DOEL-выражения.
         *
         * @author apirozhkov
         */
        public static class Link {
            private String type;
            private List<Link> next;

            /**
             * Возвращает тип доменного объекта, используемого на очередном шаге DOEL-выражения.
             *
             * @return имя типа доменного объекта
             */
            public String getType() {
                return type;
            }

            /**
             * Возвращает список элементов, содержащих следующий уровень дерева типов.
             * Чаще всего список содержит единственный элемент. Если элементов несколько, то каждый из них
             * начинает собственную ветвь дерева.
             *
             * @return список элементов следующего уровня; null, если следующий уровень отсутствует
             */
            public List<Link> getNext() {
                return next;
            }
        }

        /**
         * Возвращает цепочку (дерево) типов доменных объектов на каждом шаге вычисления выражения.
         * Первый элемент цепочки содержит тип исходного объекта, для которого должно вычисляться выражение.
         * Последующие элементы цепочки содержат типы объектов, получаемые на каждом шаге вычисления.
         * Если используемое поле связи может указывать на различные типы объектов, то цепочка типов на данном шаге
         * будет разветвляться (превращаясь в дерево).
         * <p>Для корректных выражений длина цепочки типов равна количеству элементов в выражении.
         *
         * @return Корень дерева типов выражения
         */
        public Link getTypeChain() {
            return typeChain;
        }

        /**
         * Позволяет определить, является ли вычисляемое выражением значение единственным.
         * Выражение может возвращать множество значений, если в нём используются обратные связи.
         *
         * @return true, если выражение всегда возвращает единственное значение
         */
        public boolean isSingleResult() {
            return singleResult;
        }

        /**
         * Возвращает набор типов полей, которые могут возвращаться при вычислении выражения.
         * Как правило, этот набор содержит единственный тип поля. Однако, при разветвлении цепочки типов
         * (см. {@link #getTypeChain()}) возможна ситуация, когда несколько различных путей вычисления выражения
         * являются корректными, но последнее поле в различных ветвях имеет разный тип.
         * <p>Метод возвращает null, если выражение не является корректным
         * (метод {@link #isCorrect()} возвращает false).
         *
         * @return Множество типов полей
         */
        public Set<FieldType> getResultTypes() {
            return resultTypes;
        }

        /**
         * Возвращает типы доменных объектов, которые могут возвращаться при вычислении выражения.
         * <p>Метод возвращает null, если выражение не вычисляется в поле ссылочного типа
         * (метод {@link #getResultTypes()} возвращает множество, не содержащее {@link FieldType#REFERENCE}).
         *
         * @return Множество имён типов доменных объектов или null.
         */
        public Set<String> getResultObjectTypes() {
            return resultObjectTypes;
        }

        /**
         * Позволяет определить, является ли выражение корректным.
         * Выражение является корректным, если на каждом шаге может быть получен тип доменного объекта,
         * содержащий поле, используемое на следующем шаге.
         * <p>Для корректного выражения множество, возвращаемое методом {@link #getResultTypes()},
         * содержит хотя бы одно значение.
         * <p>Корректность выражения не означает, что при его вычислении будет обязательно получено значение
         * определённого типа. Если ссылочное поле, используемое на любом шаге вычисления выражения,
         * содержит null, или же по обратной ссылке не находится ни одного элемента, вычисление выражения
         * будет прекращено и возращено пустое значение (null).
         *
         * @return true, если выражение корректно
         */
        public boolean isCorrect() {
            return resultTypes != null && resultTypes.size() > 0;
        }

        /**
         * Позволяет определить, является ли выражение корректным для любого возможного пути вычисления.
         * <p>Если цепочка типов объектов (см. {@link #getTypeChain()}) не содержит ветвлений, то этот метод
         * возвращает то же значение, что и {@link #isCorrect()}. При наличии же ветвлений этот метод возвращает
         * true, только если <i>все</i> пути в дереве от корня имеют длину, равную количеству элементов в выражении.
         * (Метод isCorrect() возвращает true, если в этот дереве есть <i>хотя бы один</i> путь такой длины.)
         *
         * @return true, если выражение корректно всегда
         */
        public boolean isAlwaysCorrect() {
            return !brokenPaths;
        }
    }

    // This class is declared as public, together with its constructor and process() method, only for testing purposes.
    // Sadly, Mockito can't mock static methods
    public static class Processor {
        DoelExpression expression;
        String sourceType;
        DoelTypes result;

        ConfigurationExplorer config;

        public Processor(DoelExpression expr, String sourceType) {
            this.expression = expr;
            this.sourceType = sourceType;
            this.config = SpringApplicationContext.getContext().getBean(ConfigurationExplorer.class);
        }

        public DoelTypes process() {
            if (result == null) {
                result = new DoelTypes();
                result.typeChain = new DoelTypes.Link();
                result.typeChain.type = sourceType;
                processStep(0, result.typeChain);
            }
            return result;
        }

        private void processStep(int step, DoelTypes.Link currentType) {
            DoelExpression.Element exprElem = expression.getElements()[step];
            FieldConfig fieldConfig;
            List<String> nextTypes = Collections.emptyList();

            if (DoelExpression.ElementType.FIELD == exprElem.getElementType()) {
                DoelExpression.Field fieldElem = (DoelExpression.Field) exprElem;
                fieldConfig = config.getFieldConfig(currentType.type, fieldElem.getName());
                if (fieldConfig != null && fieldConfig instanceof ReferenceFieldConfig) {
                    ReferenceFieldConfig refFieldConfig = (ReferenceFieldConfig) fieldConfig;
                    if (ReferenceFieldConfig.ANY_TYPE.equals(refFieldConfig.getType()) &&
                            step < expression.getElements().length - 1) {
                        DoelExpression.Element nextElem = expression.getElements()[step + 1];
                        if (DoelExpression.ElementType.FIELD == nextElem.getElementType()) {
                            nextTypes = findAllTypesHavingField(((DoelExpression.Field) nextElem).getName());
                        } else if (DoelExpression.ElementType.CHILDREN == nextElem.getElementType()) {
                            DoelExpression.Children nextLink = (DoelExpression.Children) nextElem;
                            FieldConfig nextLinkConfig =
                                    config.getFieldConfig(nextLink.childType, nextLink.parentLink);
                            if (nextLinkConfig == null || !(nextLinkConfig instanceof ReferenceFieldConfig)) {
                                //TODO: Неправильная связь на следующем шаге
                            } else {
                                nextTypes = Collections.singletonList(
                                        ((ReferenceFieldConfig) nextLinkConfig).getType());
                            }
                        }
                    } else {
                        nextTypes = Collections.singletonList(refFieldConfig.getType());
                    }
                }
            } else if (DoelExpression.ElementType.CHILDREN == exprElem.getElementType()) {
                DoelExpression.Children chilrenElem = (DoelExpression.Children) exprElem;
                fieldConfig = config.getFieldConfig(chilrenElem.getChildType(), chilrenElem.getParentLink());
                if (fieldConfig != null && (fieldConfig instanceof ReferenceFieldConfig) &&
                        !(((ReferenceFieldConfig) fieldConfig).getType().equals(currentType.getType()) ||
                        ReferenceFieldConfig.ANY_TYPE.equals(((ReferenceFieldConfig) fieldConfig).getType()))) {
                    //TODO: Несвязанная ссылка: тип и поле правильные, но не ссылается на предыдущий объект
                    result.brokenPaths = true;
                } else {
                    result.singleResult = false;
                    nextTypes = Collections.singletonList(chilrenElem.getChildType());
                }
            } else {
                throw new IllegalStateException("Unknown DOEL expression element type: " +
                        exprElem.getClass().getName());
            }

            if (fieldConfig == null) {
                //TODO: Неправильное имя поля
                result.brokenPaths = true;
            } else if (step == expression.getElements().length - 1) {
                if (result.resultTypes == null) {
                    result.resultTypes = new HashSet<>();
                }
                result.resultTypes.add(fieldConfig.getFieldType());
                if (FieldType.REFERENCE == fieldConfig.getFieldType()) {
                    if (result.resultObjectTypes == null) {
                        result.resultObjectTypes = new HashSet<>();
                    }
                    result.resultObjectTypes.addAll(nextTypes);
                }
            } else if (nextTypes.size() > 0) {
                currentType.next = new ArrayList<>(nextTypes.size());
                for (String type : nextTypes) {
                    DoelTypes.Link link = new DoelTypes.Link();
                    link.type = type;
                    currentType.next.add(link);
                    processStep(step + 1, link);
                }
            } else {
                //TODO: Поле не является ссылкой
                result.brokenPaths = true;
            }
        }

        private List<String> findAllTypesHavingField(String fieldName) {
            ArrayList<String> types = new ArrayList<>();
            for (DomainObjectTypeConfig type : config.getConfigs(DomainObjectTypeConfig.class)) {
                if (config.getFieldConfig(type.getName(), fieldName) != null) {
                    types.add(type.getName());
                }
            }
            return types;
        }
    }

    /**
     * Выполняет проверку корректности DOEL-выражения в применении к заданному типу доменного объекта,
     * определяет тип возвращаемого значения и используемые промежуточные типы доменных объектов.
     *
     * @param expr проверяемое DOEL-выражение
     * @param sourceType имя типа исходного (контекстного) доменного объекта
     * @return объект, содержащий информацию о типах, используемых и вычисляемых выражением
     */
    public static DoelTypes validateTypes(DoelExpression expr, String sourceType) {
        return new Processor(expr, sourceType).process();
    }
}
