package info.quantumflux;

import android.app.Application;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import info.quantumflux.model.generate.TableDetails;
import info.quantumflux.model.map.SqlColumnMapping;
import info.quantumflux.model.map.SqlColumnMappingFactory;
import info.quantumflux.model.query.Select;
import info.quantumflux.model.util.ContentResolverValues;
import info.quantumflux.model.util.CursorIterator;
import info.quantumflux.model.util.ManifestHelper;
import info.quantumflux.model.util.ModelInflater;
import info.quantumflux.model.util.QuantumFluxException;
import info.quantumflux.model.util.TableDetailsCache;
import info.quantumflux.provider.util.UriMatcherHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Himanshu on 8/4/2015.
 */
public class QuantumFlux {
    private static Context mApplicationContext;
    private static TableDetailsCache mTableDetailsCache;
    private static SqlColumnMappingFactory mMappingFactory;
    private static QuantumFluxDatabase mDatabase;

    /**
     * This is an necessary initialize method that will be used to set the application context
     * and to setup some initial settings
     *
     * @param app The application that will be using the orm.
     */
    public static void initialize(Application app) {
        initialize(app, null);
    }


    /**
     * This is an necessary initialize method that will be used to set the application context
     * and to setup some initial settings for custom column mappings
     *
     * @param app            The application that will be using the orm.
     * @param customMappings {@link java.util.List} of {@link SqlColumnMapping}
     *                       to use additional mappings
     */
    public static void initialize(Application app, List<SqlColumnMapping> customMappings) {
        mApplicationContext = app;
        mMappingFactory = new SqlColumnMappingFactory();
        if (customMappings != null) {
            for (SqlColumnMapping mapping : customMappings) {
                mMappingFactory.addColumnMapping(mapping);
            }
        }

        mDatabase = new QuantumFluxDatabase(app);
    }

    public static SqlColumnMappingFactory getColumnMappingFactory() {
        if (mMappingFactory == null) {
            throw new QuantumFluxException("You must call initialize() before using this method.");
        }
        return mMappingFactory;
    }

    public static QuantumFluxDatabase getDatabase() {
        return mDatabase;
    }

    /**
     * Gets the initialized application context that can be used to perform querying.
     *
     * @return The application context if available, throws an Illegal Argument Exception if no initialization has been done.
     */
    public static Context getApplicationContext() {
        if (mApplicationContext == null) {
            throw new QuantumFluxException("You must call initialize() before using this method.");
        }
        return mApplicationContext;
    }

    public static <T> long countAll(Class<T> dataModel) {
        return Select.from(dataModel).queryAsCount();
    }

    public static <T> Iterator<T> findAll(Class<T> dataModel) {
        TableDetails tableDetails = findTableDetails(dataModel);
        Uri itemUri = UriMatcherHelper.generateItemUriBuilder(tableDetails).build();
        ContentResolver contentResolver = mApplicationContext.getContentResolver();
        Cursor cursor = contentResolver.query(itemUri, null, null, null, null);

        return new CursorIterator<T>(tableDetails, cursor);
    }

    public static <T> T findByPrimaryKey(Class<T> dataModel, Object key) {
        TableDetails tableDetails = findTableDetails(dataModel);
        TableDetails.ColumnDetails primaryKeyColumn = tableDetails.findPrimaryKeyColumn();
        Object columnValue = primaryKeyColumn.getColumnTypeMapping().toSqlType(key);
        Uri itemUri = UriMatcherHelper.generateItemUriBuilder(tableDetails, String.valueOf(columnValue)).build();

        return findSingleItem(itemUri, tableDetails);
    }


    public static <T> int insertAll(List<T> dataModelObjects) {
        if (dataModelObjects == null || dataModelObjects.isEmpty())
            return 0;

        TableDetails tableDetails = findTableDetails(dataModelObjects.get(0).getClass());
        Uri insertUri = UriMatcherHelper.generateItemUriBuilder(tableDetails).build();

        ContentValues[] values = new ContentValues[dataModelObjects.size()];
        for (int i = 0; i < dataModelObjects.size(); i++) {
            values[i] = ModelInflater.deflate(tableDetails, dataModelObjects.get(i));
        }

        ContentResolver contentResolver = mApplicationContext.getContentResolver();
        return contentResolver.bulkInsert(insertUri, values);
    }

    public static <T> int insertAll(ContentProviderClient providerClient, List<T> dataModelObjects) throws RemoteException {
        if (dataModelObjects == null || dataModelObjects.isEmpty())
            return 0;

        TableDetails tableDetails = findTableDetails(dataModelObjects.get(0).getClass());
        Uri insertUri = UriMatcherHelper.generateItemUriBuilder(tableDetails).build();

        ContentValues[] values = new ContentValues[dataModelObjects.size()];
        for (int i = 0; i < dataModelObjects.size(); i++) {
            values[i] = ModelInflater.deflate(tableDetails, dataModelObjects.get(i));
        }

        return providerClient.bulkInsert(insertUri, values);
    }


    public static <T> void insert(T dataModelObject) {
        TableDetails tableDetails = findTableDetails(dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Uri insertUri = UriMatcherHelper.generateItemUriBuilder(tableDetails).build();

        ContentResolver contentResolver = mApplicationContext.getContentResolver();
        contentResolver.insert(insertUri, contentValues);
    }


    public static <T> T insertAndReturn(T dataModelObject) {
        TableDetails tableDetails = findTableDetails(dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Uri insertUri = UriMatcherHelper.generateItemUriBuilder(tableDetails).build();

        ContentResolver contentResolver = mApplicationContext.getContentResolver();
        Uri itemUri = contentResolver.insert(insertUri, contentValues);

        return findSingleItem(itemUri, tableDetails);
    }


    public static <T> ContentProviderOperation prepareInsert(T dataModelObject) {
        TableDetails tableDetails = findTableDetails(dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Uri insertUri = UriMatcherHelper.generateItemUriBuilder(tableDetails).build();

        return ContentProviderOperation.newInsert(insertUri)
                .withExpectedCount(1)
                .withValues(contentValues)
                .build();
    }


    public static <T> void update(T dataModelObject) {
        TableDetails tableDetails = findTableDetails(dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Object columnValue = ModelInflater.deflateColumn(tableDetails, tableDetails.findPrimaryKeyColumn(), dataModelObject);
        Uri itemUri = UriMatcherHelper.generateItemUriBuilder(tableDetails, String.valueOf(columnValue)).build();

        ContentResolver contentResolver = mApplicationContext.getContentResolver();
        contentResolver.update(itemUri, contentValues, null, null);
    }

    public static <T> void updateColumns(T dataModelObject, String... columns) {
        TableDetails tableDetails = findTableDetails(dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Object columnValue = ModelInflater.deflateColumn(tableDetails, tableDetails.findPrimaryKeyColumn(), dataModelObject);
        Uri itemUri = UriMatcherHelper.generateItemUriBuilder(tableDetails, String.valueOf(columnValue)).build();

        for (String contentColumn : tableDetails.getColumnNames()) {
            boolean includeColumn = false;
            for (String column : columns) {
                if (contentColumn.equals(column)) {
                    includeColumn = true;
                    break;
                }
            }

            if (!includeColumn) {
                contentValues.remove(contentColumn);
            }
        }

        ContentResolver contentResolver = mApplicationContext.getContentResolver();
        contentResolver.update(itemUri, contentValues, null, null);
    }

    public static <T> void updateColumnsExcluding(T dataModelObject, String... columnsToExclude) {
        TableDetails tableDetails = findTableDetails(dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Object columnValue = ModelInflater.deflateColumn(tableDetails, tableDetails.findPrimaryKeyColumn(), dataModelObject);
        Uri itemUri = UriMatcherHelper.generateItemUriBuilder(tableDetails, String.valueOf(columnValue)).build();

        for (String columnToExclude : columnsToExclude) {
            contentValues.remove(columnToExclude);
        }

        ContentResolver contentResolver = mApplicationContext.getContentResolver();
        contentResolver.update(itemUri, contentValues, null, null);
    }


    public static <T> ContentProviderOperation prepareUpdate(T dataModelObject) {
        TableDetails tableDetails = findTableDetails(dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Object columnValue = ModelInflater.deflateColumn(tableDetails, tableDetails.findPrimaryKeyColumn(), dataModelObject);
        Uri itemUri = UriMatcherHelper.generateItemUriBuilder(tableDetails, String.valueOf(columnValue)).build();

        return ContentProviderOperation.newUpdate(itemUri)
                .withExpectedCount(1)
                .withValues(contentValues)
                .build();
    }


    public static <T> void delete(T dataModelObject) {
        TableDetails tableDetails = findTableDetails(dataModelObject.getClass());
        Object columnValue = ModelInflater.deflateColumn(tableDetails, tableDetails.findPrimaryKeyColumn(), dataModelObject);
        Uri itemUri = UriMatcherHelper.generateItemUriBuilder(tableDetails, String.valueOf(columnValue)).build();

        ContentResolver contentResolver = mApplicationContext.getContentResolver();
        contentResolver.delete(itemUri, null, null);
    }

    public static <T> void delete(Select<T> select) {
        ContentResolverValues contentResolverValues = select.asContentResolverValue();

        ContentResolver contentResolver = mApplicationContext.getContentResolver();
        contentResolver.delete(contentResolverValues.getItemUri(), contentResolverValues.getWhere(), contentResolverValues.getWhereArgs());
    }

    public static <T> ContentProviderOperation prepareDelete(T dataModelObject) {
        return prepareDelete(getApplicationContext(), dataModelObject);
    }

    public static <T> ContentProviderOperation prepareDelete(Context context, T dataModelObject) {
        TableDetails tableDetails = findTableDetails(dataModelObject.getClass());
        Object columnValue = ModelInflater.deflateColumn(tableDetails, tableDetails.findPrimaryKeyColumn(), dataModelObject);
        Uri itemUri = UriMatcherHelper.generateItemUriBuilder(tableDetails, String.valueOf(columnValue)).build();

        return ContentProviderOperation.newDelete(itemUri)
                .withExpectedCount(1)
                .build();
    }

    public static <T> ContentProviderOperation prepareDelete(Select<T> select) {
        return prepareDelete(getApplicationContext(), select);
    }

    public static <T> ContentProviderOperation prepareDelete(Context context, Select<T> select) {
        ContentResolverValues contentResolverValues = select.asContentResolverValue();

        return ContentProviderOperation.newDelete(contentResolverValues.getItemUri())
                .withSelection(contentResolverValues.getWhere(), contentResolverValues.getWhereArgs())
                .build();
    }

    public static <T> void deleteAll(Class<T> dataModel) {
        deleteAll(getApplicationContext(), dataModel);
    }

    public static <T> void deleteAll(Context context, Class<T> dataModel) {
        TableDetails tableDetails = findTableDetails(dataModel);
        Uri itemUri = UriMatcherHelper.generateItemUriBuilder(tableDetails).build();

        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(itemUri, null, null);
    }

    public static ContentProviderResult[] applyPreparedOperations(Collection<ContentProviderOperation> operations) throws RemoteException, OperationApplicationException {
        return applyPreparedOperations(getApplicationContext(), operations);
    }

    public static ContentProviderResult[] applyPreparedOperations(Context context, Collection<ContentProviderOperation> operations) throws RemoteException, OperationApplicationException {
        return context
                .getContentResolver()
                .applyBatch(ManifestHelper.getAuthority(context), new ArrayList<ContentProviderOperation>(operations));
    }

    public static <T> Uri getItemUri(Class<T> dataModel) {
        return getItemUri(getApplicationContext(), dataModel);
    }

    public static <T> Uri getItemUri(Context context, Class<T> dataModel) {
        TableDetails tableDetails = findTableDetails(dataModel);
        return UriMatcherHelper.generateItemUriBuilder(tableDetails).build();
    }


    protected static <T> T findSingleItem(Uri itemUri, TableDetails tableDetails) {
        ContentResolver contentResolver = mApplicationContext.getContentResolver();

        Cursor cursor = null;
        try {
            cursor = contentResolver.query(itemUri, tableDetails.getColumnNames(), null, null, null);

            if (cursor.moveToFirst()) {
                return ModelInflater.inflate(cursor, tableDetails);
            } else {
                throw new QuantumFluxException("No row found with the key " + itemUri.getLastPathSegment());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public static synchronized TableDetails findTableDetails(Class<?> item) {
        if (mTableDetailsCache == null) {
            mTableDetailsCache = new TableDetailsCache();
        }
        return mTableDetailsCache.findTableDetails(mApplicationContext, item);
    }
}
