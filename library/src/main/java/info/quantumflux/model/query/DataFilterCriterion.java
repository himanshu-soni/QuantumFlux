package info.quantumflux.model.query;

import android.content.Context;
import android.text.TextUtils;

import info.quantumflux.model.map.SqlColumnMappingFactory;
import info.quantumflux.model.util.QuantumFluxException;

import java.util.Collection;
import java.util.Iterator;

/**
 * A filter mCriterion is single SQL condition.  The filter column, operator, and filter value must be supplied
 * for this mCriterion to be valid. Filter values are automatically converted to the correct sql format, and
 * the sql % are automatically added in the correct locations based on the operator used.
 */
public class DataFilterCriterion implements DataFilterClause<DataFilterCriterion> {

    public String mFilterColumn;
    public DataFilterOperator mFilterOperator;
    public Object mFilterValue;

    /**
     * Creates a new instance, using the context to determine the conversion for arguments to sql friendly format
     */
    private DataFilterCriterion() {
    }

    /**
     * Creates a new instance, using the context to determine the conversion for arguments to sql friendly format.
     * The additional arguments can be used to set the values for the mCriterion.
     *
     * @param filterColumn The column to query compare.
     * @param operator     The operator used for comparison.
     */
    public DataFilterCriterion(String filterColumn, DataFilterOperator operator, Object filterValue) {
        this.mFilterColumn = filterColumn;
        this.mFilterOperator = operator;
        this.mFilterValue = filterValue;
    }

    @Override
    public QueryBuilder buildWhereClause(SqlColumnMappingFactory columnMappingFactory) {
        QueryBuilder builder = new QueryBuilder();
        builder.append(mFilterColumn);
        builder.append(" ");
        builder.append(mFilterOperator.getSqlRepresentation());

        if (mFilterValue != null) {
            if (mFilterValue instanceof Collection) {
                Iterator collectionIterator = ((Collection) mFilterValue).iterator();
                builder.append(" (");

                while (collectionIterator.hasNext()) {
                    builder.append("?", convertToSQLFormat(columnMappingFactory, collectionIterator.next()));
                    if (collectionIterator.hasNext()) builder.append(", ");
                }
                builder.append(")");
            } else if (mFilterValue instanceof Select) {
                Select innerSelect = (Select) mFilterValue;
                if (!innerSelect.isSingleColumnProjection())
                    throw new QuantumFluxException("Inner select can only contain a single column selection");

                builder.append(" (");
                builder.append((innerSelect).getSelectQuery());
                builder.append(")");
            } else builder.append(" ?", convertToSQLFormat(columnMappingFactory, mFilterValue));
        }

        return builder;
    }

    @Override
    public String getWhereClause() {
        QueryBuilder builder = new QueryBuilder();
        builder.append(mFilterColumn);
        builder.append(" ");
        builder.append(mFilterOperator.getSqlRepresentation());

        if (mFilterValue != null) {
            if (mFilterValue instanceof Collection) {
                Iterator collectionIterator = ((Collection) mFilterValue).iterator();
                builder.append(" (");

                while (collectionIterator.hasNext()) {
                    builder.append("?");
                    if (collectionIterator.hasNext()) builder.append(", ");
                }
                builder.append(")");

            } else if (mFilterValue instanceof Select) {
                Select innerSelect = (Select) mFilterValue;
                if (!innerSelect.isSingleColumnProjection())
                    throw new QuantumFluxException("Inner select can only contain a single column selection");

                builder.append(" (");
                builder.append((innerSelect).getSelectQuery());
                builder.append(")");
            } else builder.append(" ?");
        }

        return builder.toString();
    }

    @Override
    public boolean hasFilterValue() {
        return mFilterColumn != null && mFilterOperator != null;
    }

    @Override
    public DataFilterCriterion addClause(DataFilterClause clause, DataFilterConjunction conjunction) {
        throw new QuantumFluxException("Clauses cannot be added to a data filter mCriterion");
    }

    private Object convertToSQLFormat(SqlColumnMappingFactory columnMappingFactory, Object object) {
        if (mFilterOperator == DataFilterOperator.LIKE || mFilterOperator == DataFilterOperator.NOT_LIKE)
            return "%" + object + "%";
        else if (mFilterOperator == DataFilterOperator.BEGINS_WITH) return object + "%";
        else if (mFilterOperator == DataFilterOperator.ENDS_WITH) return "%" + object;
        else return columnMappingFactory.findColumnMapping(object.getClass()).toSqlType(object);
    }

    private void validate() {
        if (TextUtils.isEmpty(mFilterColumn))
            throw new QuantumFluxException("Filter column is empty");
        if (mFilterOperator == null)
            throw new QuantumFluxException("Filter operator not specified");
        if (mFilterValue == null && mFilterOperator != DataFilterOperator.IS_NULL && mFilterOperator != DataFilterOperator.IS_NOT_NULL)
            throw new QuantumFluxException("Filter value must be supplied with this operator");
    }

    private void setFilterColumn(String filterColumn) {
        this.mFilterColumn = filterColumn;
    }

    private void setFilterOperator(DataFilterOperator filterOperator) {
        this.mFilterOperator = filterOperator;
    }

    private void setFilterValue(Object filterValue) {
        this.mFilterValue = filterValue;
    }

    public static class Builder<T extends DataFilterClause<T>> {

        private final T mOriginator;
        private final DataFilterConjunction mConjunction;
        private final DataFilterCriterion mCriterion;

        protected Builder(T originator, DataFilterConjunction conjunction) {
            this.mOriginator = originator;
            this.mConjunction = conjunction;
            mCriterion = new DataFilterCriterion();
        }

        public T equal(String column, Object value) {
            column(column);
            mCriterion.setFilterOperator(DataFilterOperator.EQUAL);
            return value(value);
        }

        public T notEqual(String column, Object value) {
            column(column);
            mCriterion.setFilterOperator(DataFilterOperator.NOT_EQUAL);
            return value(value);
        }

        public T greaterOrEqual(String column, Object value) {
            column(column);
            mCriterion.setFilterOperator(DataFilterOperator.GREATER_OR_EQUAL);
            return value(value);
        }

        public T smallerOrEqual(String column, Object value) {
            column(column);
            mCriterion.setFilterOperator(DataFilterOperator.SMALLER_OR_EQUAL);
            return value(value);
        }

        public T greaterThan(String column, Object value) {
            column(column);
            mCriterion.setFilterOperator(DataFilterOperator.GREATER_THAN);
            return value(value);
        }

        public T smallerThan(String column, Object value) {
            column(column);
            mCriterion.setFilterOperator(DataFilterOperator.SMALLER_THAN);
            return value(value);
        }

        public T like(String column, Object value) {
            column(column);
            mCriterion.setFilterOperator(DataFilterOperator.LIKE);
            return value(value);
        }

        public T notLike(String column, Object value) {
            column(column);
            mCriterion.setFilterOperator(DataFilterOperator.NOT_LIKE);
            return value(value);
        }

        public T in(String column, Collection value) {
            column(column);
            mCriterion.setFilterOperator(DataFilterOperator.IN);
            return value(value);
        }

        public T in(String column, Select value) {
            column(column);
            mCriterion.setFilterOperator(DataFilterOperator.IN);
            return value(value);
        }

        public T notIn(String column, Collection value) {
            column(column);
            mCriterion.setFilterOperator(DataFilterOperator.NOT_IN);
            return value(value);
        }

        public T notIn(String column, Select value) {
            column(column);
            mCriterion.setFilterOperator(DataFilterOperator.NOT_IN);
            return value(value);
        }

        public T isNull(String column) {
            column(column);
            mCriterion.setFilterOperator(DataFilterOperator.IS_NULL);
            return build();
        }

        public T isNotNull(String column) {
            column(column);
            mCriterion.setFilterOperator(DataFilterOperator.IS_NOT_NULL);
            return build();
        }

        private Builder<T> column(String column) {
            mCriterion.setFilterColumn(column);
            return this;
        }

        private T value(Object value) {
            mCriterion.setFilterValue(value);
            return build();
        }

        private T build() {
            mCriterion.validate();
            mOriginator.addClause(mCriterion, mConjunction);
            return mOriginator;
        }
    }

    public enum DataFilterOperator {
        EQUAL("="),
        NOT_EQUAL("<>"),
        GREATER_OR_EQUAL(">="),
        SMALLER_OR_EQUAL("<="),
        GREATER_THAN(">"),
        SMALLER_THAN("<"),
        LIKE("LIKE"),
        NOT_LIKE("NOT LIKE"),
        IN("IN"),
        NOT_IN("NOT IN"),
        IS_NULL("IS NULL"),
        IS_NOT_NULL("IS NOT NULL"),
        BEGINS_WITH("LIKE"),
        ENDS_WITH("LIKE");

        private final String sqlRepresentation;

        DataFilterOperator(String sqlRepresentation) {
            this.sqlRepresentation = sqlRepresentation;
        }

        public String getSqlRepresentation() {
            return sqlRepresentation;
        }
    }
}
