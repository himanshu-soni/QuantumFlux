package info.quantumflux.model.loader.support;

import android.content.Context;
import android.support.v4.content.CursorLoader;

import info.quantumflux.model.generate.TableDetails;
import info.quantumflux.model.query.Select;
import info.quantumflux.model.util.QuantumFluxCursor;
import info.quantumflux.model.util.ContentResolverValues;

public class QuantumFluxLoader<T> extends CursorLoader {

    private TableDetails mTableDetails;
    private int mCacheSize = 0;

    /**
     * Creates a new mCursor loader using the select statement provided. The default implementation
     * will enable the cache of the mCursor to improve view performance.  To manually specify the
     * mCursor cache size, use the overloaded constructor.
     *
     * @param context The context that will be used to create the mCursor.
     * @param select  The select statement that will be used to retrieve the data.
     */
    public QuantumFluxLoader(Context context, Select<T> select) {
        super(context);

        ContentResolverValues resolverValues = select.asContentResolverValue();
        setUri(resolverValues.getItemUri());
        setProjection(resolverValues.getProjection());
        setSelection(resolverValues.getWhere());
        setSelectionArgs(resolverValues.getWhereArgs());
        setSortOrder(resolverValues.getSortOrder());

        mTableDetails = resolverValues.getTableDetails();
    }

    /**
     * Creates a new mCursor loader using the select statement provided. You
     * can specify the cache size to use, or use -1 to disable mCursor caching.
     *
     * @param context   The context that will be used to create the mCursor.
     * @param select    The select statement that will be used to retrieve the data.
     * @param cacheSize The cache size for the mCursor, or -1 to disable caching
     */
    public QuantumFluxLoader(Context context, Select<T> select, int cacheSize) {
        this(context, select);

        enableCursorCache(cacheSize);
    }

    public void enableCursorCache(int size) {
        mCacheSize = size;
    }

    @Override
    public QuantumFluxCursor loadInBackground() {
        QuantumFluxCursor<T> cursor = new QuantumFluxCursor<>(mTableDetails, super.loadInBackground());

        if (mCacheSize == 0) {
            cursor.enableCache();
        } else if (mCacheSize > 0) {
            cursor.enableCache(mCacheSize);
        }

        return cursor;
    }
}
