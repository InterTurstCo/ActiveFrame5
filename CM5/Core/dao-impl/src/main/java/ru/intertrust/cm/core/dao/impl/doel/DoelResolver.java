package ru.intertrust.cm.core.dao.impl.doel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.model.doel.DoelExpression;
import ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper;

public class DoelResolver {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private ConfigurationExplorer configurationExplorer;

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public ConfigurationExplorer getConfigurationExplorer() {
        return configurationExplorer;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    private String generateEvaluationQuery(DoelExpression expression, String sourceType) {
        DoelExpression.Element[] doelElements = expression.getElements();
        StringBuilder query = new StringBuilder();
        int tableNum = 0;
        String currentType = sourceType;
        String fieldName = null;
        boolean backLink = false;
        for (DoelExpression.Element doelElem : doelElements) {
            //TODO: Реализовать выборку объекта из транзакционного кэша, если он там есть, вместо обращения к БД
            if (tableNum == 0) {
                ++tableNum;
                query.append(" FROM ")
                     .append(DataStructureNamingHelper.getSqlName(currentType))
                     .append(" t")
                     .append(tableNum);
            } else {
                ++tableNum;
                query.append(" JOIN ")
                     .append(DataStructureNamingHelper.getSqlName(currentType))
                     .append(" t")
                     .append(tableNum)
                     .append(" ON t")
                     .append(tableNum - 1)
                     .append(".")
                     .append(backLink ? "id" : fieldName)
                     .append("=t")
                     .append(tableNum)
                     .append(".")
                     .append(backLink ? fieldName : "id");
            }

            if (DoelExpression.ElementType.FIELD == doelElem.getElementType()) {
                DoelExpression.Field field = (DoelExpression.Field) doelElem;
                FieldConfig fieldConfig = configurationExplorer.getFieldConfig(currentType, field.getName());
                if (fieldConfig instanceof ReferenceFieldConfig) {
                    ReferenceFieldConfig refConfig = (ReferenceFieldConfig) fieldConfig;
                    currentType = refConfig.getType();
                }
                fieldName = fieldConfig.getName();
                backLink = false;
            } else if (DoelExpression.ElementType.CHILDREN == doelElem.getElementType()) {
                DoelExpression.Children children = (DoelExpression.Children) doelElem;
                currentType = children.getChildType();
                fieldName = children.getParentLink();
                backLink = true;
            } else {
                throw new RuntimeException("Unknown element type: " + doelElem.getClass().getName());
            }
        }
        query.insert(0, fieldName)      //Вставляем в начало в обратном порядке, чтобы не вычислять позицию
             .insert(0, ".")
             .insert(0, tableNum)
             .insert(0, "SELECT t");
        query.append(" WHERE t1.id=:id");
        return query.toString();
    }

    public List<?> evaluate(DoelExpression expression, DomainObject sourceObject) {
        //TODO Change return type
        String query = generateEvaluationQuery(expression, sourceObject.getTypeName());
        Map<String, Object> params = new HashMap<>();
        RdbmsId id = (RdbmsId) sourceObject.getId();
        params.put("id", id.getId());
        return jdbcTemplate.queryForList(query, params);
    }

    public DoelExpression createReverseExpression(DoelExpression expr, String sourceType) {
        StringBuilder reverseExpr = new StringBuilder();
        String currentType = sourceType;
        for (DoelExpression.Element doelElem : expr.getElements()) {
            if (reverseExpr.length() > 0) {
                reverseExpr.insert(0, ".");
            }
            if (DoelExpression.ElementType.FIELD == doelElem.getElementType()) {
                DoelExpression.Field field = (DoelExpression.Field) doelElem;
                FieldConfig fieldConfig = configurationExplorer.getFieldConfig(currentType, field.getName());
                // Вставляем в обратном порядке, чтобы не вычислять позицию
                reverseExpr.insert(0, field.getName())
                           .insert(0, "^")
                           .insert(0, currentType);
                if (fieldConfig instanceof ReferenceFieldConfig) {
                    ReferenceFieldConfig refConfig = (ReferenceFieldConfig) fieldConfig;
                    currentType = refConfig.getType();
                }
            } else if (DoelExpression.ElementType.CHILDREN == doelElem.getElementType()) {
                DoelExpression.Children children = (DoelExpression.Children) doelElem;
                reverseExpr.insert(0, children.getParentLink());
                currentType = children.getChildType();
            } else {
                throw new RuntimeException("Unknown element type: " + doelElem.getClass().getName());
            }
        }
        return DoelExpression.parse(reverseExpr.toString());
    }

    public DoelExpression createReverseExpression(DoelExpression expr, int count, String sourceType) {
        return createReverseExpression(expr.cutByCount(count), sourceType);
    }
}
