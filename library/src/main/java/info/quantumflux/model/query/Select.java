package info.quantumflux.model.query;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import info.quantumflux.QuantumFlux;
import info.quantumflux.model.generate.TableDetails;
import info.quantumflux.model.map.SqlColumnMappingFactory;
import info.quantumflux.model.util.ContentResolverValues;
import info.quantumflux.model.util.CursorIterator;
import info.quantumflux.model.util.QuantumFluxCursor;
import info.quantumflux.model.util.QuantumFluxException;
import info.quantumflux.provider.QuantumFluxContentProvider;
import info.quantumflux.provider.util.UriMatcherHelper;

/**
 * The starting point for select statements.  Contains the basic functions to do a simple select operation
 * and allows you to specify the result type you want for the query.
 */
public class Select<T> implements DataFilterClause<Select<T>> {

    private final Class<T> mDataObjectClass;
    private DataFilterCriteria mFilterCriteria;
    private List<String> mSortingOrderList;
    private List<String> mIncludedColumns;
    private List<String> mExcludedColumns;
    private Integer mOffset;
    private Integer mLimit;

    private Select(Class<T> dataObjectClass) {
        this.mDataObjectClass = dataObjectClass;
        this.mSortingOrderList = new LinkedList<>();
        this.mFilterCriteria = new DataFilterCriteria();
        this.mIncludedColumns = new ArrayList<>();
        this.mExcludedColumns = new ArrayList<>();
    }

    /**
     * The data model object that will be selected from
     *
     * @param dataObjectClass The class object
     * @param <T>             The generic type telling java the type of class
     * @return The current Select instance
     */
    public static <T> Select<T> from(Class<T> dataObjectClass) {
        return new Select<>(dataObjectClass);
    }

    /**
     * The filter clause that will be used to apply filtering, each clause will be added with ann AND conjunction
     *
     * @param filterClause the filter clause to add
     * @return The current select instance
     */
    public Select<T> where(DataFilterClause filterClause) {
        this.mFilterCriteria.addClause(filterClause);
        return this;
    }

    /**
     * The filter clause that will be used to apply filtering, adds the clause with the specified conjunction
     *
     * @param filterClause The filter clause to add
     * @param conjunction  The conjunction used to add the filter clause
     * @return The current select instance
     */
    public Select<T> where(DataFilterClause filterClause, DataFilterClause.DataFilterConjunction conjunction) {
        this.mFilterCriteria.addClause(filterClause, conjunction);
        return this;
    }

    /**
     * Convenience method that will add a equals criterion with an AND conjunction
     *
     * @param column The column to compare
     * @param value  The value to compare
     * @return The current select instance
     */
    public Select<T> whereEquals(String column, Object value) {
        addClause(new DataFilterCriterion(column, DataFilterCriterion.DataFilterOperator.EQUAL, value), DataFilterConjunction.AND);
        return this;
    }

    /**
     * Convenience method that will add a like criterion with an AND conjunction
     *
     * @param column The column to compare
     * @param value  The value to compare
     * @return The current select instance
     */
    public Select<T> whereLike(String column, Object value) {
        addClause(new DataFilterCriterion(column, DataFilterCriterion.DataFilterOperator.LIKE, value), DataFilterConjunction.AND);
        return this;
    }

    /**
     * Starts a new Criterion builder with and AND conjunction
     *
     * @return The current select instance
     */
    public DataFilterCriterion.Builder<Select<T>> and() {
        return new DataFilterCriterion.Builder<>(this, DataFilterClause.DataFilterConjunction.AND);
    }

    /**
     * Adds the specified clause with and AND conjunction
     *
     * @param clause The clause to add
     * @return The current select instance
     */
    public Select<T> and(DataFilterClause clause) {
        mFilterCriteria.addClause(clause, DataFilterConjunction.AND);
        return this;
    }

    /**
     * Starts a new Criterion builder with an OR conjunction
     *
     * @return The current select instance
     */
    public DataFilterCriterion.Builder<Select<T>> or() {
        return new DataFilterCriterion.Builder<>(this, DataFilterClause.DataFilterConjunction.OR);
    }

    /**
     * Adds the specified clause with an OR conjunction
     *
     * @param filterClause The clause to add
     * @return The current select instance
     */
    public Select<T> or(DataFilterClause filterClause) {
        mFilterCriteria.addClause(filterClause, DataFilterConjunction.OR);
        return this;
    }

    /**
     * Starts a new Criteria builder with AND conjunction
     *
     * @return The current select instance
     */
    public DataFilterCriteria.Builder<Select<T>> openBracketAnd() {
        return new DataFilterCriteria.Builder<>(this, DataFilterClause.DataFilterConjunction.AND);
    }

    /**
     * Starts a new Criteria builder with OR conjunction
     *
     * @return The current select instance
     */
    public DataFilterCriteria.Builder<Select<T>> openBracketOr() {
        return new DataFilterCriteria.Builder<>(this, DataFilterClause.DataFilterConjunction.OR);
    }

    /**
     * Sorts the specified columns in DESC order.  The order here is important, as the sorting will be done in the same order the columns are added.
     *
     * @param columns the columns to sort
     * @return The current select instance
     */
    public Select<T> sortDesc(String... columns) {
        for (String column : columns) {
            mSortingOrderList.add(column + " DESC");
        }
        return this;
    }

    /**
     * Sorts the specified columns in ASC order.  The order here is important, as the sorting will be done in the same order the columns are added.
     *
     * @param columns the columns to sort
     * @return The current select instance
     */
    public Select<T> sortAsc(String... columns) {
        for (String column : columns) {
            mSortingOrderList.add(column + " ASC");
        }
        return this;
    }

    /**
     * Sets the mOffset of rows from which the select will start executing
     *
     * @param offset the row mOffset
     * @return The current select instance
     */
    public Select<T> offset(int offset) {
        if (offset < 0) {
            throw new QuantumFluxException("Offset must be larger than 0");
        }
        this.mOffset = offset;
        return this;
    }

    /**
     * Sets the mLimit of how many rows will be selected
     *
     * @param limit Amount of rows
     * @return The current select statement
     */
    public Select<T> limit(int limit) {
        if (limit < 1) {
            throw new QuantumFluxException("Limit must be larger than 0");
        }
        this.mLimit = limit;
        return this;
    }

    /**
     * Columns to retrieve, if not specified all columns will be retrieved.  Remember, the inflated object will only contain valid values for the selected columns.
     * If this is specified the excluded columns will be ignored
     *
     * @param columns The columns to retrieve
     * @return The current select instance
     */
    public Select<T> include(String... columns) {
        Collections.addAll(mIncludedColumns, columns);
        return this;
    }

    /**
     * Columns not to retrieve.  If not specified all columns will be retrieved.  Remember, the inflated object will not contain valid values for the specified columns.
     * If the include was specified as part of the select, excluded columns are ignored.
     *
     * @param columns The columns to exclude
     * @return The current select instance
     */
    public Select<T> exclude(String... columns) {
        Collections.addAll(mExcludedColumns, columns);
        return this;
    }

    /**
     * Executes the query and returns the results as a cursor. The {@link QuantumFluxCursor} is a wrapper for the normal cursor,
     * and in addition to providing the normal cursor functionality, it also has methods to manipulate model objects, such as inflating the current cursor
     * values to a model object.
     *
     * @return The {@link QuantumFluxCursor} containing the results
     */
    public QuantumFluxCursor<T> queryAsCursor() {
        ContentResolverValues contentResolverValues = asContentResolverValue();
        ContentResolver contentResolver = QuantumFlux.getApplicationContext().getContentResolver();
        Cursor cursor = contentResolver.query(contentResolverValues.getItemUri(),
                contentResolverValues.getProjection(),
                contentResolverValues.getWhere(),
                contentResolverValues.getWhereArgs(),
                contentResolverValues.getSortOrder());

        return new QuantumFluxCursor<>(contentResolverValues.getTableDetails(), cursor);
    }

    /**
     * Does the same as query cursor, but wraps the cursor in an iterator, and inflates the objects automatically. This iterator will close the cursor
     * once all of the objects have been retrieved, so it is important to always iterate over all the objects so the cursor can close. If an exception
     * is thrown that will prevent this iterator from completing, then please close it manually.
     *
     * @return The iterator containing the results
     */
    public CursorIterator<T> queryAsIterator() {
        QuantumFluxCursor<T> cursor = queryAsCursor();
        return new CursorIterator<>(cursor.getTableDetails(), cursor);
    }


    /**
     * Does the same as the query cursor, but packs all of the cursor items into a list, once the list is populated, the cursor will be closed.
     *
     * @return The list containing the results
     */
    public List<T> queryAsList() {
        QuantumFluxCursor<T> cursor = queryAsCursor();

        try {
            List<T> resultList = new ArrayList<>(cursor.getCount());
            while (cursor.moveToNext()) {
                resultList.add(cursor.inflate());
            }
            return resultList;
        } finally {
            cursor.close();
        }
    }


    /**
     * Executes the query as a cursor, and then retrieves the row count from the cursor, the cursor is then closed and the count returned.
     *
     * @return The count indicating the amount of results for this select
     */
    public int queryAsCount() {
        QuantumFluxCursor<T> cursor = queryAsCursor();

        List<String> includedColumnsTemp = new ArrayList<>();
        List<String> excludedColumnsTemp = new ArrayList<>();
        Collections.copy(includedColumnsTemp, mIncludedColumns);
        Collections.copy(excludedColumnsTemp, mExcludedColumns);

        mIncludedColumns.clear();
        mExcludedColumns.clear();

        String columnName = QuantumFlux.findTableDetails(mDataObjectClass).findPrimaryKeyColumn().getColumnName();
        mIncludedColumns.add(columnName);
        try {
            return cursor.getCount();
        } finally {
            cursor.close();
            mIncludedColumns.clear();

            //Restore the previous includes and excludes
            Collections.copy(mIncludedColumns, mIncludedColumns);
            Collections.copy(excludedColumnsTemp, mExcludedColumns);
        }

    }


    /**
     * Packages this select into a {@link ContentResolverValues} package, this will contain all of the required arguments to run this query on
     * a content resolver, it is used internally by all of the as* methods.
     *
     * @return The {@link ContentResolverValues} containing the arguments needed by the content resolver query method
     */
    public ContentResolverValues asContentResolverValue() {

        TableDetails tableDetails = QuantumFlux.findTableDetails(mDataObjectClass);

        QueryBuilder where = buildWhereClause(QuantumFlux.getColumnMappingFactory());
        QueryBuilder sort = buildSort();

        Uri.Builder itemUri = UriMatcherHelper.generateItemUriBuilder(tableDetails);

        if (mOffset != null)
            itemUri.appendQueryParameter(QuantumFluxContentProvider.PARAMETER_OFFSET, mOffset.toString());
        if (mLimit != null)
            itemUri.appendQueryParameter(QuantumFluxContentProvider.PARAMETER_LIMIT, mLimit.toString());

        return new ContentResolverValues(tableDetails, itemUri.build(), getProjection(tableDetails), where.getQueryString(), where.getQueryArgsAsArray(), sort.getQueryString());
    }


    /**
     * Gets the results as a cursor, and returns the first item it finds.  The cursor is closed before the item is returned.  If now item is found mathing the query
     * null is returned instead.
     *
     * @return The first result if found, null otherwise.
     */
    public T first() {

        Integer currentLimit = mLimit;
        limit(1); //Add a default mLimit for the user
        QuantumFluxCursor<T> cursor = queryAsCursor();
        try {
            if (cursor.moveToNext()) {
                return cursor.inflate();
            } else return null;
        } finally {
            cursor.close();
            //Restore the previous mLimit
            mLimit = currentLimit;
        }
    }

    /**
     * Same as first, but this queries the last item in the cursor.
     *
     * @return Tha last item found in the cursor, null otherwise.
     */
    public T last() {
        QuantumFluxCursor<T> cursor = queryAsCursor();
        try {
            if (cursor.moveToLast()) {
                return cursor.inflate();
            } else return null;
        } finally {
            cursor.close();
        }
    }

    /**
     * Creates the projection based on the users inclusion, exclusion criteria.  If none is specified, all columns will be returned.
     *
     * @param tableDetails The table details object containing the column information
     * @return String[] containing the columns values to be queried
     */
    private String[] getProjection(TableDetails tableDetails) {
        if (!mIncludedColumns.isEmpty()) {

            return mIncludedColumns.toArray(new String[mIncludedColumns.size()]);
        } else if (!mExcludedColumns.isEmpty()) {

            List<String> columns = new ArrayList<String>();

            for (String column : tableDetails.getColumnNames()) {

                if (!mExcludedColumns.contains(column))
                    columns.add(column);
            }

            return columns.toArray(new String[columns.size()]);
        } else return tableDetails.getColumnNames();
    }

    /**
     * Builds the sort query
     *
     * @return {@link QueryBuilder} containing the sort information
     */
    private QueryBuilder buildSort() {

        QueryBuilder builder = new QueryBuilder();
        Iterator<String> sortIterator = mSortingOrderList.iterator();

        while (sortIterator.hasNext()) {

            builder.append(sortIterator.next());

            if (sortIterator.hasNext()) builder.append(", ");
        }

        return builder;
    }

    protected QueryBuilder getSelectQuery() {
        TableDetails tableDetails = QuantumFlux.findTableDetails(mDataObjectClass);

        QueryBuilder select = new QueryBuilder();
        QueryBuilder where = buildWhereClause(QuantumFlux.getColumnMappingFactory());

        select.append("SELECT ");

        Iterator<String> columnIterator = Arrays.asList(getProjection(tableDetails)).iterator();
        while (columnIterator.hasNext()) {
            select.append(columnIterator.next());
            if (columnIterator.hasNext()) select.append(", ");
        }
        select.append(" FROM ");
        select.append(tableDetails.getTableName());

        if (hasFilterValue()) {
            select.append(" WHERE ");
            select.append(where);
        }

        return select;
    }

    protected boolean isSingleColumnProjection() {
        return mIncludedColumns.size() == 1;
    }

    /**
     * The where clause for this query
     */
    @Override
    public QueryBuilder buildWhereClause(SqlColumnMappingFactory columnMappingFactory) {
        return mFilterCriteria.buildWhereClause(columnMappingFactory);
    }

    @Override
    public String getWhereClause() {
        return mFilterCriteria.getWhereClause();
    }

    @Override
    public Select<T> addClause(DataFilterClause clause, DataFilterConjunction conjunction) {
        this.mFilterCriteria.addClause(clause, conjunction);
        return this;
    }

    @Override
    public boolean hasFilterValue() {
        return mFilterCriteria.hasFilterValue();
    }

    @Override
    public String toString() {
        return getSelectQuery().toString();
    }
}
