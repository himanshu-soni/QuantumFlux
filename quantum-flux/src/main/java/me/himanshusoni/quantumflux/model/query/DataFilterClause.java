package me.himanshusoni.quantumflux.model.query;

import java.io.Serializable;

import me.himanshusoni.quantumflux.model.map.SqlColumnMappingFactory;

/**
 * Defines a basic clause interface, that can be used to join queries together
 */
public interface DataFilterClause<T extends DataFilterClause> extends Serializable {

    boolean hasFilterValue();

    /**
     * The filter conjunction, this is equal to SQL AND and OR
     */
    public enum DataFilterConjunction {
        AND, OR
    }

    /**
     * The where clause for this query
     */
    QueryBuilder buildWhereClause(SqlColumnMappingFactory columnMappingFactory);

    /**
     * The where clause for this query, without parameters
     */
    String getWhereClause();

    T addClause(DataFilterClause clause, DataFilterConjunction conjunction);
}
