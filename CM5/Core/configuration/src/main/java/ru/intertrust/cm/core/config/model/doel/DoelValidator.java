package ru.intertrust.cm.core.config.model.doel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldTypeConfig;
import ru.intertrust.cm.core.util.SpringApplicationContext;

public class DoelValidator {

    public static class ValidationResult {
        private Link typeChain;
        private boolean singleResult = true;
        private Set<FieldType> resultTypes;
        private Set<String> resultObjectTypes;
        private boolean brokenPaths = false;

        public Link getTypeChain() {
            return typeChain;
        }

        public boolean isSingleResult() {
            return singleResult;
        }

        public Set<FieldType> getResultTypes() {
            return resultTypes;
        }

        public Set<String> getResultObjectTypes() {
            return resultObjectTypes;
        }

        public boolean isCorrect() {
            return resultTypes != null && resultTypes.size() > 0;
        }

        public boolean isAlwaysCorrect() {
            return !brokenPaths;
        }
    }

    public static class Link {
        private String type;
        private List<Link> next;

        public String getType() {
            return type;
        }

        public List<Link> getNext() {
            return next;
        }
    }

    // This class is declared as public, together with its constructor and process() method, only for testing purposes.
    // Sadly, Mockito can't mock static methods
    public static class ValidationProcessor {
        DoelExpression expression;
        String sourceType;
        ValidationResult result;

        ConfigurationExplorer config;

        public ValidationProcessor(DoelExpression expr, String sourceType) {
            this.expression = expr;
            this.sourceType = sourceType;
            this.config = SpringApplicationContext.getContext().getBean(ConfigurationExplorer.class);
        }

        public ValidationResult process() {
            if (result == null) {
                result = new ValidationResult();
                result.typeChain = new Link();
                result.typeChain.type = sourceType;
                processStep(0, result.typeChain);
            }
            return result;
        }

        private void processStep(int step, Link currentType) {
            DoelExpression.Element exprElem = expression.getElements()[step];
            FieldConfig fieldConfig;
            List<String> nextTypes = Collections.emptyList();

            if (exprElem instanceof DoelExpression.Field) {
                DoelExpression.Field fieldElem = (DoelExpression.Field) exprElem;
                fieldConfig = config.getFieldConfig(currentType.type, fieldElem.getName());
                if (fieldConfig != null && fieldConfig instanceof ReferenceFieldConfig) {
                    ReferenceFieldConfig refFieldConfig = (ReferenceFieldConfig) fieldConfig;
                    nextTypes = new ArrayList<>(refFieldConfig.getTypes().size());
                    for (ReferenceFieldTypeConfig type : refFieldConfig.getTypes()) {
                        nextTypes.add(type.getName());
                    }
                }
            } else if (exprElem instanceof DoelExpression.Children) {
                DoelExpression.Children chilrenElem = (DoelExpression.Children) exprElem;
                fieldConfig = config.getFieldConfig(chilrenElem.getChildType(), chilrenElem.getParentLink());
                if (fieldConfig != null && !(fieldConfig instanceof ReferenceFieldConfig &&
                        ((ReferenceFieldConfig) fieldConfig).getTypes().contains(
                                new ReferenceFieldTypeConfig(currentType.getType())))) {
                    //TODO: Несвязанная ссылка: тип и поле правильные, но не ссылается на предыдущий объект
                } else {
                    result.singleResult = false;
                }
                nextTypes = Collections.singletonList(chilrenElem.getChildType());
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
                    Link link = new Link();
                    link.type = type;
                    currentType.next.add(link);
                    processStep(step + 1, link);
                }
            } else {
                //TODO: Поле не является ссылкой
                result.brokenPaths = true;
            }
        }
    }

    public static ValidationResult validateTypes(DoelExpression expr, String sourceType) {
        return new ValidationProcessor(expr, sourceType).process();
    }
}
