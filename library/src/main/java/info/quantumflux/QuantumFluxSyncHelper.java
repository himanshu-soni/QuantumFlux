package info.quantumflux;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;

import info.quantumflux.model.generate.TableDetails;
import info.quantumflux.model.util.ModelInflater;
import info.quantumflux.provider.QuantumFluxContentProvider;
import info.quantumflux.provider.util.UriMatcherHelper;

/**
 * Created by Himanshu on 8/4/2015.
 */
public class QuantumFluxSyncHelper {
    public static <T> void insert(ContentProviderClient provider, T... dataModelObjects) throws RemoteException {

        if (dataModelObjects.length == 1) {
            T modelObject = dataModelObjects[0];
            TableDetails tableDetails = QuantumFlux.findTableDetails(modelObject.getClass());
            ContentValues contentValues = ModelInflater.deflate(tableDetails, modelObject);
            Uri insertUri = UriMatcherHelper.generateItemUriBuilder(tableDetails)
                    .appendQueryParameter(QuantumFluxContentProvider.PARAMETER_SYNC, "false").build();

            provider.insert(insertUri, contentValues);
        } else {
            ContentValues[] insertObjects = new ContentValues[dataModelObjects.length];
            TableDetails tableDetails = QuantumFlux.findTableDetails(dataModelObjects[0].getClass());

            for (int i = 0; i < dataModelObjects.length; i++) {
                T modelObject = dataModelObjects[i];
                insertObjects[i] = ModelInflater.deflate(tableDetails, modelObject);
            }

            if (tableDetails != null) {
                Uri insertUri = UriMatcherHelper.generateItemUriBuilder(tableDetails)
                        .appendQueryParameter(QuantumFluxContentProvider.PARAMETER_SYNC, "false").build();
                provider.bulkInsert(insertUri, insertObjects);
            }
        }
    }

    public static <T> T insertAndReturn(ContentProviderClient provider, T dataModelObject) throws RemoteException {
        TableDetails tableDetails = QuantumFlux.findTableDetails(dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Uri insertUri = UriMatcherHelper.generateItemUriBuilder(tableDetails)
                .appendQueryParameter(QuantumFluxContentProvider.PARAMETER_SYNC, "false").build();

        Uri itemUri = provider.insert(insertUri, contentValues);

        return QuantumFlux.findSingleItem(itemUri, tableDetails);
    }

    public static <T> void update(Context context, ContentProviderClient provider, T dataModelObject) throws RemoteException {
        TableDetails tableDetails = QuantumFlux.findTableDetails(dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Object columnValue = ModelInflater.deflateColumn(tableDetails, tableDetails.findPrimaryKeyColumn(), dataModelObject);
        Uri itemUri = UriMatcherHelper.generateItemUriBuilder(tableDetails, String.valueOf(columnValue))
                .appendQueryParameter(QuantumFluxContentProvider.PARAMETER_SYNC, "false").build();

        provider.update(itemUri, contentValues, null, null);
    }

    public static <T> void updateColumns(Context context, ContentProviderClient provider, T dataModelObject, String... columns) throws RemoteException {
        TableDetails tableDetails = QuantumFlux.findTableDetails(dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Object columnValue = ModelInflater.deflateColumn(tableDetails, tableDetails.findPrimaryKeyColumn(), dataModelObject);
        Uri itemUri = UriMatcherHelper.generateItemUriBuilder(tableDetails, String.valueOf(columnValue))
                .appendQueryParameter(QuantumFluxContentProvider.PARAMETER_SYNC, "false").build();

        for (String contentColumn : tableDetails.getColumnNames()) {

            boolean includeColumn = false;
            for (String column : columns) {
                if (contentColumn.equals(column)) {
                    includeColumn = true;
                    break;
                }
            }

            if (!includeColumn)
                contentValues.remove(contentColumn);
        }

        provider.update(itemUri, contentValues, null, null);
    }

    public static <T> void updateColumnsExcluding(Context context, ContentProviderClient provider, T dataModelObject, String... columnsToExclude) throws RemoteException {
        TableDetails tableDetails = QuantumFlux.findTableDetails(dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Object columnValue = ModelInflater.deflateColumn(tableDetails, tableDetails.findPrimaryKeyColumn(), dataModelObject);
        Uri itemUri = UriMatcherHelper.generateItemUriBuilder(tableDetails, String.valueOf(columnValue))
                .appendQueryParameter(QuantumFluxContentProvider.PARAMETER_SYNC, "false").build();

        for (String columnToExclude : columnsToExclude) {

            contentValues.remove(columnToExclude);
        }

        provider.update(itemUri, contentValues, null, null);
    }

    public static <T> void delete(Context context, ContentProviderClient provider, T dataModelObject) throws RemoteException {
        TableDetails tableDetails = QuantumFlux.findTableDetails(dataModelObject.getClass());
        Object columnValue = ModelInflater.deflateColumn(tableDetails, tableDetails.findPrimaryKeyColumn(), dataModelObject);
        Uri itemUri = UriMatcherHelper.generateItemUriBuilder(tableDetails, String.valueOf(columnValue))
                .appendQueryParameter(QuantumFluxContentProvider.PARAMETER_SYNC, "false").build();

        provider.delete(itemUri, null, null);
    }
}
