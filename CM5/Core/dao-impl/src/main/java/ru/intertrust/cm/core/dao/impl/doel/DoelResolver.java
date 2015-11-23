package ru.intertrust.cm.core.dao.impl.doel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.doel.DoelExpression;
import ru.intertrust.cm.core.config.doel.DoelExpression.Function;
import ru.intertrust.cm.core.config.doel.DoelFunctionRegistry;
import ru.intertrust.cm.core.config.doel.DoelValidator;
import ru.intertrust.cm.core.config.doel.DoelValidator.DoelTypes;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.SystemSubject;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DoelEvaluator;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.DomainObjectCacheServiceImpl;
import ru.intertrust.cm.core.dao.impl.DomainObjectQueryHelper;
import ru.intertrust.cm.core.dao.impl.sqlparser.SqlQueryModifier;
import ru.intertrust.cm.core.dao.impl.sqlparser.WrapAndLowerCaseSelectVisitor;
import ru.intertrust.cm.core.dao.impl.utils.BasicRowMapper;
import ru.intertrust.cm.core.dao.impl.utils.ValueReader;
import ru.intertrust.cm.core.model.DoelException;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.*;

public class DoelResolver implements DoelEvaluator {

    private static final Logger log = LoggerFactory.getLogger(DoelResolver.class);

    @Autowired
    @Qualifier("switchableNamedParameterJdbcTemplate")
    private NamedParameterJdbcOperations jdbcTemplate;
    //private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private AccessControlService accessControlService;
    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;
    @Autowired
    private DomainObjectCacheServiceImpl domainObjectCacheService;
    @Autowired
    private DoelFunctionRegistry doelFunctionRegistry;
    @Autowired
    private CurrentUserAccessor currentUserAccessor;
    @Autowired
    private UserGroupGlobalCache userGroupCache;
    @Autowired
    private DomainObjectQueryHelper domainObjectQueryHelper;

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

    public void setDomainObjectCacheService(DomainObjectCacheServiceImpl domainObjectCacheService) {
        this.domainObjectCacheService = domainObjectCacheService;
    }

    public void setAccessControlService(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }
    
    public AccessControlService getAccessControlService() {
        return accessControlService;
    }

    @Override
    public <T extends Value> List<T> evaluate(DoelExpression expression, Id sourceObjectId, AccessToken accessToken) {
        return evaluateInternal(expression, sourceObjectId, accessToken);
    }

    public <T extends Value> List<T> evaluateInternal(DoelExpression expression, Id sourceObjectId,
            AccessToken accessToken) {
        try {
            RdbmsId id = (RdbmsId) sourceObjectId;
            DoelValidator.DoelTypes check =
                    DoelValidator.validateTypes(expression, domainObjectTypeIdCache.getName(id));
            if (!check.isCorrect()) {
                return Collections.emptyList();
            }
            ArrayList<T> result = new ArrayList<>();
            for (DoelValidator.DoelTypes.Link type : check.getTypeChains()) {
                evaluateBranch(expression, type, Collections.singletonList(id), result, accessToken);
            }
            if (log.isTraceEnabled()) {
                log.trace("Calculated " + expression + " for " + sourceObjectId + ": " + result);
            }
            return result;
        } catch (DoelException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DoelException("Error evaluating DOEL expression \"" + expression + "\" on type \""
                    + domainObjectTypeIdCache.getName(sourceObjectId) + "\".", ex);
        }
    }

    /**
     * Метод вычисляет одну ветвь выражения, начинающуюся с группы однотипных объектов. Если на некотором шаге
     * вычисления могут быть получены объекты разных типов, метод вызывает рекурсивно сам себя для отдельного
     * вычисления каждой подветки.
     * В качестве разных типов рассматриваются только типы, требующие обращения к различным таблицам БД.
     * Объекты-наследники разных типов не считаются разнотиповыми, если при дальнейшем вычислении выражения
     * используется поле, объявленное в их общем родительском типе.
     * При вычислении используется кэш транзакции, пока это возможно. Для дальнейшего вычисления формируются
     * запросы к БД.
     *
     * @param expr
     *            Частичное выражение, соответствующее ветке дерева типов
     * @param branch
     *            Ветка дерева типов, требующая вычисления
     * @param sourceIds
     *            Идентификаторы объектов, с которых начинается вычисление ветки выражения
     * @param result
     *            Выходной параметр - список, в который заносятся вычисленные значения выражения
     */
    @SuppressWarnings("unchecked")
    private <T extends Value> void evaluateBranch(DoelExpression expr, DoelTypes.Link branch,
            List<RdbmsId> sourceIds, List<T> result, AccessToken accessToken) {
        //String type = branch.getType();

        ArrayList<RdbmsId> nextIds = new ArrayList<>();
        ArrayList<DomainObject> nextObjects = null;
        int step = 0;

        // Получение объектов из кэша
        useCache: while (step < expr.getElements().length) {
            boolean lastStep = step == expr.getElements().length - 1;
            DoelExpression.Element element = expr.getElements()[step];
            if (DoelExpression.ElementType.FIELD == element.getElementType()) {
                DoelExpression.Field fieldElem = (DoelExpression.Field) element;
                // Если на предыдущем шаге выражения использовалась обратная связь, то мы получили из кэша
                // сами доменные объекты, из которых можем сразу брать поля со ссылками или значениями
                if (nextObjects == null) {
                    nextObjects = new ArrayList<>(nextIds.size());
                    for (RdbmsId objId : sourceIds) {
                        DomainObject obj = domainObjectCacheService.get(objId, accessToken);
                        if (obj == null) {
                            break useCache;
                        }
                        nextObjects.add(obj);
                    }
                }
                for (DomainObject obj : nextObjects) {
                    if (lastStep) {
                        /*Value value = obj.getValue(fieldElem.getName());
                        if (value != null) {
                            result.add((T) value);
                        }*/
                        if (fieldElem.getName().equalsIgnoreCase(DomainObjectDao.ID_COLUMN)) {
                            result.add((T) new ReferenceValue(obj.getId()));
                        } else {
                            T value = obj.getValue(fieldElem.getName());
                            if (value != null) {
                                result.add((T) value);
                            }
                        }
                    } else {
                        Id link = obj.getReference(fieldElem.getName());
                        if (link != null) {
                            nextIds.add((RdbmsId) link);
                        }
                    }
                }
                nextObjects = null;
            } else if (DoelExpression.ElementType.CHILDREN == element.getElementType()) {
                DoelExpression.Children childrenElem = (DoelExpression.Children) element;
                // Если на следующем шаге используется поле объекта, лучше сохранить сами объекты, а не идентификаторы
                boolean needObjects = !lastStep &&
                        DoelExpression.ElementType.FIELD == expr.getElements()[step + 1].getElementType();

                for (RdbmsId objId : sourceIds) {
                    List<DomainObject> children = domainObjectCacheService.getAll(objId, accessToken,
                            childrenElem.getChildType(), childrenElem.getParentLink(), "0", "0");
                    if (children == null) {
                        break useCache;
                    }
                    if (needObjects) {
                        if (nextObjects == null) {
                            nextObjects = new ArrayList<>();
                        }
                        nextObjects.addAll(children);
                    } else {
                        for (DomainObject child : children) {
                            if (lastStep) {
                                result.add((T) new ReferenceValue(child.getId()));
                            } else {
                                nextIds.add((RdbmsId) child.getId());
                            }
                        }
                    }
                }
            } else if (DoelExpression.ElementType.SUBEXPRESSION == element.getElementType()) {
                throw new RuntimeException("Subexpressions not supported yet");
            }

            // Готовимся к следующему шагу
            if (nextIds.size() == 0 && (nextObjects == null || nextObjects.size() == 0)) {
                return;
            }
            sourceIds = nextIds;
            nextIds = new ArrayList<>();
            step++;
            List<DoelTypes.Link> nextTypes = branch.getNext();
            if (nextTypes.size() == 0) {
                return;
            } else if (nextTypes.size() == 1) {
                branch = nextTypes.get(0);
            } else /*if (nextTypes.size() > 1)*/ {
                DoelExpression subExpr = expr.excludeCommonBeginning(expr.cutByCount(step));
                Map<String, List<RdbmsId>> groupedIds = groupByType(nextTypes, sourceIds);
                for (DoelTypes.Link subBranch : nextTypes) {
                    if (groupedIds.containsKey(subBranch.getType())) {
                        evaluateBranch(subExpr, subBranch, groupedIds.get(subBranch.getType()), result, accessToken);
                    }
                }
                return;
            }
        }

        // Формируем запрос в БД
        PlainSelect select = new PlainSelect();
        ArrayList<Join> joins = new ArrayList<>();
        int tableNum = 0;
        String currentTable = null;
        String linkField = null;
        while (step < expr.getElements().length) {
            DoelExpression.Element element = expr.getElements()[step];
            if (DoelExpression.ElementType.FIELD == element.getElementType()) {
                DoelExpression.Field fieldElem = (DoelExpression.Field) element;
                String realTable = getColumnTable(branch.getType(), fieldElem.getName());
                if (!realTable.equals(currentTable)) {
                    Table from = new Table();
                    from.setName(realTable);
                    from.setAlias(new Alias("t" + tableNum, false));
                    if (linkField == null) {
                        select.setFromItem(from);
                        select.setWhere(makeWhere("id", sourceIds));
                    } else {
                        Join join = new Join();
                        join.setRightItem(from);
                        EqualsTo link = new EqualsTo();
                        link.setLeftExpression(new Column(new Table(null, "t" + (tableNum - 1)),
                                getSqlName(linkField)));
                        link.setRightExpression(new Column(new Table(null, "t" + tableNum), getSqlName("id")));
                        join.setOnExpression(link);
                        joins.add(join);
                    }
                    tableNum++;
                }
                currentTable = null;
                linkField = fieldElem.getName();

            } else if (DoelExpression.ElementType.CHILDREN == element.getElementType()) {
                DoelExpression.Children childrenElem = (DoelExpression.Children) element;
                currentTable = getColumnTable(childrenElem.getChildType(), childrenElem.getParentLink());
                Table from = new Table();
                from.setName(currentTable);
                from.setAlias(new Alias("t" + tableNum, false));
                if (tableNum == 0) {
                    select.setFromItem(from);
                    select.setWhere(makeWhere(childrenElem.getParentLink(), sourceIds));
                } else {
                    Join join = new Join();
                    join.setRightItem(from);
                    EqualsTo link = new EqualsTo();
                    link.setLeftExpression(new Column(new Table(null, "t" + (tableNum - 1)), getSqlName(linkField)));
                    link.setRightExpression(new Column(new Table(null, "t" + tableNum),
                            getSqlName(childrenElem.getParentLink())));
                    join.setOnExpression(link);
                    joins.add(join);
                }
                tableNum++;
                linkField = "id";

            } else if (DoelExpression.ElementType.SUBEXPRESSION == element.getElementType()) {
                throw new RuntimeException("Subexpressions not supported yet");
            }

            // Готовимся к следующему шагу
            step++;

            Function[] functions = element.getFunctions();
            if (functions != null && functions.length > 0) {
                // Вычисление функции требует значений, т.е. немедленного выполнения запроса
                break;
            }

            if (step < expr.getElements().length) {
                List<DoelTypes.Link> nextTypes = branch.getNext();
                if (nextTypes.size() == 0) {
                    return;
                } else if (nextTypes.size() == 1) {
                    branch = nextTypes.get(0);
                } else /*if (nextTypes.size() > 1)*/ {
                    break;
                }
            }
        }

        select.setJoins(joins);
        FieldConfig fieldConfig = configurationExplorer.getFieldConfig(branch.getType(), linkField);
        List<String> columns = getColumnNames(Collections.singletonList(fieldConfig));
        ArrayList<SelectItem> fields = new ArrayList<>(columns.size());
        for (String column : columns) {
            SelectExpressionItem item = new SelectExpressionItem();
            item.setExpression(new Column(new Table(null, "t" + (tableNum - 1)), column));
            fields.add(item);
        }
        select.setSelectItems(fields);
        select.accept(new WrapAndLowerCaseSelectVisitor());

        String query = applyAcl(select, accessToken);
        Map<String, Object> parameters = new HashMap<String, Object>();
        applyAclParameters(parameters, accessToken);

        List<T> values = jdbcTemplate.query(query, parameters, new DoelResolverRowMapper<T>(linkField, fieldConfig));
        if (values.size() == 0) {
            return;
        }

        Function[] functions = expr.getElements()[step - 1].getFunctions();
        if (functions != null) {
            for (Function function : functions) {
                DoelFunctionImplementation impl = doelFunctionRegistry.getFunctionImplementation(function.getName());
                if (log.isTraceEnabled()) {
                    log.trace("Processing function " + function + " [" + impl.getClass().getName() + "] on " + values);
                }
                values = impl.process(values, function.getArguments(), accessToken);
                if (values.size() == 0) {
                    return;
                }
            }
        }

        if (step < expr.getElements().length) {
            // Продолжаем обработку, если оказалось несколько ветвей
            List<DoelTypes.Link> nextTypes = branch.getNext();
            DoelExpression subExpr = expr.excludeCommonBeginning(expr.cutByCount(step));
            Map<String, List<RdbmsId>> groupedIds = groupByType(nextTypes, values);
            for (DoelTypes.Link subBranch : nextTypes) {
                if (groupedIds.containsKey(subBranch.getType())) {
                    evaluateBranch(subExpr, subBranch, groupedIds.get(subBranch.getType()), result, accessToken);
                }
            }
        } else {
            result.addAll(values);
        }
    }

    private boolean isSystemAccessToken(AccessToken accessToken) {
        return accessToken.getSubject() instanceof SystemSubject;
    }

    private int getUserId(AccessToken accessToken) {
        return ((UserSubject) accessToken.getSubject()).getUserId();
    }

    private void applyAclParameters(Map<String, Object> parameters, AccessToken accessToken) {
        if (!isSystemAccessToken(accessToken)) {
            int userId = getUserId(accessToken);
            parameters.put("user_id", userId);
        }
    }

    /**
     * Добавляет проверки ACL, если маркер доступа не системный
     * @param select изначальный запрос
     * @param accessToken маркер доступа
     * @return измененный SQL запрос
     */
    private String applyAcl(PlainSelect select, AccessToken accessToken) {
        SqlQueryModifier sqlQueryModifier = createSqlQueryModifier();

        String query = null;
        if (!isSystemAccessToken(accessToken)) {
            query = sqlQueryModifier.addAclQuery(select.toString());
        } else {
            query = select.toString();
        }
        return query;
    }

    private SqlQueryModifier createSqlQueryModifier() {
        return new SqlQueryModifier(configurationExplorer, userGroupCache, currentUserAccessor, domainObjectQueryHelper);
    }

    private Map<String, List<RdbmsId>> groupByType(List<DoelTypes.Link> types, List<?> ids) {
        HashSet<String> typeSet = new HashSet<>();
        HashMap<String, String> typeMap = new HashMap<>();
        for (DoelTypes.Link link : types) {
            typeSet.add(link.getType());
        }
        HashMap<String, List<RdbmsId>> result = new HashMap<>();
        idCycle: for (Object item : ids) {
            RdbmsId id = null;
            if (item instanceof RdbmsId) {
                id = (RdbmsId) item;
            } else if (item instanceof ReferenceValue) {
                id = (RdbmsId) ((ReferenceValue) item).get();
            } else {
                throw new IllegalArgumentException("ids list must contain only Ids or ReferenceValues");
            }
            String type = domainObjectTypeIdCache.getName(id);
            if (typeMap.containsKey(type)) {
                type = typeMap.get(type);
            }
            while (type != null) {
                if (typeSet.contains(type)) {
                    if (!result.containsKey(type)) {
                        result.put(type, new ArrayList<RdbmsId>());
                    }
                    result.get(type).add(id);
                    continue idCycle;
                }
                type = configurationExplorer.getConfig(DomainObjectTypeConfig.class, type).getExtendsAttribute();
            }
            if (log.isInfoEnabled()) {
                log.info("Unexpected object type: " + domainObjectTypeIdCache.getName(id)); //*****
            }
        }
        return result;
    }

    private String getColumnTable(String type, String field) {
        do {
            DomainObjectTypeConfig typeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, type);
            FieldConfig fieldConfig = configurationExplorer.getFieldConfig(type, field, false);
            // FIX CMFIVE-3811 - временное решение, удалить после реализации CMFIVE-3839
            if (typeConfig.getExtendsAttribute() != null &&
                    SystemField.isSystemField(field) && !"id".equalsIgnoreCase(field)) {
                fieldConfig = null;
            }
            // FIX CMFIVE-3811 end
            if (fieldConfig != null) {
                return getSqlName(typeConfig);
            }
            type = typeConfig.getExtendsAttribute();
        } while (type != null);
        throw new IllegalArgumentException("Field " + field + " not exists in type " + type);
    }

    @SuppressWarnings("unused")
    private Join makeJoin(String tableName, int num, String prevField, String currentField) {
        Join join = new Join();
        Table table = new Table();
        table.setName(tableName);
        table.setAlias(new Alias("t" + num));
        join.setRightItem(table);
        EqualsTo link = new EqualsTo();
        link.setLeftExpression(new Column(new Table(null, "t" + (num - 1)), getSqlName(prevField)));
        link.setRightExpression(new Column(new Table(null, "t" + num), getSqlName(currentField)));
        join.setOnExpression(link);
        return join;
    }

    private Expression makeWhere(String field, List<RdbmsId> ids) {
        Column column = new Column(new Table(null, "t0"), getSqlName(field));
        if (ids.size() == 1) {
            EqualsTo where = new EqualsTo();
            where.setLeftExpression(column);
            where.setRightExpression(new LongValue(String.valueOf(ids.get(0).getId())));
            return where;
        } else {
            ArrayList<Expression> idList = new ArrayList<>(ids.size());
            for (RdbmsId id : ids) {
                idList.add(new LongValue(String.valueOf(id.getId())));
            }
            return new InExpression(column, new ExpressionList(idList));
        }
    }

    @Deprecated
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

    @Deprecated
    public DoelExpression createReverseExpression(DoelExpression expr, int count, String sourceType) {
        return createReverseExpression(expr.cutByCount(count), sourceType);
    }

    private class DoelResolverRowMapper<T extends Value> extends ValueReader implements RowMapper<T> {

        private FieldConfig fieldConfig;
        private BasicRowMapper.ColumnModel columnModel;

        private DoelResolverRowMapper(String columnName, FieldConfig fieldConfig) {
            this.fieldConfig = fieldConfig;

            columnModel = new ColumnModel();
            columnModel.getColumns().add(new Column(1, columnName));

            if (fieldConfig instanceof ReferenceFieldConfig) {
                columnModel.getColumns().add(new Column(2, getReferenceTypeColumnName(columnName)));
            } else if (fieldConfig instanceof DateTimeWithTimeZoneFieldConfig) {
                columnModel.getColumns().add(new Column(2, getTimeZoneIdColumnName(columnName)));
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public T mapRow(ResultSet rs, int rowNum) throws SQLException {
            return (T) readValue(rs, columnModel.getColumns(), 0, fieldConfig);
        }
    }
}
