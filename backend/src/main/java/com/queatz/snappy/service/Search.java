package com.queatz.snappy.service;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.SearchServiceFactory;

import java.util.HashMap;

/**
 * Created by jacob on 11/17/14.
 */
public class Search {
    private static Search _service;

    public static Search getService() {
        if(_service == null)
            _service = new Search();

        return _service;
    }

    public static enum Type {
        PERSON,
        PARTY,
        LOCATION,
        MESSAGE,
        UPDATE,
        JOIN,
        FOLLOW,
        CONTACT,
        BUY,
        OFFER,
        BOUNTY
    }

    public HashMap<Type, Index> index;

    public Search() {
        index = new HashMap<>();

        String name;
        IndexSpec indexSpec;
        Index idx;

        for (Type type : Type.values()) {
            name = type.name().toLowerCase();
            indexSpec = IndexSpec.newBuilder().setName(name).build();
            idx = SearchServiceFactory.getSearchService().getIndex(indexSpec);

            index.put(type, idx);
        }
    }

    public Document get(Type type, String id) {
        if(id == null || id.isEmpty())
            return null;

        try {
            return index.get(type).get(id);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }
}
