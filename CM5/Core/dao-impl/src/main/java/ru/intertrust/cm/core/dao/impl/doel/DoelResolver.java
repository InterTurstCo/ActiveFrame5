package ru.intertrust.cm.core.dao.impl.doel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ru.intertrust.cm.core.business.api.dto.DecimalValue;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.TimestampValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.DateTimeFieldConfig;
import ru.intertrust.cm.core.config.model.DecimalFieldConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.LongFieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.model.StringFieldConfig;
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
                    //TODO: Реализовать обработку множественных типов ссылки
                    currentType = refConfig.getTypes().get(0).getName();
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

    private class ExpressionTypes {
        String[] elementTypes;
        Class<? extends Value> resultType;
    }

    private ExpressionTypes evaluateExpressionTypes(DoelExpression expr, String sourceType) {
        ExpressionTypes types = new ExpressionTypes();
        types.elementTypes = new String[expr.getElements().length];
        String currentType = sourceType;
        int i = 0;
        for (DoelExpression.Element doelElem : expr.getElements()) {
            if (DoelExpression.ElementType.FIELD == doelElem.getElementType()) {
                DoelExpression.Field field = (DoelExpression.Field) doelElem;
                FieldConfig fieldConfig = configurationExplorer.getFieldConfig(currentType, field.getName());
                if (fieldConfig instanceof ReferenceFieldConfig) {
                    ReferenceFieldConfig refConfig = (ReferenceFieldConfig) fieldConfig;
                    types.elementTypes[i] = currentType;
                    //TODO: Реализовать обработку множественных типов ссылки
                    currentType = refConfig.getTypes().get(0).getName();
                } else if (i == types.elementTypes.length - 1) {
                    types.resultType = getValueClass(fieldConfig);
                } else {
                    //TODO: Выбрасывать ли исключение? Выбрать собственный тип или заменить на return
                    throw new IllegalStateException();
                }
            } else if (DoelExpression.ElementType.CHILDREN == doelElem.getElementType()) {
                DoelExpression.Children children = (DoelExpression.Children) doelElem;
                currentType = children.getChildType();
                //fieldName = children.getParentLink();
            }
        }
        if (types.resultType == null) {
            //types.resultType = 
        }
        return types;
    }

    //TODO: Сделать публичным сервисом и перенести в более подходящее место
    private Class<? extends Value> getValueClass(FieldConfig fieldConfig) {
        Class<? extends FieldConfig> fieldClass = fieldConfig.getClass();
        if (ReferenceFieldConfig.class.equals(fieldClass)) {
            return ReferenceValue.class;
        } else if (StringFieldConfig.class.equals(fieldClass)) {
            return StringValue.class;
        } else if (DateTimeFieldConfig.class.equals(fieldClass)) {
            return TimestampValue.class;
        } else if (LongFieldConfig.class.equals(fieldClass)) {
            return LongValue.class;
        } else if (DecimalFieldConfig.class.equals(fieldClass)) {
            return DecimalValue.class;
        }
        throw new IllegalArgumentException("Unknown field type: " + fieldClass.getName());
    }

    public <T extends Value> List<T> evaluate(DoelExpression expression, Id sourceObjectId) {
        //TODO Change return type
        RdbmsId id = (RdbmsId) sourceObjectId;
        //TODO: Реализовать выборку объекта из транзакционного кэша, если он там есть, вместо обращения к БД
        String query = generateEvaluationQuery(expression, id.getTypeName());
        Map<String, Object> params = new HashMap<>();
        params.put("id", id.getId());

        return jdbcTemplate.query(query, params, new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                /*T value = new T();
                return value;*/
                return null;
            }
        });
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
                    //TODO: Реализовать обработку множественных типов ссылки
                    currentType = refConfig.getTypes().get(0).getName();
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
