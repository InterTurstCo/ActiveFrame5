package ru.intertrust.cm.core.config.doel;

import ru.intertrust.cm.core.business.api.dto.FieldType;

/**
 * Интерфейс, позволяющий проверить допустимость использования функции в контексте конкретного DOEL-выражения.
 * Функции не обязаны реализовывать этот интерфейс, т.к. предоставляется реализация по умолчанию
 * ({@link AnnotationFunctionValidator}), выполняющая формальные проверки по атрибутам аннотации {@link DoelFunction}.
 * Функции могут реализовать этот интерфейс, если им требуется более детальная проверка.
 */
public interface DoelFunctionValidator {

    //boolean acceptsParameters(String[] params);

    //boolean acceptsContext(String[] params, FieldType fieldType, boolean multiple);

    /**
     * Определяет, может ли функция быть вычислена в данном контексте, а в случае положительного ответа -
     * тип и множественность значений, получаемых в результате вычисления функции в этом контексте.
     * @param params массив строк - параметров функции
     * @param fieldType тип значений, поступающих на вход функции
     * @param multiple true, если значений может быть несколько
     * @param report объект, получающий сообщения об ошибках валидации
     * @return тип значений на выходе функции или null, если функция не применима в данном контексте
     */
    ResultInfo validateContext(String[] params, FieldType fieldType, boolean multiple, ValidationReport report);

    /**
     * Хранилище результата выполнения метода
     * {@link DoelFunctionValidator#validateContext(String[], FieldType, boolean, ValidationReport)}.
     */
    public static class ResultInfo {
        private FieldType fieldType;
        private boolean multipleValues;

        public ResultInfo(FieldType fieldType, boolean multipleValues) {
            this.fieldType = fieldType;
            this.multipleValues = multipleValues;
        }

        /**
         * @return тип значений, возвращаемых функцией (в заданном контексте)
         */
        public FieldType getFieldType() {
            return fieldType;
        }

        /**
         * @return true, если функция может возвращать множественные значения (в заданном контексте)
         */
        public boolean isMultipleValues() {
            return multipleValues;
        }
    }
}
