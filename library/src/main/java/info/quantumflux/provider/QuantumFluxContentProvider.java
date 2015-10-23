package info.quantumflux.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;

import info.quantumflux.QuantumFlux;
import info.quantumflux.QuantumFluxDatabase;
import info.quantumflux.logger.QuantumFluxLog;
import info.quantumflux.model.generate.TableDetails;
import info.quantumflux.model.util.ManifestHelper;
import info.quantumflux.model.util.QuantumFluxException;
import info.quantumflux.provider.util.UriMatcherHelper;

/**
 * The base content provided that will expose all of the model objects.
 * Objects are expose in the form of authority/table_name/*
 */
public class QuantumFluxContentProvider extends ContentProvider {

    public static final String PARAMETER_OFFSET = "OFFSET";
    public static final String PARAMETER_LIMIT = "LIMIT";
    public static final String PARAMETER_SYNC = "IS_SYNC";

    private QuantumFluxDatabase mDatabase;
    private UriMatcherHelper mUriMatcherHelper;
    private boolean mDebugEnabled;

    @Override
    public boolean onCreate() {
        mDatabase = QuantumFlux.getDatabase();
        mUriMatcherHelper = new UriMatcherHelper(getContext());
        mUriMatcherHelper.init(getContext(), mDatabase.getTableDetailsCache());

        mDebugEnabled = ManifestHelper.isQueryLogEnabled(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        TableDetails tableDetails = mUriMatcherHelper.getTableDetails(uri);
        SQLiteDatabase db = mDatabase.getReadableDatabase();
        String limit = constructLimit(uri);

        if (mDebugEnabled) {
            QuantumFluxLog.d("********* Query **********");
            QuantumFluxLog.d("Uri: " + uri);
            QuantumFluxLog.d("Projection: " + Arrays.toString(projection));
            QuantumFluxLog.d("Selection: " + selection);
            QuantumFluxLog.d("Args: " + Arrays.toString(selectionArgs));
            QuantumFluxLog.d("Sort: " + sortOrder);
            QuantumFluxLog.d("Limit: " + limit);
        }

        Cursor cursor;

        if (mUriMatcherHelper.isSingleItemRequested(uri)) {
            String itemId = uri.getLastPathSegment();
            TableDetails.ColumnDetails primaryKeyColumn = tableDetails.findPrimaryKeyColumn();
            cursor = db.query(tableDetails.getTableName(), projection, primaryKeyColumn.getColumnName() + " = ?", new String[]{itemId}, null, sortOrder, limit);
        } else {
            cursor = db.query(tableDetails.getTableName(), projection, selection, selectionArgs, null, null, sortOrder, limit);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return mUriMatcherHelper.getType(uri);
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        TableDetails tableDetails = mUriMatcherHelper.getTableDetails(uri);
        SQLiteDatabase db = mDatabase.getWritableDatabase();

        if (mDebugEnabled) {
            QuantumFluxLog.d("********* Insert **********");
            QuantumFluxLog.d("Uri: " + uri);
            QuantumFluxLog.d("Content Values: " + contentValues);
        }

        long insertId = db.insertOrThrow(tableDetails.getTableName(), null, contentValues);

        if (insertId == -1) {
            throw new QuantumFluxException("Failed to insert row for into table " + tableDetails.getTableName() + " using values " + contentValues);
        }

        notifyChanges(uri, tableDetails);

        TableDetails.ColumnDetails primaryKeyColumn = tableDetails.findPrimaryKeyColumn();
        if (primaryKeyColumn.isAutoIncrement()) {
            return mUriMatcherHelper.generateSingleItemUri(tableDetails, insertId);
        } else {
            String primaryKeyValue = contentValues.getAsString(primaryKeyColumn.getColumnName());
            return mUriMatcherHelper.generateSingleItemUri(tableDetails, primaryKeyValue);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String where, String[] args) {
        TableDetails tableDetails = mUriMatcherHelper.getTableDetails(uri);
        SQLiteDatabase db = mDatabase.getWritableDatabase();

        if (mDebugEnabled) {
            QuantumFluxLog.d("********* Delete **********");
            QuantumFluxLog.d("Uri: " + uri);
            QuantumFluxLog.d("Where: " + where);
            QuantumFluxLog.d("Args: " + Arrays.toString(args));
        }

        int deleteCount;

        if (mUriMatcherHelper.isSingleItemRequested(uri)) {
            String itemId = uri.getLastPathSegment();
            TableDetails.ColumnDetails primaryKeyColumn = tableDetails.findPrimaryKeyColumn();
            deleteCount = db.delete(tableDetails.getTableName(), primaryKeyColumn.getColumnName() + " = ?", new String[]{itemId});
        } else {
            deleteCount = db.delete(tableDetails.getTableName(), where, args);
        }

        if (deleteCount == 0) {
            return deleteCount;
        }

        notifyChanges(uri, tableDetails);

        return deleteCount;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String where, String[] args) {
        TableDetails tableDetails = mUriMatcherHelper.getTableDetails(uri);
        SQLiteDatabase db = mDatabase.getWritableDatabase();

        if (mDebugEnabled) {
            QuantumFluxLog.d("********* Update **********");
            QuantumFluxLog.d("Uri: " + uri);
            QuantumFluxLog.d("Content Values: " + contentValues);
            QuantumFluxLog.d("Where: " + where);
            QuantumFluxLog.d("Args: " + Arrays.toString(args));
        }

        int updateCount;

        if (mUriMatcherHelper.isSingleItemRequested(uri)) {
            String itemId = uri.getLastPathSegment();
            TableDetails.ColumnDetails primaryKeyColumn = tableDetails.findPrimaryKeyColumn();
            updateCount = db.update(tableDetails.getTableName(), contentValues, primaryKeyColumn.getColumnName() + " = ?", new String[]{itemId});
        } else {
            updateCount = db.update(tableDetails.getTableName(), contentValues, where, args);
        }

        if (updateCount > 0 && shouldChangesBeNotified(tableDetails, contentValues)) {
            notifyChanges(uri, tableDetails);
        }

        return updateCount;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        if (values.length == 0) return 0;

        TableDetails tableDetails = mUriMatcherHelper.getTableDetails(uri);
        SQLiteDatabase db = mDatabase.getWritableDatabase();

        if (mDebugEnabled) {
            QuantumFluxLog.d("********* Bulk Insert **********");
            QuantumFluxLog.d("Uri: " + uri);
        }

        int count = 0;

        try {
            db.beginTransactionNonExclusive();
            String tableName = tableDetails.getTableName();

            for (ContentValues value : values) {
                db.insertOrThrow(tableName, null, value);

                count++;
            }
            db.setTransactionSuccessful();

            notifyChanges(uri, tableDetails);
        } finally {
            db.endTransaction();
        }
        return count;
    }

    private String constructLimit(Uri uri) {
        String offsetParam = uri.getQueryParameter(PARAMETER_OFFSET);
        String limitParam = uri.getQueryParameter(PARAMETER_LIMIT);

        Integer offset = null;
        Integer limit = null;

        if (!TextUtils.isEmpty(offsetParam) && TextUtils.isDigitsOnly(offsetParam)) {
            offset = Integer.valueOf(offsetParam);
        }
        if (!TextUtils.isEmpty(limitParam) && TextUtils.isDigitsOnly(limitParam)) {
            limit = Integer.valueOf(limitParam);
        }

        if (limit == null && offset == null) {
            return null;
        }

        StringBuilder limitStatement = new StringBuilder();

        if (limit != null && offset != null) {
            limitStatement.append(offset);
            limitStatement.append(",");
            limitStatement.append(limit);
        } else if (limit != null) {
            limitStatement.append(limit);
        } else {
            throw new QuantumFluxException("A limit must also be provided when setting an offset");
        }

        return limitStatement.toString();
    }

    private boolean shouldChangesBeNotified(TableDetails tableDetails, ContentValues contentValues) {
        boolean notify = false;

        for (String columnName : contentValues.keySet()) {
            TableDetails.ColumnDetails column = tableDetails.findColumn(columnName);
            if (column != null) {
                notify = notify || column.notifyChanges();
            }
        }

        return notify;
    }

    private void notifyChanges(Uri uri, TableDetails tableDetails) {

        Boolean sync = uri.getBooleanQueryParameter(PARAMETER_SYNC, true);
        getContext().getContentResolver().notifyChange(uri, null, sync);

        List<Class<?>> changeListeners = tableDetails.getChangeListeners();
        if (!changeListeners.isEmpty()) {

            for (int i = 0; i < changeListeners.size(); i++) {
                Class<?> changeListener = changeListeners.get(i);
                TableDetails changeListenerDetails = mDatabase.getTableDetailsCache().findTableDetails(getContext(), changeListener);

                if (changeListenerDetails == null) {
                    continue;
                }

                //Change listeners are registered on views, so the entire view needs to be updated if changes to its data occurs
                Uri changeUri = mUriMatcherHelper.generateItemUri(changeListenerDetails);
                getContext().getContentResolver().notifyChange(changeUri, null, sync);
            }
        }
    }
}
