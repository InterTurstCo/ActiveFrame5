package ru.intertrust.cm.core.dao.impl.doel;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.*;
import ru.intertrust.cm.core.config.model.doel.DoelExpression;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoelResolver {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private ConfigurationExplorer configurationExplorer;
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public ConfigurationExplorer getConfigurationExplorer() {
        return configurationExplorer;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    public void setDomainObjectTypeIdCache(DomainObjectTypeIdCache domainObjectTypeIdCache) {
        this.domainObjectTypeIdCache = domainObjectTypeIdCache;
    }

    private EvaluationQueryResult generateEvaluationQuery(DoelExpression expression, String sourceType) {
        DoelExpression.Element[] doelElements = expression.getElements();
        StringBuilder query = new StringBuilder();
        int tableNum = 0;
        String currentType = sourceType;
        String fieldName = null;
        FieldConfig resultFieldConfig = null;
        boolean backLink = false;
        String resultDomainObjectType = null;
        for (DoelExpression.Element doelElem : doelElements) {
            if (tableNum == 0) {
                ++tableNum;
                query.append(" FROM ")
                     .append(DataStructureNamingHelper.getSqlName(currentType))
                     .append(" t")
                     .append(tableNum);
                resultDomainObjectType = currentType;
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
                    fieldName = DataStructureNamingHelper.getSqlName(refConfig);
                    resultFieldConfig = refConfig;
                } else {
                    fieldName = fieldConfig.getName();

                }
                resultFieldConfig = fieldConfig;
                backLink = false;
            } else if (DoelExpression.ElementType.CHILDREN == doelElem.getElementType()) {
                DoelExpression.Children children = (DoelExpression.Children) doelElem;
                currentType = children.getChildType();
                fieldName = children.getParentLink();

                FieldConfig fieldConfig = configurationExplorer.getFieldConfig(children.getChildType(), children.getParentLink());
                if(fieldConfig instanceof ReferenceFieldConfig) {
                    ReferenceFieldConfig refConfig = (ReferenceFieldConfig) fieldConfig;
                    String childParentType = refConfig.getType();
                    fieldName = DataStructureNamingHelper.getSqlName(refConfig);
                    resultFieldConfig = refConfig;
                }

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
        return new EvaluationQueryResult(query.toString(), fieldName, resultFieldConfig);
    }

    /**
     * Результат анализа Doel выражения. Содержит SQL запрос, дескриптор возвращаемого поля и название возвращаемого
     * поля в базе (в случае ссылочных полей название поля в базе отличается от названия в дескрипторе).
     * @author atsvetkov
     */
    private class EvaluationQueryResult {

        private String query;
        private String fieldName;
        private FieldConfig resultFieldConfig;

        public EvaluationQueryResult(String query, String fieldName, FieldConfig resultFieldConfig) {
            this.query = query;
            this.fieldName = fieldName;
            this.resultFieldConfig = resultFieldConfig;
        }

        public String getQuery() {
            return query;
        }

        public String getFieldName() {
            return fieldName;
        }

        public FieldConfig getResultFieldConfig() {
            return resultFieldConfig;
        }

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
                    currentType = refConfig.getType();
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
        } else if (BooleanFieldConfig.class.equals(fieldClass)) {
            return BooleanValue.class;
        }
        throw new IllegalArgumentException("Unknown field type: " + fieldClass.getName());
    }

    public <T extends Value> List<Value> evaluate(DoelExpression expression, Id sourceObjectId) {
        //TODO Change return type
        RdbmsId id = (RdbmsId) sourceObjectId;
        //TODO: Реализовать выборку объекта из транзакционного кэша, если он там есть, вместо обращения к БД
        final EvaluationQueryResult evaluationQueryResult = generateEvaluationQuery(expression, domainObjectTypeIdCache.getName(id.getTypeId()));
        String query = evaluationQueryResult.getQuery();

        Map<String, Object> params = new HashMap<>();
        params.put("id", id.getId());

        final FieldConfig fieldConfig = evaluationQueryResult.getResultFieldConfig();
        final String columnName = evaluationQueryResult.getFieldName();

        return jdbcTemplate.query(query, params, new RowMapper<Value>() {
            @Override
            public Value mapRow(ResultSet rs, int rowNum) throws SQLException {
                return createFieldValueFromFieldConfig(rs, fieldConfig, columnName);
            }

            private Value createFieldValueFromFieldConfig(ResultSet rs, final FieldConfig fieldConfig,
                    final String columnName) throws SQLException {
                Value value = null;
                if (fieldConfig != null && StringFieldConfig.class.equals(fieldConfig.getClass())) {
                    String fieldValue = rs.getString(columnName);
                    if (!rs.wasNull()) {
                        value = new StringValue(fieldValue);
                    } else {
                        value = new StringValue();
                    }
                } else if (fieldConfig != null && LongFieldConfig.class.equals(fieldConfig.getClass())) {
                    Long longValue = rs.getLong(columnName);
                    if (!rs.wasNull()) {
                        value = new LongValue(longValue);
                    } else {
                        value = new LongValue();
                    }
                } else if (fieldConfig != null && DecimalFieldConfig.class.equals(fieldConfig.getClass())) {
                    BigDecimal fieldValue = rs.getBigDecimal(columnName);
                    if (!rs.wasNull()) {
                        value = new DecimalValue(fieldValue);
                    } else {
                        value = new DecimalValue();
                    }
                } else if (fieldConfig != null && ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                    if (!rs.wasNull()) {
                        String referenceType = ((ReferenceFieldConfig) fieldConfig).getType();
                        Long longValue = rs.getLong(columnName);

                        value = new ReferenceValue(new RdbmsId(domainObjectTypeIdCache.getId(referenceType), longValue));
                    } else {
                        value = new ReferenceValue();
                    }
                } else if (fieldConfig != null && DateTimeFieldConfig.class.equals(fieldConfig.getClass())) {
                    Timestamp timestamp = rs.getTimestamp(columnName);
                    if (!rs.wasNull()) {
                        Date date = new Date(timestamp.getTime());
                        value = new TimestampValue(date);
                    } else {
                        value = new TimestampValue();
                    }
                } else if (fieldConfig != null && BooleanFieldConfig.class.equals(fieldConfig.getClass())) {
                    Integer booleanInt = rs.getInt(columnName);
                    if (!rs.wasNull()) {
                        value = new BooleanValue(booleanInt == 1);
                    } else {
                        value = new BooleanValue();
                    }
                }

                return value;
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
