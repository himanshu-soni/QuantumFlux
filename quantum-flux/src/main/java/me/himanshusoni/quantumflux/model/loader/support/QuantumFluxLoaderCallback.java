package me.himanshusoni.quantumflux.model.loader.support;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;

import me.himanshusoni.quantumflux.model.query.Select;


public class QuantumFluxLoaderCallback<T> implements LoaderManager.LoaderCallbacks<Cursor> {

    private final Context mContext;
    private final CursorAdapter mAdapter;
    private final Select<T> mSelect;

    public QuantumFluxLoaderCallback(Context context, CursorAdapter listAdapter, Select<T> select) {
        this.mContext = context;
        this.mAdapter = listAdapter;
        this.mSelect = select;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new QuantumFluxLoader<T>(mContext, mSelect);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }
}
