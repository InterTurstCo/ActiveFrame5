package ru.intertrust.cm.core.business.impl.search;

import java.io.PrintStream;
import java.util.*;

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

import ru.intertrust.cm.core.business.api.BaseAttachmentService;
import ru.intertrust.cm.core.business.api.SearchService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.doel.DoelExpression;
import ru.intertrust.cm.core.config.search.*;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.model.RemoteSuitableException;
import ru.intertrust.cm.core.model.SearchException;
import ru.intertrust.cm.core.tools.SearchAreaFilterScriptContext;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

@Stateless(name = "SearchService")
@Local(SearchService.class)
@Remote(SearchService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class SearchServiceImpl implements SearchService, SearchService.Remote {

    protected Logger log = LoggerFactory.getLogger(getClass());

    @Value("${search.results.limit:5000}")
    private int RESULTS_LIMIT;

    @Autowired
    private SolrServerWrapperMap solrServerWrapperMap;

    @Autowired
    private ImplementorFactory<SearchFilter, FilterAdapter<? extends SearchFilter>> searchFilterImplementorFactory;

    @Autowired
    private SearchConfigHelper configHelper;

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
            if (solrQuery.getSorts().isEmpty()){
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
                IdentifiableObjectCollection result = new NamedCollectionRetriever(targetCollectionName)
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
        //return result;
    }

    @Override
    public IdentifiableObjectCollection search(SearchQuery query, String targetCollectionName, int maxResults) {
        return complexSearch(query, new NamedCollectionRetriever(targetCollectionName), maxResults);
    }

    @Override
    public IdentifiableObjectCollection search(SearchQuery query, String targetCollectionName,
            List<? extends Filter> collectionFilters, int maxResults) {
        return complexSearch(query, new NamedCollectionRetriever(targetCollectionName, collectionFilters), maxResults);
    }

    @Override
    public IdentifiableObjectCollection searchAndQuery(SearchQuery searchQuery, String sqlQuery, int maxResults) {
        return complexSearch(searchQuery, new QueryCollectionRetriever(sqlQuery), maxResults);
    }

    @Override
    public IdentifiableObjectCollection searchAndQuery(SearchQuery searchQuery, String sqlQuery,
            List<? extends ru.intertrust.cm.core.business.api.dto.Value<?>> sqlParams, int maxResults) {
        return complexSearch(searchQuery, new QueryCollectionRetriever(sqlQuery, sqlParams), maxResults);
    }

    @Override
    public IdentifiableObjectCollection searchCntx(SearchQuery query, String targetCollectionName, int maxResults) {
        Map<CustomKey<String, String>, SearchQuery> queries = splitQueryBySolrServer(query);
        List<IdentifiableObjectCollection> collections = new ArrayList<>();
        for (Map.Entry<CustomKey<String, String>, SearchQuery> entry : queries.entrySet()) {
            IdentifiableObjectCollection collectionPart = null;
            if (!SolrServerWrapper.REGULAR.equals(entry.getKey())) {
                Set<String> solrFieldNames = getSolrFieldNames(entry.getValue(), entry.getKey().getServerKey(), new SolrNameExtractor());
                Set<String> hlFieldNames = getSolrFieldNames(entry.getValue(), entry.getKey().getServerKey(), new HighlightingNameExtractor());
                Collection<TargetResultField> fieldsCollection = getResultFields(entry.getValue(), entry.getKey().getServerKey(), new ResultFieldsExtractor());
                SearchAreaConfig searchAreaConfig = configHelper.getSearchAreaDetailsConfig(entry.getKey().getAreaKey());
                String targetFilterName = searchAreaConfig != null ? searchAreaConfig.getTargetFilterName() : null;
                collectionPart = cntxSearch(query, new CntxCollectionRetriever(targetCollectionName, targetFilterName, fieldsCollection),
                        entry.getKey().getServerKey(), solrFieldNames, hlFieldNames, maxResults);
            }
            if (collectionPart != null) {
                collections.add(collectionPart);
            }
        }
        return mergeCollections(collections, maxResults);
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
        // ортировка по редевантности
        CollectionRetriever.sortByRelevance(collection);
        // cокращение до maxResult
        if (collection.size() > maxResults) {
            int cnt = 0;
            Iterator<IdentifiableObject> iterator = collection.iterator();
            while (iterator.hasNext()) {
                iterator.next();
                if (cnt >= maxResults) {
                    iterator.remove();
                } else {
                    cnt ++;
                }
            }
        }
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
            while(true) {
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

    private IdentifiableObjectCollection cntxSearch(SearchQuery searchQuery,
                                                    CollectionRetriever collectionRetriever,
                                                    String solrServerKey,
                                                    Set<String> solrFields,
                                                    Set<String> highlightingFields,
                                                    int maxResults) {
        try {
            if (maxResults <= 0 || maxResults > RESULTS_LIMIT) {
                maxResults = RESULTS_LIMIT;
            }

            CntxQuery solrCntxQuery = new CntxQuery(solrServerKey);
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
            } while(true);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    private Map<CustomKey<String, String>, SearchQuery> splitQueryBySolrServer(SearchQuery query) {
        Map<CustomKey<String, String>, SearchQuery> queryMap = new HashMap<>();
        // разбиваем запрос на несколько - обычный и контекстные по областям поиска
        List<String> areas = query.getAreas();
        if (areas != null && !areas.isEmpty()) {
            for (String area : areas) {
                String key = configHelper.getSearchAreaDetailsConfig(area).getSolrServerKey();
                key = solrServerWrapperMap.isCntxSolrServer(key) ? key : SolrServerWrapper.REGULAR;
                CustomKey<String, String> customKey = new CustomKey<>(key, area);
                SearchQuery newQuery = queryMap.get(customKey);
                if (newQuery == null) {
                    newQuery = new SearchQuery(query);
                    newQuery.clearAreas();
                    queryMap.put(customKey, newQuery);
                }
                newQuery.addArea(area);
            }
        } else {
            queryMap.put(new CustomKey(SolrServerWrapper.REGULAR, null), query);
        }
        return queryMap;
    }

    class CustomKey <T1, T2>{
        private final T1 solrServerKey;
        private final T2 areaKey;

        CustomKey (T1 solrServerKey, T2 areaKey) {
            this.solrServerKey = solrServerKey;
            this.areaKey = areaKey;
        }

        public T1 getServerKey() {
            return solrServerKey;
        }

        public T2 getAreaKey() {
            return areaKey;
        }

        @Override
        public int hashCode() {
            int hash = solrServerKey != null ? solrServerKey.hashCode() : 0;
            hash = hash * 31 ^ (areaKey != null ? areaKey.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CustomKey)) {
                return false;
            }
            CustomKey<T1, T2> other = (CustomKey) obj;
            return this.solrServerKey == null ? other.solrServerKey == null : this.solrServerKey.equals(other.solrServerKey)
                    && this.areaKey == null ? other.areaKey == null : this.areaKey.equals(other.areaKey);
        }
    }

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

        public void setNegativeResult(boolean negativeResult){
            this.negateResult = negativeResult;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
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
                    log.info("Field " + filter.getFieldName() + " is not indexed; excluded from search");
                }

                if (types.size() > 1){
                    addFilterValue(SearchConfigHelper.ALL_TYPES, filterValue);
                }else if(types.size() == 1){
                    addFilterValue((String)types.toArray()[0], filterValue);
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
                ArrayList<SolrDocumentList> foundParts = new ArrayList<>(filterStrings.size());
                for (Map.Entry<String, StringBuilder> entry : filterStrings.entrySet()) {
                    SolrDocumentList cached = foundCache.get(entry.getKey());
                    if (cached != null && (cached.size() >= cached.getNumFound() || cached.size() >= rows)) {
                        foundParts.add(cached);
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
                    if (solrQuery.getSorts().isEmpty()){
                        solrQuery.addSort(SolrUtils.SCORE_FIELD, SolrQuery.ORDER.desc)
                                .addSort(SolrFields.MAIN_OBJECT_ID, SolrQuery.ORDER.asc);
                    }
                    if (rows > 0) {
                        solrQuery.setRows(rows);
                    }
                    QueryResponse response = executeSolrQuery(solrQuery, solrServerKey);
                    foundParts.add(response.getResults());
                    foundCache.put(entry.getKey(), response.getResults());
                    clipped = clipped || rows > 0 && response.getResults().size() == rows;
                }
                for (String filterString : multiTypeFilterStrings) {
                    SolrDocumentList cached = foundCache.get(":" + filterString);
                    if (cached != null && (cached.size() >= cached.getNumFound() || cached.size() >= rows)) {
                        foundParts.add(cached);
                        continue;
                    }

                    SolrQuery solrQuery = new SolrQuery()
                            .setQuery(filterString)
                            .addFilterQuery(SolrFields.AREA + ":" + areas)
                            .addFilterQuery(SolrFields.TARGET_TYPE + ":" + targetTypes)
                            .addField(SolrFields.MAIN_OBJECT_ID)
                            .addField(SolrUtils.SCORE_FIELD);
                    if (solrQuery.getSorts().isEmpty()){
                        solrQuery.addSort(SolrUtils.SCORE_FIELD, SolrQuery.ORDER.desc)
                                .addSort(SolrFields.MAIN_OBJECT_ID, SolrQuery.ORDER.asc);
                    }
                    if (rows > 0) {
                        solrQuery.setRows(rows);
                    }
                    QueryResponse response = executeSolrQuery(solrQuery, solrServerKey);
                    foundParts.add(response.getResults());
                    foundCache.put(":" + filterString, response.getResults());
                    clipped = clipped || rows > 0 && response.getResults().size() == rows;
                }
                for (ComplexQuery nested : nestedQueries) {
                    SolrDocumentList part = nested.execute(rows, query);
                    foundParts.add(part);
                    clipped = clipped || rows > 0 && part.size() >= rows;
                }
                if (foundParts.size() == 1) {
                    result = foundParts.get(0);
                    compactList(result);
                } else {
                    // TODO special processing for negateResult==true needed
                    result = combineResults(foundParts, combineOperation == CombiningFilter.AND
                            ? new IntersectCombiner(foundParts.size()) : new UnionCombiner(), fetchLimit);
                }
                clippingFactor *= Math.max(1f, 0.9f * result.size()) / rows;
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

        protected HashMap<String, StringBuilder> filterStrings = new HashMap<>();
        protected ArrayList<String> multiTypeFilterStrings = new ArrayList<>();
        protected ArrayList<CntxQuery> nestedQueries = new ArrayList<>();
        CombiningFilter.Op combineOperation = CombiningFilter.AND;
        boolean negateResult = false;

        protected HashMap<String, SolrDocumentList> foundCache = new HashMap<>();
        private final String solrServerKey;

        CntxQuery(String solrServerKey) {
            this.solrServerKey = solrServerKey;
        }

        @Override
        public CntxQuery newNestedQuery() {
            CntxQuery nestedQuery = new CntxQuery(solrServerKey);
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

                Collection<String> types = configHelper.findApplicableTypes(filter.getFieldName(),
                        query.getAreas(), query.getTargetObjectTypes());
                if (types.size() == 0) {
                    log.info("Field " + filter.getFieldName() + " is not indexed; excluded from search");
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

        public QueryResponse execute(SearchQuery query,
                                     String solrServerKey,
                                     Set<String> solrFields,
                                     Set<String> highlightingFields,
                                     int fetchLimit) {
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

            StringBuilder queryString = new StringBuilder();
            StringBuilder objectTypes = new StringBuilder();

            for (Map.Entry<String, StringBuilder> entry : filterStrings.entrySet()) {
                queryString.append(queryString.length() > 0 ? (combineOperation == CombiningFilter.AND ? " AND " : " OR ") : "")
                        .append(entry.getValue().toString());
                objectTypes.append(objectTypes.length() == 0 ? "(" : " OR ")
                        .append("\"")
                        .append(entry.getKey())
                        .append("\"");
            }
            objectTypes.append(")");

            for (String filterString : multiTypeFilterStrings) {
                queryString.append(queryString.length() > 0 ? (combineOperation == CombiningFilter.AND ? " AND " : " OR ") : "")
                        .append(filterString);
            }

            float clippingFactor = 1f;
            boolean clipped;
            QueryResponse response = null;
            do {
                clipped = false;
                int rows = Math.round(fetchLimit / clippingFactor);
                SolrQuery solrQuery = new SolrQuery()
                        .setQuery(queryString.toString())
                        .addFilterQuery(SolrFields.AREA + ":" + areas)
                        .addFilterQuery(SolrFields.TARGET_TYPE + ":" + targetTypes)
                        .addFilterQuery(SolrFields.OBJECT_TYPE + ":" + objectTypes)
                        .addField(SolrUtils.ID_FIELD)
                        .addField(SolrFields.OBJECT_ID)
                        .addField(SolrFields.MAIN_OBJECT_ID)
                        .addField(SolrUtils.SCORE_FIELD);
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

                if (highlightingFields != null && !highlightingFields.isEmpty()) {
                    solrQuery.setHighlight(true)
                            .setHighlightRequireFieldMatch(true)
                            .set(HL_usePhraseHighlighter, true)
                            .set(HL_highlightMultiTerm, true)
                            .set(HL_simple_pre, "<hl>")
                            .set(HL_simple_post, "</hl>")
                            .set(HL_snippets, "5")
                            .set(HL_fragsize, "50");
                    for (String hlField : highlightingFields) {
                        solrQuery.addHighlightField(hlField);
                    }
                }

                if (rows > 0) {
                    solrQuery.setRows(rows);
                }
                response = executeSolrQuery(solrQuery, solrServerKey);
                clipped = clipped || rows > 0 && response.getResults().size() == rows;
                compactQueryResults(response);
                clippingFactor *= Math.max(1f, 0.9f * response.getResults().size()) / rows;
            } while (clipped && response.getResults().size() < fetchLimit);
            return response;
        }

        private void compactQueryResults(QueryResponse response) {
            if (response == null || response.getResults() == null) {
                return;
            }
            HashSet<String> ids = new HashSet<>(response.getResults().size());
            for (Iterator<SolrDocument> itr = response.getResults().iterator(); itr.hasNext(); ) {
                SolrDocument doc = itr.next();
                String mainId = (String) doc.getFieldValue(SolrFields.MAIN_OBJECT_ID);
                String id = (String) doc.getFieldValue(SolrUtils.ID_FIELD);
                if (ids.contains(mainId)) {
                    itr.remove();
                    if (response.getHighlighting() != null && response.getHighlighting().containsKey(id)) {
                        response.getHighlighting().remove(id);
                    }
                } else {
                    ids.add(mainId);
                }
            }
        }
    }
    // =================================================================================================================

    public Set<String> getSolrFieldNames(SearchQuery query, String solrServerKey, ConfigDataExtractor extractor) {
        Set<String> solrFieldNames = new LinkedHashSet<>();
        try {
            if (extractor != null) {
                for (String type : query.getTargetObjectTypes()) {
                    List<SearchConfigHelper.SearchAreaDetailsConfig> searchAreaDetailsConfigs = configHelper.findEffectiveConfigs(type);
                    if (searchAreaDetailsConfigs == null || searchAreaDetailsConfigs.isEmpty()) {
                        continue;
                    }
                    for (SearchConfigHelper.SearchAreaDetailsConfig searchAreaDetailsConfig : searchAreaDetailsConfigs) {
                        String solrKey = searchAreaDetailsConfig.getSolrServerKey();
                        if (!solrServerKey.equalsIgnoreCase(solrKey)) {
                            continue;
                        }
                        String searchAreaName = searchAreaDetailsConfig.getAreaName();
                        for (String area : query.getAreas()) {
                            if (!searchAreaName.equalsIgnoreCase(area)) {
                                continue;
                            }
                            // получаем список полей
                            IndexedDomainObjectConfig objectConfig = searchAreaDetailsConfig.getObjectConfig();
                            Collection<String> extractedValues = extractor != null ? extractor.getExtractedValues(type, objectConfig) : Collections.emptySet();
                            solrFieldNames.addAll(extractedValues);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw RemoteSuitableException.convert(e);
        }
        return solrFieldNames;
    }

    public List<TargetResultField> getResultFields(SearchQuery query, String solrServerKey, ConfigDataExtractor extractor) {
        List<TargetResultField> solrFields = new ArrayList<>();
        try {
            if (extractor != null) {
                for (String type : query.getTargetObjectTypes()) {
                    List<SearchConfigHelper.SearchAreaDetailsConfig> searchAreaDetailsConfigs = configHelper.findEffectiveConfigs(type);
                    if (searchAreaDetailsConfigs == null || searchAreaDetailsConfigs.isEmpty()) {
                        continue;
                    }
                    for (SearchConfigHelper.SearchAreaDetailsConfig searchAreaDetailsConfig : searchAreaDetailsConfigs) {
                        String solrKey = searchAreaDetailsConfig.getSolrServerKey();
                        if (!solrServerKey.equalsIgnoreCase(solrKey)) {
                            continue;
                        }
                        String searchAreaName = searchAreaDetailsConfig.getAreaName();
                        for (String area : query.getAreas()) {
                            if (!searchAreaName.equalsIgnoreCase(area)) {
                                continue;
                            }
                            // получаем список полей
                            IndexedDomainObjectConfig objectConfig = searchAreaDetailsConfig.getObjectConfig();
                            Collection<TargetResultField> extractedValues = extractor != null ? extractor.getExtractedValues(type, objectConfig) : Collections.emptySet();
                            solrFields.addAll(extractedValues);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw RemoteSuitableException.convert(e);
        }
        return solrFields;
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

            for (IndexedFieldConfig fieldConfig : indexedFileldConfigs) {
                if (fieldConfig.getShowInResults()) {
                    String targetName = fieldConfig.getTargetFieldName();
                    Collection<SearchFieldType> fieldTypes = calculateFieldType(mainType, fieldConfig);
                    if (fieldTypes != null && !fieldTypes.isEmpty()) {
                        for (SearchFieldType fieldType : fieldTypes) {
                            Collection<String> list = fieldType.getSolrFieldNames(fieldConfig.getName(), true);
                            if (!list.isEmpty()) {
                                TargetResultField ssrf = new TargetResultField(fieldConfig.getName(), targetName, fieldType.getDataFieldType());
                                ssrf.getSolrFieldNames().addAll(list);
                                solrFields.put(targetName, ssrf);
                            }
                        }
                    }
                }
            }
            for (LinkedDomainObjectConfig linkedFileldConfig : linkedFileldConfigs) {
                String linkedType = linkedFileldConfig.getType();
                for (IndexedFieldConfig fieldConfig : linkedFileldConfig.getFields()) {
                    if (fieldConfig.getShowInResults()) {
                        String targetName = fieldConfig.getTargetFieldName();
                        Collection<SearchFieldType> fieldTypes = calculateFieldType(linkedType, fieldConfig);
                        for (SearchFieldType fieldType : fieldTypes) {
                            Collection<String> list = fieldType.getSolrFieldNames(fieldConfig.getName(), true);
                            if (!list.isEmpty()) {
                                TargetResultField ssrf = new TargetResultField(fieldConfig.getName(), targetName, fieldType.getDataFieldType());
                                ssrf.getSolrFieldNames().addAll(list);
                                solrFields.put(targetName, ssrf);
                            }
                        }
                    }
                }
            }

            for (IndexedContentConfig indexedContentConfig : indexedContentConfigs) {
                for (ContentFieldConfig contentFieldConfig : indexedContentConfig.getFields()) {
                    if (contentFieldConfig.getShowInResults()) {
                        SearchFieldType searchFieldType = null;
                        String targetName = contentFieldConfig.getTargetFieldName();
                        Collection<String> fieldNameList;
                        switch (contentFieldConfig.getType()) {
                            case NAME:
                            case PATH:
                            case MIMETYPE:
                            case DESCRIPTION:
                                searchFieldType = new TextSearchFieldType(
                                        configHelper.getSupportedLanguages(), false, false);
                                fieldNameList = searchFieldType.getSolrFieldNames(contentFieldConfig.getType().getSolrFieldName(),true);
                                if (!fieldNameList.isEmpty()) {
                                    TargetResultField ssrf = new TargetResultField(contentFieldConfig.getTypeString(),
                                            targetName, searchFieldType.getDataFieldType());
                                    ssrf.getSolrFieldNames().addAll(fieldNameList);
                                    solrFields.put(targetName, ssrf);
                                }
                                break;
                            case HIGHLIGHTING:
                                searchFieldType = new SpecialTextSearchFieldType(
                                        configHelper.getSupportedLanguages());
                                fieldNameList = searchFieldType.getSolrFieldNames(contentFieldConfig.getType().getSolrFieldName(),true);
                                if (!fieldNameList.isEmpty()) {
                                    TargetResultField ssrf = new TargetResultField(SearchFilter.CONTENT,
                                            targetName, searchFieldType.getDataFieldType(),true);
                                    ssrf.getSolrFieldNames().addAll(fieldNameList);
                                    solrFields.put(targetName, ssrf);
                                }
                                break;
                            case LENGTH:
                                searchFieldType = new SimpleSearchFieldType(SimpleSearchFieldType.Type.LONG);
                                fieldNameList = searchFieldType.getSolrFieldNames(contentFieldConfig.getType().getSolrFieldName(),true);
                                if (!fieldNameList.isEmpty()) {
                                    TargetResultField ssrf = new TargetResultField(contentFieldConfig.getTypeString(),
                                            targetName, searchFieldType.getDataFieldType());
                                    ssrf.getSolrFieldNames().addAll(fieldNameList);
                                    solrFields.put(targetName, ssrf);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
            return solrFields.values();
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

            for (IndexedFieldConfig fieldConfig : indexedFileldConfigs) {
                if (fieldConfig.getShowInResults()) {
                    Collection<SearchFieldType> fieldTypes = calculateFieldType(mainType, fieldConfig);
                    for (SearchFieldType fieldType : fieldTypes) {
                        solrFieldNames.addAll(fieldType.getSolrFieldNames(fieldConfig.getName(), true));
                    }
                }
            }
            for (LinkedDomainObjectConfig linkedFileldConfig : linkedFileldConfigs) {
                String linkedType = linkedFileldConfig.getType();
                for (IndexedFieldConfig fieldConfig : linkedFileldConfig.getFields()) {
                    if (fieldConfig.getShowInResults()) {
                        Collection<SearchFieldType> fieldTypes = calculateFieldType(linkedType, fieldConfig);
                        for (SearchFieldType fieldType : fieldTypes) {
                            solrFieldNames.addAll(fieldType.getSolrFieldNames(fieldConfig.getName(), true));
                        }
                    }
                }
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
                                searchFieldType = new TextSearchFieldType(
                                        configHelper.getSupportedLanguages(), false, false);
                                break;
                            case LENGTH:
                                searchFieldType = new SimpleSearchFieldType(SimpleSearchFieldType.Type.LONG);
                                break;
                            default:
                                break;
                        }
                        if (searchFieldType != null) {
                            solrFieldNames.addAll(searchFieldType.getSolrFieldNames(
                                    contentFieldConfig.getType().getSolrFieldName(),true));
                        }
                    }
                }
            }
            return solrFieldNames;
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
                            solrFieldNames.addAll(searchFieldType.getSolrFieldNames(SearchFilter.CONTENT, true));
                        }
                    }
                }
            }
            return solrFieldNames;
        }
    }

    interface ConfigDataExtractor<T> {
        Collection<T> getExtractedValues (String type, IndexedDomainObjectConfig objectConfig);
    }

    private interface Combiner {
        boolean add(SolrDocument doc, SolrDocumentList list);
    }

    private static class UnionCombiner implements Combiner {

        private HashSet<String> ids = new HashSet<>();

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

        private HashMap<String, FoundDoc> docMap = new HashMap<>();
        private int threshold;

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

        // Инициализация служебных структур
        int i = 0;
        for (SolrDocumentList list : results) {
            // Обрабатываем все непустые списки
            if (list.size() > 0) {
                compactList(list);
                lists.put(i, list);
                cursors.add(0);
                float score = list.getMaxScore();
                if (!scores.containsKey(score)) {
                    scores.put(score, new LinkedList<Integer>());
                }
                scores.get(score).add(i);
                ++i;
            }
        }

        // Соединение списков
        SolrDocumentList combined = new SolrDocumentList();
        combined.ensureCapacity(maxSize);
        combined.setMaxScore(scores.size() > 0 ? scores.lastKey() : 0.0f);
        globalCycle:
        while (lists.size() > 0) {
            // Вытаскиваем список номеров списков, имеющих следующий элемент с наибольшим рейтингом
            Float maxScore = scores.lastKey();
            LinkedList<Integer> listNums = scores.get(maxScore);
            while(listNums.size() > 0) {
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
                        scores.put(score, new LinkedList<Integer>());
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
            StringBuilder everything = new StringBuilder()
                    .append(SolrFields.EVERYTHING)
                    .append(langId.isEmpty() ? "" : "_")
                    .append(langId);
            fields.add(everything.toString());
            StringBuilder content = new StringBuilder()
                    .append(SolrFields.CONTENT)
                    .append(langId.isEmpty() ? "" : "_")
                    .append(langId);
            fields.add(content.toString());
        }
        return fields;
    }

    private QueryResponse executeSolrQuery(SolrQuery query, String solrServerKey) {
        try {
            SolrServer solrServer = solrServerWrapperMap.getSolrServerWrapper(solrServerKey).getSolrServer();
            if (solrServer == null) {
                throw new Exception("Can't find SolrServer by key : " + (solrServerKey != null ? solrServerKey : "null"));
            }
            QueryResponse response = solrServer.query(query);
            if (log.isDebugEnabled()) {
                log.debug("Response: " + response);
            }
            return response;
        } catch (Exception e) {
            log.error("Search error", e);
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
        PrintStream out = null;
        try {
            out = new PrintStream(dumpFileName, dumpFileEncoding);
            SolrQuery testQuery = new SolrQuery()
                    .setQuery("*:*")
                    .addField("*")
                    .setRows(1000000);
            SolrServer solrServer = solrServerWrapperMap.getRegularSolrServerWrapper().getSolrServer();
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
            e.printStackTrace();
            log.error(e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    @Override
    public void disableIndexing(boolean disableFlag) {
        configHelper.disableIndexing(disableFlag);
    }
}
