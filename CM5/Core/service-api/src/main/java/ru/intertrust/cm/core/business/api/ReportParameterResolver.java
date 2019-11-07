package ru.intertrust.cm.core.business.api;

import java.util.Map;

/**
 * Интерфейс сприн бина, который вычисляет части наименования отчета
 */
public interface ReportParameterResolver {

    /**
     * Метод выполняет вычисление части имени фала результата генерации отчета
     * @param reportName Имя отчета из template.xml
     * @param inParams Все входные параметры с которыми генерируется отчет
     * @param paramName Имя параметра, который необходимо преобразовать и подставить в имя файла
     * @return
     */
    String resolve(String reportName, Map<String, Object> inParams, Object paramName);
}
