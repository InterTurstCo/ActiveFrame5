package ru.intertrust.cm.core.dao.impl.doel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.doel.DoelExpression;
import ru.intertrust.cm.core.config.doel.DoelExpression.Function;
import ru.intertrust.cm.core.config.doel.DoelFunctionRegistry;
import ru.intertrust.cm.core.config.doel.DoelValidator;
import ru.intertrust.cm.core.config.doel.DoelValidator.DoelTypes;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.*;
import ru.intertrust.cm.core.dao.impl.DomainObjectCacheServiceImpl;
import ru.intertrust.cm.core.dao.impl.DomainObjectQueryHelper;
import ru.intertrust.cm.core.model.DoelException;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.*;

public class DoelResolver implements DoelEvaluator {

    private static final Logger log = LoggerFactory.getLogger(DoelResolver.class);

    @org.springframework.beans.factory.annotation.Value("${doel.caches.skip:false}")
    private boolean skipCaches;
    @org.springframework.beans.factory.annotation.Value("${doel.debug.expressions:}")
    private String debuggingExpressionsSetting;
    private Set<DoelExpression> debuggingExpressions;

    @Autowired
    private CollectionsDao collectionsDao;
    @Autowired
    private AccessControlService accessControlService;
    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;
    @Autowired
    private DomainObjectCacheServiceImpl domainObjectCacheService;
    @Autowired
    private GlobalCacheClient globalCacheClient;
    @Autowired
    private DoelFunctionRegistry doelFunctionRegistry;
    @Autowired
    private CurrentUserAccessor currentUserAccessor;
    @Autowired
    private UserGroupGlobalCache userGroupCache;
    @Autowired
    private DomainObjectQueryHelper domainObjectQueryHelper;
/*
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
*/
    public void setAccessControlService(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }
    
    public AccessControlService getAccessControlService() {
        return accessControlService;
    }

    @PostConstruct
    private void initialize() {
        if (debuggingExpressionsSetting != null && !debuggingExpressionsSetting.isEmpty()) {
            debuggingExpressions = new HashSet<>();
            for (String expr : debuggingExpressionsSetting.split(";")) {
                debuggingExpressions.add(DoelExpression.parse(expr));
            }
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public <T extends Value> List<T> evaluate(DoelExpression expression, Id sourceObjectId, AccessToken accessToken) {
        return evaluateInternal(expression, sourceObjectId, accessToken);
    }

    @SuppressWarnings("rawtypes")
    public <T extends Value> List<T> evaluateInternal(DoelExpression expression, Id sourceObjectId,
            AccessToken accessToken) {
        DebugPrinter debugPrinter = null;
        if (needToDebugExpression(expression)) {
            debugPrinter = new DebugPrinter(expression, sourceObjectId);
        }
        if (sourceObjectId == null) {
            if (debugPrinter != null) {
                debugPrinter.print(str("no evaluation"));
            }
            return Collections.emptyList();
        }
        try {
            RdbmsId id = (RdbmsId) sourceObjectId;
            DoelValidator.DoelTypes check =
                    DoelValidator.validateTypes(expression, domainObjectTypeIdCache.getName(id));
            if (!check.isCorrect()) {
                if (debugPrinter != null) {
                    debugPrinter.print(str("not valid for type " + domainObjectTypeIdCache.getName(id)));
                }
                return Collections.emptyList();
            }
            ArrayList<T> result = new ArrayList<>();
            for (DoelValidator.DoelTypes.Link type : check.getTypeChains()) {
                evaluateBranch(expression, type, Collections.singletonList(id), result, accessToken, debugPrinter);
            }
            if (debugPrinter != null) {
                debugPrinter.print(list("result", result));
            }
            else if (log.isTraceEnabled()) {
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <T extends Value> void evaluateBranch(DoelExpression expr, DoelTypes.Link branch,
            List<RdbmsId> sourceIds, List<T> result, AccessToken accessToken, DebugPrinter debugPrinter) {
        //String type = branch.getType();

        ArrayList<RdbmsId> nextIds = new ArrayList<>();
        ArrayList<DomainObject> nextObjects = null;
        int step = 0;

        // Получение объектов из кэша
        useCache: while (step < expr.getElements().length) {
            if (skipCaches) {
                break useCache;
            }
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
                        if (debugPrinter != null && obj != null) {
                            debugPrinter.print(var("step", step), var("elem", element), var("objId", objId),
                                    str("got from tx cache"), var("object", obj));
                        }
                        if (obj == null) {
                            obj = globalCacheClient.getDomainObject(objId, accessToken);
                            if (debugPrinter != null && obj != null) {
                                debugPrinter.print(var("step", step), var("elem", element), var("objId", objId),
                                        str("got from global cache"), var("object", obj));
                            }
                        }

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
                            if (debugPrinter != null) {
                                debugPrinter.print(var("step", step), var("elem", element), var("objId", obj.getId()),
                                        str("use ID " + obj.getId()));
                            }
                        } else {
                            T value = obj.getValue(fieldElem.getName());
                            if (value != null) {
                                result.add(value);
                            }
                            if (debugPrinter != null) {
                                debugPrinter.print(var("step", step), var("elem", element), var("objId", obj.getId()),
                                        str(value == null ? "no value; skipped" : "use value " + value.get()));
                            }
                        }
                    } else {
                        Id link = obj.getReference(fieldElem.getName());
                        if (link != null) {
                            nextIds.add((RdbmsId) link);
                        }
                        if (debugPrinter != null) {
                            debugPrinter.print(var("step", step), var("elem", element), var("objId", obj.getId()),
                                    str(link == null ? "no ref; skipped" : "use ref " + link));
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
                            childrenElem.getChildType(), childrenElem.getParentLink(), String.valueOf(false));
                    if (debugPrinter != null && children != null) {
                        debugPrinter.print(var("step", step), var("elem", element), var("objId", objId),
                                str("got linked from tx cache:"), list(children));
                    }

                    if (children == null) {
                        children = globalCacheClient.getLinkedDomainObjects(objId, childrenElem.getChildType(),
                                childrenElem.getParentLink(), false, accessToken);
                        if (debugPrinter != null && children != null) {
                            debugPrinter.print(var("step", step), var("elem", element), var("objId", objId),
                                    str("got linked from global cache:"), list(children));
                        }
                    }

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
                Map<String, List<RdbmsId>> groupedIds = groupByType(nextTypes, sourceIds, debugPrinter);
                for (DoelTypes.Link subBranch : nextTypes) {
                    if (groupedIds.containsKey(subBranch.getType())) {
                        if (debugPrinter != null) {
                            debugPrinter.print(var("step", step),
                                    str("evaluate subexpr " + subExpr + " for type" + subBranch.getType()),
                                    list(groupedIds.get(subBranch.getType())));
                        }
                        evaluateBranch(subExpr, subBranch, groupedIds.get(subBranch.getType()), result, accessToken,
                                debugPrinter);
                    }
                }
                return;
            }
        }

        // Формируем запрос в БД
        Select select = new Select();
        PlainSelect plainSelect = new PlainSelect();
        select.setSelectBody(plainSelect);

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
                        plainSelect.setFromItem(from);
                        plainSelect.setWhere(makeWhere("id", sourceIds));
                    } else {
                        Join join = new Join();
                        join.setRightItem(from);
                        EqualsTo link = new EqualsTo();
                        link.setLeftExpression(new Column(new Table(null, "t" + (tableNum - 1)),
                                getSqlName(linkField)));
                        link.setRightExpression(new Column(new Table(null, "t" + tableNum), getSqlName("id")));
                        EqualsTo linkType = new EqualsTo();
                        linkType.setLeftExpression(new Column(new Table(null, "t" + (tableNum - 1)),
                                getSqlName(linkField) + "_type"));
                        linkType.setRightExpression(new Column(new Table(null, "t" + tableNum), getSqlName("id_type")));
                        join.setOnExpression(new AndExpression(link, linkType));
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
                    plainSelect.setFromItem(from);
                    plainSelect.setWhere(makeWhere(childrenElem.getParentLink(), sourceIds));
                } else {
                    Join join = new Join();
                    join.setRightItem(from);
                    EqualsTo link = new EqualsTo();
                    link.setLeftExpression(new Column(new Table(null, "t" + (tableNum - 1)), getSqlName(linkField)));
                    link.setRightExpression(new Column(new Table(null, "t" + tableNum),
                            getSqlName(childrenElem.getParentLink())));
                    EqualsTo linkType = new EqualsTo();
                    linkType.setLeftExpression(new Column(new Table(null, "t" + (tableNum - 1)),
                            getSqlName(linkField + "_type")));
                    linkType.setRightExpression(new Column(new Table(null, "t" + tableNum),
                            getSqlName(childrenElem.getParentLink() + "_type")));
                    join.setOnExpression(new AndExpression(link, linkType));
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

        plainSelect.setJoins(joins);
        FieldConfig fieldConfig = configurationExplorer.getFieldConfig(branch.getType(), linkField);
        List<String> columns = getColumnNames(Collections.singletonList(fieldConfig));
        ArrayList<SelectItem> fields = new ArrayList<>(columns.size());
        for (String column : columns) {
            SelectExpressionItem item = new SelectExpressionItem();
            item.setExpression(new Column(new Table(null, "t" + (tableNum - 1)), column));
            fields.add(item);
        }
        plainSelect.setSelectItems(fields);
        if (debugPrinter != null) {
            debugPrinter.print(var("step", step), var("exec sql", select));
        }

        List<Value> values = executeQuery(select.toString(), accessToken);
        if (debugPrinter != null) {
            debugPrinter.print(var("step", step), list("got from db", values));
        }

        Function[] functions = expr.getElements()[step - 1].getFunctions();
        if (functions != null) {
            for (Function function : functions) {
                DoelFunctionImplementation impl = doelFunctionRegistry.getFunctionImplementation(function.getName());
                if (log.isTraceEnabled()) {
                    log.trace("Processing function " + function + " [" + impl.getClass().getName() + "] on " + values);
                }
                values = impl.process(values, function.getArguments(), accessToken);
                if (debugPrinter != null) {
                    debugPrinter.print(var("step", step), var("func", function), list("replaced with", values));
                }
                if (values.size() == 0) {
                    return;
                }
            }
        }

        if (step < expr.getElements().length) {
            // Продолжаем обработку, если оказалось несколько ветвей
            List<DoelTypes.Link> nextTypes = branch.getNext();
            DoelExpression subExpr = expr.excludeCommonBeginning(expr.cutByCount(step));
            Map<String, List<RdbmsId>> groupedIds = groupByType(nextTypes, values, debugPrinter);
            for (DoelTypes.Link subBranch : nextTypes) {
                if (groupedIds.containsKey(subBranch.getType())) {
                    if (debugPrinter != null) {
                        debugPrinter.print(var("step", step),
                                str("evaluate subexpr " + subExpr + " for type" + subBranch.getType()),
                                list(groupedIds.get(subBranch.getType())));
                    }
                    evaluateBranch(subExpr, subBranch, groupedIds.get(subBranch.getType()), result, accessToken,
                            debugPrinter);
                }
            }
        } else {
            result.addAll((List<T>) values);
        }
    }

    @SuppressWarnings("rawtypes")
    private List<Value> executeQuery(String query, AccessToken accessToken) {
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, 0, 0, accessToken);
        if (collection == null || collection.size() == 0) {
            return Collections.emptyList();
        }
        List<Value> values = new ArrayList<>(collection.size());
        for (int i = 0; i < collection.size(); ++i) {
            values.add(collection.get(0, i));
        }
        return values;
    }

    private Map<String, List<RdbmsId>> groupByType(List<DoelTypes.Link> types, List<?> ids, DebugPrinter debugPrinter) {
        HashSet<String> typeSet = new HashSet<>();
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
                if (id == null) {
                    continue idCycle;
                }
            } else {
                throw new IllegalArgumentException("ids list must contain only Ids or ReferenceValues");
            }
            String idType = domainObjectTypeIdCache.getName(id);
            String[] hierarchyTypes = configurationExplorer.getDomainObjectTypesHierarchyBeginningFromType(idType);

            for (String type : hierarchyTypes) {
                if (typeSet.contains(type)) {
                    if (!result.containsKey(type)) {
                        result.put(type, new ArrayList<RdbmsId>());
                    }
                    result.get(type).add(id);
                    if (debugPrinter != null) {
                        debugPrinter.print(var("objId", id), var("type", idType), str("will be processed as " + type));
                    }
                    continue idCycle;
                }
            }

            if (log.isInfoEnabled()) {
                log.info("Unexpected object type: " + domainObjectTypeIdCache.getName(id)); //*****
            }
            if (debugPrinter != null) {
                debugPrinter.print(var("objId", id), var("type", idType), str("not valid; skipped"));
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

    private Expression makeWhere(String field, List<RdbmsId> ids) {
        Column column = new Column(new Table(null, "t0"), getSqlName(field));
        Column typeCol = new Column(new Table(null, "t0"), getSqlName(field + "_type"));
        Expression where = null;
        for (RdbmsId id : ids) {
            EqualsTo eqId = new EqualsTo();
            eqId.setLeftExpression(column);
            eqId.setRightExpression(new LongValue(String.valueOf(id.getId())));
            EqualsTo eqType = new EqualsTo();
            eqType.setLeftExpression(typeCol);
            eqType.setRightExpression(new LongValue(String.valueOf(id.getTypeId())));
            AndExpression eq = new AndExpression(eqId, eqType);
            if (where == null) {
                where = eq;
            } else {
                where = new OrExpression(new Parenthesis(where), new Parenthesis(eq));
            }
        }
        return where;
    }

    //@Deprecated
    public DoelExpression createReverseExpression(DoelExpression expr, String sourceType) {
        StringBuilder reverseExpr = new StringBuilder();
        String currentType = sourceType;
        for (DoelExpression.Element doelElem : expr.getElements()) {
            if (reverseExpr.length() > 0) {
                reverseExpr.insert(0, ".");
            }
            if (doelElem.getFunctions() != null && doelElem.getFunctions().length > 0 ) {
                //TODO add reversible funtionns processing
                throw new DoelException("Can't reverse expression that contains functions");
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
                    if (ReferenceFieldConfig.ANY_TYPE.equals(currentType)) {
                        throw new DoelException("Can't reverse expression that uses untyped reference fields (*)");
                    }
                } else {
                    throw new DoelException("Can't reverse expression that contains non-reference fields");
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

    //@Deprecated
    public DoelExpression createReverseExpression(DoelExpression expr, int count, String sourceType) {
        return createReverseExpression(expr.cutByCount(count), sourceType);
    }
/*
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
*/
    private interface Printable {
        void print(StringBuilder out);
    }

    private boolean needToDebugExpression(DoelExpression expr) {
        return debuggingExpressions != null && debuggingExpressions.contains(expr);
    }

    private class DebugPrinter {
        String prologue;

        DebugPrinter(DoelExpression expression, Id sourceObjectId) {
            prologue = "[doel.debug] expr=" + expression + "; srcId=" + sourceObjectId;
        }

        void print(Printable... items) {
            StringBuilder sb = new StringBuilder(prologue);
            for (Printable item : items) {
                item.print(sb);
            }
            log.warn(sb.toString());
        }
    }

    private static Printable str(final String str) {
        return new Printable() {
            @Override
            public void print(StringBuilder out) {
                out.append(": ").append(str);
            }
        };
    }

    private static Printable var(final String name, final Object var) {
        return new Printable() {
            @Override
            public void print(StringBuilder out) {
                out.append("; ").append(name).append("=").append(var);
            }
        };
    }
/*
    private static <T> Printable arr(final String name, final T[] array) {
        return new Printable() {
            @Override
            public void print(StringBuilder out) {
                out.append("; ").append(name).append(": ").append(array.length).append(" items");
                for (T item : array) {
                    out.append("\n\t\t\t").append(item);
                }
            }
        };
    }

    private static <T> Printable arr(final T[] array) {
        return new Printable() {
            @Override
            public void print(StringBuilder out) {
                out.append(array.length).append(" items");
                for (T item : array) {
                    out.append("\n\t\t\t").append(item);
                }
            }
        };
    }
*/
    private static <T> Printable list(final String name, final List<T> array) {
        return new Printable() {
            @Override
            public void print(StringBuilder out) {
                out.append("; ").append(name).append(": ").append(array.size()).append(" items");
                for (T item : array) {
                    out.append("\n\t\t\t").append(item);
                }
            }
        };
    }

    private static <T> Printable list(final List<T> array) {
        return new Printable() {
            @Override
            public void print(StringBuilder out) {
                out.append(array.size()).append(" items");
                for (T item : array) {
                    out.append("\n\t\t\t").append(item);
                }
            }
        };
    }
}
