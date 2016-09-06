package me.himanshusoni.quantumflux.model.query;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class will keep track of the query and its supplied arguments , so that when multiple
 * queries are appended, the query and arguments will always match up.
 */
public class QueryBuilder implements Serializable {

    private final StringBuilder mQueryBuffer;
    private final List<Object> mArgsStore;

    public QueryBuilder() {
        mQueryBuffer = new StringBuilder();
        mArgsStore = new LinkedList<>();
    }

    public QueryBuilder(String init, Object... args) {
        mQueryBuffer = new StringBuilder(init);
        mArgsStore = new LinkedList<>(Arrays.asList(args));
    }

    public void append(String query, Object... args) {
        mQueryBuffer.append(query);
        for (Object arg : args) {
            mArgsStore.add(String.valueOf(arg));
        }
    }

    public void append(QueryBuilder queryBuilder) {
        mQueryBuffer.append(queryBuilder.getQueryString());
        mArgsStore.addAll(queryBuilder.getQueryArgs());
    }

    public String getQueryString() {
        return String.valueOf(mQueryBuffer);
    }

    private Collection<Object> getQueryArgs() {
        List<Object> queryArgs = new LinkedList<>();
        queryArgs.addAll(mArgsStore);

        return Collections.unmodifiableCollection(queryArgs);
    }

    public String[] getQueryArgsAsArray() {
        String[] args = new String[mArgsStore.size()];

        for (int i = 0; i < mArgsStore.size(); i++) {
            args[i] = String.valueOf(mArgsStore.get(i));
        }
        return args;
    }

    @Override
    public String toString() {
        return getQueryString();
    }
}
