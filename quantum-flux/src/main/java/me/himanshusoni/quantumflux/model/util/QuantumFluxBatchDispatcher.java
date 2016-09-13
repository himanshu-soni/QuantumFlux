package me.himanshusoni.quantumflux.model.util;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;

import java.util.ArrayList;

import me.himanshusoni.quantumflux.QuantumFlux;
import me.himanshusoni.quantumflux.QuantumFluxSyncHelper;
import me.himanshusoni.quantumflux.model.generate.TableDetails;

public class QuantumFluxBatchDispatcher<T> extends ArrayList<T> {

    private final Context mContext;
    private final Class<? extends T> mInsertObject;
    private final int mDispatchSize;
    private final Uri mUri;
    private final TableDetails mTableDetails;

    private ContentProviderClient mContentProviderClient;
    private boolean isSync;
    private boolean mReleaseProvider;

    public QuantumFluxBatchDispatcher(Context context, Class<? extends T> insertObject, int dispatchSize) {
        this.mContext = context;
        this.mInsertObject = insertObject;
        this.mDispatchSize = dispatchSize;
        this.mUri = QuantumFlux.getItemUri(insertObject);
        this.mTableDetails = QuantumFlux.findTableDetails(insertObject);

        ensureCapacity(dispatchSize);
    }

    public QuantumFluxBatchDispatcher(Context context, ContentProviderClient provider, Class<? extends T> insertObject, boolean isSync, int dispatchSize) {
        this(context, insertObject, dispatchSize);
        this.mContentProviderClient = provider;
        this.isSync = isSync;
        mReleaseProvider = false;
    }

    @Override
    public boolean add(T object) {
        checkSizeAndDispatch();
        return super.add(object);
    }

    private void checkSizeAndDispatch() {
        if (size() >= mDispatchSize) dispatch();
    }

    public void dispatch() {
        if (isEmpty()) return;

        if (mContentProviderClient == null) {
            this.mContentProviderClient = mContext.getContentResolver().acquireContentProviderClient(mUri);
            mReleaseProvider = true;
        }

        try {
            if (isSync) {
                QuantumFluxSyncHelper.insert(mContentProviderClient, toArray());
            } else {
                ContentValues[] values = ModelInflater.deflateAll(mTableDetails, toArray());
                mContentProviderClient.bulkInsert(mUri, values);
            }
            clear();
        } catch (RemoteException e) {
            release(false);
            throw new QuantumFluxException("Failed to insert objects", e);
        }
    }

    public void release(boolean dispatchRemaining) {
        if (dispatchRemaining) dispatch();

        clear();

        if (mReleaseProvider) mContentProviderClient.release();
    }
}
