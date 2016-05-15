package com.queatz.snappy.api;

import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceFactory;
import com.queatz.snappy.service.Api;

import java.util.Iterator;

/**
 * Created by jacob on 2/8/15.
 */

public class Pirate extends Api.Path {
    public Pirate(Api api) {
        super(api);
    }

    @Override
    public void call() {


        ok("yarr!");
    }

    private String doType(String object) {
        String s = "";
        int num = 0;

        PagedResults results = new PagedResults(object);

        while (true) {
            ScoredDocument document = results.next();

            if (document == null) {
                break;
            }

            s += document.getId() + '\n';

            num++;
        }

        return s + "\nprocessed: " + num;
    }

    private class PagedResults implements Iterator<ScoredDocument> {
        Iterator<ScoredDocument> results;
        int offset;
        boolean terminated;
        IndexSpec indexSpec;
        Index index;
        static final int PAGE_SIZE = 500;

        PagedResults(String type) {
            indexSpec = IndexSpec.newBuilder().setName(type).build();
            index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
        }

        private Iterator<ScoredDocument> nextPage() {
            QueryOptions options = QueryOptions.newBuilder().setLimit(PAGE_SIZE).setOffset(offset).build();
            Query query = Query.newBuilder().setOptions(options).build("");

            offset += PAGE_SIZE;

            return index.search(query).iterator();
        }

        @Override
        public ScoredDocument next() {
            if (terminated) {
                return null;
            }

            if (results == null) {
                results = nextPage();
            }

            if (results == null) {
                terminated = true;
                return null;
            }

            if (!results.hasNext()) {
                results = null;
                return next();
            }

            ScoredDocument document = results.next();

            if (document == null) {
                terminated = true;
                return null;
            }

            return document;
        }

        @Override
        public boolean hasNext() {
            return !terminated;
        }

        @Override
        public void remove() {

        }
    }
}