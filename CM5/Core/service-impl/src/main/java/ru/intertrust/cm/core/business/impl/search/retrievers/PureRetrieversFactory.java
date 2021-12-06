package ru.intertrust.cm.core.business.impl.search.retrievers;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

@Component
public class PureRetrieversFactory {

    @Lookup
    public CntxCollectionRetriever getCntxCollectionRetriever() {
        //spring will override this method
        return null;
    }

    @Lookup
    public NamedCollectionRetriever getNamedCollectionRetriever() {
        //spring will override this method
        return null;
    }

    @Lookup
    public QueryCollectionRetriever getQueryCollectionRetriever() {
        //spring will override this method
        return null;
    }

}
