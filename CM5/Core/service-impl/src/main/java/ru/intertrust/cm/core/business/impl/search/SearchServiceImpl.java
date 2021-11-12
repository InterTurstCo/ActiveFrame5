package ru.intertrust.cm.core.business.impl.search;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ru.intertrust.cm.core.business.api.SearchService;
import ru.intertrust.cm.core.business.api.dto.CombiningFilter;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.GenericIdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SearchFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.config.search.ContentFieldConfig;
import ru.intertrust.cm.core.config.search.HighlightingConfig;
import ru.intertrust.cm.core.config.search.HighlightingRawParam;
import ru.intertrust.cm.core.config.search.IndexedContentConfig;
import ru.intertrust.cm.core.config.search.IndexedDomainObjectConfig;
import ru.intertrust.cm.core.config.search.IndexedFieldConfig;
import ru.intertrust.cm.core.config.search.LinkedDomainObjectConfig;
import ru.intertrust.cm.core.config.search.SearchAreaConfig;
import ru.intertrust.cm.core.model.RemoteSuitableException;
import ru.intertrust.cm.core.model.SearchException;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

@Stateless(name = "SearchService")
@Local(SearchService.class)
@Remote(SearchService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class SearchServiceImpl implements SearchService, SearchService.Remote {

    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);

    @Value("${search.results.limit:5000}")
    private int RESULTS_LIMIT;

    @Autowired
    private SolrServerWrapperMap solrServerWrapperMap;

    @Autowired
    private ImplementorFactory<SearchFilter, FilterAdapter<? extends SearchFilter>> searchFilterImplementorFactory;

    @Autowired
    private SearchConfigHelper configHelper;

    @Autowired
    private CollectionRetrieverFactory collectionRetrieverFactory;

    @Autowired
    private SolrSearchConfiguration solrSearchConfiguration;

    @Override
    public IdentifiableObjectCollection search(String query, String areaName, String targetCollectionName,
                                               int maxResults) {
        try {
            if (maxResults <= 0 || maxResults > RESULTS_LIMIT) {
                maxResults = RESULTS_LIMIT;
            }
            StringBuilder queryString = new StringBuilder();
            for (String field : listCommonSolrFields()) {
                queryString.append(queryString.length() == 0 ? "" : " OR ")
                        .append(field)
                        .append(":(")
                        .append(SolrUtils.protectSearchString(query))
                        .append(")");
            }
            SolrQuery solrQuery = new SolrQuery()
                    .setQuery(queryString.toString())
                    .addFilterQuery(SolrFields.AREA + ":\"" + areaName + "\"")
                    //.addFilterQuery(SolrFields.TARGET_TYPE + ":" + configHelper.getTargetObjectType(targetCollectionName))
                    .addField(SolrFields.MAIN_OBJECT_ID)
                    .addField(SolrUtils.SCORE_FIELD);
            if (solrQuery.getSorts().isEmpty()) {
                solrQuery.addSort(SolrUtils.SCORE_FIELD, SolrQuery.ORDER.desc)
                        .addSort(SolrFields.MAIN_OBJECT_ID, SolrQuery.ORDER.asc);
            }
            if (maxResults > 0) {
                solrQuery.setRows(maxResults);
            }

            int fetchLimit = maxResults;
            while (true) {
                QueryResponse response = executeSolrQuery(solrQuery, SolrServerWrapper.REGULAR);
                if (fetchLimit <= 0) {
                    fetchLimit = response.getResults().size();
                }
                IdentifiableObjectCollection result = collectionRetrieverFactory.newNamedCollectionRetriever(targetCollectionName)
                        .queryCollection(response.getResults(), maxResults);
                if (response.getResults().size() == fetchLimit && result.size() < maxResults) {
                    // Увеличиваем размер выборки в Solr
                    fetchLimit = estimateFetchLimit(response.getResults().size(), result.size());
                    solrQuery.setRows(fetchLimit);
                    continue;
                }
                return result;
            }
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public IdentifiableObjectCollection search(SearchQuery query, String targetCollectionName, int maxResults) {
        return commonSearch(query, targetCollectionName, maxResults, false);
    }

    @Override
    public IdentifiableObjectCollection search(SearchQuery query, String targetCollectionName,
                                               List<? extends Filter> collectionFilters, int maxResults) {
        NamedCollectionRetriever cr = collectionRetrieverFactory
                .newNamedCollectionRetriever(targetCollectionName, collectionFilters);
        return complexSearch(query, cr, maxResults);
    }

    @Override
    public IdentifiableObjectCollection searchAndQuery(SearchQuery searchQuery, String sqlQuery, int maxResults) {
        QueryCollectionRetriever collectionRetriever = collectionRetrieverFactory.newQueryCollectionRetriever(sqlQuery);
        return complexSearch(searchQuery, collectionRetriever, maxResults);
    }

    @Override
    public IdentifiableObjectCollection searchAndQuery(SearchQuery searchQuery, String sqlQuery,
                                                       List<? extends ru.intertrust.cm.core.business.api.dto.Value<?>> sqlParams, int maxResults) {
        QueryCollectionRetriever collectionRetriever = collectionRetrieverFactory.newQueryCollectionRetriever(sqlQuery, sqlParams);
        return complexSearch(searchQuery, collectionRetriever, maxResults);
    }

    @Override
    public IdentifiableObjectCollection searchCntx(SearchQuery query, String targetCollectionName, int maxResults) {
        return commonSearch(query, targetCollectionName, maxResults, true);
    }

    private IdentifiableObjectCollection commonSearch(SearchQuery query, String targetCollectionName,
                                                      int maxResults, boolean onlyContextSearch) {
        Map<String, SearchQuery> queries = splitQueryBySolrServer(query);
        List<IdentifiableObjectCollection> collections = new ArrayList<>();
        for (Map.Entry<String, SearchQuery> entry : queries.entrySet()) {
            if (SolrServerWrapper.REGULAR.equals(entry.getKey())) {
                if (onlyContextSearch) {
                    continue;
                }
                NamedCollectionRetriever collectionRetriever = collectionRetrieverFactory.newNamedCollectionRetriever(targetCollectionName);
                collections.add(complexSearch(entry.getValue(), collectionRetriever, maxResults));
            } else {
                List<String> areas = entry.getValue().getAreas();
                if (areas.size() == 1) {
                    collections.add(contextSearch(areas.get(0), entry.getKey(), entry.getValue(), targetCollectionName, maxResults));
                } else {
                    for (String area : areas) {
                        SearchQuery q = new SearchQuery(entry.getValue());
                        q.clearAreas();
                        q.addArea(area);
                        collections.add(contextSearch(area, entry.getKey(), q, targetCollectionName, maxResults));
                    }
                }
            }
        }
        return collections.size() == 1 ? collections.get(0) : mergeCollections(collections, maxResults);
    }

    private IdentifiableObjectCollection contextSearch(String area, String solrServerKey,
                                                       SearchQuery query, String targetCollectionName, int maxResults) {
        Set<String> solrFieldNames = getSolrFieldsMetaData(area, solrServerKey,
                query.getTargetObjectTypes(), new SolrNameExtractor());
        Set<String> hlFieldNames = getSolrFieldsMetaData(area, solrServerKey,
                query.getTargetObjectTypes(), new HighlightingNameExtractor());
        Set<TargetResultField> fieldsCollection = getSolrFieldsMetaData(area, solrServerKey,
                query.getTargetObjectTypes(), new ResultFieldsExtractor());
        SearchAreaConfig searchAreaConfig = configHelper.getSearchAreaDetailsConfig(area);
        String targetFilterName = searchAreaConfig != null ? searchAreaConfig.getTargetFilterName() : null;

        CntxCollectionRetriever collectionRetriever = collectionRetrieverFactory
                .newCntxCollectionRetriever(targetCollectionName, targetFilterName, fieldsCollection);
        return contextSearch(query, collectionRetriever,
                solrServerKey, solrFieldNames, hlFieldNames, maxResults);
    }

    private IdentifiableObjectCollection mergeCollections(List<IdentifiableObjectCollection> collections, int maxResults) {
        IdentifiableObjectCollection collection = new GenericIdentifiableObjectCollection();
        // TODO установить список полей
        if (collections != null) {
            for (IdentifiableObjectCollection collectionPart : collections) {
                if (collectionPart != null) {
                    collection.append(collectionPart);
                }
            }
        }
        // сортировка по редевантности
        CollectionRetriever.sortByRelevance(collection);
        CollectionRetriever.truncCollection(collection, maxResults);
        return collection;
    }

    private IdentifiableObjectCollection complexSearch(SearchQuery query, CollectionRetriever collectionRetriever,
                                                       int maxResults) {
        try {
            if (maxResults <= 0 || maxResults > RESULTS_LIMIT) {
                maxResults = RESULTS_LIMIT;
            }

            // Анализ запроса и разделение фильтров по типам объектов
            ComplexQuery solrMultiQuery = new ComplexQuery();
            solrMultiQuery.addFilters(query.getFilters(), query);

            // Выполнение запросов в Solr и формирование коллекции найденных документов
            int fetchLimit = maxResults;
            while (true) {
                SolrDocumentList found = solrMultiQuery.execute(fetchLimit, query);
                if (fetchLimit <= 0) {
                    fetchLimit = found.size();
                }
                IdentifiableObjectCollection result = collectionRetriever.queryCollection(found, maxResults);
                if (found.size() >= fetchLimit && result.size() < maxResults) {
                    // Увеличиваем размер выборки в Solr
                    fetchLimit = estimateFetchLimit(found.size(), result.size());
                    continue;
                }
                return result;
            }
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    private IdentifiableObjectCollection contextSearch(SearchQuery searchQuery,
                                                       CollectionRetriever collectionRetriever,
                                                       String solrServerKey,
                                                       Set<String> solrFields,
                                                       Set<String> highlightingFields,
                                                       int maxResults) {
        try {
            if (maxResults <= 0 || maxResults > RESULTS_LIMIT) {
                maxResults = RESULTS_LIMIT;
            }

            CntxQuery solrCntxQuery = new CntxQuery();
            solrCntxQuery.addFilters(searchQuery.getFilters(), searchQuery);

            int fetchLimit = maxResults;
            do {
                QueryResponse found = solrCntxQuery.execute(searchQuery, solrServerKey, solrFields, highlightingFields, fetchLimit);
                if (found == null || found.getResults() == null) {
                    return new GenericIdentifiableObjectCollection();
                }
                if (fetchLimit <= 0) {
                    fetchLimit = found.getResults().size();
                }

                IdentifiableObjectCollection result = collectionRetriever.queryCollection(
                        found.getResults(), found.getHighlighting(), maxResults);
                if (found.getResults().size() >= fetchLimit && result.size() < maxResults) {
                    fetchLimit = estimateFetchLimit(found.getResults().size(), result.size());
                    continue;
                }
                return result;
            } while (true);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    private Map<String, SearchQuery> splitQueryBySolrServer(SearchQuery query) {
        //CMSEVEN-11017 (CONRSHB-3351)
        if (configHelper == null) {
            RuntimeException e = new RuntimeException("@Autowired private SearchConfigHelper configHelper == null!");
            log.error("SearchServiceImpl error.", e);
            throw e;
        }
        //CMSEVEN-11017 (CONRSHB-3351)
        Map<String, SearchQuery> queryMap = new HashMap<>();
        // разбиваем запрос на несколько - обычный и контекстные по областям поиска
        List<String> areas = query.getAreas();
        if (areas == null || areas.isEmpty()) {
            queryMap.put(SolrServerWrapper.REGULAR, query);
        } else if (areas.size() == 1) {
            final String key = getSolrKeyByArea(areas.get(0));
            queryMap.put(key, query);
        } else {
            // TODO подумать нужно ли делить еще и по типам объектов
            for (final String area : areas) {
                final String key = getSolrKeyByArea(area);
                SearchQuery newQuery = queryMap.get(key);
                if (newQuery == null) {
                    newQuery = new SearchQuery(query);
                    newQuery.clearAreas();
                    queryMap.put(key, newQuery);
                }
                newQuery.addArea(area);
            }
        }
        return queryMap;
    }

    //CMSEVEN-11017 (CONRSHB-3351)
    private String getSolrKeyByArea(String area) {
        log.debug("getSolrKeyByArea:area=" + (area != null ? area : "null"));
        SearchAreaConfig config = configHelper.getSearchAreaDetailsConfig(area);
        if (config == null) {
            RuntimeException e = new RuntimeException("configHelper.getSearchAreaDetailsConfig("
                    + (area == null ? "null" : ("\"" + area + "\"")) +") returned null.");
            log.error("SearchServiceImpl error.", e);
            throw e;
        }
        String key = config.getSolrServerKey();
        key = solrServerWrapperMap.isCntxSolrServer(key) ? key : SolrServerWrapper.REGULAR;
        return key;
    }
    //CMSEVEN-11017 (CONRSHB-3351)

    interface QueryProcessor {
        QueryProcessor newNestedQuery();

        void setCombineOperation(CombiningFilter.Op operation);

        void setNegativeResult(boolean negativeResault);

        void addFilters(Collection<SearchFilter> filters, SearchQuery query);
    }

    class ComplexQuery implements QueryProcessor {
        protected HashMap<String, StringBuilder> filterStrings = new HashMap<>();
        protected ArrayList<String> multiTypeFilterStrings = new ArrayList<>();
        protected ArrayList<ComplexQuery> nestedQueries = new ArrayList<>();
        protected CombiningFilter.Op combineOperation = CombiningFilter.AND;
        protected boolean negateResult = false;
        private final String solrServerKey = SolrServerWrapper.REGULAR;

        protected HashMap<String, SolrDocumentList> foundCache = new HashMap<>();

        @Override
        public ComplexQuery newNestedQuery() {
            ComplexQuery nestedQuery = new ComplexQuery();
            nestedQueries.add(nestedQuery);
            return nestedQuery;
        }

        @Override
        public void setCombineOperation(CombiningFilter.Op operation) {
            this.combineOperation = operation;
        }

        public void setNegativeResult(boolean negativeResult) {
            this.negateResult = negativeResult;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public void addFilters(Collection<SearchFilter> filters, SearchQuery query) {
            for (SearchFilter filter : filters) {
                FilterAdapter adapter = searchFilterImplementorFactory.createImplementorFor(filter.getClass());

                if (adapter.isCompositeFilter(filter)) {
                    ((CompositeFilterAdapter) adapter).processCompositeFilter(filter, this, query);
                    continue;
                }

                String filterValue = adapter.getFilterString(filter, query);
                if (filterValue == null || filterValue.isEmpty()) {
                    continue;
                }

                Collection<String> types = configHelper.findApplicableTypes(filter.getFieldName(), query.getAreas(),
                        query.getTargetObjectTypes());
                if (types.size() == 0) {
                    logger.info("Field " + filter.getFieldName() + " is not indexed; excluded from search");
                }

                if (types.size() > 1) {
                    addFilterValue(SearchConfigHelper.ALL_TYPES, filterValue);
                } else if (types.size() == 1) {
                    addFilterValue((String) types.toArray()[0], filterValue);
                }
            }
        }

        private void addFilterValue(String type, String filterValue) {
            if (SearchConfigHelper.ALL_TYPES.equals(type)) {
                multiTypeFilterStrings.add(filterValue);
                return;
            }
            StringBuilder filterString;
            if (!filterStrings.containsKey(type)) {
                filterString = new StringBuilder();
                filterStrings.put(type, filterString);
            } else {
                filterString = filterStrings.get(type);
                filterString.append(combineOperation == CombiningFilter.AND ? " AND " : " OR ");
            }
            filterString.append(filterValue);
        }

        public SolrDocumentList execute(int fetchLimit, SearchQuery query) {
            if (negateResult) {
                for (StringBuilder str : filterStrings.values()) {
                    str.insert(0, "-(").append(")");
                }
            }

            StringBuilder areas = new StringBuilder();
            for (String areaName : query.getAreas()) {
                areas.append(areas.length() == 0 ? "(" : " OR ")
                        .append("\"")
                        .append(areaName)
                        .append("\"");
            }
            areas.append(")");

            StringBuilder targetTypes = new StringBuilder();
            for (String targetObjectType : query.getTargetObjectTypes()) {
                targetTypes.append(targetTypes.length() == 0 ? "(" : " OR ")
                        .append("\"")
                        .append(targetObjectType)
                        .append("\"");
            }
            targetTypes.append(")");

            SolrDocumentList result;
            float clippingFactor = 1f;
            boolean clipped;
            do {
                clipped = false;
                int rows = Math.round(fetchLimit / clippingFactor);
                logger.trace("Attempting to get {} rows", rows);

                ArrayList<SolrDocumentList> foundParts = new ArrayList<>(filterStrings.size());
                for (Map.Entry<String, StringBuilder> entry : filterStrings.entrySet()) {
                    logger.trace("Prepare Solr Query, query {}, objectType {}", entry.getKey(), entry.getValue());
                    SolrDocumentList cached = foundCache.get(entry.getKey());
                    if (cached != null && (cached.size() >= cached.getNumFound() || cached.size() >= rows)) {
                        foundParts.add(cached);
                        logger.trace("Result found in cache, skipping...");
                        continue;
                    }

                    SolrQuery solrQuery = new SolrQuery()
                            .setQuery(entry.getValue().toString())
                            .addFilterQuery(SolrFields.AREA + ":" + areas)
                            .addFilterQuery(SolrFields.TARGET_TYPE + ":" + targetTypes)
                            .addFilterQuery(SolrFields.OBJECT_TYPE + ":\"" + entry.getKey() + "\"")
                            .addField(SolrFields.MAIN_OBJECT_ID)
                            .addField(SolrUtils.SCORE_FIELD);
                    /*if (!SearchConfigHelper.ALL_TYPES.equals(entry.getKey())) {
                        solrQuery.addFilterQuery(SolrFields.OBJECT_TYPE + ":\"" + entry.getKey() + "\"");
                    }*/
                    if (solrQuery.getSorts().isEmpty()) {
                        solrQuery.addSort(SolrUtils.SCORE_FIELD, SolrQuery.ORDER.desc)
                                .addSort(SolrFields.MAIN_OBJECT_ID, SolrQuery.ORDER.asc);
                    }
                    if (rows > 0) {
                        solrQuery.setRows(rows);
                    }
                    QueryResponse response = executeSolrQuery(solrQuery, solrServerKey);
                    if (response.getResults() != null) {
                        foundParts.add(response.getResults());
                        foundCache.put(entry.getKey(), response.getResults());
                        clipped = clipped || rows > 0 && response.getResults().size() == rows;
                    }
                }
                for (String filterString : multiTypeFilterStrings) {
                    logger.trace("Prepare multiType query. Filter {}", filterString);
                    SolrDocumentList cached = foundCache.get(":" + filterString);
                    if (cached != null && (cached.size() >= cached.getNumFound() || cached.size() >= rows)) {
                        foundParts.add(cached);
                        logger.trace("Result found in cache, skipping...");
                        continue;
                    }

                    SolrQuery solrQuery = new SolrQuery()
                            .setQuery(filterString)
                            .addFilterQuery(SolrFields.AREA + ":" + areas)
                            .addFilterQuery(SolrFields.TARGET_TYPE + ":" + targetTypes)
                            .addField(SolrFields.MAIN_OBJECT_ID)
                            .addField(SolrUtils.SCORE_FIELD);
                    if (solrQuery.getSorts().isEmpty()) {
                        solrQuery.addSort(SolrUtils.SCORE_FIELD, SolrQuery.ORDER.desc)
                                .addSort(SolrFields.MAIN_OBJECT_ID, SolrQuery.ORDER.asc);
                    }
                    if (rows > 0) {
                        solrQuery.setRows(rows);
                    }
                    QueryResponse response = executeSolrQuery(solrQuery, solrServerKey);
                    if (response.getResults() != null) {
                        foundParts.add(response.getResults());
                        foundCache.put(":" + filterString, response.getResults());
                        clipped = clipped || rows > 0 && response.getResults().size() == rows;
                    }
                }
                for (ComplexQuery nested : nestedQueries) {
                    logger.trace("Nested query will be executed...");
                    SolrDocumentList part = nested.execute(rows, query);
                    foundParts.add(part);
                    clipped = clipped || rows > 0 && part.size() >= rows;
                }
                if (foundParts.size() == 1) {
                    result = foundParts.get(0);
                    logger.trace("Compact single result...");
                    compactList(result);
                } else {
                    // TODO special processing for negateResult==true needed
                    logger.trace("Attempt to combine results...");
                    result = combineResults(foundParts, combineOperation == CombiningFilter.AND
                            ? new IntersectCombiner(foundParts.size()) : new UnionCombiner(), fetchLimit);
                }
                float prev = clippingFactor;
                clippingFactor *= Math.max(1f, 0.9f * result.size()) / rows;
                if (clippingFactor == prev) {
                    // По фату, надо покрыть случай, когда у нас не найдено ни 1 записи, а пользователь ищет 1 строку,
                    // тогда поток зависнет, потому что всегда clippingFactor будет = 1.0, а значит мы всегда будем продолжать искать
                    // по одной строке. Но на вякий случай, я добавлю условие на одинаковость
                    logger.trace("Previous clippingFactor is equals to new one. Divide by 100... Previous value was {}", clippingFactor);
                    clippingFactor /= 100;
                }
                logger.trace("New clippingFactor is {}", clippingFactor);

            } while (clipped && result.size() < fetchLimit);
            return result;
        }
    }

    // =================================================================================================================
    class CntxQuery implements QueryProcessor {
        private final static String HL_usePhraseHighlighter = "hl.usePhraseHighlighter";
        private final static String HL_highlightMultiTerm = "hl.highlightMultiTerm";
        private final static String HL_simple_pre = "hl.simple.pre";
        private final static String HL_simple_post = "hl.simple.post";
        private final static String HL_snippets = "hl.snippets";
        private final static String HL_fragsize = "hl.fragsize";

        CombiningFilter.Op combineOperation = CombiningFilter.AND;
        private String queryString = "";
        private final Set<String> objectTypeSet = new HashSet<>();

        CntxQuery() {
        }

        @Override
        public CntxQuery newNestedQuery() {
            throw new RuntimeException("not implemented");
        }

        @Override
        public void setCombineOperation(CombiningFilter.Op operation) {
            this.combineOperation = operation;
        }

        @Override
        public void setNegativeResult(boolean negativeResult) {
            throw new RuntimeException("not implemented");
        }

        @Override
        public void addFilters(Collection<SearchFilter> filters, SearchQuery query) {
            String queryString = composeQueryString(filters, query, CombiningFilter.Op.OR);
            String operation = this.combineOperation == CombiningFilter.AND ? " AND " : " OR ";
            this.queryString += (!this.queryString.isEmpty() ? operation : "") + queryString;
            // добавляем условие на поиск только вложений, либо не добавляем вовсе
            switch (getCntxMode(query)) {
                case ATTACHMENTS:
                    this.queryString += (!this.queryString.isEmpty() ? " AND " : "") + SolrUtils.ATTACH_FLAG_FIELD + ":(true)";
                    break;
                default:
                    break;
            }
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private String composeQueryString(Collection<SearchFilter> filters, SearchQuery query, CombiningFilter.Op combineOperation) {
            StringBuilder queryString = new StringBuilder();
            String operation = combineOperation == CombiningFilter.AND ? " AND " : " OR ";
            for (SearchFilter filter : filters) {
                FilterAdapter adapter = searchFilterImplementorFactory.createImplementorFor(filter.getClass());

                String filterValue;
                if (adapter.isCompositeFilter(filter) && filter instanceof CombiningFilter) {
                    filterValue = composeQueryString(((CombiningFilter) filter).getFilters(), query, ((CombiningFilter) filter).getOperation());
                    queryString.append((queryString.length() > 0) ? operation : "").append(filterValue);
                    continue;
                }

                filterValue = adapter.getFilterString(filter, query);
                if (filterValue == null || filterValue.isEmpty()) {
                    continue;
                }

                Collection<String> types = configHelper.findApplicableTypes(filter.getFieldName(),
                        query.getAreas(), query.getTargetObjectTypes());
                if (types.size() == 0) {
                    logger.info("Field " + filter.getFieldName() + " is not indexed; excluded from search");
                } else {
                    queryString.append((queryString.length() > 0) ? operation : "").append(filterValue);
                    if (types.size() == 1) {
                        objectTypeSet.add((String) types.toArray()[0]);
                    }
                }
            }
            return filters.size() > 1 ? ("(" + queryString + ")") : queryString.toString();
        }

        public QueryResponse execute(SearchQuery query,
                                     String solrServerKey,
                                     Set<String> solrFields,
                                     Set<String> highlightingFields,
                                     int fetchLimit) {
            StringBuilder areas = composeAreaFilterValue(query);
            StringBuilder targetTypes = composeTargetTypeFilterValue(query);
            StringBuilder objectTypes = composeObjectTypeFilterValue(objectTypeSet);

            float clippingFactor = 1f;
            boolean clipped;
            QueryResponse response;
            do {
                clipped = false;
                int rows = Math.round(fetchLimit / clippingFactor);
                SolrQuery solrQuery = new SolrQuery()
                        .setQuery(queryString)
                        .addField(SolrUtils.ID_FIELD)
                        .addField(SolrFields.OBJECT_ID)
                        .addField(SolrFields.MAIN_OBJECT_ID)
                        .addField(SolrUtils.SCORE_FIELD)
                        .addField(SolrUtils.ATTACH_FLAG_FIELD);
                if (areas.length() > 0) {
                    solrQuery.addFilterQuery(SolrFields.AREA + ":" + areas);
                }
                if (targetTypes.length() > 0) {
                    solrQuery.addFilterQuery(SolrFields.TARGET_TYPE + ":" + targetTypes);
                }
                if (objectTypes.length() > 0) {
                    solrQuery.addFilterQuery(SolrFields.OBJECT_TYPE + ":" + objectTypes);
                }
                // TODO подумать, нужно ли дополнить всеми полями из конфигурации области поиска и типа объекта.
                // Или все данные для результирующей коллекции лучше получить запросом из базы с фильтрацией по набору id,
                // который вернул solr. Запрос все равно нужно сделать, чтобы выполнить фильтрацию по ACL
                if (solrFields != null && !solrFields.isEmpty()) {
                    // добавляем в поисковый запрос бизнес поля
                    for (String solrField : solrFields) {
                        solrQuery.addField(solrField);
                    }
                }

                if (solrQuery.getSorts().isEmpty()) {
                    solrQuery.addSort(SolrUtils.SCORE_FIELD, SolrQuery.ORDER.desc)
                            .addSort(SolrFields.OBJECT_ID, SolrQuery.ORDER.asc)
                            .addSort(SolrFields.MAIN_OBJECT_ID, SolrQuery.ORDER.asc);
                }

                addHighlightingToQuery(!query.getAreas().isEmpty() ? query.getAreas().get(0) : null,
                        solrQuery, highlightingFields);

                if (rows > 0) {
                    solrQuery.setRows(rows);
                }
                response = executeSolrQuery(solrQuery, solrServerKey);
                if (response.getResults() != null) {
                    clipped = rows > 0 && response.getResults().size() == rows;
                    compactQueryResults(response, getCntxMode(query));
                    clippingFactor *= Math.max(1f, 0.9f * response.getResults().size()) / rows;
                }
            } while (clipped && response.getResults().size() < fetchLimit);
            return response;
        }

        private StringBuilder composeAreaFilterValue(SearchQuery query) {
            StringBuilder areas = new StringBuilder();
            if (!query.getAreas().isEmpty()) {
                for (String areaName : query.getAreas()) {
                    areas.append(areas.length() == 0 ? "(" : " OR ")
                            .append("\"")
                            .append(areaName)
                            .append("\"");
                }
                areas.append(")");
            }
            return areas;
        }

        private StringBuilder composeTargetTypeFilterValue(SearchQuery query) {
            StringBuilder targetTypes = new StringBuilder();
            if (!query.getTargetObjectTypes().isEmpty()) {
                for (String targetObjectType : query.getTargetObjectTypes()) {
                    targetTypes.append(targetTypes.length() == 0 ? "(" : " OR ")
                            .append("\"")
                            .append(targetObjectType)
                            .append("\"");
                }
                targetTypes.append(")");
            }
            return targetTypes;
        }

        private StringBuilder composeObjectTypeFilterValue(Set<String> objectTypeSet) {
            StringBuilder objectTypes = new StringBuilder();
            if (!objectTypeSet.isEmpty()) {
                for (String type : objectTypeSet) {
                    objectTypes.append(objectTypes.length() == 0 ? "(" : " OR ")
                            .append("\"")
                            .append(type)
                            .append("\"");
                }
                objectTypes.append(")");
            }
            return objectTypes;
        }

        private void addHighlightingToQuery(String area, SolrQuery solrQuery, Set<String> highlightingFields) {
            if (area != null && solrQuery != null && highlightingFields != null && !highlightingFields.isEmpty()) {
                SearchAreaConfig searchAreaConfig = configHelper.getSearchAreaDetailsConfig(area);
                HighlightingConfig highlightingConfig = searchAreaConfig != null ? searchAreaConfig.getHighlightingConfig() : null;
                if (highlightingConfig == null) {
                    highlightingConfig = new HighlightingConfig(true);
                }
                if (highlightingConfig.getEnabled()) {
                    solrQuery.setHighlight(highlightingConfig.getEnabled())
                            .setHighlightRequireFieldMatch(highlightingConfig.getHighlightRequireMatch())
                            .set(HL_usePhraseHighlighter, highlightingConfig.getHighlightPhrase())
                            .set(HL_highlightMultiTerm, highlightingConfig.getHighlightMultiTerm())
                            .set(HL_simple_pre, highlightingConfig.getPreTag() != null ? highlightingConfig.getPreTag() : "")
                            .set(HL_simple_post, highlightingConfig.getPostTag() != null ? highlightingConfig.getPostTag() : "")
                            .set(HL_snippets, highlightingConfig.getSnippetCount() != null ? highlightingConfig.getSnippetCount() : 5)
                            .set(HL_fragsize, highlightingConfig.getFragmentSize() != null ? highlightingConfig.getFragmentSize() : 50);
                    if (highlightingConfig.getRawParams() != null) {
                        for (HighlightingRawParam param : highlightingConfig.getRawParams()) {
                            solrQuery.set(param.getName(), param.getValue());
                        }
                    }
                    for (String hlField : highlightingFields) {
                        solrQuery.addHighlightField(hlField);
                    }
                }
            }
        }

        private void compactQueryResults(QueryResponse response, SearchQuery.CntxMode cntxMode) {
            if (response == null || response.getResults() == null) {
                return;
            }
            HashSet<String> ids = new HashSet<>(response.getResults().size());
            // выбираем самые релевантные записи
            // ATTACHMENTS - для одного документа самые релевантные вложения
            // DOCUMENTS - документ
            // ALL - документ или вложение
            for (Iterator<SolrDocument> itr = response.getResults().iterator(); itr.hasNext(); ) {
                SolrDocument doc = itr.next();
                Boolean isAttach = (Boolean) doc.getFieldValue(SolrUtils.ATTACH_FLAG_FIELD);
                String mainId = (String) doc.getFieldValue(SolrFields.MAIN_OBJECT_ID);
                String id = (String) doc.getFieldValue(SolrUtils.ID_FIELD);
                boolean bRemove = false;
                switch (cntxMode) {
                    case ATTACHMENTS:
                        bRemove = (isAttach != null && !(isAttach.booleanValue())) || ids.contains(mainId);
                        break;
                    case ALL:
                        bRemove = ids.contains(mainId);
                        break;
                }
                if (bRemove) {
                    itr.remove();
                    if (response.getHighlighting() != null) {
                        response.getHighlighting().remove(id);
                    }
                } else {
                    ids.add(mainId);
                }
            }
        }
    }

    private SearchQuery.CntxMode getCntxMode(SearchQuery searchQuery) {
        return SolrUtils.CNTX_MODE_SMART.equalsIgnoreCase(solrSearchConfiguration.getSolrCntxMode()) ?
                searchQuery.getCntxMode() : SearchQuery.CntxMode.ALL;
    }

    // =================================================================================================================

    public <T> Set<T> getSolrFieldsMetaData(String area, String solrServerKey,
                                            List<String> targetObjectTypes, ConfigDataExtractor<T> extractor) {
        Set<T> solrFieldsMetaData = new LinkedHashSet<>();
        try {
            if (extractor != null && targetObjectTypes != null) {
                for (String type : targetObjectTypes) {
                    List<SearchConfigHelper.SearchAreaDetailsConfig> searchAreaDetailsConfigs = configHelper.findEffectiveConfigs(type);
                    if (searchAreaDetailsConfigs == null || searchAreaDetailsConfigs.isEmpty()) {
                        continue;
                    }
                    for (SearchConfigHelper.SearchAreaDetailsConfig searchAreaDetailsConfig : searchAreaDetailsConfigs) {
                        String solrKey = searchAreaDetailsConfig.getSolrServerKey();
                        String searchAreaName = searchAreaDetailsConfig.getAreaName();
                        if (!solrServerKey.equalsIgnoreCase(solrKey) || !searchAreaName.equalsIgnoreCase(area)) {
                            continue;
                        }
                        // получаем список полей
                        IndexedDomainObjectConfig objectConfig = searchAreaDetailsConfig.getObjectConfig();
                        Collection<T> extractedValues = extractor.getExtractedValues(type, objectConfig);
                        solrFieldsMetaData.addAll(extractedValues);
                    }
                }
            }
        } catch (Exception e) {
            throw RemoteSuitableException.convert(e);
        }
        return solrFieldsMetaData;
    }

    private Collection<SearchFieldType> calculateFieldType(String objectTypeName, IndexedFieldConfig config) {
        return configHelper.getFieldTypes(config, objectTypeName);
    }

    private class ResultFieldsExtractor implements ConfigDataExtractor<TargetResultField> {
        @Override
        public Collection<TargetResultField> getExtractedValues(String mainType, IndexedDomainObjectConfig objectConfig) {
            Map<String, TargetResultField> solrFields = new HashMap<>();
            List<IndexedFieldConfig> indexedFileldConfigs = objectConfig != null ?
                    objectConfig.getFields() : new ArrayList<>(0);
            List<LinkedDomainObjectConfig> linkedFileldConfigs = objectConfig != null ?
                    objectConfig.getLinkedObjects() : new ArrayList<>(0);
            List<IndexedContentConfig> indexedContentConfigs = objectConfig != null ?
                    objectConfig.getContentObjects() : new ArrayList<>(0);

            collectFieldConfigData(solrFields, mainType, indexedFileldConfigs);
            for (LinkedDomainObjectConfig linkedFileldConfig : linkedFileldConfigs) {
                String linkedType = linkedFileldConfig.getType();
                collectFieldConfigData(solrFields, linkedType, linkedFileldConfig.getFields());
            }

            for (IndexedContentConfig indexedContentConfig : indexedContentConfigs) {
                for (ContentFieldConfig contentFieldConfig : indexedContentConfig.getFields()) {
                    if (contentFieldConfig.getShowInResults()) {
                        String targetName = contentFieldConfig.getTargetFieldName();
                        switch (contentFieldConfig.getType()) {
                            case NAME:
                            case PATH:
                            case MIMETYPE:
                            case DESCRIPTION:
                                addFieldMetaData(solrFields, new TextSearchFieldType(
                                                configHelper.getSupportedLanguages()),
                                        contentFieldConfig.getType().getSolrFieldName(),
                                        contentFieldConfig.getTypeString(), targetName, false);
                                break;
                            case HIGHLIGHTING:
                                addFieldMetaData(solrFields, new SpecialTextSearchFieldType(configHelper.getSupportedLanguages()),
                                        contentFieldConfig.getType().getSolrFieldName(),
                                        SearchFilter.CONTENT, targetName, true);
                                break;
                            case LENGTH:
                                addFieldMetaData(solrFields, new SimpleSearchFieldType(SimpleSearchFieldType.Type.LONG),
                                        contentFieldConfig.getType().getSolrFieldName(),
                                        contentFieldConfig.getTypeString(), targetName, false);
                                break;
                            case REFID:
                                TargetResultField trf = new TargetResultField(ContentFieldConfig.Type.REFID.getSolrFieldName(),
                                        targetName, FieldType.REFERENCE);
                                trf.getSolrFieldNames().add(ContentFieldConfig.Type.REFID.getSolrFieldName());
                                solrFields.put(targetName, trf);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
            return solrFields.values();
        }

        private void addFieldMetaData(Map<String, TargetResultField> solrFields,
                                      SearchFieldType searchFieldType,
                                      String solrFieldName,
                                      String indexedFieldName,
                                      String targetName,
                                      boolean isHighlighting) {
            Collection<String> fieldNameList = searchFieldType.getSolrFieldNames(solrFieldName);
            if (!fieldNameList.isEmpty()) {
                TargetResultField trf = new TargetResultField(indexedFieldName,
                        targetName, searchFieldType.getDataFieldType(), isHighlighting);
                trf.getSolrFieldNames().addAll(fieldNameList);
                solrFields.put(targetName, trf);
            }
        }

        private void collectFieldConfigData(Map<String, TargetResultField> solrFields,
                                            String type, List<IndexedFieldConfig> indexedFileldConfigs) {
            for (IndexedFieldConfig fieldConfig : indexedFileldConfigs) {
                if (fieldConfig.getShowInResults()) {
                    String targetName = fieldConfig.getTargetFieldName();
                    Collection<SearchFieldType> fieldTypes = calculateFieldType(type, fieldConfig);
                    if (fieldTypes != null && !fieldTypes.isEmpty()) {
                        for (SearchFieldType fieldType : fieldTypes) {
                            Collection<String> list = fieldType.getSolrFieldNames(fieldConfig.getName());
                            if (!list.isEmpty()) {
                                TargetResultField trf = new TargetResultField(fieldConfig.getName(),
                                        targetName, fieldType.getDataFieldType());
                                trf.getSolrFieldNames().addAll(list);
                                solrFields.put(targetName, trf);
                            }
                        }
                    }
                }
            }
        }
    }

    private class SolrNameExtractor implements ConfigDataExtractor<String> {
        @Override
        public Collection<String> getExtractedValues(String mainType, IndexedDomainObjectConfig objectConfig) {
            Set<String> solrFieldNames = new HashSet<>();
            List<IndexedFieldConfig> indexedFileldConfigs = objectConfig != null ?
                    objectConfig.getFields() : new ArrayList<>(0);
            List<LinkedDomainObjectConfig> linkedFileldConfigs = objectConfig != null ?
                    objectConfig.getLinkedObjects() : new ArrayList<>(0);
            List<IndexedContentConfig> indexedContentConfigs = objectConfig != null ?
                    objectConfig.getContentObjects() : new ArrayList<>(0);

            collectFieldConfigData(solrFieldNames, mainType, indexedFileldConfigs);
            for (LinkedDomainObjectConfig linkedFileldConfig : linkedFileldConfigs) {
                String linkedType = linkedFileldConfig.getType();
                collectFieldConfigData(solrFieldNames, linkedType, linkedFileldConfig.getFields());
            }
            for (IndexedContentConfig indexedContentConfig : indexedContentConfigs) {
                for (ContentFieldConfig contentFieldConfig : indexedContentConfig.getFields()) {
                    if (contentFieldConfig.getShowInResults()) {
                        SearchFieldType searchFieldType = null;
                        switch (contentFieldConfig.getType()) {
                            case NAME:
                            case PATH:
                            case MIMETYPE:
                            case DESCRIPTION:
                                searchFieldType = new TextSearchFieldType(configHelper.getSupportedLanguages());
                                break;
                            case LENGTH:
                                searchFieldType = new SimpleSearchFieldType(SimpleSearchFieldType.Type.LONG);
                                break;
                            case REFID:
                                solrFieldNames.add(ContentFieldConfig.Type.REFID.getSolrFieldName());
                                break;
                            default:
                                break;
                        }
                        if (searchFieldType != null) {
                            solrFieldNames.addAll(searchFieldType.getSolrFieldNames(
                                    contentFieldConfig.getType().getSolrFieldName()));
                        }
                    }
                }
            }
            return solrFieldNames;
        }

        private void collectFieldConfigData(Set<String> solrFieldNames,
                                            String type, List<IndexedFieldConfig> indexedFileldConfigs) {
            for (IndexedFieldConfig fieldConfig : indexedFileldConfigs) {
                if (fieldConfig.getShowInResults()) {
                    Collection<SearchFieldType> fieldTypes = calculateFieldType(type, fieldConfig);
                    for (SearchFieldType fieldType : fieldTypes) {
                        solrFieldNames.addAll(fieldType.getSolrFieldNames(fieldConfig.getName()));
                    }
                }
            }
        }
    }

    private class HighlightingNameExtractor implements ConfigDataExtractor<String> {
        @Override
        public Collection<String> getExtractedValues(String type, IndexedDomainObjectConfig objectConfig) {
            Set<String> solrFieldNames = new HashSet<>();
            List<IndexedContentConfig> indexedContentConfigs = objectConfig != null ?
                    objectConfig.getContentObjects() : new ArrayList<>(0);
            for (IndexedContentConfig indexedContentConfig : indexedContentConfigs) {
                for (ContentFieldConfig contentFieldConfig : indexedContentConfig.getFields()) {
                    if (contentFieldConfig.getShowInResults()) {
                        if (ContentFieldConfig.Type.HIGHLIGHTING == contentFieldConfig.getType()) {
                            SearchFieldType searchFieldType = new SpecialTextSearchFieldType(
                                    configHelper.getSupportedLanguages());
                            solrFieldNames.addAll(searchFieldType.getSolrFieldNames(SearchFilter.CONTENT));
                        }
                    }
                }
            }
            return solrFieldNames;
        }
    }

    interface ConfigDataExtractor<T> {
        Collection<T> getExtractedValues(String type, IndexedDomainObjectConfig objectConfig);
    }

    private interface Combiner {
        boolean add(SolrDocument doc, SolrDocumentList list);
    }

    private static class UnionCombiner implements Combiner {

        private final Set<String> ids = new HashSet<>();

        @Override
        public boolean add(SolrDocument doc, SolrDocumentList list) {
            String id = (String) doc.getFieldValue(SolrFields.MAIN_OBJECT_ID);
            if (ids.contains(id)) {
                return false;
            }

            list.add(doc);
            ids.add(id);
            return true;
        }
    }

    private static class IntersectCombiner implements Combiner {

        private static class FoundDoc {
            SolrDocument doc;
            int count = 0;

            FoundDoc(SolrDocument doc) {
                this.doc = doc;
            }

            int increment() {
                return ++count;
            }
        }

        private final HashMap<String, FoundDoc> docMap = new HashMap<>();
        private final int threshold;

        IntersectCombiner(int threshold) {
            this.threshold = threshold;
        }

        @Override
        public boolean add(SolrDocument doc, SolrDocumentList list) {
            String id = (String) doc.getFieldValue(SolrFields.MAIN_OBJECT_ID);
            if (!docMap.containsKey(id)) {
                docMap.put(id, new FoundDoc(doc));
            }
            FoundDoc firstFound = docMap.get(id);
            if (firstFound.increment() == threshold) {
                list.add(firstFound.doc);
                return true;
            }
            return false;
        }
    }

    private SolrDocumentList combineResults(Collection<SolrDocumentList> results, Combiner combiner, int maxSize) {
        // Копия списка списков результатов - с фиксированными номерами
        HashMap<Integer, SolrDocumentList> lists = new HashMap<>(results.size());
        // Массив указателей на текущий документ в каждом списке
        ArrayList<Integer> cursors = new ArrayList<>(lists.size());
        // Карта соответствия значения рейтинга следующего элемента в списке номеру этого списка -
        // отсортированная по весу. Поскольку, теоретически, несколько разных элементов могут иметь
        // одинаковый рейтинг, используется список номеров.
        SortedMap<Float, LinkedList<Integer>> scores = new TreeMap<>();

        logger.trace("Initialize system structures... Number of results is {}", results.size());
        // Инициализация служебных структур
        initSystemStructures(results, lists, cursors, scores);

        logger.trace("Join lists...");
        SolrDocumentList combined = joinLists(combiner, maxSize, lists, cursors, scores);

        logger.trace("Results successfully combined to single SolrDocumentList...");
        return combined;
    }

    private SolrDocumentList joinLists(Combiner combiner, int maxSize, HashMap<Integer, SolrDocumentList> lists, ArrayList<Integer> cursors, SortedMap<Float, LinkedList<Integer>> scores) {
        // Соединение списков
        SolrDocumentList combined = new SolrDocumentList();
        combined.ensureCapacity(maxSize);
        combined.setMaxScore(scores.size() > 0 ? scores.lastKey() : 0.0f);

        globalCycle:
        while (lists.size() > 0) {
            // Вытаскиваем список номеров списков, имеющих следующий элемент с наибольшим рейтингом
            Float maxScore = scores.lastKey();
            LinkedList<Integer> listNums = scores.get(maxScore);
            while (listNums.size() > 0) {
                // Берём первый из номеров списков
                int listNum = listNums.remove();
                int docNum = cursors.get(listNum);
                SolrDocumentList list = lists.get(listNum);
                boolean added = combiner.add(list.get(docNum), combined);
                // Немедленно останавливаемся, если достигнут необходимый размер списка
                if (added && combined.size() == maxSize) {
                    break globalCycle;
                }
                // Продвигаем выбранный список на один элемент вперёд
                docNum++;
                if (list.size() > docNum) {
                    cursors.set(listNum, docNum);
                    // Добавляем рейтинг элемента в карту рейтингов
                    SolrDocument nextDoc = list.get(docNum);
                    float score = (Float) nextDoc.getFieldValue(SolrUtils.SCORE_FIELD);
                    if (!scores.containsKey(score)) {
                        scores.put(score, new LinkedList<>());
                    }
                    scores.get(score).add(listNum);
                } else {
                    // Если дошли до последнего элемента в списке, удаляем его
                    lists.remove(listNum);
                    // Курсор для списка остаётся, но использоваться не будет. Удалять нельзя, т.к. изменятся номера
                }
            }
            scores.remove(maxScore);
        }
        combined.setNumFound(combined.size());
        return combined;
    }

    private void initSystemStructures(Collection<SolrDocumentList> results, HashMap<Integer, SolrDocumentList> lists, ArrayList<Integer> cursors, SortedMap<Float, LinkedList<Integer>> scores) {
        int i = 0;
        for (SolrDocumentList list : results) {
            // Обрабатываем все непустые списки
            if (list.size() > 0) {
                logger.trace("Compact sublist...");
                compactList(list);
                lists.put(i, list);
                cursors.add(0);
                float score = list.getMaxScore();
                if (!scores.containsKey(score)) {
                    scores.put(score, new LinkedList<>());
                }
                scores.get(score).add(i);
                ++i;
            }
        }
    }

    /**
     * Оставляем в списке только записи с уникальным cm_main
     */
    private void compactList(SolrDocumentList list) {
        HashSet<String> ids = new HashSet<>(list.size());
        for (Iterator<SolrDocument> itr = list.iterator(); itr.hasNext(); ) {
            SolrDocument doc = itr.next();
            String id = (String) doc.getFieldValue(SolrFields.MAIN_OBJECT_ID);
            if (ids.contains(id)) {
                itr.remove();
            } else {
                ids.add(id);
            }
        }
    }

    /*
        public SolrDocumentList unionResults(Collection<SolrDocumentList> results) {
            return combineResults(results, new UnionCombiner());
        }

        public SolrDocumentList intersectResults(Collection<SolrDocumentList> results) {
            return combineResults(results, new IntersectCombiner(results.size()));
        }
    */
    private List<String> listCommonSolrFields() {
        List<String> langs = configHelper.getSupportedLanguages();
        if (langs.size() == 0) {
            return Arrays.asList(SolrFields.EVERYTHING, SolrFields.CONTENT);
        }
        ArrayList<String> fields = new ArrayList<>(2 * langs.size());
        for (String langId : langs) {
            final String everything = SolrFields.EVERYTHING +
                    (langId.isEmpty() ? "" : "_") +
                    langId;
            fields.add(everything);
            final String content = SolrFields.CONTENT +
                    (langId.isEmpty() ? "" : "_") +
                    langId;
            fields.add(content);
        }
        return fields;
    }

    private QueryResponse executeSolrQuery(SolrQuery query, String solrServerKey) {
        try {
            SolrClient solrServer = solrServerWrapperMap.getSolrServerWrapper(solrServerKey).getSolrServer();
            if (solrServer == null) {
                throw new Exception("Can't find SolrClient by key : " + (solrServerKey != null ? solrServerKey : "null"));
            }

            if (logger.isTraceEnabled()) {
                logger.trace("Attempting to execute solr query: {}", query.toString());
            }

            QueryResponse response = solrServer.query(query);
            if (logger.isDebugEnabled()) {
                logger.debug("Response: " + response);
            }
            return response;
        } catch (Exception e) {
            logger.error("Search error", e);
            throw new SearchException("Search error: " + e.getMessage());
        }
    }

    private int estimateFetchLimit(int foundSize, int collectionSize) {
        int factor = 100;
        if (collectionSize > 0) {
            // Пытаемся оценить процент отсева 
            factor = 2 * foundSize / collectionSize;
        }
        return foundSize * factor;
    }

    @Value("${search.dump.file:search-index-dump.txt}")
    private String dumpFileName;
    @Value("${search.dump.encoding:cp1251}")
    private String dumpFileEncoding;

    @Override
    public void dumpAll() {
        try (PrintStream out = new PrintStream(dumpFileName, dumpFileEncoding)) {
            SolrQuery testQuery = new SolrQuery()
                    .setQuery("*:*")
                    .addField("*")
                    .setRows(1000000);
            SolrClient solrServer = solrServerWrapperMap.getRegularSolrServerWrapper().getSolrServer();
            QueryResponse all = solrServer.query(testQuery);
            out.println("Total " + all.getResults().getNumFound() + " document(s)");
            int i = 0;
            for (SolrDocument doc : all.getResults()) {
                out.println("==> Document #" + ++i);
                for (String field : doc.getFieldNames()) {
                    out.println("\t" + field + " = " + doc.getFieldValues(field));
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void disableIndexing(boolean disableFlag) {
        configHelper.disableIndexing(disableFlag);
    }
}
