package info.quantumflux.model.util;

import android.database.Cursor;
import android.database.SQLException;

import info.quantumflux.model.generate.TableDetails;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * The iterator will just iterator over a mCursor, it does that by checking in the has next method
 * if the mCursor is open and not after the last item.  If the mCursor is can fetch a next item, that item is returned in next,
 * if the returned item is the last one, the mCursor is automatically closed.
 */
public class CursorIterator<T> implements Iterator<T>, Closeable {
    private final TableDetails mTableDetails;
    private final Cursor mCursor;

    public CursorIterator(TableDetails tableDetails, Cursor cursor) {
        this.mTableDetails = tableDetails;
        this.mCursor = cursor;
    }

    @Override
    public boolean hasNext() {
        if (mCursor != null && mCursor.isAfterLast())
            mCursor.close();//Close the mCursor if we reached the last position

        return mCursor != null && !mCursor.isClosed() && !mCursor.isAfterLast();
    }

    @Override
    public T next() {
        T entity = null;
        if (mCursor == null || mCursor.isAfterLast()) {
            throw new QuantumFluxException();
        }

        try {
            if (mCursor.isBeforeFirst()) {
                mCursor.moveToFirst();
            }

            try {
                entity = ModelInflater.inflate(mCursor, mTableDetails);
            } finally {
                mCursor.moveToNext();
            }
        } catch (SQLException sqle) {
            mCursor.close();
            throw sqle;
        }

        return entity;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        if (!mCursor.isClosed()) {
            mCursor.close();
        }
    }
}
