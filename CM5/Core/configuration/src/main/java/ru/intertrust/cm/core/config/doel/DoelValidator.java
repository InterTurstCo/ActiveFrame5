package ru.intertrust.cm.core.config.doel;

import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.doel.DoelExpression.Function;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контейнер статических методов, осуществляющих проверку DOEL-выражений.
 * Создание экземпляров данного класса не имеет смысла.
 *
 * @author apirozhkov
 */
public class DoelValidator {

    private static final Logger logger = LoggerFactory.getLogger(DoelValidator.class);

    /**
     * Хранилище информации о типах проверенного DOEL-выражения.
     * Экземпляры этого класса возвращаются методом {@link DoelValidator#validateTypes(DoelExpression, String)}.
     *
     * @author apirozhkov
     */
    public static class DoelTypes {
        private List<Link> typeChains;
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
            private FieldConfig fieldConfig;
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

            public FieldConfig getFieldConfig() {
                return fieldConfig;
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
        @Deprecated
        public Link getTypeChain() {
            if (typeChains.size() > 1) {
                logger.warn("Deprecated method call caused loss of possible results for DOEL expression");
            }
            return typeChains.isEmpty() ? null : typeChains.get(0);
        }

        //TODO Дописать JavaDoc
        public List<Link> getTypeChains() {
            return typeChains;
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
        ValidationReport report;

        ConfigurationExplorer config;
        DoelFunctionRegistry functionRegistry;

        public Processor(DoelExpression expr, String sourceType) {
            this.expression = expr;
            this.sourceType = sourceType;
            this.config = SpringApplicationContext.getContext().getBean(ConfigurationExplorer.class);
            this.functionRegistry = SpringApplicationContext.getContext().getBean(DoelFunctionRegistry.class);
        }

        public Processor(DoelExpression expr, String sourceType, ValidationReport report) {
            this(expr, sourceType);
            this.report = report;
        }

        public DoelTypes process() {
            if (result == null) {
                result = new DoelTypes();
                result.typeChains = processStep(0, sourceType, false);
            }
            return result;
        }

        private List<DoelTypes.Link> processStep(int step, String currentType, boolean multiple) {
            DoelExpression.Element exprElem = expression.getElements()[step];

            // Попытка расщепить выражение на несколько ветвей, если тип неизвестен или не содержит указанное поле
            List<String> typeVariants = Collections.singletonList(currentType);
            if (DoelExpression.ElementType.FIELD == exprElem.getElementType()) {
                DoelExpression.Field fieldElem = (DoelExpression.Field) exprElem;
                if (ReferenceFieldConfig.ANY_TYPE.equals(currentType)) {
                    typeVariants = findAllTypesHavingField(fieldElem.name);
                } else {
                    FieldConfig fieldConfig = config.getFieldConfig(currentType, fieldElem.getName());
                    if (fieldConfig == null) {
                        result.brokenPaths = true;
                        typeVariants = findAllChildTypesHavingField(fieldElem.name, currentType);
                    }
                }
            }

            if (typeVariants.size() == 0) {
                result.brokenPaths = true;
                return Collections.emptyList();
            }

            ArrayList<DoelTypes.Link> branches = new ArrayList<>(typeVariants.size());
            for (String type : typeVariants) {
                String nextType = null;
                FieldConfig fieldConfig = null;
                // Поиск поля в конфигурации и проверка допустимости его использования
                switch (exprElem.getElementType()) {
                    case FIELD:
                        DoelExpression.Field fieldElem = (DoelExpression.Field) exprElem;
                        fieldConfig = config.getFieldConfig(type, fieldElem.getName());
                        if (fieldConfig instanceof ReferenceFieldConfig) {
                            ReferenceFieldConfig refFieldConfig = (ReferenceFieldConfig) fieldConfig;
                            nextType = refFieldConfig.getType();
                        }
                        break;
                    case CHILDREN:
                        DoelExpression.Children childrenElem = (DoelExpression.Children) exprElem;
                        fieldConfig = config.getFieldConfig(childrenElem.getChildType(), childrenElem.getParentLink());
                        if (fieldConfig instanceof ReferenceFieldConfig) {
                            ReferenceFieldConfig refFieldConfig = (ReferenceFieldConfig) fieldConfig;
                            if (checkTypesCompatibility(type, refFieldConfig.getType())) {
                                nextType = childrenElem.getChildType();
                                if (ReferenceFieldConfig.ANY_TYPE.equals(type)) {
                                    type = nextType;
                                    result.brokenPaths = true;
                                }
                            }
                            //TODO Добавить проверку наличия ключа уникальности = единственной ссылки
                            multiple = true;//result.singleResult = false;
                        }
                        break;
                    case SUBEXPRESSION:
                        throw new UnsupportedOperationException("Subexpressions not implemented yet");
                }

                // Валидация функций
                FieldType fieldType = null;
                if (fieldConfig != null) {
                    fieldType = fieldConfig.getFieldType();
                    if (exprElem.getFunctions() != null) {
                        for (Function func : exprElem.getFunctions()) {
                            DoelFunctionValidator funcValidator = functionRegistry.getFunctionValidator(func.getName());
                            if (funcValidator == null) {
                                if (report != null) {
                                    report.addRecord("Функция " + func.getName() + " не определена");
                                }
                                fieldType = null;
                                break;
                            }
                            DoelFunctionValidator.ResultInfo resultInfo = funcValidator.validateContext(
                                    func.getArguments(), fieldType, multiple, report);
                            if (resultInfo == null) {
                                fieldType = null;
                                break;
                            }
                            fieldType = resultInfo.getFieldType();
                            if (FieldType.REFERENCE == fieldType && nextType == null) {
                                nextType = ReferenceFieldConfig.ANY_TYPE;
                            }
                            multiple = resultInfo.isMultipleValues();
                        }
                    }
                }
                if (fieldType == null) {
                    result.brokenPaths = true;
                    continue;
                }

                DoelTypes.Link branch = new DoelTypes.Link();
                branch.type = type;
                branch.fieldConfig = fieldConfig;
                boolean lastStep = step == expression.getElements().length - 1;
                if (!lastStep) {
                    if (nextType == null) {
                        result.brokenPaths = true;
                        continue;
                    }
                    // Рекурсивное вычисление последующих типов
                    List<DoelTypes.Link> subbranches = processStep(step + 1, nextType, multiple);
                    if (subbranches != null && subbranches.size() > 0) {
                        branch.next = subbranches;
                        branches.add(branch);
                    }
                } else {
                    // Определение типов, возвращаемых выражением
                    if (result.resultTypes == null) {
                        result.resultTypes = new HashSet<>();
                    }
                    if (fieldType != null) {
                        result.resultTypes.add(fieldType);
                        if (FieldType.REFERENCE == fieldType) {
                            if (result.resultObjectTypes == null) {
                                result.resultObjectTypes = new HashSet<>();
                            }
                            result.resultObjectTypes.add(nextType);
                        }
                        if (multiple) {
                            result.singleResult = false;
                        }
                        branches.add(branch);
                    }
                }
            }
            return branches;
        }

        private List<String> findAllTypesHavingField(String fieldName) {
            ArrayList<String> types = new ArrayList<>();
            for (DomainObjectTypeConfig type : config.getConfigs(DomainObjectTypeConfig.class)) {
                if (config.getFieldConfig(type.getName(), fieldName) != null) {
                    types.add(type.getName());
                } else {
                    result.brokenPaths = true;
                }
            }
            return types;
        }

        private List<String> findAllChildTypesHavingField(String fieldName, String rootType) {
            ArrayList<String> types = new ArrayList<>();
            for (DomainObjectTypeConfig type : config.findChildDomainObjectTypes(rootType, false)) {
                if (config.getFieldConfig(type.getName(), fieldName) != null) {
                    types.add(type.getName());
                } else {
                    List<String> childTypes = findAllChildTypesHavingField(fieldName, type.getName());
                    if (childTypes.size() > 0) {
                        types.addAll(childTypes);
                    } else {
                        result.brokenPaths = true;
                    }
                }
            }
            return types;
        }

        private boolean checkTypesCompatibility(String type1, String type2) {
            if (ReferenceFieldConfig.ANY_TYPE.equals(type2) || ReferenceFieldConfig.ANY_TYPE.equals(type1)) {
                return true;
            }
            return config.getDomainObjectRootType(type1).equals(config.getDomainObjectRootType(type2));
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
        ValidationReport report = null;
        if (logger.isTraceEnabled()) {
            report = new ValidationReport();
        }
        DoelTypes result = new Processor(expr, sourceType, report).process();
        if (report != null && report.hasRecords()) {
            logger.trace("DOEL expression validation discovered some potential problems in " + expr + ":\n" + report);
        }
        return result;
    }
}
