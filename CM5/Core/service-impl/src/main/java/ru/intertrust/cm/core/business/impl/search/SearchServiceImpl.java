package ru.intertrust.cm.core.business.impl.search;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.SearchService;
import ru.intertrust.cm.core.business.api.dto.CombiningFilter;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SearchFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.model.SearchException;
import ru.intertrust.cm.core.model.SystemException;
import ru.intertrust.cm.core.model.UnexpectedException;

@Stateless(name = "SearchService")
@Local(SearchService.class)
@Remote(SearchService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class SearchServiceImpl implements SearchService, SearchService.Remote {

    protected Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private SolrServer solrServer;

    @Autowired
    private ImplementorFactory<SearchFilter, FilterAdapter<? extends SearchFilter>> searchFilterImplementorFactory;

    @Autowired
    private SearchConfigHelper configHelper;

    @Override
    public IdentifiableObjectCollection search(String query, String areaName, String targetCollectionName,
            int maxResults) {
        try {
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
            if (maxResults > 0) {
                solrQuery.setRows(maxResults);
            }

            int fetchLimit = maxResults;
            while (true) {
                QueryResponse response = executeSolrQuery(solrQuery);
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
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected exception caught in search", ex);
            throw new UnexpectedException("SearchService", "search",
                "query: " + query + ", areaName: " + areaName + ", targetCollectionName: " + targetCollectionName, ex);
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

    private IdentifiableObjectCollection complexSearch(SearchQuery query, CollectionRetriever collectionRetriever,
            int maxResults) {
        try {
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
                if (found.size() == fetchLimit && result.size() < maxResults) {
                    // Увеличиваем размер выборки в Solr
                    fetchLimit = estimateFetchLimit(found.size(), result.size());
                    continue;
                }
                return result;
            }
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected exception caught in search", ex);
            throw new UnexpectedException("SearchService", "search",
                "query: " + query /*+ ", targetCollectionName: " + targetCollectionName*/, ex);
        }
    }

    private class ComplexQuery {
        private HashMap<String, StringBuilder> filterStrings = new HashMap<>();
        private ArrayList<ComplexQuery> nestedQueries = new ArrayList<>();
        private CombiningFilter.Op combineOperation = CombiningFilter.AND;

        @SuppressWarnings("unchecked")
        public void addFilters(Collection<SearchFilter> filters, SearchQuery query) {
            for (SearchFilter filter : filters) {
                if (filter instanceof CombiningFilter) {
                    CombiningFilter combiningFilter = (CombiningFilter) filter;
                    if (combiningFilter.getOperation() == combineOperation) {
                        addFilters(combiningFilter.getFilters(), query);
                    } else {
                        ComplexQuery nestedQuery = new ComplexQuery();
                        nestedQuery.combineOperation = combiningFilter.getOperation();
                        nestedQuery.addFilters(combiningFilter.getFilters(), query);
                        nestedQueries.add(nestedQuery);
                    }
                    continue;
                }

                @SuppressWarnings("rawtypes")
                FilterAdapter adapter = searchFilterImplementorFactory.createImplementorFor(filter.getClass());
                String filterValue = adapter.getFilterString(filter, query);
                if (filterValue == null || filterValue.isEmpty()) {
                    continue;
                }

                Collection<String> types = configHelper.findApplicableTypes(filter.getFieldName(), query.getAreas(),
                        query.getTargetObjectType());
                if (types.size() == 0) {
                    log.info("Field " + filter.getFieldName() + " is not indexed; excluded from search");
                }

                for (String type : types) {
                    addFilterValue(type, filterValue);
                }
            }
        }

        private void addFilterValue(String type, String filterValue) {
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
            StringBuilder areas = new StringBuilder();
            for (String areaName : query.getAreas()) {
                areas.append(areas.length() == 0 ? "(" : " OR ")
                     .append("\"")
                     .append(areaName)
                     .append("\"");
            }
            areas.append(")");

            SolrDocumentList result;
            float clippingFactor = 1f;
            /*if (combineOperation == CombiningFilter.AND) {
                clippingFactor /= filterStrings.size();
            }*/
            boolean clipped;
            do {
                clipped = false;
                int rows = Math.round(fetchLimit / clippingFactor);
                ArrayList<SolrDocumentList> foundParts = new ArrayList<>(filterStrings.size());
                for (Map.Entry<String, StringBuilder> entry : filterStrings.entrySet()) {
                    SolrQuery solrQuery = new SolrQuery()
                            .setQuery(entry.getValue().toString())
                            .addFilterQuery(SolrFields.AREA + ":" + areas)
                            .addFilterQuery(SolrFields.TARGET_TYPE + ":\"" + query.getTargetObjectType() + "\"")
                            //.addFilterQuery(SolrFields.OBJECT_TYPE + ":\"" + entry.getKey() + "\"")
                            .addField(SolrFields.MAIN_OBJECT_ID)
                            .addField(SolrUtils.SCORE_FIELD);
                    if (!SearchFilter.EVERYWHERE.equals(entry.getKey())) {
                        solrQuery.addFilterQuery(SolrFields.OBJECT_TYPE + ":\"" + entry.getKey() + "\"");
                    }
                    if (rows > 0) {
                        solrQuery.setRows(rows);
                    }
                    QueryResponse response = executeSolrQuery(solrQuery);
                    foundParts.add(response.getResults());
                    clipped = clipped || rows > 0 && response.getResults().size() == rows;
                }
                for (ComplexQuery nested : nestedQueries) {
                    SolrDocumentList part = nested.execute(rows, query);
                    foundParts.add(part);
                    clipped = clipped || rows > 0 && part.size() == rows;
                }
                if (foundParts.size() == 1) {
                    result = foundParts.get(0);
                } else {
                    result = combineResults(foundParts, combineOperation == CombiningFilter.AND
                            ? new IntersectCombiner(foundParts.size()) : new UnionCombiner(), fetchLimit);
                }
                clippingFactor /= fetchLimit - result.size() + 1;
            } while (clipped && result.size() < fetchLimit);
            return result;
        }
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

    private QueryResponse executeSolrQuery(SolrQuery query) {
        try {
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
        int factor = 10;
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
}
