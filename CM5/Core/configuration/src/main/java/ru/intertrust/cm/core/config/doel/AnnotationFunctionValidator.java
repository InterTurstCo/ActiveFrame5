package ru.intertrust.cm.core.config.doel;

import ru.intertrust.cm.core.business.api.dto.FieldType;

public class AnnotationFunctionValidator implements DoelFunctionValidator {

    private DoelFunction functionAnnotation;

    public AnnotationFunctionValidator(DoelFunction functionAnnotation) {
        this.functionAnnotation = functionAnnotation;
    }

    //@Override
    public boolean acceptsParameters(String[] params) {
        return params.length >= functionAnnotation.requiredParams() &&
                params.length <= functionAnnotation.requiredParams() + functionAnnotation.optionalParams();
    }

    //@Override
    public boolean acceptsContext(String[] params, FieldType fieldType, boolean multiple) {
        if (params.length < functionAnnotation.requiredParams()
                || params.length > functionAnnotation.requiredParams() + functionAnnotation.optionalParams()) {
            return false;
        }
        if (multiple && !functionAnnotation.contextMultiple()) {
            return false;
        }
        if (functionAnnotation.contextTypes().length == 0) {
            return true;
        }
        for (FieldType allowed : functionAnnotation.contextTypes()) {
            if (fieldType == allowed) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ResultInfo validateContext(String[] params, FieldType fieldType, boolean multipleValues,
            ValidationReport report) {
        // Проверка правильности количества параметров
        if (params.length < functionAnnotation.requiredParams()
                || params.length > functionAnnotation.requiredParams() + functionAnnotation.optionalParams()) {
            if (report != null) {
                if (functionAnnotation.optionalParams() == 0) {
                    report.addRecord("Функция требует " + functionAnnotation.requiredParams() + " параметр(а/ов)");
                } else {
                    report.addRecord("Функция принимает от " + functionAnnotation.requiredParams()
                            + " до " + (functionAnnotation.requiredParams() + functionAnnotation.optionalParams())
                            + " параметров");
                }
            }
            return null;
        }
        // Проверка применимости в контексте множественных значений
        if (multipleValues && !functionAnnotation.contextMultiple()) {
            if (report != null) {
                report.addRecord("Функция не может обрабатывать множественные значения");
            }
            return null;
        }

        ResultInfo result = new ResultInfo(
                functionAnnotation.changesType() ? functionAnnotation.resultType() : fieldType,
                multipleValues && functionAnnotation.resultMultiple());
        // Проверка применимости к конкретному типу значений
        if (functionAnnotation.contextTypes().length == 0) {
            return result;
        }
        for (FieldType allowed : functionAnnotation.contextTypes()) {
            if (fieldType == allowed) {
                return result;
            }
        }
        if (report != null) {
            report.addRecord("Функция не обрабатывает значения типа " + fieldType);
        }
        return null;
    }
}
