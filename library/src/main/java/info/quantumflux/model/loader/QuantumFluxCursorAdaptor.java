package info.quantumflux.model.loader;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import info.quantumflux.model.util.QuantumFluxCursor;
import info.quantumflux.model.util.QuantumFluxException;

/**
 * A cursor adaptor that will automatically handle view and view holder creation.
 * Extend this class and implement the abstract methods.
 * T = Domain Model Object
 * K = View Holder Class
 */
public abstract class QuantumFluxCursorAdaptor<T, K> extends CursorAdapter {

    private final int mLayoutId;

    public QuantumFluxCursorAdaptor(Context context, int layoutId) {
        this(context, null, layoutId);
    }

    public QuantumFluxCursorAdaptor(Context context, Cursor c, int layoutId) {
        this(context, c, layoutId, 0);
    }

    public QuantumFluxCursorAdaptor(Context context, Cursor c, int layoutId, int flags) {
        super(context, c, flags);
        this.mLayoutId = layoutId;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(mLayoutId, viewGroup, false);
        K viewHolder = createViewHolder(view);
        view.setTag(viewHolder);

        bindView(view, context, cursor);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        K viewHolder = (K) view.getTag();
        setViewInformation(viewHolder, ((QuantumFluxCursor<T>) cursor).inflate());
    }

    public abstract K createViewHolder(View view);

    public abstract void setViewInformation(K viewHolder, T information);

    @Override
    public void changeCursor(Cursor cursor) {
        if (cursor instanceof QuantumFluxCursor || cursor == null) {
            super.changeCursor(cursor);
        } else {
            throw new QuantumFluxException("The cursor is not of the instance " + QuantumFluxCursor.class.getSimpleName());
        }
    }

    /**
     * Returns the inflated item at the cursor position
     *
     * @param position The position to inflate
     * @return The inflated item if found, null otherwise
     */
    public T getInflatedItem(int position) {
        QuantumFluxCursor<T> cursor = (QuantumFluxCursor<T>) getCursor();
        return cursor != null && cursor.moveToPosition(position) ? cursor.inflate() : null;
    }
}
